/**
 * 
 */
package ar.uba.dcao.dbclima.data;

import java.util.Date;

/**
 * Clase que representa a una sequia. Tiene una referencia a la estacion que pertenece
 * y al registro diario que indica su comienzo.
 *
 */
public final class Sequia extends PersistentObject {
  
  /**
   * la cantidad de dias que dura la sequia.
   */
  private Integer longitud;
  
  /**
   * la fecha donde comienza la sequia.
   */
  private Date comienzo;
  
  /**
   * la estacion a la que pertenece la sequia.
   */
  private Estacion estacion;
  
  /**
   * el registro del primer dia de la sequia.
   */
  private RegistroDiario registroComienzo;
  
  /**
   * Constructor por omision.
   */
  public Sequia() {}
  
  /**
   * @return la cantidad de dias que dura la sequia.
   */
  public Integer getLongitud() {
    return this.longitud;
  }
  
  /**
   * @return la fecha donde comienza la sequia.
   */
  public Date getComienzo() {
    return this.comienzo;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Drought{ Start Date:" + this.getComienzo() + "; Length:" + this.getLongitud() + '}';
  }

  public void setLongitud(Integer longitud) {
    this.longitud = longitud;
  }

  public void setComienzo(Date comienzo) {
    this.comienzo = comienzo;
  }

  public Estacion getEstacion() {
    return estacion;
  }

  public void setEstacion(Estacion estacion) {
    this.estacion = estacion;
  }

  public RegistroDiario getRegistroComienzo() {
    return registroComienzo;
  }

  public void setRegistroComienzo(RegistroDiario registroComienzo) {
    this.registroComienzo = registroComienzo;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((comienzo == null) ? 0 : comienzo.hashCode());
    result = prime * result + ((estacion == null) ? 0 : estacion.hashCode());
    result = prime * result + ((longitud == null) ? 0 : longitud.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    final Sequia other = (Sequia) obj;
    if (comienzo == null) {
      if (other.comienzo != null)
        return false;
    } else if (!comienzo.equals(other.comienzo))
      return false;
    if (estacion == null) {
      if (other.estacion != null)
        return false;
    } else if (!estacion.equals(other.estacion))
      return false;
    if (longitud == null) {
      if (other.longitud != null)
        return false;
    } else if (!longitud.equals(other.longitud))
      return false;
    return true;
  }

}