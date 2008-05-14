package ar.uba.dcao.dbclima.casosDeUso.browsers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.EstacionHelper;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.utils.CollectionUtils;

public class VariacionTemp implements Browser {

  private static final DecimalFormat frmt = new DecimalFormat("00.00");

  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    EstacionDAO edao = DAOFactory.getEstacionDAO(sess);
    List<Long> estIDs = edao.findAllNamedIDs();

    for (Long id : estIDs) {
      Estacion e = edao.findByID(id);
      System.out.println("\nEstacion " + e.getNombre() + " / " + e.getProvincia());
      verVariacion(e, ProyectorRegistro.PROY_TMIN);
      sess.clear();
    }

    sess.close();
  }

  public static void verVariacion(Estacion e, ProyectorRegistro proy) {
    List<RegistroDiario>[] registrosPorMes = EstacionHelper.getRegistrosPorMes(e);

    for (List<RegistroDiario> regs : registrosPorMes) {
      verVariacion(regs, proy);
    }
  }

  public static void verVariacion(List<RegistroDiario> regs, ProyectorRegistro proy) {
    List<RegistroDiario> fuente = new ArrayList<RegistroDiario>(regs);
    List<RegistroDiario> dest = new ArrayList<RegistroDiario>(regs);

    fuente.remove(fuente.size() - 1);
    dest.remove(0);

    List<Double> valsF = new ArrayList<Double>();
    List<Double> valsD = new ArrayList<Double>();

    for (RegistroDiario rd : fuente) {
      Integer valor = proy.getValor(rd);
      if (valor != null) {
        valsF.add(valor.doubleValue());
      }
    }
    for (RegistroDiario rd : dest) {
      Integer valor = proy.getValor(rd);
      if (valor != null) {
        valsD.add(valor.doubleValue());
      }
    }

    double avgF = CollectionUtils.avg(valsF);
    double avgD = CollectionUtils.avg(valsD);
    double stdvF = CollectionUtils.stdv(valsF, avgF);
    double stdvD = CollectionUtils.stdv(valsD, avgD);

    double sumXY = 0;
    double sumX = 0;
    double sumY = 0;
    double sumX2 = 0;
    double sumY2 = 0;

    int n = 0;

    for (int i = 0; i < fuente.size(); i++) {
      Integer v1I = proy.getValor(fuente.get(i));
      Integer v2I = proy.getValor(dest.get(i));

      if (v1I != null && v2I != null) {
        double v1 = (v1I - avgF) / stdvF;
        double v2 = (v2I - avgD) / stdvD;

        n++;
        sumXY += v1 * v2;
        sumX += v1;
        sumY += v2;
        sumX2 += v1 * v1;
        sumY2 += v2 * v2;
      }
    }

    /*
     * Calculo de relacion lineal x minimos cuadrados
     */
    double b = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    double a = (sumY * sumX2 - sumX * sumXY) / (n * sumX2 - sumX * sumX);

    /* Forma vieja, la habia despejado yo a mano, casi igual resultado. */
    // double b = (sumXY - sumX * sumY / n) / (sumX2 - sumX * sumX / n);
    // double a = (sumY - b * sumX) / n;
    List<Double> error = new ArrayList<Double>();

    for (int i = 0; i < fuente.size(); i++) {
      Integer v1I = proy.getValor(fuente.get(i));
      Integer v2I = proy.getValor(dest.get(i));

      if (v1I != null && v2I != null) {
        double v1 = (v1I - avgF) / stdvF;
        double v2 = (v2I - avgD) / stdvD;

        error.add(v2 - v1 * b - a);
      }
    }

    double avgErr = CollectionUtils.avg(error);
    double stdvErr = CollectionUtils.stdv(error, avgErr);
    System.out.println("Pendiente estLin: " + frmt.format(b) + " varError: " + frmt.format(stdvErr) + " sum: "
        + frmt.format(b + stdvErr * 0.5));
  }
}
