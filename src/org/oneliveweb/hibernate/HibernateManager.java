package org.oneliveweb.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermediadb.asset.MediaArchive;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.openedit.CatalogEnabled;
import org.openedit.ModuleManager;
import org.openedit.OpenEditException;
import org.openedit.data.Searcher;
import org.openedit.page.manage.PageManager;

public class HibernateManager implements CatalogEnabled {

	protected SessionFactory fieldSessionFactory;
	protected PageManager fieldPageManager;
	protected String fieldCatalogId;
	private static final Log log = LogFactory.getLog(HibernateManager.class);


	protected ModuleManager fieldModuleManager;

	public String getCatalogId() {
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId) {
		fieldCatalogId = inCatalogId;
	}

	public ModuleManager getModuleManager() {
		return fieldModuleManager;
	}

	public void setModuleManager(ModuleManager inModuleManager) {
		fieldModuleManager = inModuleManager;
	}

	public PageManager getPageManager() {
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager) {
		fieldPageManager = inPageManager;
	}

	public Session beginTransaction() {
		Session hibernateSession = getCurrentSession();
		hibernateSession.beginTransaction();
		return hibernateSession;
	}

	public Session getCurrentSession() {
		return getSessionFactory().openSession();
	}

	public void commitTransaction(Session s) {
		s.getTransaction().commit();
	}

	public void rollbackTransaction(Session s) {
		s.getTransaction().rollback();
	}

	public void closeSession(Session s) {
		s.close();
	}

	public MediaArchive getMediaArchive() {
		return (MediaArchive) getModuleManager().getBean(getCatalogId(), "mediaArchive");
	}

	public SessionFactory getSessionFactory() {
		if (fieldSessionFactory == null) {

//			Map<String, String> settings = new HashMap<>();
//			settings.put("connection.driver_class", "com.mysql.jdbc.Driver");
//			settings.put("dialect", "org.hibernate.dialect.MySQL8Dialect");
//			settings.put("hibernate.connection.url", "jdbc:mysql://localhost/testu_staging");
//			settings.put("hibernate.connection.username", "root");
//			// settings.put("hibernate.connection.password", null);
//			settings.put("hibernate.current_session_context_class", "thread");
//			settings.put("hibernate.show_sql", "true");
//			settings.put("hibernate.format_sql", "true");
//			
//			
			
			Map<String, String> settings = new HashMap<>();

			String driver = getMediaArchive().getCatalogSettingValue("hibernate-driver");
			settings.put("connection.driver_class", driver);
			settings.put("hibernate.connection.driver_class", driver);
			settings.put("driver_class", driver);
			settings.put("hibernate.driver_class", driver);
			String dialect = getMediaArchive().getCatalogSettingValue("hibernate-dialect");
			settings.put("dialect", dialect);
			settings.put("hibernate.dialect", dialect);
			settings.put("hibernate.connection.dialect", dialect);
			settings.put("connection.dialect", dialect);
			// https://topic.alibabacloud.com/a/javasqlsqlexceptionzero-date-value-prohibited-exception-handling_1_27_30012188.html
			// https://stackoverflow.com/questions/55905022/zerodatetimebehavior-converttonull-not-working-in-jdbc-url-using-hibernate
			String url = getMediaArchive().getCatalogSettingValue("hibernate-url");
			settings.put("hibernate.connection.url", url);

			String username = getMediaArchive().getCatalogSettingValue("hibernate-username");
			settings.put("hibernate.connection.username", username);

			String password = getMediaArchive().getCatalogSettingValue("hibernate-password");
			if (password != null && !password.isEmpty()) {
				settings.put("hibernate.connection.password", password);
			} else {
				settings.put("hibernate.connection.password", "");
			}

			settings.put("hibernate.current_session_context_class", "thread");
			settings.put("hibernate.show_sql", "true");
			settings.put("hibernate.format_sql", "true");
			settings.put("hibernate.hbm2ddl.auto", "update");
			
			
			
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(settings).build();

			// Class c = Class.forName("com.package.MyClass"); Make this catalog ID based,
			// one per catalog, register based on hibernate searchers

			MetadataSources metadataSources = new MetadataSources(serviceRegistry);

			MediaArchive archive = getMediaArchive();
			List sorted = archive.getSearcherManager().getPropertyDetailsArchive(getCatalogId()).listSearchTypes();

			for (Iterator iterator = sorted.iterator(); iterator.hasNext();) {
				String type = (String) iterator.next();
				Searcher searcher = archive.getSearcher(type);
				// TODO Make this faster, smarter, don't load the searchers for no reason
				if (searcher instanceof ElasticHibernateSearcher || searcher instanceof HibernateSearcher) {
					String classname = searcher.getPropertyDetails().getBaseSetting("package") + "."
							+ searcher.getPropertyDetails().getBaseSetting("class");
					try {
						Class clazz = Class.forName(classname);
						metadataSources.addAnnotatedClass(clazz);
					} catch (ClassNotFoundException e) {
						// throw new OpenEditException("Couldn't create " + classname + " Add it!");
						log.info("Couldn't find " + classname );

					}

				}

			}
//			Page global = getPageManager().getPage("/WEB-INF/hibernate.xml");
//			if(global.exists()) {
//				metadataSources.addFile(global.getContentItem().getAbsolutePath());			
//			}
//			Page catalog = getPageManager().getPage(getCatalogId() + "/configuration/hibernate.xml");
//			if(catalog.exists()) {
//				metadataSources.addFile(catalog.getContentItem().getAbsolutePath());
//			}
			Metadata metadata = metadataSources.buildMetadata();

			fieldSessionFactory = metadata.getSessionFactoryBuilder().build();

		}

		return fieldSessionFactory;

	}

	public void setSessionFactory(SessionFactory inSessionFactory) {
		fieldSessionFactory = inSessionFactory;
	}

	public Session getCurrentSessionFromConfig() {
		Configuration config = new Configuration();
		config.configure();
		SessionFactory sessionFactory = config.buildSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		return session;
	}

}