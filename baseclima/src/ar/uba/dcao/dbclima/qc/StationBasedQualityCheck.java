package ar.uba.dcao.dbclima.qc;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.TaskResult;
import ar.uba.dcao.dbclima.data.Estacion;

/**
 * Clase abstracta que separa los chequeos de calidad por estaciones. 
 * Las clases que hereden de esta, implementaran las descripciones correspondientes
 * y el codigo para procesar una estacion.
 *
 */
public abstract class StationBasedQualityCheck extends AbstractQualityCheck {
  
  public final boolean run(SessionFactory runningFactory) {
    this.setProgressDescription(this.startingDescription());
    List<Estacion> stations = this.getStationsToProcess(runningFactory);
    for (int i = 0; i < stations.size(); i++) {
      Session sess = runningFactory.getCurrentSession();
      sess.beginTransaction();
      sess.clear();
      Estacion estacion = stations.get(i);
      estacion = (Estacion) sess.merge(estacion);
      this.processStation(sess, estacion);
      sess.update(estacion);
      sess.getTransaction().commit();
      this.setCompletionState((double)(i + 1) / (double)stations.size());
      this.setProgressDescription(this.progressDescription(i + 1, stations.size()));
    }

    this.setCompletionState(1);
    this.setComplete(true);
    this.setProgressDescription(this.finalDescription(stations.size()));
    this.setResult(TaskResult.buildSuccessfulResult(this.getProgressDescription()));

    return true;
  }

  protected abstract String startingDescription();
  protected abstract String progressDescription(int processedStations, int totalStations);
  protected abstract String finalDescription(int totalStations);

  protected abstract void processStation(Session sess, Estacion station);

  public void updateGUIWhenCompleteSuccessfully() {
    // does nothing
  }

}
