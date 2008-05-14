package ar.uba.dcao.dbclima.data;

import java.util.Date;
import java.util.List;

/**
 * Dato de persistencia que representa a un punto de la grilla de medicion de precipitacion utilizando satelites.
 *
 */
public class PuntoSatelital extends PersistentObject {

  private Integer latitud;
  
  private Integer longitud;
  
  private Dataset dataset;
  
  private Date fechaInicio;

  private Date fechaFin;
  
  private List<RegistroSatelital> registrosSatelitales;

  public Integer getLatitud() {
    return latitud;
  }

  public void setLatitud(Integer latitud) {
    this.latitud = latitud;
  }

  public Integer getLongitud() {
    return longitud;
  }

  public void setLongitud(Integer longitud) {
    this.longitud = longitud;
  }
  
  public Dataset getDataset() {
    return dataset;
  }

  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }

  public Date getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(Date fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public Date getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(Date fechaFin) {
    this.fechaFin = fechaFin;
  }

  public List<RegistroSatelital> getRegistrosSatelitales() {
    return registrosSatelitales;
  }

  public void setRegistrosSatelitales(List<RegistroSatelital> registrosSatelitales) {
    this.registrosSatelitales = registrosSatelitales;
  }
  
  @Override
  public String toString() {
    return "PntSat:{lat:" + this.getLatitud() + " lot:" + this.getLongitud() + "}"; 
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((latitud == null) ? 0 : latitud.hashCode());
    result = prime * result + ((longitud == null) ? 0 : longitud.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    final PuntoSatelital other = (PuntoSatelital) obj;
    if (latitud == null) {
      if (other.latitud != null)
        return false;
    } else if (!latitud.equals(other.latitud))
      return false;
    if (longitud == null) {
      if (other.longitud != null)
        return false;
    } else if (!longitud.equals(other.longitud))
      return false;
    return true;
  }

}
