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
import org.hibernate.Transaction;

import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.PersistentObject;
import ar.uba.dcao.dbclima.data.PuntoSatelital;
import ar.uba.dcao.dbclima.data.RegistroSatelital;

/**
 * Parseo de un archivo de texto de entrada que contiene datos y registros satelitales.
 *
 */
public class RegistroSatelitalParser implements PersistentObjectParser {

  private static final SimpleDateFormat DATE_PARSER = (SimpleDateFormat) DateFormat.getDateInstance();
  
  private Map<String, PuntoSatelital> puntosSatelitales = new HashMap<String, PuntoSatelital>();
  
  private Dataset dataset;
  
  static {
    DATE_PARSER.applyPattern("dd/MM/yyyy");
  }
  
  public RegistroSatelitalParser() {
  }

  public void init(Dataset ds, Session sess) {
    this.dataset = ds;
    this.llenarPuntosSatelitalesExistentes(sess);
  }

  @SuppressWarnings("unchecked")
  private void llenarPuntosSatelitalesExistentes(Session sess) {
    Transaction transaction = sess.getTransaction();
    if (transaction != null && transaction.isActive()) {
      transaction = null;
    } else {
      transaction = sess.beginTransaction();
    }

    List<PuntoSatelital> puntos = sess.createQuery("FROM PuntoSatelital").list();
    for (PuntoSatelital ps : puntos) {
      sess.evict(ps);
      String key = this.getPuntoSatelitalKey(ps.getLatitud(), ps.getLongitud());
      this.puntosSatelitales.put(key, ps);
    }
    if (transaction != null) {
      transaction.commit();
    }
  }

  public void parseRegistro(String input, Dataset dataset, Queue<PersistentObject> storeQueue) throws ParseException {

    String[] inputLine = strictSplit(input, ",");

    if (inputLine.length != 4) {
      throw new ParseException("La entrada en formato csv no tiene la cantidad de valores esperada.");
    }

    RegistroSatelital rs = new RegistroSatelital();

    Date fecha = null;
    try {
      fecha = DATE_PARSER.parse(inputLine[2]);
    } catch (java.text.ParseException e) {
      throw new ParseException("No se reconoce el formato de la fecha del registro", e);
    }

    Integer latitud = inputLine[0].length() == 0 ? null : Integer.parseInt(inputLine[0]);
    Integer longitud = inputLine[1].length() == 0 ? null : Integer.parseInt(inputLine[1]);
    Integer precipitacion = inputLine[3].length() == 0 ? null : Integer.parseInt(inputLine[3]);
    if (precipitacion < 0) {
      precipitacion = null;
    }
    
    PuntoSatelital puntoSatelital = this.obtenerPuntoSatelital(latitud, longitud, storeQueue);

    rs.setPuntoSatelital(puntoSatelital);
    rs.setFecha(fecha);
    rs.setLluvia(precipitacion);
    rs.setDataset(dataset);
    storeQueue.add(rs);
  }
  
  private PuntoSatelital obtenerPuntoSatelital(Integer latitud, Integer longitud,
      Queue<PersistentObject> storeQueue) {
    String key = this.getPuntoSatelitalKey(latitud, longitud);
    PuntoSatelital puntoSatelital = this.puntosSatelitales.get(key);
    if (puntoSatelital == null) {
      puntoSatelital = new PuntoSatelital();
      puntoSatelital.setDataset(this.dataset);
      puntoSatelital.setLatitud(latitud);
      puntoSatelital.setLongitud(longitud);
      storeQueue.add(puntoSatelital);
      this.puntosSatelitales.put(key, puntoSatelital);
    }
    return puntoSatelital;
  }

  private String getPuntoSatelitalKey(Integer latitud, Integer longitud) {
    return latitud.toString() + '/' + longitud.toString();
  }

  public void prepareForCommit() {
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
