package ar.uba.dcao.dbclima.casosDeUso.batchs;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.persistence.TestResultsFileOut;
import ar.uba.dcao.dbclima.qc.qc1.QC1;

public class SOMExporter {

  private static final String EXP_TN_FILENAME = "tn.txt";

  private static final String EXP_TX_FILENAME = "tx.txt";

  public static void main(String[] args) {

    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    List<Estacion> ests = new ArrayList<Estacion>();
    for (Estacion e : new EstacionDAO().findAll()) {
      if (e.getCodigoSMN().equals(166) || e.getCodigoSMN().equals(175)) {
        ests.add(e);
      }
    }

    TestResultsFileOut trfo;

    trfo = new TestResultsFileOut(ests, QC1.testsQC1Tn) {
      @Override
      protected String getLabel(RegistroDiario rd) {
        String temps = "(";
        if (rd.getAyer() != null) {
          temps += rd.getAyer().getTempMin();
        }
        temps += " -> " + rd.getTempMin() + " -> ";
        if (rd.getManiana() != null) {
          temps += rd.getManiana().getTempMin();
        }
        temps += ")";
        String est = "[" + String.valueOf(rd.getEstacion().getNombre()) + "]";
        return est + temps;
      }
    };
    trfo.export(EXP_TN_FILENAME, " ");

    trfo = new TestResultsFileOut(ests, QC1.testsQC1Tx) {
      @Override
      protected String getLabel(RegistroDiario rd) {
        String temps = "(";
        if (rd.getAyer() != null) {
          temps += rd.getAyer().getTempMax();
        }
        temps += " -> " + rd.getTempMax() + " -> ";
        if (rd.getManiana() != null) {
          temps += rd.getManiana().getTempMax();
        }
        temps += ")";
        String est = "[" + String.valueOf(rd.getEstacion().getNombre()) + "]";
        return est + temps;
      }
    };
    trfo.export(EXP_TX_FILENAME, " ");
  }
}
