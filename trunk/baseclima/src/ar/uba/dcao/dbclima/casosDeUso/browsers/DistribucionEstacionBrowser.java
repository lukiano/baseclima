package ar.uba.dcao.dbclima.casosDeUso.browsers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.EstacionHelper;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.utils.CollectionUtils;

public class DistribucionEstacionBrowser implements Browser {

  private static final DecimalFormat ftr = new DecimalFormat("0.00");

  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    printDistInfo(100, sess);

    sess.close();
  }

  @SuppressWarnings("unchecked")
  public static void printDistInfo(int idEstacion, Session sess) {
    Estacion e = (Estacion) sess.createQuery("FROM Estacion rd WHERE codigoSMN = " + idEstacion).uniqueResult();

    List<RegistroDiario>[] regsXMes = EstacionHelper.getRegistrosPorMes(e);
    List<List<Double>> regsLT = new ArrayList<List<Double>>();

    for (List<RegistroDiario> r : regsXMes) {
      List<Double> regsT = new ArrayList<Double>();
      regsLT.add(regsT);
      for (RegistroDiario rd : r) {
        if (rd.getTempMax() != null) {
          regsT.add(rd.getTempMax().doubleValue());
        }
      }
    }

    double minDistP25 = 1000;
    double maxDistP25 = 0;
    double minDistP75 = 1000;
    double maxDistP75 = 0;

    System.out.println("Estacion " + idEstacion);
    for (int i = 0; i < 12; i++) {
      List<Double> regsT = regsLT.get(i);
      double av = CollectionUtils.avg(regsT);
      double stdv = CollectionUtils.stdv(regsT, av);
      double p25 = CollectionUtils.percentil(regsT, 0.25);
      double p50 = CollectionUtils.percentil(regsT, 0.5);
      double p75 = CollectionUtils.percentil(regsT, 0.75);
      double darr = p75 - p50;
      double dab = p50 - p25;

      minDistP75 = Math.min(minDistP75, darr);
      minDistP25 = Math.min(minDistP25, dab);
      maxDistP75 = Math.max(maxDistP75, darr);
      maxDistP25 = Math.max(maxDistP25, dab);

      int qty = regsT.size();
      System.out.println((i+1) + ": " + qty + " stdv " + ftr.format(stdv) + " y distancia de la med a cuart (sup/inf) de " + darr + " y " + dab);
    }

    System.out.println("Distancias extremas: " + minDistP25 + "/" + maxDistP25 + "(ab) y " + minDistP75 + "/" + maxDistP75 + "(arr)");
  }
}
