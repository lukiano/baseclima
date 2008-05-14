package ar.uba.dcao.dbclima.smn.parse;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.parse.CorrectorRegistroCrudo;
import ar.uba.dcao.dbclima.parse.ParseProblem;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;
import ar.uba.dcao.dbclima.parse.RegistroClimaticoParser;
import ar.uba.dcao.dbclima.parse.RegistroCrudo;

public class RegistroSMNParser implements RegistroClimaticoParser {

  public static DataValueInf inf = new DataValueInf();

  public static final int TIPO_REGISTRO_MENSUAL = 2;

  public static final int TIPO_REGISTRO_DIARIO = 1;

  public static final int TIPO_REGISTRO_HORARIO = 0;

  protected static Set<String> DATO_FALTANTE = new HashSet<String>();
  static {
    DATO_FALTANTE.add("");
    DATO_FALTANTE.add("0-");
    DATO_FALTANTE.add("-");
  }

  private CorrectorRegistroCrudo corrector;

  private boolean parseHR;

  private boolean parseDR;

  private boolean parseMR;

  public RegistroSMNParser(boolean parseHR, boolean parseDR, boolean parseMR) {
    this.parseHR = parseHR;
    this.parseDR = parseDR;
    this.parseMR = parseMR;
  }

  public static int getTipoRegistro(String registro) {
    char indicadorTipo = registro.charAt(0);
    if (indicadorTipo == '1') {
      return 0;
    } else if (indicadorTipo == '2') {
      return 1;
    } else if (indicadorTipo == '8' || indicadorTipo == '9') {
      return 2;
    } else {
      return -1;
    }
  }

  @SuppressWarnings("unchecked")
  public static Short getNroEstacion(RegistroCrudo rc, ParseProblemLog log) {
    return ParseSMNHelper.parseShort(rc.getRegistro(), 1, 4, "Codigo de Estacion", Collections.EMPTY_SET, log);
  }

  @SuppressWarnings("unchecked")
  public final RegistroDiario parse(RegistroCrudo rsc, ParseProblemLog log) {
    String registroStr = rsc.getRegistro();

    int tipo = getTipoRegistro(registroStr);
    if ((tipo == 0 && !this.parseHR) || (tipo == 1 && !this.parseDR) || (tipo == 2 && !this.parseMR)) {
      // El parser no procesa registros con la frecuencia de rc.
      return null;
    }

    char indicadorTipo = registroStr.charAt(0);
    if (registroStr.length() != 80) {
      log.logException(new ParseProblem(registroStr, "Longitud del registro", String.valueOf(registroStr.length())));
      return null;
    } else if (indicadorTipo == '2') {
      RegistroClimaticoParser parser = RegistroDiarioSMNParser.getInstance();

      if (this.corrector != null) {
       this.corrector.corregirRegistro(rsc); 
      }

      RegistroDiario rv = parser.parse(rsc, log);
      return rv;
    } else {
      // No se hace nada con los registros que no son diarios.
      return null;
    }
  }
  
  public CorrectorRegistroCrudo getCorrector() {
    return corrector;
  }
  
  public void setCorrector(CorrectorRegistroCrudo corrector) {
    this.corrector = corrector;
  }
}