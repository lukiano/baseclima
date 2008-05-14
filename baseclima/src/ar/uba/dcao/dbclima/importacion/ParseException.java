package ar.uba.dcao.dbclima.importacion;

@SuppressWarnings("serial")
public class ParseException extends Exception {

  private int lineNumber;

  private String line;

  public ParseException() {
    super();
  }

  public ParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParseException(String message) {
    super(message);
  }

  public ParseException(Throwable cause) {
    super(cause);
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public String getLine() {
    return line;
  }

  public void setLine(String line) {
    this.line = line;
  }
}
