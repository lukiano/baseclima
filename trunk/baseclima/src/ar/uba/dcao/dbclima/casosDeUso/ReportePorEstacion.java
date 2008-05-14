package ar.uba.dcao.dbclima.casosDeUso;

import java.io.IOException;
import java.io.Writer;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;

public abstract class ReportePorEstacion {

  protected abstract String printHeader();

  protected abstract String printEstacion(Estacion estacion);

  public void writeReport(Writer writer, SessionFactory factory) throws IOException {
    Session sess = factory.getCurrentSession();
    sess.beginTransaction();
    EstacionDAO edao = DAOFactory.getEstacionDAO(sess);

    writer.write(printHeader() + "\n");

    for (Long estID : edao.findAllIDs()) {
      Estacion est = (Estacion) sess.load(Estacion.class, estID);
      String descEstacion = this.printEstacion(est);

      if (descEstacion != null) {
        writer.write(descEstacion + "\n");
      }

      writer.flush();
      sess.clear();
    }

    writer.close();
    sess.close();
  }
}
