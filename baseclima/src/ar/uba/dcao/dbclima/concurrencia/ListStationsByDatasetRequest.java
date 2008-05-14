package ar.uba.dcao.dbclima.concurrencia;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;

public class ListStationsByDatasetRequest extends ListStationsRequest {
  
  private Long datasetId;
  
  public ListStationsByDatasetRequest(Long datasetId) {
    this.datasetId = datasetId;
  }
  
  protected void obtenerEstaciones(Session sess) {
    this.estaciones = DAOFactory.getEstacionDAO(sess).findAllByDataset(this.datasetId);
  }

}
