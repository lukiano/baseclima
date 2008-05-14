package ar.uba.dcao.dbclima.casosDeUso.reportes.precip;

public class PrecipitacionMesEstacion {

  private double[] distribucionPrecipitacion;

  private int diasPrecipitacion;

  private int registros;

  private int mes;

  private final String nombreEstacion;

  private final int latitud;

  private final int longitud;

  private final int altura;

  private final double[] distTmin;

  private final double[] distTmax;

  public PrecipitacionMesEstacion(String nombreEstacion, int latitud, int longitud, int altura,
      double[] distribucionPrecipitacion, double[] distTmin, double[] distTmax, int diasPrecipitacion, int registros, int mes) {
    this.nombreEstacion = nombreEstacion;
    this.latitud = latitud;
    this.longitud = longitud;
    this.altura = altura;
    this.distribucionPrecipitacion = distribucionPrecipitacion;
    this.distTmin = distTmin;
    this.distTmax = distTmax;
    this.diasPrecipitacion = diasPrecipitacion;
    this.mes = mes;
    this.registros = registros;
  }

  public String getNombreEstacion() {
    return nombreEstacion;
  }

  public int getLatitud() {
    return latitud;
  }

  public int getLongitud() {
    return longitud;
  }

  public int getAltura() {
    return altura;
  }

  public double[] getDistribucionPrecipitacion() {
    return distribucionPrecipitacion;
  }

  public double[] getDistTmin() {
    return distTmin;
  }

  public double[] getDistTmax() {
    return distTmax;
  }

  public int getDiasPrecipitacion() {
    return diasPrecipitacion;
  }

  public int getRegistros() {
    return registros;
  }

  public int getMes() {
    return mes;
  }
}
