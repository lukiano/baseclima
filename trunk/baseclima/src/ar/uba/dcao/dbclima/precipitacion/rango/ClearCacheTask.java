package ar.uba.dcao.dbclima.precipitacion.rango;

import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.AbstractTask;
import ar.uba.dcao.dbclima.concurrencia.TaskResult;

/**
 * When a dataset change happens (import or delete a dataset), the cache needs to be
 * cleared to avoid inconsistencies.
 *
 */
public final class ClearCacheTask extends AbstractTask {

  public ClearCacheTask() {
    super();
  }

  public boolean run(SessionFactory runningFactory) {
    this.setProgressDescription("Clearing cache...");
    this.setCompletionState(0);
    PromedioPrecipitacionAnualProyectorRango.clearCache();
    this.setCompletionState(0.5);
    PromedioPrecipitacionAnualConSatelitesProyectorRango.clearCache();
    this.setCompletionState(1);
    this.setComplete(true);
    this.setResult(TaskResult.buildSuccessfulResult("Cache cleared"));
    return true;
  }

  public void updateGUIWhenCompleteSuccessfully() {
  }

}
