package ar.uba.dcao.dbclima.persistence;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

/**
 * This class garanties that only one single SessionFactory is instanciated and that the
 * configuration is done thread safe as singleton. Actually it only wraps the Hibernate
 * SessionFactory. When a JNDI name is configured the session is bound to to JNDI, else it
 * is only saved locally. You are free to use any kind of JTA or Thread
 * transactionFactories.
 */
public class DBSessionFactory {

  /**
   * Default constructor.
   */
  private DBSessionFactory() {
  }

  /**
   * Location of hibernate.cfg.xml file. NOTICE: Location should be on the classpath as
   * Hibernate uses #resourceAsStream style lookup for its configuration file. That is
   * place the config file in a Java package - the default location is the default Java
   * package.<br>
   * <br>
   * Examples: <br>
   * <code>CONFIG_FILE_LOCATION = "/hibernate.conf.xml". 
   * CONFIG_FILE_LOCATION = "/com/foo/bar/myhiberstuff.conf.xml".</code>
   */
  /** The single instance of hibernate configuration */
  private static final Configuration cfg = new Configuration();

  /** The single instance of hibernate SessionFactory */
  private static SessionFactory sessionFactory;

  /**
   * initialises the configuration if not yet done and returns the current instance
   * 
   * @return
   */
  public static SessionFactory getInstance() {
    if (sessionFactory == null)
      initSessionFactory();
    return sessionFactory;
  }

  /**
   * This would return the current open session or if this does not exist, will create a
   * new session
   */
  public Session getCurrentSession() {
    return sessionFactory.getCurrentSession();
  }

  /**
   * Initializes the sessionfactory in a safe way even if more than one thread tries to
   * build a sessionFactory
   */
  private static synchronized void initSessionFactory() {
    /*
     * check again for null because sessionFactory may have been initialized between the
     * last check and now
     * 
     */
    Logger log = Logger.getLogger(DBSessionFactory.class);
    if (sessionFactory == null) {

      try {
        cfg.configure();
        // new SchemaExport(cfg).create(false, true);
        new SchemaUpdate(cfg).execute(true, true);
        String sessionFactoryJndiName = cfg.getProperty(Environment.SESSION_FACTORY_NAME);
        if (sessionFactoryJndiName != null) {
          cfg.buildSessionFactory();
          log.debug("get a jndi session factory");
          sessionFactory = (SessionFactory) (new InitialContext()).lookup(sessionFactoryJndiName);
        } else {
          log.debug("classic factory");
          sessionFactory = cfg.buildSessionFactory();
        }

      } catch (Exception e) {
        System.err.println("%%%% Error Creating HibernateSessionFactory %%%%");
        e.printStackTrace();
        throw new HibernateException("Could not initialize the Hibernate configuration");
      }
    }
  }

  public static void close() {
    if (sessionFactory != null) {
      if (sessionFactory.getCurrentSession().isOpen()) {
        if (sessionFactory.getCurrentSession().getTransaction().isActive()) {
          sessionFactory.getCurrentSession().getTransaction().rollback();
        }
        sessionFactory.getCurrentSession().close();
      }
    }

    sessionFactory = null;
  }
}