package ar.uba.dcao.dbclima.test;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import junit.framework.TestCase;

public abstract class ConnectedTestCase extends TestCase {
  private Session sess;

  @Override
  protected void setUp() throws Exception {
    this.sess = DBSessionFactory.getInstance().getCurrentSession();
    this.sess.beginTransaction();
  }
  
  @Override
  protected void tearDown() throws Exception {
    this.sess.close();
    DBSessionFactory.getInstance().close();
  }
  
  public Session getSession() {
    return sess;
  }
}
