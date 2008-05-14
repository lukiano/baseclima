package ar.uba.dcao.dbclima.data;

import java.util.Date;
import java.util.List;

/**
 * Un dataset encapsula una importacion determinada de datos. Estos pueden ser registros satelitales (que son auxiliares)
 * o registros de estaciones de metereologia, que son los que seran examinados y clasificados por los controles de calidad.
 * <P>Este conjunto de datos puede marcarse como prueba o de referencia. Pueden haber varios datasets de referencia, y los datos
 * de los mismos sirven para realizar controles de calidad en los datasets de prueba.
 * Los datasets de referencia tambien pueden ser analizados por los controles de calidad, y de hecho este es el analisis mas importante
 * pues da un panorama general de la calidad de todos los datos disponibles. 
 */
public class Dataset extends PersistentObject {

  public static final int ESTADO_INGRESADO = 0;

  public static final int ESTADO_CHECK = 1;

  public static final int ESTADO_CLASIFICADO = 2;

  private String usuario;

  private String fuente;

  private Date fechaCreacion;

  private boolean referente = false;

  private int estado = ESTADO_INGRESADO;

  private List<Estacion> estaciones;

  private List<RegistroDiario> registros;
  
  private List<PuntoSatelital> puntosSatelitales;
  
  private List<RegistroSatelital> registrosSatelitales;

  public Dataset() {
  }

  public Dataset(String usuario, String fuente, Date fechaCreacion) {
    this.usuario = usuario;
    this.fuente = fuente;
    this.fechaCreacion = fechaCreacion;
  }

  public String getUsuario() {
    return usuario;
  }

  public void setUsuario(String autor) {
    this.usuario = autor;
  }

  public boolean isReferente() {
    return referente;
  }

  public void setReferente(boolean referente) {
    this.referente = referente;
  }

  public Date getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(Date fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  public String getFuente() {
    return fuente;
  }

  public void setFuente(String fuente) {
    this.fuente = fuente;
  }

  public int getEstado() {
    return estado;
  }

  public void setEstado(int estado) {
    this.estado = estado;
  }

  public List<RegistroDiario> getRegistros() {
    return registros;
  }

  public void setRegistros(List<RegistroDiario> registros) {
    this.registros = registros;
  }
  
  public List<Estacion> getEstaciones() {
    return estaciones;
  }

  public void setEstaciones(List<Estacion> estaciones) {
    this.estaciones = estaciones;
  }

  
  public List<PuntoSatelital> getPuntosSatelitales() {
    return puntosSatelitales;
  }

  public void setPuntosSatelitales(List<PuntoSatelital> puntosSatelitales) {
    this.puntosSatelitales = puntosSatelitales;
  }

  public List<RegistroSatelital> getRegistrosSatelitales() {
    return registrosSatelitales;
  }

  public void setRegistrosSatelitales(List<RegistroSatelital> registrosSatelitales) {
    this.registrosSatelitales = registrosSatelitales;
  }

  @Override
  public String toString() {
    return "Dataset importado por " + this.usuario + " con fuente " + this.fuente;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + estado;
    result = prime * result + ((fechaCreacion == null) ? 0 : fechaCreacion.hashCode());
    result = prime * result + ((fuente == null) ? 0 : fuente.hashCode());
    result = prime * result + (referente ? 1231 : 1237);
    result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Dataset other = (Dataset) obj;
    if (estado != other.estado)
      return false;
    if (fechaCreacion == null) {
      if (other.fechaCreacion != null)
        return false;
    } else if (!fechaCreacion.equals(other.fechaCreacion))
      return false;
    if (fuente == null) {
      if (other.fuente != null)
        return false;
    } else if (!fuente.equals(other.fuente))
      return false;
    if (referente != other.referente)
      return false;
    if (usuario == null) {
      if (other.usuario != null)
        return false;
    } else if (!usuario.equals(other.usuario))
      return false;
    return true;
  }

}
