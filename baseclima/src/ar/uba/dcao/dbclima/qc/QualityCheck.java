package ar.uba.dcao.dbclima.qc;

import java.util.List;

import ar.uba.dcao.dbclima.concurrencia.Task;
import ar.uba.dcao.dbclima.data.Estacion;

/**
 * Esta interfaz representa a una tarea especializada al chequeo de calidad de una estacion o varias.
 *
 */
public interface QualityCheck extends Task {
  
  /**
   * Establece cuales estaciones seran tomadas en cuenta en el chequeo de calidad.
   * @param stationsToProcess las estaciones que seran comprobadas.
   */
  void setStationsToProcess(List<Estacion> stationsToProcess);
  
}
