package ar.uba.dcao.dbclima.smn;

import java.io.InputStreamReader;

import ar.uba.dcao.dbclima.parse.InputClimaPorLineaReader;
import ar.uba.dcao.dbclima.smn.parse.RegistroSMNParser;

public class SMNReader extends InputClimaPorLineaReader {

  public SMNReader(InputStreamReader reader) {
    super(reader);
  }

  public int getTipoProximoRegistro() {
    bufferLine();
    return RegistroSMNParser.getTipoRegistro(this.nextLine);
  }

}
