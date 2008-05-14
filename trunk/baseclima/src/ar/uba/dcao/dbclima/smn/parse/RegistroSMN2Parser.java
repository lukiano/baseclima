package ar.uba.dcao.dbclima.smn.parse;

import java.text.SimpleDateFormat;
import java.util.Date;

import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.parse.ParseProblem;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;
import ar.uba.dcao.dbclima.parse.RegistroClimaticoParser;
import ar.uba.dcao.dbclima.parse.RegistroCrudo;

public class RegistroSMN2Parser implements RegistroClimaticoParser {

  protected static final SimpleDateFormat MED_DIA_DATE_FORMATTER = new SimpleDateFormat("ddMMyyyy");

  public RegistroSMN2Parser() { }

  public RegistroDiario parse(RegistroCrudo rc, ParseProblemLog log) {

    String registro = rc.getRegistro();
    try {

      Date fecha = ParseSMNHelper.parseDate(registro, 37, 45, MED_DIA_DATE_FORMATTER, "Fecha", log);
      //Short codEstacion = ParseSMNHelper.parseShort(registro, 0, 3, "Cod estacion", null, log);

      Short tx = ParseSMNHelper.parseTenthShort(registro, 51, 56, "Temp max", null, log);
      Short tn = ParseSMNHelper.parseTenthShort(registro, 62, 67, "Temp min", null, log);
      Integer prec = ParseSMNHelper.parseTenthInteger(registro, 74, 78, "Precipitacion", null, log);

      RegistroDiario rd = new RegistroDiario();
      //rd.setNumeroEstacion(codEstacion);
      rd.setFecha(fecha);
      rd.setTempMax(tx);
      rd.setTempMin(tn);
      rd.setPrecipitacion(prec);

      return rd;
    } catch (Exception e) {
      log.logException(new ParseProblem(registro, null, null));
      return null;
    }

  }
}
