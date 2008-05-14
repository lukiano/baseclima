package ar.uba.dcao.dbclima.casosDeUso.reportes;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.utils.FechaHelper;

public class ReporteErrores {

  private static DecimalFormat df = new DecimalFormat("0.00000");

  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    getErroresXTipoXAnio(sess);
    DBSessionFactory.getInstance().close();
  }

  public static void getErroresXTipoXAnio(Session sess) {
    List<Long> idEsts = DAOFactory.getEstacionDAO(sess).findAllIDs();
    Map<String, Integer> problemas = new HashMap<String, Integer>();

    for (int i = 0; i < idEsts.size(); i++) {
      Long idEst = idEsts.get(i);
      System.out.println("Procesando estacion " + (i + 1) + "/" + idEsts.size());
      Estacion estacion = DAOFactory.getEstacionDAO(sess).findByID(idEst);

      for (RegistroDiario rd : estacion.getRegistros()) {
        ConfianzaVariable confianza = rd.getConfianzaTempMin();
        if (confianza != null && confianza.getConfianza() > 0) {
          String codigo = confianza.getCodigo().substring(0,4);
          String descErr = codigo + "," + confianza.getConfianza() + "," + FechaHelper.dameAnio(rd.getFecha());
          Integer times = problemas.get(descErr);
          if (times == null) {
            times = 0;
          }
          times++;
          problemas.put(descErr, times);
        }
      }

      sess.clear();
    }

    System.out.println("Problema,Confianza,Anio,Cantidad");
    for (Map.Entry<String, Integer> e : problemas.entrySet()) {
      System.out.println(e.getKey() + "," + e.getValue());
    }
  }

  public static void getCorrelacionErroresEntreSi(Session sess) {
    List<Long> idEsts = DAOFactory.getEstacionDAO(sess).findAllIDs();
    int[] combs = new int[8];

    for (int i = 0; i < idEsts.size(); i++) {
      Long idEst = idEsts.get(i);
      System.out.println("Procesando estacion " + (i + 1) + "/" + idEsts.size());
      Estacion estacion = DAOFactory.getEstacionDAO(sess).findByID(idEst);

      for (RegistroDiario rd : estacion.getRegistros()) {
        Integer cMax = rd.getConfianzaTempMax() == null ? null : Math.min(rd.getConfianzaTempMax().getConfianza(), 1);
        Integer cMin = rd.getConfianzaTempMin() == null ? null : Math.min(rd.getConfianzaTempMin().getConfianza(), 1);
        Integer cRng = rd.getConfianzaTempRange() == null ? null : Math.min(rd.getConfianzaTempRange().getConfianza(),
            1);

        /* Con esto filtro los problemas de grado < 2 */
        if (cMax != null && rd.getConfianzaTempMax().getConfianza() < 2)
          cMax = 0;
        if (cMin != null && rd.getConfianzaTempMin().getConfianza() < 2)
          cMin = 0;
        if (cRng != null && rd.getConfianzaTempRange().getConfianza() < 2)
          cRng = 0;

        if (cMax != null && cMin != null && cRng != null) {
          int index = cRng + cMin * 2 + cMax * 4;
          combs[index]++;
        }
      }
      sess.clear();
    }

    System.out.println();
    System.out.println("X: " + combs[0]);
    System.out.println("TR: " + combs[1]);
    System.out.println("TN: " + combs[2]);
    System.out.println("TR+TN: " + combs[3]);
    System.out.println("TX: " + combs[4]);
    System.out.println("TR+TX: " + combs[5]);
    System.out.println("TN+TX: " + combs[6]);
    System.out.println("TR+TN+TX: " + combs[7]);
  }

  @SuppressWarnings("unchecked")
  public static void getCorrelacionErroresPorAnio(Session sess) {
    /* TipoTemp x NivelError x Anio. */
    int[][][] errs = new int[3][4][2007 - 1959 + 1];

    /* TipoTemp x Anio. */
    int[][] cantVals = new int[3][2007 - 1959 + 1];

    System.out
        .println("Anio,Datos TMax OK,Datos TMax NCheck,Datos TMax Doubt,Datos TMax Err,Datos TMax Total,Datos TMin OK,Datos TMin NCheck,Datos TMin Doubt,TMin Err,Datos TMin Total"
            +
            /* ",TR OK,TR NCheck,TR Doubt,TR Err,#Muestras TR" + */
            ",#Regs Anio");

    for (int anioFull = 1959; anioFull < 107; anioFull++) {
      Date from = FechaHelper.dameFecha(anioFull, 1, 1);
      Date to = FechaHelper.dameFecha(anioFull + 1, 1, 1);
      String q = "FROM RegistroDiario r WHERE r.fecha >= ? AND r.fecha <= ?";
      List<RegistroDiario> regs = sess.createQuery(q).setDate(0, from).setDate(1, to).list();
      int indAno = anioFull - 1959;

      for (RegistroDiario rd : regs) {
        Byte confMax = rd.getConfianzaTempMax() == null ? null : rd.getConfianzaTempMax().getConfianza();
        Byte confMin = rd.getConfianzaTempMin() == null ? null : rd.getConfianzaTempMin().getConfianza();
        Byte confRng = rd.getConfianzaTempRange() == null ? null : rd.getConfianzaTempRange().getConfianza();

        if (confMax != null) {
          errs[0][confMax][indAno]++;
          cantVals[0][indAno]++;
        }
        if (confMin != null) {
          errs[1][confMin][indAno]++;
          cantVals[1][indAno]++;
        }
        if (confRng != null) {
          errs[2][confRng][indAno]++;
          cantVals[2][indAno]++;
        }
      }

      System.out.print(anioFull + ",");
      /* iterar con tTemp hasta 3 para estadisticas de rango */
      for (int tTemp = 0; tTemp < 2; tTemp++) {
        for (int nivErr = 0; nivErr < 4; nivErr++) {
          double errRate = errs[tTemp][nivErr][indAno] / (double) cantVals[tTemp][indAno];
          String errRateStr = df.format(errRate);
          System.out.print(errRateStr + ",");
        }
        System.out.print(cantVals[tTemp][indAno] + ",");
      }
      System.out.print(regs.size() + "\n");
      sess.clear();
    }
  }
}
