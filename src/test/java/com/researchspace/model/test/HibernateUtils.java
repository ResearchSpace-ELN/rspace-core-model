package com.researchspace.model.test;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtils {
	private static SessionFactory sessionFactory;

	public static SessionFactory getSessionFactory(String databaseName, Class<?>... clazzes) {
		try {
			Configuration configuration = createConfiguration(databaseName);
			for (Class<?> clazz : clazzes) {
				configuration.addAnnotatedClass(clazz);
			}
			doBuild(configuration);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return sessionFactory;
	}

	private static void doBuild(Configuration configuration) {
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	}

	private static Configuration createConfiguration(String dbName) {
		Configuration configuration = new Configuration();
		// Hibernate settings equivalent to hibernate.cfg.xml's properties
		Properties settings = new Properties();
		settings.put(Environment.DRIVER, "com.mysql.jdbc.Driver");
		settings.put(Environment.URL, "jdbc:mysql://localhost:3306/" + dbName + "?useSSL=false");
		settings.put(Environment.USER, "rspacedbuser");
		settings.put(Environment.PASS, "rspacedbpwd");
		settings.put("hibernate.search.default.directory_provider","filesystem");
		settings.put("hibernate.search.default.indexBase", dbName);
		settings.put("hibernate.search.default.exclusive_index_use","true");

		settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5InnoDBDialect");
		settings.put(Environment.SHOW_SQL, "true");
		settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
		settings.put(Environment.HBM2DDL_AUTO, "create-drop");
		configuration.setProperties(settings);
		return configuration;
	}

}
