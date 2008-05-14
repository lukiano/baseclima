package ar.uba.dcao.dbclima.data;

import java.util.Date;

/**
 * Registro diario, semanal o mensual (segun corresponda) que indica para un determinado momento
 * la cantidad de precipitacion caida en un determinado punto satelital.
 * @see PuntoSatelital
 */
public class RegistroSatelital extends PersistentObject {

  private Date fecha;
  
  private Integer lluvia;
  
  private Dataset dataset;
  
  private PuntoSatelital puntoSatelital;
  
  public Date getFecha() {
    return this.fecha;
  }

  public void setFecha(Date fecha) {
    this.fecha = fecha;
  }

  public Integer getLluvia() {
    return this.lluvia;
  }

  public void setLluvia(Integer lluvia) {
    this.lluvia = lluvia;
  }

  public Dataset getDataset() {
    return dataset;
  }

  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }

  public PuntoSatelital getPuntoSatelital() {
    return puntoSatelital;
  }

  public void setPuntoSatelital(PuntoSatelital puntoSatelital) {
    this.puntoSatelital = puntoSatelital;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
    result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
    result = prime * result + ((puntoSatelital == null) ? 0 : puntoSatelital.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final RegistroSatelital other = (RegistroSatelital) obj;
    if (dataset == null) {
      if (other.dataset != null)
        return false;
    } else if (!dataset.equals(other.dataset))
      return false;
    if (fecha == null) {
      if (other.fecha != null)
        return false;
    } else if (!fecha.equals(other.fecha))
      return false;
    if (puntoSatelital == null) {
      if (other.puntoSatelital != null)
        return false;
    } else if (!puntoSatelital.equals(other.puntoSatelital))
      return false;
    return true;
  }
  
  

}