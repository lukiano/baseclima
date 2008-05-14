package ar.uba.dcao.dbclima.concurrencia;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Dataset;


public class ListDatasetsRequest implements Query {

  private volatile List<Dataset> datasets;

  private boolean complete;

  public Object getResult() {
    return this.datasets;
  }

  public boolean isComplete() {
    return this.complete;
  }

  @SuppressWarnings("unchecked")
  public boolean run(SessionFactory runningFactory) {
    Session sess = runningFactory.getCurrentSession();
    sess.beginTransaction();
    
    this.datasets = DAOFactory.getDatasetDAO(sess).findAll();

    for (Dataset ds : this.datasets) {
      sess.evict(ds);
    }
    sess.close();
    this.complete = true;
    
    return true;
  }
  
  public void updateGUIWhenCompleteSuccessfully() {
  }
}
