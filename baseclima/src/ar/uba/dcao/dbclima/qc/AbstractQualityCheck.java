package ar.uba.dcao.dbclima.qc;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.AbstractTask;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.Estacion;

/**
 * Si se establecen las estaciones a procesar, tiene preferencia por esas. Sino, se fija si hay
 * establecido un dataset. Si lo hay, obtiene sus estaciones y las procesa. Sino, obtiene las
 * estaciones de referencia y las procesa.
 * 
 * @author Luciano (me hago cargo :P )
 * 
 */
public abstract class AbstractQualityCheck extends AbstractTask implements QualityCheck {

  private List<Estacion> stationsToProcess = null;

  private Dataset dataset = null;

  public final List<Estacion> getStationsToProcess(SessionFactory runningFactory) {
    if (this.stationsToProcess == null) {
      if (this.dataset == null) {
        Session session = runningFactory.getCurrentSession();
        session.beginTransaction();
        List<Estacion> stations = DAOFactory.getEstacionDAO(session).findAllForBDR();
        for (Estacion estacion : stations) {
          session.evict(estacion);
        }
        session.close();
        return stations;
      } else {
        Session session = runningFactory.getCurrentSession();
        session.beginTransaction();
        List<Estacion> stations = DAOFactory.getEstacionDAO(session).findAllByDataset(this.dataset);
        for (Estacion estacion : stations) {
          session.evict(estacion);
        }
        session.close();
        return stations;
      }
    } else {
      return this.stationsToProcess;
    }
  }

  public final void setDatasetToProcess(Dataset dataset) {
    this.dataset = dataset;
  }

  public final void setStationsToProcess(List<Estacion> stationsToProcess) {
    this.stationsToProcess = stationsToProcess;
  }

}
