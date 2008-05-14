package ar.uba.dcao.dbclima.casosDeUso.browsers;

import java.util.List;

import org.hibernate.classic.Session;

import ar.uba.dcao.dbclima.correlation.CatalogoCorrelacion;
import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Browser que releva las estaciones para las que no se podria usar
 * fuertemente la correlacion con otras estaciones en la deteccion de
 * problemas.
 * 
 * <p>
 * Variables libres:
 * <ul>
 * <li>Minima correlacion aceptada entre estaciones.
 * <li>Maxima desviacion de la estimacion asociada.
 * </ul>
 * </p>
 */
public class CorrelationsBrowser implements Browser {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    Session session = DBSessionFactory.getInstance().getCurrentSession();
    session.beginTransaction();
    
    relevo("Tn", session);

    session.close();
  }
  
  @SuppressWarnings("unchecked")
  private static void relevo(String variable, Session session) {
    CatalogoCorrelacion cat = new CatalogoCorrelacion(variable);

    List<Estacion> ests = session.createQuery("FROM Estacion").list();

    System.out.println("Buscando correlaciones entre " + ests.size() + " estaciones");
    for (Estacion e : ests) {
      List<CorrelacionEstaciones> corrs = cat.getCorrelaciones(e, session, 0.83, 32, 20);

      if (corrs.size() < 3) {
        System.out.println("Estacion " + e.getNombre() + " correlacionada con " + corrs.size());
      }
    }

  }
}
