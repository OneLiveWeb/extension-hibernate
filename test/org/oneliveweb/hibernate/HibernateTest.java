package org.oneliveweb.hibernate;


import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermediadb.asset.MediaArchive;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.openedit.BaseTestCase;
import org.openedit.Data;
import org.openedit.data.Searcher;
import org.openedit.hittracker.HitTracker;

public class HibernateTest extends BaseTestCase{

	private static final Log log = LogFactory.getLog(HibernateTest.class);
	private SessionFactory sessionFactory;
	

	protected void setUp() throws Exception {
		// A SessionFactory is set up once for an application!
		
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.configure() // configures settings from hibernate.cfg.xml
				.build();
		
		try {
			sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
			// so destroy it manually.
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void testBasicUsage() {
		// create a couple of events...
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save( new Event( "Our very first event!", new Date() ) );
		session.save( new Event( "A follow up event", new Date() ) );
		session.getTransaction().commit();
		session.close();

		// now lets pull events from the database and list them
		session = sessionFactory.openSession();
		session.beginTransaction();
		List result = session.createQuery( "from Event" ).list();
		for ( Event event : (List<Event>) result ) {
			System.out.println( "Event (" + event.getDate() + ") : " + event.getTitle() );
		}
		session.getTransaction().commit();
		session.close();
	}
	
	
	@SuppressWarnings("unchecked")
	public void testUTest() {
		// create a couple of events...
		Session session = sessionFactory.openSession();
	

		// now lets pull events from the database and list them
		session = sessionFactory.openSession();
		session.beginTransaction();
		List result = session.createQuery( "from TuQuestion" ).list();
//		for ( TuQuestion event : (List<TuQuestion>) result ) {
//			System.out.println(event.getId() +  event.getQuestion() );
//		}
		session.getTransaction().commit();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public void testManager() {
		
		HibernateManager manager = (HibernateManager) getBean("hibernateManager");
		Session session = manager.getCurrentSessionFromConfig();		
		session.beginTransaction();
//		List result = session.createQuery( "from TuQuestion" ).list();
//		for ( TuQuestion event : (List<TuQuestion>) result ) {
//			System.out.println(event.getQuestion() );
//		}
		session.getTransaction().commit();
		session.close();
		
		
		
		
		
	}
	
	
	
	
public void testManagerAgain() {
		
		HibernateManager manager = (HibernateManager) getBean("hibernate/system","hibernateManager");
		Session session = manager.getCurrentSession();		
		session.beginTransaction();
		List result = session.createQuery( "from Event" ).list();
		
		session.getTransaction().commit();
		session.close();
		
		Session another = manager.getCurrentSession();
		
		List<Object> list = another.createSQLQuery("show tables").list();     

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			System.out.println(object);
			List columns = another.createSQLQuery("show columns from " + object.toString()).list();
			for (Iterator iterator2 = columns.iterator(); iterator2.hasNext();) {
				Object object2 = (Object) iterator2.next();
				System.out.println(object2);

				
			}
		
		}
		
	}
	
	
	
	public void testSearcher() {
		MediaArchive archive = (MediaArchive) getBean("hibernate/system", "mediaArchive");
		Searcher searcher = archive.getSearcher("event");
		
		assertTrue(searcher instanceof HibernateSearcher);
		
		HitTracker hits =  searcher.getAllHits();
		
		assertTrue(hits.size() > 0);
		
		
		HibernateData data = (HibernateData) hits.get(0);
		
		assertTrue(data.get("question").length() >0);
		
		Data question = (Data) searcher.searchById("1");
		
		assertNotNull(question);
		assertNotSame("Ian was here", question.get("question"));
		question.setValue("question", "Ian was here");
		searcher.saveData(question);

		HitTracker morehits = searcher.query().exact("category_id", "1").sort("test_id").search();
		assertTrue(morehits.size() < hits.size());
		assertTrue(morehits.size() > 0);
		
		 morehits = searcher.query().exact("category_id", "1").exact("sub_category_id", "1").sort("test_id").search();
		assertTrue(morehits.size() < hits.size());
		assertTrue(morehits.size() > 0);

		

	}
	
	
	
	
	

}
