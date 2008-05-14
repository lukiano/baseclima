package ar.uba.dcao.dbclima.concurrencia;

import org.hibernate.SessionFactory;

public class MultiTask implements Task {

  private volatile int currentTaskIndex = 0;

  private Task[] tasks;

  private TaskResult taskResult;

  public MultiTask(Task... tasks) {
    this.tasks = tasks;
  }
  
  protected Task[] getTasks() {
    return this.tasks;
  }

  public double getCompletionState() {
    double compl;
    if (this.currentTaskIndex < this.tasks.length) {
      compl = this.currentTaskIndex + this.tasks[this.currentTaskIndex].getCompletionState();
      compl /= this.tasks.length;
    } else {
      compl = 1;
    }

    return compl;
  }

  public boolean run(SessionFactory runningFactory) {
    boolean everythingOK = true;
    for (this.currentTaskIndex = 0; this.currentTaskIndex < this.tasks.length; this.currentTaskIndex++) {
      Task currentTask = this.tasks[this.currentTaskIndex];
      if (everythingOK) {
        everythingOK = everythingOK && currentTask.run(runningFactory);
        if (!currentTask.getResult().isSuccessfull()) {
          this.taskResult = currentTask.getResult();
        }
      }
    }

    if (everythingOK) {
      String situation = "";
      for (Task aInnerTask : this.tasks) {
        situation += aInnerTask.getResult().getSituation() + "\n";
      }
      this.taskResult = TaskResult.buildSuccessfulResult(situation);
    }

    return everythingOK;
  }

  public String getProgressDescription() {
    if (this.currentTaskIndex < this.tasks.length) {
      return "Task " + (this.currentTaskIndex + 1) + "/" + this.tasks.length + ": "
          + this.tasks[this.currentTaskIndex].getProgressDescription();
    } else {
      return null;
    }
  }

  public TaskResult getResult() {
    return this.taskResult;
  }

  public boolean isComplete() {
    return this.taskResult != null;
  }

  public void updateGUIWhenCompleteSuccessfully() {
    for (Task aInnerTask : this.tasks) {
      aInnerTask.updateGUIWhenCompleteSuccessfully();
    }
  }
  
}
