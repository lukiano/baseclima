package ar.uba.dcao.dbclima.smn.parse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;
import ar.uba.dcao.dbclima.parse.RegistroClimaticoParser;
import ar.uba.dcao.dbclima.parse.RegistroCrudo;

public class RegistroDiarioSMNParser implements RegistroClimaticoParser {

  protected static final SimpleDateFormat MED_DIA_DATE_FORMATTER = new SimpleDateFormat("yyMMdd");

  private static RegistroDiarioSMNParser INSTANCE;

  public static RegistroDiarioSMNParser getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new RegistroDiarioSMNParser();
    }
    return INSTANCE;
  }

  private RegistroDiarioSMNParser() { }

  @SuppressWarnings("unchecked")
  public RegistroDiario parse(RegistroCrudo rc, ParseProblemLog log) {
    Set<String> df = RegistroSMNParser.DATO_FALTANTE;

    String reg = rc.getRegistro();
    RegistroDiario rv = new RegistroDiario();
    Date fecha = ParseSMNHelper.parseDate(reg, 4, 10, MED_DIA_DATE_FORMATTER, "Fecha", log);
    Short tempMax = ParseSMNHelper.parseTemp(reg, 12, "Temperatura maxima", log);
    Short tempMin = ParseSMNHelper.parseTemp(reg, 16, "Temperatura minima", log);
//    Short heliofaniaE = ParseSMNHelper.parseShort(reg, 28, 31, "Heliofania efectiva", df, log);
//    Short heliofaniaR = ParseSMNHelper.parseShort(reg, 31, 33, "Heliofania relativa", df, log);
//    Short dirViento = ParseSMNHelper.parseShort(reg, 33, 35, "Direccion viento", df, log);
//    Short velViento = ParseSMNHelper.parseShort(reg, 35, 38, "Velocidad Viento", df, log);
    Boolean hayPrecipitacion = ParseSMNHelper.parseBoolean(reg, 38, "Precipitacion (B)", df, log);
    Boolean hayLluvia = ParseSMNHelper.parseBoolean(reg, 39, "Lluvia (B)", df, log);

    Integer precipitacion;
    if (hayPrecipitacion != null && !hayPrecipitacion) {
      precipitacion = 0;
    } else {
      precipitacion = ParseSMNHelper.parseInteger(reg, 67, 71, "Precipitacion", df, log);
    }

    //Short nroEstacion = ParseSMNHelper.parseShort(reg, 1, 4, "Codigo de Estacion", Collections.EMPTY_SET, log);
    //rv.setNumeroEstacion(nroEstacion);
    rv.setFecha(fecha);
    rv.setTempMax(tempMax);
    rv.setTempMin(tempMin);
//    rv.setHeliofaniaEfect(heliofaniaE);
//    rv.setHeliofaniaRelat(heliofaniaR);
//    rv.setDirViento(dirViento);
//    rv.setVelViento(velViento);
    rv.setHayLluvia(hayLluvia);
    rv.setPrecipitacion(precipitacion);

    return rv;
  }
}