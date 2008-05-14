package ar.uba.dcao.dbclima.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.ReadableInstant;

/**
 * Clase con diversos metodos de ayuda para la manipulacion de fechas.
 *
 */
public final class FechaHelper {
  
  private FechaHelper() {}
  
  private static Map<Integer, String> MESES = new HashMap<Integer, String>(12);
  
  static {
    MESES.put(1, "January");
    MESES.put(2, "February");
    MESES.put(3, "March");
    MESES.put(4, "April");
    MESES.put(5, "May");
    MESES.put(6, "June");
    MESES.put(7, "July");
    MESES.put(8, "August");
    MESES.put(9, "September");
    MESES.put(10, "October");
    MESES.put(11, "November");
    MESES.put(12, "December");
  }
  
  public static int dameDifereciaDeDias(Date comienzo, Date fin) {
    if (comienzo.compareTo(fin) == 1) {
      return 0;
    }
    /*
    long diferenciaMillis = fin.getTime() - comienzo.getTime();
    long divisor = 1000*60*60*24; // un dia
    return diferenciaMillis / divisor;
    */
    ReadableInstant comDT = new DateTime(comienzo, DateTimeZone.UTC);
    ReadableInstant finDT = new DateTime(fin, DateTimeZone.UTC);
    comDT = ((DateTime)comDT).toDateMidnight();
    finDT = ((DateTime)finDT).toDateMidnight();
    Days diferencia = Days.daysBetween(comDT, finDT);
    return diferencia.getDays();
  }

  /**
   * Retorna la fecha pasada por parametro pero con el anio deseado.
   */
  public static Date dameFechaConAnio(Date comienzo, int anio) {
    /*
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(comienzo);
    calendar.set(Calendar.YEAR, anio);
    return calendar.getTime();
    */
    return new DateTime(comienzo).withYear(anio).toDate();
  }
  
  public static Date dameFecha(int anio, int mes, int dia) {
    return new DateTime(anio, mes, dia, 0, 0, 0, 0).toDate();
  }

  public static Date dameFechaSumada(Date comienzo, int cantidadDias) {
    return new DateTime(comienzo).plusDays(cantidadDias).toDate();
  }

  public static int dameAnio(Date fecha) {
    return new DateTime(fecha).getYear();
  }

  public static int dameMes(Date fecha) {
    return new DateTime(fecha).getMonthOfYear();
  }

  public static int dameMes0a11(Date fecha) {
    return new DateTime(fecha).getMonthOfYear() - 1;
  }

  public static String mes(int mes) {
    return MESES.get(mes);
  }

}
