package ar.uba.dcao.dbclima.importacion;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.AbstractTask;
import ar.uba.dcao.dbclima.concurrencia.TaskResult;

/**
 * Clase que convierte un dataset de prueba a uno de referencia.
 * No esta siendo usada actualmente.
 *
 */
public class ReferenceDatasetTask extends AbstractTask {

  private Integer idDataset;

  //TODO: decidir si se escogen los que pasan a referencia, o se pasan todos
  public ReferenceDatasetTask(Integer idDataset) {
    this.idDataset = idDataset;
  }

  public boolean run(SessionFactory runningFactory) {
    this.setProgressDescription("Deleting DataSet with ID: " + this.idDataset);
    Session sess = runningFactory.getCurrentSession();
    sess.beginTransaction();
    sess.createQuery("DELETE FROM ResultadoTestQC rtqc WHERE rtqc.registro.dataset.id = " + this.idDataset).executeUpdate();
    this.setCompletionState(0.33);
    sess.createQuery("DELETE FROM RegistroDiario rd WHERE rd.dataset.id = " + this.idDataset).executeUpdate();
    this.setCompletionState(0.66);
    sess.createQuery("DELETE FROM Dataset ds WHERE ds.id = " + this.idDataset).executeUpdate();
    sess.getTransaction().commit();
    sess.close();
    this.setCompletionState(1);
    this.setComplete(true);
    this.setResult(TaskResult.buildSuccessfulResult("done?"));
    return true;
  }

  public void updateGUIWhenCompleteSuccessfully() {
  }
  
}
