package ar.uba.dcao.dbclima.concurrencia;

public class AlreadyExecutingException extends RuntimeException {

  private static final long serialVersionUID = -850582336898862973L;

  public AlreadyExecutingException() {
    super();
  }

  public AlreadyExecutingException(String message, Throwable cause) {
    super(message, cause);
  }

  public AlreadyExecutingException(String message) {
    super(message);
  }

  public AlreadyExecutingException(Throwable cause) {
    super(cause);
  }
}
