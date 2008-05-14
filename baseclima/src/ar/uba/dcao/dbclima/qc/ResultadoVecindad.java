package ar.uba.dcao.dbclima.qc;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.RegistroDiario;

public class ResultadoVecindad {

  /*
   * Desviacion estandard de los valores de la estacion. Corresponde a la desviacion de la
   * temperatura (min/max) para el mes del registro en cuestion.
   */
  private double desvEstandard;

  /* Prediccion de la vecindad. */
  private double prediccion;

  /* Valor de la temperatura segun medicion de la estacion. */
  private double valorReal;

  /* Estimaciones individuales de los vecinos. */
  private List<EstimacionVecino> estimaciones = new ArrayList<EstimacionVecino>();

  private double anguloCubierto;

  public int getCantidadVecinos() {
    return this.estimaciones.size();
  }

  public double getDesviacionEstimacion() {
    return (this.valorReal - this.prediccion) / this.desvEstandard;
  }

  public double consenso() {
    double estimanMas = 0;
    double estimanMenos = 0;

    double topeEstimacionOK = 0.5d;

    for (EstimacionVecino estim : this.estimaciones) {
      if (estim.getDesvNorm() > topeEstimacionOK) {
        estimanMas++;
      } else if (estim.getDesvNorm() < -topeEstimacionOK) {
        estimanMenos++;
      }
    }

    return Math.max(estimanMas, estimanMenos) / this.estimaciones.size();
  }

  /* Getters y Setters */

  public double getAnguloCubierto() {
    return anguloCubierto;
  }

  public void setAnguloCubierto(double anguloCubierto) {
    this.anguloCubierto = anguloCubierto;
  }

  public double getDesvEstandard() {
    return desvEstandard;
  }

  public void setDesvEstandard(double desvEstandard) {
    this.desvEstandard = desvEstandard;
  }

  public double getPrediccion() {
    return prediccion;
  }

  public void setPrediccion(double estimacion) {
    this.prediccion = estimacion;
  }

  public List<EstimacionVecino> getEstimaciones() {
    return estimaciones;
  }

  public void setEstimaciones(List<EstimacionVecino> estimaciones) {
    this.estimaciones = estimaciones;
  }

  public double getValorReal() {
    return valorReal;
  }

  public void setValorReal(double valorReal) {
    this.valorReal = valorReal;
  }

  public static class EstimacionVecino {

    private RegistroDiario rd;

    private double prediccion;

    private double desvNorm;

    private double anguloEstacion;

    public EstimacionVecino(RegistroDiario rd, double prediccion, double desvNorm, double anguloEstacion) {
      this.rd = rd;
      this.prediccion = prediccion;
      this.desvNorm = desvNorm;
      this.anguloEstacion = anguloEstacion;
    }

    public double getAnguloEstacion() {
      return anguloEstacion;
    }

    public double getDesvNorm() {
      return desvNorm;
    }

    public double getPrediccion() {
      return prediccion;
    }

    public RegistroDiario getRd() {
      return rd;
    }
  }
}