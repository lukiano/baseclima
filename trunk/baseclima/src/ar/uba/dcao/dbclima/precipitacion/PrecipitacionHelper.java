package ar.uba.dcao.dbclima.precipitacion;

/**
 * Metodos de ayuda para la manipulacion de los registros de precipitacion de la base de datos.
 *
 */
public final class PrecipitacionHelper {
  
  private PrecipitacionHelper() {}
  
  public static double ajustarPrecipitacionSatelital(double valor) {
    return valor / 100; //XXX: el 100 es porque los registros satelitales en la base estan en enteros en centesimas de mm
  }

  public static double ajustarPrecipitacion(double valor) {
    return valor / 100; //XXX: el 100 es porque los registros en la base estan en enteros en centesimas de mm
  }

  public static int ajustarGrados(double grados) {
    int multiplicador = 100; //XXX 100 porque en la base las coordenadas estan con 2 digitos decimales
    return (int)(grados * multiplicador);
  }

}
