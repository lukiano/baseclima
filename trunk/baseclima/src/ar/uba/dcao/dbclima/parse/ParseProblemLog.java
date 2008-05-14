package ar.uba.dcao.dbclima.parse;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParseProblemLog {

  private List<ParseProblem> problemas = new ArrayList<ParseProblem>();

  public static final int MAX_LOG_SIZE = 5000;

  public void logException(ParseProblem e) throws IllegalStateException {
    if (this.problemas.size() > MAX_LOG_SIZE ) { throw new IllegalStateException("Demasiados errores"); }
    this.problemas.add(e);
  }

  public List<ParseProblem> getProblemas() {
    return Collections.unmodifiableList(this.problemas);
  }

  public void printToStream(OutputStreamWriter stream) {
    for (ParseProblem p : problemas) {
      try {
        stream.write(p.getDescripcion() + "\n");
      } catch (IOException e1) {
        throw new IllegalArgumentException(e1);
      }
    }
  }
  
  public boolean maxLogSizeReached() {
    return this.problemas.size() >= MAX_LOG_SIZE;
  }
}
