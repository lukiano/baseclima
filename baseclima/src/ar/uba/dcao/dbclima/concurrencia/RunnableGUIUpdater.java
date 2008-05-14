package ar.uba.dcao.dbclima.concurrencia;

import ar.uba.dcao.dbclima.gui.ManagementConsole;

public class RunnableGUIUpdater implements Runnable {

  private ManagementConsole console;

  private volatile Task task;

  private Thread t;

  public RunnableGUIUpdater(ManagementConsole console, Task task) {
    this.console = console;
    this.task = task;

    this.t = new Thread(this);
    this.t.setDaemon(true);
    this.t.setName("GUI Updater Thread");
    this.t.start();
  }

  public void run() {

    this.console.getCancelButton().setVisible(true);
    this.console.getTaskLabel().setText("");
    this.console.getTaskLabel().setVisible(true);
    this.console.getProgressBar().setValue(0);
    this.console.getProgressBar().setVisible(true);

    console.worker.executeRequest(this.task);
    while (!this.task.isComplete() && console.worker.isWorking()) {
      synchronized (this) {
        try {
          this.wait(700);
        } catch (InterruptedException ie) {
          System.out.println("GUI thread " + Thread.currentThread().getName() + " interrupted. Awaking.");
        }
      }

      if (this.console.isVisible()) {
        this.console.getProgressBar().setValue((int) (this.task.getCompletionState() * 100));
        this.console.getTaskLabel().setText(this.task.getProgressDescription());
      }
    }

    if (this.console.isVisible()) {
      this.console.getProgressBar().setValue((int) (this.task.getCompletionState() * 100));
      this.console.getTaskLabel().setText(this.task.getResult().getSituation());
      this.console.getCancelButton().setVisible(false);
      this.task.updateGUIWhenCompleteSuccessfully();
    }
  }
}
