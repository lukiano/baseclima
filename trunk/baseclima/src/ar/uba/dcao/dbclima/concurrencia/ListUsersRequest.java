package ar.uba.dcao.dbclima.concurrencia;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;


public class ListUsersRequest implements Query {

  private volatile List<String> userList;

  private boolean complete;

  public Object getResult() {
    return this.userList;
  }

  public boolean isComplete() {
    return this.complete;
  }

  @SuppressWarnings("unchecked")
  public boolean run(SessionFactory runningFactory) {
    Session sess = runningFactory.getCurrentSession();
    sess.beginTransaction();
  
    List<String> rs = sess.createQuery("SELECT DISTINCT usuario FROM Dataset").list();
    this.userList = rs;

    this.complete = true;
    sess.close();
    
    return true;
  }
  
  public void updateGUIWhenCompleteSuccessfully() {
  }
}
