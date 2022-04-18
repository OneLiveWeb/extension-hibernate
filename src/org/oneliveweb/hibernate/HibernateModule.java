package org.oneliveweb.hibernate;

import java.util.Iterator;
import java.util.List;

import org.entermediadb.asset.MediaArchive;
import org.entermediadb.asset.modules.BaseMediaModule;
import org.openedit.WebPageRequest;

public class HibernateModule extends BaseMediaModule 
{

	

	
	public void generateFieldMappings(WebPageRequest inReq) {
		MediaArchive archive = getMediaArchive(inReq);
		
		List searchtypes = archive.getPropertyDetailsArchive().listSearchTypes();
		for (Iterator iterator = searchtypes.iterator(); iterator.hasNext();) {
			String searchtype = (String) iterator.next();
			
			
		}
		
	}
	
	
}
