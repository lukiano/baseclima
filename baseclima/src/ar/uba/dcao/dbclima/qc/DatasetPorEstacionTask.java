package ar.uba.dcao.dbclima.qc;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.Session;

import ar.uba.dcao.dbclima.concurrencia.Task;
import ar.uba.dcao.dbclima.concurrencia.TaskResult;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;

public abstract class DatasetPorEstacionTask implements Task {

  private double completionState = 0d;

  private boolean isComplete = false;

  private TaskResult result;

  public abstract Long getDatasetId();

  public abstract boolean estacionIsToBeProcessed(Estacion estacion);

  public abstract void processEstacion(Estacion estacion, Session session);

  public abstract void updateGUIWhenCompleteSuccessfully();

  public double getCompletionState() {
    return this.completionState;
  }

  public TaskResult getResult() {
    return this.result;
  }

  public boolean isComplete() {
    return this.isComplete;
  }

  public boolean run(SessionFactory runningFactory) {
    Session session = runningFactory.getCurrentSession();
    session.beginTransaction();

    List<Estacion> estacionesDataset = this.getEstacionesFromDataset(session);

    List<Long> estacionesAProcesar = filtrarEstaciones(estacionesDataset);

    boolean finalizoConExito = true;
    try {
      for (int i = 0; i < estacionesAProcesar.size(); i++) {
        session = runningFactory.getCurrentSession();
        session.beginTransaction();
        EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(session);

        Estacion estacion = estacionDAO.findByID(estacionesAProcesar.get(i));
        this.processEstacion(estacion, session);

        /* Actualizo el progreso. */
        this.completionState = (i + 1) / (double) estacionesAProcesar.size();

        session.getTransaction().commit();
      }
    } catch (Exception e) {
      e.printStackTrace();
      this.result = TaskResult.buildUnsuccessfulResult("Se encontro un error durante el proceso", e);
      finalizoConExito = false;
    }

    this.isComplete = true;

    if (finalizoConExito) {
      this.result = TaskResult.buildSuccessfulResult("Proceso finalizo con exito");
    }

    return finalizoConExito;
  }

  private List<Long> filtrarEstaciones(List<Estacion> estacionesListadas) {
    List<Long> estacionesAProcesar = new ArrayList<Long>();
    for (Estacion estacion : estacionesListadas) {
      if (this.estacionIsToBeProcessed(estacion)) {
        estacionesAProcesar.add(estacion.getId());
      }
    }

    return estacionesAProcesar;
  }

  private List<Estacion> getEstacionesFromDataset(Session session) {
    EstacionDAO estDAO = DAOFactory.getEstacionDAO(session);
    List<Estacion> rv;

    if (this.getDatasetId() == null) {
      rv = estDAO.findAllForBDR();
    } else {
      rv = estDAO.findAllByDataset(this.getDatasetId());
    }

    return rv;
  }
}
