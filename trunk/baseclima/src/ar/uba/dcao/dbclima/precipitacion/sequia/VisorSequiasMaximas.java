package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Este programa muestra en pantalla a aquellas sequias de mayor longitud, independientemente
 * de la estacion en la que se encuentren.
 *
 */
public class VisorSequiasMaximas {
  
  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    List<Sequia> sequias = DAOFactory.getSequiaDAO(sess).findAllOrderedByLength();
    try {
      for (int i = 0; i < sequias.size() && i < 100; i++) {
        Sequia sequia = sequias.get(i);
        Estacion estacion = sequia.getEstacion();
        String mensaje = "Station: " + estacion.getNombre() + " (" + estacion.getId() + ") - Start Date:" + sequia.getComienzo() + " - Days:" + (int)sequia.getLongitud();
        System.out.println(mensaje);
      }
    } finally {
      sess.getTransaction().commit();
    }
  }

}
