package ar.uba.dcao.dbclima.importacion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.PersistentObject;
import ar.uba.dcao.dbclima.data.RegistroDiario;

public class BCRegistroDiarioParser implements PersistentObjectParser {

  private static final SimpleDateFormat DATE_PARSER = (SimpleDateFormat) DateFormat.getDateInstance();

  private CatalogoEstaciones catalogo;

  private Map<Estacion, List<RegistroDiario>> regsSinCommit = new HashMap<Estacion, List<RegistroDiario>>();

  static {
    DATE_PARSER.applyPattern("dd/MM/yyyy");
  }

  public BCRegistroDiarioParser() {
    this.catalogo = new CatalogoEstaciones();
  }

  public void init(Dataset ds, Session sess) {
    this.catalogo.init(ds, sess);
  }

  public void parseRegistro(String input, Dataset dataset, Queue<PersistentObject> storeQueue) throws ParseException {

    String[] inputLine = strictSplit(input, ",");

    if (inputLine.length != 12) {
      throw new ParseException("La entrada en formato csv no tiene la cantidad de valores esperada.");
    }

    Estacion estacion = getEstacionInput(inputLine, storeQueue);
    RegistroDiario rd = new RegistroDiario();

    Date fecha = null;
    try {
      fecha = DATE_PARSER.parse(inputLine[8]);
    } catch (java.text.ParseException e) {
      throw new ParseException("No se reconoce el formato de la fecha del registro", e);
    }

    Short tempMin;
    Short tempMax;
    Integer precipitacion;

    tempMin = inputLine[9].length() == 0 ? null : Short.parseShort(inputLine[9]);
    tempMax = inputLine[10].length() == 0 ? null : Short.parseShort(inputLine[10]);
    precipitacion = inputLine[11].length() == 0 ? null : Integer.parseInt(inputLine[11]);

    rd.setFecha(fecha);
    rd.setTempMax(tempMax);
    rd.setTempMin(tempMin);
    rd.setPrecipitacion(precipitacion);

    rd.setEstacion(estacion);
    rd.setDataset(dataset);

    storeQueue.add(rd);
    this.cacheRegistro(rd);
  }

  private void cacheRegistro(RegistroDiario rd) {
    Estacion estacion = rd.getEstacion();
    List<RegistroDiario> regsEstacion = this.regsSinCommit.get(estacion);

    if (regsEstacion == null) {
      regsEstacion = new ArrayList<RegistroDiario>();
      this.regsSinCommit.put(estacion, regsEstacion);
    }

    regsEstacion.add(rd);
  }

  public void prepareForCommit() {
    for (Map.Entry<Estacion, List<RegistroDiario>> listRegs : this.regsSinCommit.entrySet()) {
      Estacion estacion = listRegs.getKey();
      List<RegistroDiario> rds = listRegs.getValue();

      estacion.addRegistros(rds, Estacion.COLLITION_POLICY_REPLACE);
    }

    this.regsSinCommit.clear();
  }

  private Estacion getEstacionInput(String[] inputLine, Queue<PersistentObject> storeQueue) throws ParseException {

    Integer codPais = null;
    Integer codOMM = null;
    String codNac = null;

    try {
      codPais = inputLine[0].length() == 0 ? null : Integer.valueOf(inputLine[0]);
      codOMM = inputLine[1].length() == 0 ? null : Integer.valueOf(inputLine[1]);
      codNac = inputLine[2];
    } catch (NumberFormatException e) {
      throw new ParseException(e);
    }

    if (codPais == null || (codNac == null && codOMM == null)) {
      throw new ParseException("La estacion de la entrada en formato CSV no tiene los identificadores necesarios.");
    }

    Estacion estacion = this.catalogo.findEstacion(codOMM, codPais, codNac);
    if (estacion == null) {
      Integer lat = null;
      Integer lon = null;
      Integer altura = null;
      try {
        lat = Integer.valueOf(inputLine[5]);
        lon = Integer.valueOf(inputLine[6]);
        altura = Integer.valueOf(inputLine[7]);
      } catch (NumberFormatException e) {
        throw new ParseException(e);
      }

      estacion = this.catalogo.createEstacion(codOMM, codPais, codNac, inputLine[4], inputLine[3], lat, lon, altura);
      storeQueue.add(estacion);
    }

    return estacion;
  }

  /**
   * Splits an input string into an array. The string will be splitted each time a
   * separator instance appears, even if the result contains empty strings.
   */
  private static String[] strictSplit(String input, String separator) {
    List<String> occurences = new ArrayList<String>();
    int indOf = input.indexOf(separator);
    while (indOf >= 0) {
      occurences.add(input.substring(0, indOf));
      input = input.substring(indOf + separator.length(), input.length());
      indOf = input.indexOf(separator);
    }
    occurences.add(input);
    return occurences.toArray(new String[occurences.size()]);
  }
}
