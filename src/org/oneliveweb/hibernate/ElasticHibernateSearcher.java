package org.oneliveweb.hibernate;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.entermediadb.elasticsearch.searchers.BaseElasticSearcher;
import org.entermediadb.scripts.Script;
import org.entermediadb.scripts.ScriptManager;
import org.hibernate.Session;
import org.openedit.Data;
import org.openedit.ModuleManager;
import org.openedit.OpenEditException;
import org.openedit.event.EventManager;
import org.openedit.page.Page;
import org.openedit.page.manage.PageManager;
import org.openedit.users.User;

public class ElasticHibernateSearcher extends BaseElasticSearcher implements OrmDataSearcher {

	protected HibernateManager fieldHibernateManager;
	protected ModuleManager fieldModuleManager;
	 
	protected ScriptManager fieldScriptManager;
	protected PageManager fieldPageManager;

	
	

	public ScriptManager getScriptManager() {
		return fieldScriptManager;
	}

	public void setScriptManager(ScriptManager inScriptManager) {
		fieldScriptManager = inScriptManager;
	}

	public PageManager getPageManager() {
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager) {
		fieldPageManager = inPageManager;
	}

	public ModuleManager getModuleManager() {
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager) {
		fieldModuleManager = inModuleManager;
	}

	public EventManager getEventManager() {
		if (fieldEventManager == null) {
			fieldEventManager = (EventManager) getModuleManager().getBean(getCatalogId(), "eventManager");
			
		}

		return fieldEventManager;
	}

	public void setEventManager(EventManager inEventManager) {
		fieldEventManager = inEventManager;
	}

	public HibernateManager getHibernateManager() {
		if (fieldHibernateManager == null) {
			fieldHibernateManager = (HibernateManager) getModuleManager().getBean(getCatalogId(), "hibernateManager");
			
		}
		

		return fieldHibernateManager;
	}

	public void setHibernateManager(HibernateManager inHibernateManager) {
		fieldHibernateManager = inHibernateManager;
	}

	public void reIndexAll() throws OpenEditException {
		List hits = getHibernateManager().getCurrentSession().createQuery("from " + getNewDataName()).list();
		ArrayList tosave = new ArrayList();
		
		for (Iterator iterator = hits.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			HibernateData data = new HibernateData();
			data.setData(object);
			tosave.add(data);
			if(tosave.size() > 50000) {
				updateInBatch(tosave, null);
				tosave.clear();
			}
			
		}
		updateInBatch(tosave, null);

	}

	
	@Override
	public void saveData(Data inData, User inUser) {
		saveData(inData);
		clearIndex();
	}
	
	@Override
	public Data createNewData() {
		try {
			Class clazz = Class.forName(getClassName());
			Constructor ctor = clazz.getConstructor();
			Object object = ctor.newInstance();
			HibernateData data = new HibernateData();
			data.setData(object);
			return data;
		} catch (Exception e) {
			throw new OpenEditException(e);
		}
		
	}
	
	
	public Object runDataScript(Data inData, String inType, HashMap extras) {
		Page page = getPageManager().getPage("/" + getCatalogId() + "/events/scripts/" + getSearchType() + "/" + inType + ".groovy");
		if(page.exists()) {
			Script script = getScriptManager().loadScript(page.getPath());
			if(extras == null) {
				extras = new HashMap();				
			}
			extras.put("searcher", this);
			extras.put("data", inData);	
			return  getScriptManager().execScript(extras, script);		
		}
		return null;		
	}
	
	public Data loadData(String inId) {
		long id = Long.valueOf(inId);
		Object hit = getHibernateManager().getCurrentSession().get(getClassName(), id);
		HibernateData data = new HibernateData();
		data.setData(hit);
		

		
		return data;
	}
	@Override
	public void saveData(Data inData) {
		
		runDataScript(inData, "presave", null);
		
		
		//Update database first
		HibernateData data = (HibernateData)inData;
		Session session= getHibernateManager().getCurrentSession();
		session.beginTransaction();
		Object returned = session.merge(data.getData());
		session.getTransaction().commit();
		session.close();
		data.setData(returned);
		ArrayList update = new ArrayList();
		update.add(data);
		//Now exists in MySql but not in elasticsearch
		
		updateIndex(update, null);
		//Now exists in both places
		
		
	}
	
	
	@Override
	public Data loadData(Data inHit) {
		if(inHit == null) {
			return null;
		}
		long id = Long.valueOf(inHit.getId());
		Object hit =  getHibernateManager().getCurrentSession().get(getClassName() , id);
		if(hit == null) {
			//Object was deleted so remove from index too?
			super.delete(inHit, null);
			return null;
		}
		HibernateData data = new HibernateData();
		data.setData(hit);
		ArrayList update = new ArrayList();
		update.add(data);
		updateIndex(update, null);
		return data;
		
	}

	private String getClassName() {
		return getPropertyDetails().getBaseSetting("package") + "." + getPropertyDetails().getBaseSetting("class");
	}
	
	
	@Override
	public Object searchById(String inId) {
		if(inId == null) {
			return null;
			
		}
		if("generictableedit".equals(inId)) {
			return null; //This is nonsense
		}
		if("generictablesave".equals(inId)) {
			return null; //This is nonsense
		}
		long id = Long.valueOf(inId);
		Object hit = getHibernateManager().getCurrentSession().get(getClassName() , id);
		if(hit == null) {
			//Object was deleted so remove from index too?			
			return null;
		}
		HibernateData data = new HibernateData();
		data.setData(hit);
		ArrayList update = new ArrayList();
		update.add(data);
		updateIndex(update, null);
		return data;
	}
	
	


	@Override
	public void deleteAll(User inUser) {
		for (Iterator iterator = getAllHits().iterator(); iterator.hasNext();) {
			Data hit = (Data) iterator.next();
			delete(hit, inUser);
		}
		
		super.deleteAll(inUser);

	}

	@Override
	public void delete(Data inData, User inUser) {
		try {
			HibernateData data = (HibernateData)inData;
			Session session= getHibernateManager().getCurrentSession();
			session.beginTransaction();
			session.delete(data.getData());
			session.getTransaction().commit();
			session.close();
			super.delete(inData, inUser);
			clearIndex();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveAllData(Collection<Data> inAll, User inUser) {
	for (Iterator iterator = inAll.iterator(); iterator.hasNext();) {
		Data data = (Data) iterator.next();
		saveData(data);
	}

	}

	

}
