package ar.uba.dcao.dbclima.casosDeUso.reportes.precip;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

public class ReportePrecipitacionMensual {

  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    EstacionDAO edao = DAOFactory.getEstacionDAO(sess);

    PrecipitacionMesEstaciones[] pmes = new PrecipitacionMesEstaciones[12];
    for (int i = 0; i < pmes.length; i++) {
      pmes[i] = new PrecipitacionMesEstaciones(i);
    }

    for (Long estID : edao.findAllIDs()) {
      Estacion est = edao.findByID(estID);

      for (int i = 0; i < pmes.length; i++) {
        pmes[i].registrarMesEstacion(est);
      }

      sess.clear();
    }

    System.out.println("Estacion,LatO,LonO,Mes,Lat,Lon,Altura,Ratio" +
    		",p5,p15,p25,p35,p45,p55,p65,p75,p85,p95" +
    		",tn5,tn15,tn25,tn35,tn45,tn55,tn65,tn75,tn85,tn95" +
    		",tx5,tx15,tx25,tx35,tx45,tx55,tx65,tx75,tx85,tx95");

    for (int i = 0; i < pmes.length; i++) {
      pmes[i].emitirReporte(PrecipitacionMesEstaciones.MODO_NORM_CLASSIC);
    }

    sess.close();
  }
}
