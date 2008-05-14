package ar.uba.dcao.dbclima.precipitacion.satelital;

import java.util.Calendar;
import java.util.Date;

final class Helper {
  
  private Helper() {}

  // public static int dameCantidadDiasEnElMes(int mes, int anio) {
  // Calendar calendar = Calendar.getInstance();
  // calendar.set(Calendar.MILLISECOND, 0);
  // calendar.set(Calendar.SECOND, 0);
  // calendar.set(Calendar.MINUTE, 0);
  // calendar.set(Calendar.HOUR_OF_DAY, 0);
  // calendar.set(Calendar.DAY_OF_MONTH, 1);
  // calendar.set(Calendar.MONTH, mes + 1);
  // calendar.set(Calendar.YEAR, anio);
  // return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
  // }

  public static int comparaFecha(Date fecha, int dia, int mes) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(fecha);
    calendar.set(Calendar.DAY_OF_MONTH, dia);
    calendar.set(Calendar.MONTH, mes - 1);

    return fecha.compareTo(calendar.getTime());
  }

  public static Date dameFecha(Date fecha, int dia, int mes) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(fecha);
    calendar.set(Calendar.DAY_OF_MONTH, dia);
    calendar.set(Calendar.MONTH, mes - 1);

    return calendar.getTime();
  }

  public static Date dameFecha(int dia, int mes, int anio) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.DAY_OF_MONTH, dia);
    calendar.set(Calendar.MONTH, mes - 1);
    calendar.set(Calendar.YEAR, anio);
    return calendar.getTime();
  }


}
