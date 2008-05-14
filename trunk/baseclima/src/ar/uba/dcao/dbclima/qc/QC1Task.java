package ar.uba.dcao.dbclima.qc;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.qc.qc1.QC1;

/**
 * Task que corre el proceso de QualityCheck interno a las estaciones de la base.<br>
 * XXX: Falta incorporar el concepto de Dataset.
 */
public class QC1Task extends DatasetPorEstacionTask {

  private final Long datasetID;

  public QC1Task(Long datasetID) {
    this.datasetID = datasetID;
  }

  @Override
  public void processEstacion(Estacion estacion, Session session) {
    new QC1(estacion).correrTests();
  }

  @Override
  public boolean estacionIsToBeProcessed(Estacion estacion) {
    /* XXX: Por ahora no se filtran estaciones para QC. */
    return true;
  }

  @Override
  public Long getDatasetId() {
    return this.datasetID;
  }

  @Override
  public void updateGUIWhenCompleteSuccessfully() {
  }

  public String getProgressDescription() {
    return "Aplicando QC1 a estaciones.";
  }

}
