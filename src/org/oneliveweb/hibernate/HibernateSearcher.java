package org.oneliveweb.hibernate;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.entermediadb.elasticsearch.searchers.BaseElasticSearcher;
import org.hibernate.Session;
import org.openedit.Data;
import org.openedit.ModuleManager;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.hittracker.Term;
import org.openedit.users.User;

public class HibernateSearcher extends BaseElasticSearcher {

	protected HibernateManager fieldHibernateManager;
	protected ModuleManager fieldModuleManager;
	
	
	

	public HibernateManager getHibernateManager() {
		if (fieldHibernateManager == null) {
			fieldHibernateManager = (HibernateManager) getModuleManager().getBean(getCatalogId(), "hibernateManager");
			fieldHibernateManager.setCatalogId(getCatalogId());

		}
		fieldHibernateManager.setCatalogId(getCatalogId());

		return fieldHibernateManager;
	}

	public void setHibernateManager(HibernateManager inHibernateManager) {
		fieldHibernateManager = inHibernateManager;
	}

	public void reIndexAll() throws OpenEditException {
		// TODO Auto-generated method stub

	}

	@Override
	public SearchQuery createSearchQuery() {
		return new SearchQuery();

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
	
	
	
	@Override
	public void saveData(Data inData) {
		//https://www.baeldung.com/hibernate-save-persist-update-merge-saveorupdate
		
		HibernateData data = (HibernateData)inData;
		Session session= getHibernateManager().getCurrentSession();
		session.beginTransaction();
		session.merge(data.getData());
		session.getTransaction().commit();

		session.close();
		
	}
	
	
	@Override
	public Data loadData(Data inHit) {
		if(inHit == null) {
			return null;
		}
		long id = Long.valueOf(inHit.getId());
		Object hit =  getHibernateManager().getCurrentSession().get(getClassName() , id);
		HibernateData data = new HibernateData();
		data.setData(hit);
		
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
		long id = Integer.valueOf(inId);
		Object hit = getHibernateManager().getCurrentSession().get(getClassName() , id);
		HibernateData data = new HibernateData();
		data.setData(hit);
		
		return data;
	}
	
	
	@Override
	public HitTracker search(SearchQuery inQuery) {
		String querystring = createQueryString(inQuery);
		
		List hits = getHibernateManager().getCurrentSession().createQuery(querystring).list();
		
		return new HibernateHitTracker(hits);
		
	}
	
	
	@Override
	public HitTracker getAllHits(WebPageRequest inReq) {
		// TODO Auto-generated method stub
		return getAllHits();
	}
	

	protected String createQueryString(SearchQuery inQuery) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("from ");
		buffer.append(getNewDataName());
		if(inQuery.getTerms().size() > 0) {
			buffer.append(" where ");
		}
		for (Iterator iterator = inQuery.getTerms().iterator(); iterator.hasNext();) {
			Term term = (Term) iterator.next();
			String field = term.getDetail().getId();
			buffer.append(field);
			buffer.append("=");
			buffer.append(term.getValue());
			if(iterator.hasNext()) {
				buffer.append(" and ");
			}		
		}
		 
		if(inQuery.getSorts() != null && inQuery.getSorts().size() > 0) {
			buffer.append(" ");
			buffer.append("order by");
			buffer.append(" ");
			buffer.append(inQuery.getSorts().get(0));
		}
		return buffer.toString();
	}

	@Override
	public String getIndexId() {
		if (fieldIndexId == -1)
		{
			fieldIndexId = System.currentTimeMillis();
		}
		return String.valueOf(fieldIndexId);
	}

	@Override
	public void clearIndex() {
		fieldIndexId = -1;

	}

	@Override
	public void deleteAll(User inUser) {
		for (Iterator iterator = getAllHits().iterator(); iterator.hasNext();) {
			Data hit = (Data) iterator.next();
			delete(hit, inUser);
		}
		clearIndex();

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

	@Override
	public HitTracker getAllHits() {
		List hits = getHibernateManager().getCurrentSession().createQuery("from " + getNewDataName()).list();
		return new HibernateHitTracker(hits);

	}

}
