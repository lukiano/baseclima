package ar.uba.dcao.dbclima.parse;

import java.io.InputStreamReader;

import ar.uba.dcao.dbclima.utils.InputLineReader;

public class InputClimaPorLineaReader implements InputClimaReader {

  private CorrectorRegistroCrudo corrector;

  private InputLineReader reader;

  protected String nextLine;

  public InputClimaPorLineaReader(InputStreamReader reader) {
    this.reader = new InputLineReader(reader);
  }

  public RegistroCrudo proximoRegistro() {
    this.bufferLine();
    RegistroCrudo registroCrudo = new RegistroCrudo(this.nextLine);
    this.nextLine = null;
    return registroCrudo;
  }

  public boolean quedanRegistros() {
    return !this.reader.eof();
  }

  protected void bufferLine() {
    if (this.nextLine == null && this.quedanRegistros()) {
      this.nextLine = this.reader.next();
    }
  }

  public CorrectorRegistroCrudo getCorrector() {
    return corrector;
  }

  public void setCorrector(CorrectorRegistroCrudo corrector) {
    this.corrector = corrector;
  }

  public void close() {
    this.reader.closeInput();
  }
}
