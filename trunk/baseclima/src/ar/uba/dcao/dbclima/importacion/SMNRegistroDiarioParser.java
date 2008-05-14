package ar.uba.dcao.dbclima.importacion;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.PersistentObject;
import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Parseo de un archivo de texto de entrada que contiene estaciones y registros diarios.
 *
 */
public class SMNRegistroDiarioParser implements PersistentObjectParser {

  protected static final SimpleDateFormat MED_DIA_DATE_FORMATTER = new SimpleDateFormat("ddMMyyyy");

  private static final int CODIGO_PAIS_SMN = 87;

  private static final double COORD_SCALE = -1000;

  private CatalogoEstaciones catalogo;

  public SMNRegistroDiarioParser() {
    this.catalogo = new CatalogoEstaciones();
  }

  public void init(Dataset ds, Session sess) {
    this.catalogo.init(ds, sess);
  }

  public void parseRegistro(String input, Dataset dataset, Queue<PersistentObject> storeQueue) throws ParseException {
    try {

      Date fecha = ParseHelper.parseDate(input, 37, 45, MED_DIA_DATE_FORMATTER, "Fecha");

      Short tx = ParseHelper.parseTenthShort(input, 51, 56, "Temp max", null);
      Short tn = ParseHelper.parseTenthShort(input, 62, 67, "Temp min", null);
      Integer prec = ParseHelper.parseTenthInteger(input, 74, 78, "Precipitacion", null);

      if (prec != null) {
        prec *= 10;
      }

      if (tx != null) {
        tx = (short) (tx.intValue() * 10);
      }

      if (tn != null) {
        tn = (short) (tn.intValue() * 10);
      }

      Estacion estacion = getEstacionInput(input, storeQueue);
      RegistroDiario rd = new RegistroDiario();
      rd.setFecha(fecha);
      rd.setTempMax(tx);
      rd.setTempMin(tn);
      rd.setPrecipitacion(prec);

      rd.setEstacion(estacion);

      rd.setDataset(dataset);
      storeQueue.add(rd);

    } catch (Exception e) {
      throw new ParseException(e);
    }

  }

  private Estacion getEstacionInput(String inputLine, Queue<PersistentObject> storeQueue) throws ParseException {
    Integer codNacional = ParseHelper.parseInteger(inputLine, 0, 3, "Cod estacion", null);
    String codNacionalString = codNacional == null ? null : codNacional.toString();
    Integer codOMM = ParseHelper.parseInteger(inputLine, 6, 9, "Cod OMM", null);

    if (codNacional == null && codOMM == null) {
      throw new ParseException("La estacion de la entrada en formato SMN no tiene los identificadores necesarios.");
    }
    Estacion estacion = this.catalogo.findEstacion(codOMM, CODIGO_PAIS_SMN, codNacionalString);

    if (estacion == null) {
      BigDecimal lat = ParseHelper.parseDecimal(inputLine, 15, 20, "Latitud", null);
      BigDecimal lon = ParseHelper.parseDecimal(inputLine, 26, 31, "Longitud", null);

      Integer altura = ParseHelper.parseInteger(inputLine, 32, 36, "Altura", null);
      Integer formattedLon = convertCoordinate(lon);
      Integer formattedLat = convertCoordinate(lat);

      estacion = this.catalogo.createEstacion(codOMM, CODIGO_PAIS_SMN, codNacionalString, null, null, formattedLat,
          formattedLon, altura);
    }

    return estacion;
  }

  private static Integer convertCoordinate(BigDecimal coord) {
    Integer rv = null;
    if (coord != null) {
      BigDecimal minutes = coord.remainder(BigDecimal.ONE).scaleByPowerOfTen(2);

      double grades = coord.divideToIntegralValue(BigDecimal.ONE).doubleValue();
      double coordCent = minutes.doubleValue() / 60d;

      double doubleCoord = (grades + coordCent) * COORD_SCALE;
      rv = (int) Math.round(doubleCoord);
    }

    return rv;
  }

  public void prepareForCommit() {
    // TODO Auto-generated method stub
    
  }
}
