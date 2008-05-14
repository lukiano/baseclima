package ar.uba.dcao.dbclima.importacion;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.Task;
import ar.uba.dcao.dbclima.concurrencia.TaskResult;

public class DeleteEstacionTask implements Task {

  private Integer idEstacion;

  private boolean done = false;

  private TaskResult taskResult;

  public DeleteEstacionTask(Integer idEstacion) {
    this.idEstacion = idEstacion;
  }

  public double getCompletionState() {
    return this.done ? 1 : 0;
  }

  public TaskResult getResult() {
    return this.taskResult;
  }

  public String getProgressDescription() {
    return "Borrando estacion with id " + this.idEstacion;
  }

  public boolean isComplete() {
    return done;
  }

  public boolean run(SessionFactory runningFactory) {
    Session sess = runningFactory.getCurrentSession();
    sess.beginTransaction();
    sess.createQuery("DELETE FROM RegistroDiario rd WHERE rd.estacion = " + this.idEstacion).executeUpdate();
    sess.createQuery("DELETE FROM Estacion e WHERE e.id = " + this.idEstacion).executeUpdate();
    sess.close();

    this.done = true;
    this.taskResult = TaskResult.buildSuccessfulResult("Estacion eliminada");
    return true;
  }
  
  public void updateGUIWhenCompleteSuccessfully() {
  }
}
