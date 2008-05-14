package ar.uba.dcao.dbclima.precipitacion.rango;

import java.util.Date;

/**
 * Implementacion basica de la interfaz Rango. Soporta toString() para depuracion.
 * @see Rango
 *
 */
public class RangoImpl implements Rango {
  
  private Date comienzo;
  private Date fin;
  private String nombre;
  private Double valor;
  
  public RangoImpl(Date comienzo, Date fin, String nombre, Double valor) {
    this.comienzo = comienzo;
    this.fin = fin;
    this.nombre = nombre;
    this.valor = valor;
  }

  public Date comienzo() {
    return this.comienzo;
  }

  public Date fin() {
    return this.fin;
  }

  public String nombre() {
    return this.nombre;
  }

  public Double valor() {
    return this.valor;
  }
  
  @Override
  public String toString() {
    return "Rango{"
      + "Comienzo:" + this.comienzo + ";"
      + "Fin:" + this.fin + ";"
      + "Nombre:" + this.nombre + ";"
      + "Valor:" + this.valor + "}";
  }

}
