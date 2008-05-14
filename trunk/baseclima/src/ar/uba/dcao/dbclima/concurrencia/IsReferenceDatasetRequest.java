package ar.uba.dcao.dbclima.concurrencia;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Dataset;


public class IsReferenceDatasetRequest implements Query {

  private volatile Boolean result;

  private boolean complete;
  
  private Long datasetId;
  
  public IsReferenceDatasetRequest(Long datasetId) {
    this.datasetId = datasetId;
  }

  public Object getResult() {
    return this.result;
  }

  public boolean isComplete() {
    return this.complete;
  }

  @SuppressWarnings("unchecked")
  public boolean run(SessionFactory runningFactory) {
    Session sess = runningFactory.getCurrentSession();
    sess.beginTransaction();
    Dataset ds = DAOFactory.getDatasetDAO(sess).findByID(this.datasetId);
    this.result = Boolean.valueOf(ds.isReferente());
    sess.close();
    this.complete = true;
    return true;
  }
  
  public void updateGUIWhenCompleteSuccessfully() {
  }
}
