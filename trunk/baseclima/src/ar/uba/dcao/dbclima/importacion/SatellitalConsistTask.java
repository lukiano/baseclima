package ar.uba.dcao.dbclima.importacion;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.AbstractTask;
import ar.uba.dcao.dbclima.concurrencia.TaskResult;
import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.PuntoSatelital;

/**
 * Tarea de consistencia de los puntos satelitales. Identifica, para cada uno, en que fecha comienzan sus datos y cuando terminan.
 *
 */
public class SatellitalConsistTask extends AbstractTask {

  private Dataset dataset;

  public SatellitalConsistTask() {
  }

  protected String startingDescription() {
    return "Consisting satellital points...";
  }
  
  protected String progressDescription(int processedPoints, int totalPoints) {
    return "Consisting satellital point " + processedPoints + "/" + totalPoints; //TODO consisting o consistencing? 
  }
  
  protected String finalDescription(int totalPoints) {
    return totalPoints + " processed satellital points.";
  }

  protected void processPoint(Session sess, PuntoSatelital ps) {
    Date inicio = (Date) sess.createQuery("SELECT MIN(fecha) FROM RegistroSatelital WHERE puntoSatelital = ?").setParameter(0, ps).uniqueResult();
    Date fin = (Date) sess.createQuery("SELECT MAX(fecha) FROM RegistroSatelital WHERE puntoSatelital = ?").setParameter(0, ps).uniqueResult();
    ps.setFechaInicio(inicio);
    ps.setFechaFin(fin);
  }

  public void updateGUIWhenCompleteSuccessfully() {
  }

  public boolean run(SessionFactory runningFactory) {
    this.setProgressDescription(this.startingDescription());
    List<PuntoSatelital> sps = this.getSatellitalPointsToProcess(runningFactory);
    for (int i = 0; i < sps.size(); i++) {
      Session sess = runningFactory.getCurrentSession();
      sess.beginTransaction();
      sess.clear();
      PuntoSatelital puntoSatelital = sps.get(i);
      puntoSatelital = (PuntoSatelital) sess.merge(puntoSatelital);
      this.processPoint(sess, puntoSatelital);
      sess.update(puntoSatelital);
      sess.getTransaction().commit();
      this.setCompletionState((double)(i + 1) / (double)sps.size());
      this.setProgressDescription(this.progressDescription(i + 1, sps.size()));
    }

    this.setCompletionState(1);
    this.setComplete(true);
    this.setProgressDescription(this.finalDescription(sps.size()));
    this.setResult(TaskResult.buildSuccessfulResult(this.getProgressDescription()));

    return true;
  }

  @SuppressWarnings("unchecked")
  private List<PuntoSatelital> getSatellitalPointsToProcess(SessionFactory runningFactory) {
    Session sess = runningFactory.getCurrentSession();
    sess.beginTransaction();
    List<PuntoSatelital> resultado = 
      sess.createQuery("FROM PuntoSatelital WHERE dataset = ?").setParameter(0, this.dataset).list();
    sess.clear();
    sess.getTransaction().commit();
    return resultado;
  }

  public Dataset getDataset() {
    return dataset;
  }

  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }
  
}
