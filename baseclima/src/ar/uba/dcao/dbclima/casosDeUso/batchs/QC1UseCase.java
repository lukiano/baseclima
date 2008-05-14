package ar.uba.dcao.dbclima.casosDeUso.batchs;

import java.util.List;

import org.hibernate.classic.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.qc.qc1.QC1;

public class QC1UseCase {

  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);
    List<Long> estaciones = estacionDAO.findAllIDs();

    batchQC1(estaciones);
  }

  @SuppressWarnings("unchecked")
  public static void batchQC1(List<Long> ids) {

    for (int i = 0; i < ids.size(); i++) {
      System.out.println("Aplicando QC1 a estacion " + (i + 1) + "/" + ids.size());

      Long cod = ids.get(i);
      Session sess = DBSessionFactory.getInstance().getCurrentSession();
      sess.beginTransaction();
      EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);

      Estacion e = estacionDAO.findByID(cod);
      correrTests(sess, e);

      sess.update(e);
      sess.getTransaction().commit();
    }
  }

  private static void correrTests(Session sess, Estacion e) {
    QC1 qc1 = new QC1(e);
    qc1.correrTests();
  }
}
