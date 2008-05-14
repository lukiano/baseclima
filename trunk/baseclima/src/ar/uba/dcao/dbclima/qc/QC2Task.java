package ar.uba.dcao.dbclima.qc;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.qc.qc1.AsignadorConfianza;

/**
 * Task que corre el proceso de QualityCheck interno a las estaciones de la base.<br>
 * XXX: Falta incorporar el concepto de Dataset.
 */
public class QC2Task extends DatasetPorEstacionTask {

  private final Long datasetID;

  private AsignadorConfianza asignadorTN = new AsignadorConfianza(ProyectorRegistro.PROY_TMIN);

  private AsignadorConfianza asignadorTX = new AsignadorConfianza(ProyectorRegistro.PROY_TMAX);

  public QC2Task(Long datasetID) {
    this.datasetID = datasetID;
  }

  @Override
  public void processEstacion(Estacion estacion, Session sess) {
    asignadorTN.asignarConfianzaL1(estacion, sess);
    asignadorTX.asignarConfianzaL1(estacion, sess);
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
    return "Clasificando registros.";
  }

}
