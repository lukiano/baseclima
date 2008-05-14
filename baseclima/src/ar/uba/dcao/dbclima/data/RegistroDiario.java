package ar.uba.dcao.dbclima.data;

import java.util.Date;
import java.util.Set;

import ar.uba.dcao.dbclima.qc.ConfVarFactory;

/**
 * Clase que representa un registro de un dia de una estacion.
 * Contiene la fecha y datos varios, como valores de temperatura y precipitacion para
 * ese dia en esa estacion.
 * Tambien se guardan los codigos clasificatorios de esos valores.
 * Un RegistroDiario pertenece a un unico Dataset.
 * @see Estacion
 * @see Dataset
 *
 */
public class RegistroDiario extends PersistentObject {

  public static final String SIGLA_TX = "Tx";

  public static final String SIGLA_TN = "Tn";

  private Date fecha;

  private Short tempMin;

  private Short tempMax;

  private Integer precipitacion;

  private Boolean hayLluvia;

  private RegistroDiario ayer;

  private RegistroDiario maniana;

  private String codigoConfianzaTempMin;

  private String codigoConfianzaTempMax;

  private String codigoConfianzaTempRange;
  
  private String codigoConfianzaPrecip;
  
  private String codigoConfianzaDrought;

  private Dataset dataset;

  private Estacion estacion;

  private Set<ResultadoTestQC> resultadosQC;

  public Date getFecha() {
    return fecha;
  }

  public void setFecha(Date fecha) {
    this.fecha = fecha;
  }

  public Boolean getHayLluvia() {
    return hayLluvia;
  }

  public void setHayLluvia(Boolean hayLluvia) {
    this.hayLluvia = hayLluvia;
  }

  public Integer getPrecipitacion() {
    return precipitacion;
  }

  public void setPrecipitacion(Integer precipitacion) {
    this.precipitacion = precipitacion;
  }

  public Short getTempMax() {
    return tempMax;
  }

  public void setTempMax(Short tempMax) {
    this.tempMax = tempMax;
  }

  public Short getTempMin() {
    return tempMin;
  }

  public void setTempMin(Short tempMin) {
    this.tempMin = tempMin;
  }

  public RegistroDiario getAyer() {
    return ayer;
  }

  public void setAyer(RegistroDiario ayer) {
    this.ayer = ayer;
  }

  public RegistroDiario getManiana() {
    return maniana;
  }

  public void setManiana(RegistroDiario maniana) {
    this.maniana = maniana;
  }

  public Estacion getEstacion() {
    return estacion;
  }

  public void setEstacion(Estacion estacion) {
    this.estacion = estacion;
  }

  public void setResultadosQC(Set<ResultadoTestQC> resultadosQC) {
    this.resultadosQC = resultadosQC;
  }

  public Set<ResultadoTestQC> getResultadosQC() {
    return resultadosQC;
  }

  public void registrarResultadoTestQC(ResultadoTestQC res) {
    // Se elimina cualquier version anterior del resultado a registrar.
    ResultadoTestQC oldRes = null;
    for (ResultadoTestQC rt : this.getResultadosQC()) {
      if (rt.getTestID().equals(res.getTestID())) {
        oldRes = rt;
        break;
      }
    }

    this.getResultadosQC().remove(oldRes);

    res.setRegistro(this);
    this.resultadosQC.add(res);
  }

  @Override
  public String toString() {
    return this.getEstacion() + " / " + this.getFecha();
  }

  public ResultadoTestQC getResultadoByID(String resID) {
    for (ResultadoTestQC res : this.getResultadosQC()) {
      if (res.getTestID().equals(resID)) {
        return res;
      }
    }

    return null;
  }

  public ConfianzaVariable getConfianzaTempMax() {
    return ConfVarFactory.get(this.codigoConfianzaTempMax);
  }

  public ConfianzaVariable getConfianzaTempMin() {
    return ConfVarFactory.get(this.codigoConfianzaTempMin);
  }

  public ConfianzaVariable getConfianzaTempRange() {
    return ConfVarFactory.get(this.codigoConfianzaTempRange);
  }

  public String getCodigoConfianzaTempMin() {
    return codigoConfianzaTempMin;
  }

  public void setCodigoConfianzaTempMin(String codigoConfianzaTn) {
    this.codigoConfianzaTempMin = codigoConfianzaTn;
  }

  public String getCodigoConfianzaTempMax() {
    return codigoConfianzaTempMax;
  }

  public void setCodigoConfianzaTempMax(String codigoConfianzaTx) {
    this.codigoConfianzaTempMax = codigoConfianzaTx;
  }

  public String getCodigoConfianzaTempRange() {
    return codigoConfianzaTempRange;
  }

  public void setCodigoConfianzaTempRange(String codigoConfianzaTr) {
    this.codigoConfianzaTempRange = codigoConfianzaTr;
  }

  public Dataset getDataset() {
    return dataset;
  }

  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }

  public String getCodigoConfianzaPrecip() {
    return codigoConfianzaPrecip;
  }

  public void setCodigoConfianzaPrecip(String codigoConfianzaPrecip) {
    this.codigoConfianzaPrecip = codigoConfianzaPrecip;
  }

  public String getCodigoConfianzaDrought() {
    return codigoConfianzaDrought;
  }

  public void setCodigoConfianzaDrought(String codigoConfianzaDrought) {
    this.codigoConfianzaDrought = codigoConfianzaDrought;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
    result = prime * result + ((estacion == null) ? 0 : estacion.hashCode());
    result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
    result = prime * result + ((precipitacion == null) ? 0 : precipitacion.hashCode());
    result = prime * result + ((tempMax == null) ? 0 : tempMax.hashCode());
    result = prime * result + ((tempMin == null) ? 0 : tempMin.hashCode());
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
    final RegistroDiario other = (RegistroDiario) obj;
    if (dataset == null) {
      if (other.dataset != null)
        return false;
    } else if (!dataset.equals(other.dataset))
      return false;
    if (estacion == null) {
      if (other.estacion != null)
        return false;
    } else if (!estacion.equals(other.estacion))
      return false;
    if (fecha == null) {
      if (other.fecha != null)
        return false;
    } else if (!fecha.equals(other.fecha))
      return false;
    if (precipitacion == null) {
      if (other.precipitacion != null)
        return false;
    } else if (!precipitacion.equals(other.precipitacion))
      return false;
    if (tempMax == null) {
      if (other.tempMax != null)
        return false;
    } else if (!tempMax.equals(other.tempMax))
      return false;
    if (tempMin == null) {
      if (other.tempMin != null)
        return false;
    } else if (!tempMin.equals(other.tempMin))
      return false;
    return true;
  }
  
}