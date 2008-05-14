package ar.uba.dcao.dbclima.concurrencia;

public class TaskResult {

  private final boolean successfull;

  private final String situation;

  private final Throwable exception;

  public static TaskResult buildSuccessfulResult(String situation) {
    return new TaskResult(true, situation, null);
  }

  public static TaskResult buildUnsuccessfulResult(String situation, Throwable exception) {
    return new TaskResult(false, situation, exception);
  }

  private TaskResult(boolean success, String situation, Throwable exception) {
    this.successfull = success;
    this.situation = situation;
    this.exception = exception;
  }

  public boolean isSuccessfull() {
    return successfull;
  }

  public Throwable getException() {
    return exception;
  }

  public String getSituation() {
    return situation;
  }
}
