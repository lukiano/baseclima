package ar.uba.dcao.dbclima.casosDeUso.batchs;

import java.util.List;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

public class ConvTemp {

  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    Transaction transaction = sess.beginTransaction();

    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);
    List<Estacion> estaciones = estacionDAO.findAll();

    for (Estacion e : estaciones) {
      int lat = e.getLatitud();
      int lon = e.getLongitud();

      float latFraccion = (lat % 100) / 0.6f;
      lat += latFraccion - (lat % 100);

      float lonFraccion = (lon % 100) / 0.6f;
      lon += lonFraccion - (lon % 100);

      System.out.println(e.getLatitud() + " a " + lat);
      System.out.println(e.getLongitud() + " a " + lon);

      e.setLatitud(lat);
      e.setLongitud(lon);
    }

    transaction.commit();
    DBSessionFactory.getInstance().close();
  }
}
