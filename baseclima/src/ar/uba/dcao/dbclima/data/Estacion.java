package ar.uba.dcao.dbclima.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Clase que representa una estacion de metereologia. Contiene su ubicacion en el mapa, altura, 
 * y codigos identificatorios. 
 *
 */
public class Estacion extends PersistentObject {

  private static final Comparator<RegistroDiario> COMPARADOR_POR_FECHA = new Comparator<RegistroDiario>() {
    public int compare(RegistroDiario o1, RegistroDiario o2) {
      return o1.getFecha().compareTo(o2.getFecha());
    }
  };

  public static final int COLLITION_POLICY_REPLACE = 0;

  public static final int COLLITION_POLICY_FAIL = 1;

  public static final int COLLITION_POLICY_PRESERVE = 2;

  private Integer codigoPais;

  private String codigoNacional;

  private Integer codigoSMN;

  private Integer codigoOMM;

  /* Altura sobre el nivel del mar, en metros */
  private Integer altura;

  /* Latitud, en centecimos de grado */
  private Integer latitud;

  /* Longitud, en centecimos de grado */
  private Integer longitud;

  private String nombre;

  /* Referencia de la provincia de la estacion. */
  private String provincia;

  private Date fechaInicio;

  private Date fechaFin;

  private List<RegistroDiario> registros;
  
  private List<Sequia> sequias;

  /*
   * Dataset para el cual fue creado la estacion. Si la estacion forma parte de la BDR su
   * dataset es nulo.
   */
  private Dataset dataset;

  private String ubicacion;

  public Estacion() {
  }

  public Estacion(String nombre, Integer codigoSMN, Integer codigoOMM, Integer altura, Integer latitud, Integer longitud) {
    super();
    this.nombre = nombre;
    this.codigoSMN = codigoSMN;
    this.codigoOMM = codigoOMM;
    this.altura = altura;
    this.latitud = latitud;
    this.longitud = longitud;
    this.registros = new ArrayList<RegistroDiario>();
  }

  public Estacion(String nombre, String ubicacion, Integer codigoPais, Integer codigoOMM, String codNacional,
      Integer altura, Integer latitud, Integer longitud) {
    super();
    this.nombre = nombre;
    this.ubicacion = ubicacion;
    this.codigoPais = codigoPais;
    this.codigoOMM = codigoOMM;
    this.altura = altura;
    this.latitud = latitud;
    this.longitud = longitud;
    this.codigoNacional = codNacional;
    this.registros = new ArrayList<RegistroDiario>();
  }

  public String getUbicacion() {
    return ubicacion;
  }

  public void setUbicacion(String ubicacion) {
    this.ubicacion = ubicacion;
  }

  public Integer getCodigoPais() {
    return codigoPais;
  }

  public void setCodigoPais(Integer codigoPais) {
    this.codigoPais = codigoPais;
  }

  public String getCodigoNacional() {
    return codigoNacional;
  }

  public void setCodigoNacional(String codigoNacional) {
    this.codigoNacional = codigoNacional;
  }

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

  public void setAltura(Integer altura) {
    this.altura = altura;
  }

  public Integer getAltura() {
    return altura;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Integer getCodigoSMN() {
    return codigoSMN;
  }

  public void setCodigoSMN(Integer codigo) {
    this.codigoSMN = codigo;
  }

  public Integer getCodigoOMM() {
    return codigoOMM;
  }

  public void setCodigoOMM(Integer codigoOMN) {
    this.codigoOMM = codigoOMN;
  }

  public List<RegistroDiario> getRegistros() {
    return registros;
  }

  public void setRegistros(List<RegistroDiario> registros) {
    this.registros = registros;
  }

  public List<Sequia> getSequias() {
    return sequias;
  }

  public void setSequias(List<Sequia> sequias) {
    this.sequias = sequias;
  }

  public void addRegistros(List<RegistroDiario> rds, int collitionPolicy) {
    Collections.sort(rds, COMPARADOR_POR_FECHA);
    int iRN = 0;
    int iRV = 0;

    List<RegistroDiario> nuevosRegs = new ArrayList<RegistroDiario>();

    while (iRN < rds.size() && iRV < this.registros.size()) {
      RegistroDiario rN = rds.get(iRN);
      RegistroDiario rV = this.registros.get(iRV);
      int regsComp = rN.getFecha().compareTo(rV.getFecha());

      if (regsComp < 0) {
        nuevosRegs.add(rN);
        iRN++;

      } else if (regsComp > 0) {
        nuevosRegs.add(rV);
        iRV++;

      } else {
        RegistroDiario regToStore = replaceRegistro(rV, rN, collitionPolicy);
        nuevosRegs.add(regToStore);
        iRV++;
        iRN++;
      }
    }

    if (iRV < this.registros.size()) {
      nuevosRegs.addAll(this.registros.subList(iRV, this.registros.size()));

    } else if (iRN < rds.size()) {
      nuevosRegs.addAll(rds.subList(iRN, rds.size()));
    }

    this.setRegistros(nuevosRegs);
  }

  private RegistroDiario replaceRegistro(RegistroDiario oldReg, RegistroDiario newReg, int replacePolicy) {
    RegistroDiario rv;

    if (replacePolicy == COLLITION_POLICY_REPLACE) {
      /* Guarda el nuevo registro. */
      rv = newReg;

    } else if (replacePolicy == COLLITION_POLICY_PRESERVE) {
      /* Guarda el registro original. */
      rv = oldReg;

    } else if (replacePolicy == COLLITION_POLICY_FAIL) {
      /* Registro ya existia y no se permiten remplazos. */
      throw new IllegalArgumentException("El registro ya existe.");

    } else {
      throw new IllegalArgumentException("No se reconoce la politica de colision.");
    }

    return rv;
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

  public String getProvincia() {
    return provincia;
  }

  public void setProvincia(String provincia) {
    this.provincia = provincia;
  }

  public Dataset getDataset() {
    return dataset;
  }

  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }

  @Override
  public String toString() {
    //return "Est: " + this.getCodigoOMM();
    return "Est:{OMM:" + this.getCodigoOMM() + " lat:" + this.getLatitud() + " lot:" + this.getLongitud() + "}";
  }

  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((altura == null) ? 0 : altura.hashCode());
    result = prime * result + ((codigoNacional == null) ? 0 : codigoNacional.hashCode());
    result = prime * result + ((codigoOMM == null) ? 0 : codigoOMM.hashCode());
    result = prime * result + ((codigoPais == null) ? 0 : codigoPais.hashCode());
    result = prime * result + ((latitud == null) ? 0 : latitud.hashCode());
    result = prime * result + ((longitud == null) ? 0 : longitud.hashCode());
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
    final Estacion other = (Estacion) obj;
    if (altura == null) {
      if (other.altura != null)
        return false;
    } else if (!altura.equals(other.altura))
      return false;
    if (codigoNacional == null) {
      if (other.codigoNacional != null)
        return false;
    } else if (!codigoNacional.equals(other.codigoNacional))
      return false;
    if (codigoOMM == null) {
      if (other.codigoOMM != null)
        return false;
    } else if (!codigoOMM.equals(other.codigoOMM))
      return false;
    if (codigoPais == null) {
      if (other.codigoPais != null)
        return false;
    } else if (!codigoPais.equals(other.codigoPais))
      return false;
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