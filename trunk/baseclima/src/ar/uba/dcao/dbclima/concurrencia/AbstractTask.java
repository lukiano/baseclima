package ar.uba.dcao.dbclima.concurrencia;


/**
 * Clase abstracta que guarda las propiedades comunes de toda tarea, para no repetir codigo.
 *
 */
public abstract class AbstractTask implements Task {
  
  private volatile String progressDescription;

  private volatile double completionState;
  
  private volatile boolean complete;

  private TaskResult result;

  public final double getCompletionState() {
    return this.completionState;
  }

  public final TaskResult getResult() {
    return this.result;
  }

  public final String getProgressDescription() {
    return this.progressDescription;
  }

  public final boolean isComplete() {
    return this.complete;
  }

  public final void setProgressDescription(String progressDescription) {
    this.progressDescription = progressDescription;
  }

  public final void setCompletionState(double completionState) {
    this.completionState = completionState;
  }

  public final void setResult(TaskResult result) {
    this.result = result;
  }

  public final void setComplete(boolean complete) {
    this.complete = complete;
  }

}
