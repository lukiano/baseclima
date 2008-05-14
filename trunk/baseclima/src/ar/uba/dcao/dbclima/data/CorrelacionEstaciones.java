package ar.uba.dcao.dbclima.data;

import java.util.Date;

/**
 * Indica la correlacion entre dos estaciones. Se guardan las estaciones, la variable de
 * interes (Tn, Tx, ...), el periodo sobre el cual se calculo la correlacion el numero de
 * registros que se uso, el mes para el cual vale la correlacion y la correlacion en si.
 * Tambien se guardan datos utiles para estimar el valor de la variable en una estacion en
 * funcion del valor de la variable en la otra.
 */
public class CorrelacionEstaciones extends PersistentObject {

  private Estacion e1;

  private Estacion e2;

  private String variable;

  private Date comienzo;

  private Date fin;

  private Integer numRegsUsados = 0;

  private Integer mes;

  private Double correlacion;

  /*
   * Ordenada al origen de la funcion lineal para transformar un valor de la serie 1 en el
   * equivalente de la serie 2.
   */
  private Double ordenadaOrigen;

  /* Pendiente de la funcion lineal.. */
  private Double pendiente;

  private Double desviacionEstimacion;
  
  public CorrelacionEstaciones() {
  }

  public CorrelacionEstaciones(Estacion e1, Estacion e2, int mes) {
    this.e1 = e1;
    this.e2 = e2;
    this.mes = mes;
    this.switchIfApplies();
  }

  private void switchIfApplies() {
    if (e1 != null && e2 != null && e1.getId() > e2.getId()) {
      Estacion t = e1;
      e1 = e2;
      e2 = t;
    }
  }

  public double getDistancia() {
    double latDif = Math.pow(this.getE1().getLatitud() - this.getE2().getLatitud(), 2);
    double lonDif = Math.pow(this.getE1().getLongitud() - this.getE2().getLongitud(), 2);

    return Math.sqrt(latDif * latDif + lonDif * lonDif);
  }

  public Estacion getE1() {
    return e1;
  }

  public void setE1(Estacion e1) {
    this.e1 = e1;
    this.switchIfApplies();
  }

  public Estacion getE2() {
    return e2;
  }

  public void setE2(Estacion e2) {
    this.e2 = e2;
    this.switchIfApplies();
  }

  public Date getFin() {
    return fin;
  }

  public void setFin(Date endDate) {
    this.fin = endDate;
  }

  public Date getComienzo() {
    return comienzo;
  }

  public void setComienzo(Date startDate) {
    this.comienzo = startDate;
  }

  public String getVariable() {
    return variable;
  }

  public void setVariable(String variable) {
    this.variable = variable;
  }

  public Double getCorrelacion() {
    return correlacion;
  }

  public void setCorrelacion(Double correlacion) {
    this.correlacion = correlacion;
  }

  public Integer getNumRegsUsados() {
    return numRegsUsados;
  }

  public void setNumRegsUsados(Integer regsComun) {
    this.numRegsUsados = regsComun;
  }

  public Double getOrdenadaOrigen() {
    return ordenadaOrigen;
  }

  public void setOrdenadaOrigen(Double ordenadaOrigen) {
    this.ordenadaOrigen = ordenadaOrigen;
  }

  public Double getPendiente() {
    return pendiente;
  }

  public void setPendiente(Double pendiente) {
    this.pendiente = pendiente;
  }

  public Double getDesviacionEstimacion() {
    return desviacionEstimacion;
  }

  public void setDesviacionEstimacion(Double desviacionEstimacion) {
    this.desviacionEstimacion = desviacionEstimacion;
  }

  public Integer getMes() {
    return mes;
  }

  public void setMes(Integer mes) {
    this.mes = mes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = prime;
    result = prime * result + ((e1 == null) ? 0 : e1.hashCode());
    result = prime * result + ((e2 == null) ? 0 : e2.hashCode());
    result = prime * result + ((mes == null) ? 0 : mes.hashCode());
    result = prime * result + ((variable == null) ? 0 : variable.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    final CorrelacionEstaciones other = (CorrelacionEstaciones) obj;
    if (e1 == null) {
      if (other.e1 != null)
        return false;
    } else if (!e1.equals(other.e1))
      return false;
    if (e2 == null) {
      if (other.e2 != null)
        return false;
    } else if (!e2.equals(other.e2))
      return false;
    if (mes == null) {
      if (other.mes != null)
        return false;
    } else if (!mes.equals(other.mes))
      return false;
    if (variable == null) {
      if (other.variable != null)
        return false;
    } else if (!variable.equals(other.variable))
      return false;
    return true;
  }

  public double predecirEstacion(Estacion estacionAPredecir, double valorEstacionVecina) {
    double rv;

    if (estacionAPredecir == this.getE1()) {
      rv = (valorEstacionVecina - this.getOrdenadaOrigen()) / this.getPendiente();

    } else if (estacionAPredecir == this.getE2()) {
      rv = valorEstacionVecina * this.getPendiente() + this.getOrdenadaOrigen();

    } else {
      throw new IllegalArgumentException("La estacion no esta en la correlacion");
    }

    return rv;
  }

  public Estacion getCorrelacionado(Estacion e) {
    Estacion rv = null;
    if (e == this.getE1()) {
      rv = this.getE2();
    } else if (e == this.getE2()) {
      rv = this.getE1();
    }

    return rv;
  }
}