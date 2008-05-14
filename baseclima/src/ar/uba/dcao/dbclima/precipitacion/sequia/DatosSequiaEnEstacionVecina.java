/**
 * 
 */
package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.utils.DobleHelper;

/**
 * Estructura que guarda los datos de una estacion vecina y su comparacion con la estacion base.
 * 
 *
 */
public class DatosSequiaEnEstacionVecina {
  
  enum ConfidenceType {
    KS, CORR;
  }

  public Double precipitacionVecina;
  
  public Double spiVecino;
  
  public Double spiLocalAcotado;
  
  public List<Integer> aniosEnComun;
  
  public Double distributionConfidence;
  
  public ConfidenceType distributionConfidenceType;
  
  public DatosSequiaEnEstacionVecina(Double precipitacionVecina, 
      Double spiVecino, 
      Double spiLocalAcotado,
      List<Integer> aniosEnComun,
      ConfidenceType distributionConfidenceType,
      Double distributionConfidence) {
    this.precipitacionVecina = precipitacionVecina;
    this.spiVecino = spiVecino;
    this.spiLocalAcotado = spiLocalAcotado;
    this.aniosEnComun = new ArrayList<Integer>(aniosEnComun);
    this.distributionConfidenceType = distributionConfidenceType;
    this.distributionConfidence = distributionConfidence;
  }
  
  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Original value:");
    stringBuilder.append(DobleHelper.doble2String(this.precipitacionVecina));
    if (this.spiVecino != null) {
      stringBuilder.append(" - Neighbor SPI value :");
      stringBuilder.append(DobleHelper.doble2String(this.spiVecino));
    }
    if (this.spiLocalAcotado != null) {
      stringBuilder.append(" - Local SPI value from neighbor's years:");
      stringBuilder.append(DobleHelper.doble2String(this.spiLocalAcotado));
    }
    if (this.distributionConfidenceType != null) {
      if (distributionConfidenceType == ConfidenceType.KS) {
        stringBuilder.append(" - KS test");
      } else {
        stringBuilder.append(" - Correlation");
      }
      stringBuilder.append(" confidence:");
      stringBuilder.append(DobleHelper.doble2String(this.distributionConfidence));
    }
    return stringBuilder.toString();
  }
  
}