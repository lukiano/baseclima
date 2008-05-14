package ar.uba.dcao.dbclima.concurrencia;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;

public class ListBDRStationsRequest extends ListStationsRequest {
  
  public ListBDRStationsRequest() {
  }
  
  protected void obtenerEstaciones(Session sess) {
    this.estaciones = DAOFactory.getEstacionDAO(sess).findAllForBDR();
  }

}
