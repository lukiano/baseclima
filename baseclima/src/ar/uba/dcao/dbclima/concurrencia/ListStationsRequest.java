package ar.uba.dcao.dbclima.concurrencia;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;


public class ListStationsRequest implements Query {

  protected volatile List<Estacion> estaciones;

  private boolean complete;
  
  public ListStationsRequest() {
  }

  public Object getResult() {
    return this.estaciones;
  }

  public boolean isComplete() {
    return this.complete;
  }

  @SuppressWarnings("unchecked")
  public boolean run(SessionFactory runningFactory) {
    Session sess = runningFactory.getCurrentSession();
    sess.beginTransaction();
    this.obtenerEstaciones(sess);
    for (Estacion estacion : this.estaciones) {
      sess.evict(estacion);
    }
    sess.close();
    this.complete = true;
    return true;
  }

  protected void obtenerEstaciones(Session sess) {
    this.estaciones = DAOFactory.getEstacionDAO(sess).findAll();
  }
  
  public void updateGUIWhenCompleteSuccessfully() {
  }
}
