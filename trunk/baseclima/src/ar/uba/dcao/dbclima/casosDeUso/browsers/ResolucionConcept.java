package ar.uba.dcao.dbclima.casosDeUso.browsers;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.ClasificadorRegistroDiario;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.qc.resolucion.ChiSquareTest;
import ar.uba.dcao.dbclima.qc.resolucion.ResolucionHelper;
import ar.uba.dcao.dbclima.utils.FechaHelper;

public class ResolucionConcept implements Browser {

  private static final DecimalFormat frmt = new DecimalFormat("0.000");

  private ProyectorRegistro proyector;

  private static final ClasificadorRegistroDiario clfSem = new ClasificadorRegistroDiario() {
    private final Calendar cal = Calendar.getInstance();

    public Serializable clasificar(RegistroDiario rd) {
      cal.setTime(rd.getFecha());
      int mes = cal.get(Calendar.MONTH);
      int ano = cal.get(Calendar.YEAR);
      return String.valueOf(ano) + (mes > 5 ? 2 : 1);
    }
  };

  public static void main(String[] args) {
    ResolucionConcept res = new ResolucionConcept(ProyectorRegistro.PROY_TMAX);

    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);
    List<Long> ests = estacionDAO.findAllNamedIDs();

    for (Long id : ests) {
      sess.beginTransaction();

      Estacion e = estacionDAO.findByID(id);
      res.evaluarEstacion(e);

      sess.clear();
    }
  }

  public ResolucionConcept(ProyectorRegistro proyector) {
    this.proyector = proyector;
  }

  public void evaluarEstacion(Estacion e) {
    System.out.println("Estacion " + e.getNombre() + "/" + e.getProvincia());

    Map<Serializable, List<RegistroDiario>> regsXano = clasificarRegistros(e, clfSem);
    List<List<RegistroDiario>> regsAsList = new ArrayList<List<RegistroDiario>>(regsXano.values());
    ResolucionHelper.definirResolucionPeriodo(regsAsList, proyector);

    // buscarSaltos(e.getRegistros(), this.proyector);
  }

  public void buscarSaltos(List<RegistroDiario> regs, ProyectorRegistro proy) {
    int mes = FechaHelper.dameMes(regs.get(300).getFecha());
    List<Double> diferencias = new ArrayList<Double>();
    List<Integer> idxReg = new ArrayList<Integer>();

    for (int i = 300; i < regs.size() - 300; i++) {
      if (mes != FechaHelper.dameMes(regs.get(i).getFecha())) {
        mes = FechaHelper.dameMes(regs.get(i).getFecha());
        Double diff = this.verDiff(regs.subList(i - 300, i), regs.subList(i + 1, i + 300), proy);
        diferencias.add(diff);
        idxReg.add(i);
      }
    }

    for (int i = 1; i < diferencias.size() - 1; i++) {
      Double dif = diferencias.get(i);
//      Double difAnt = diferencias.get(i - 1);
//      Double difSig = diferencias.get(i + 1);

      // if (dif != null && difAnt != null && difSig != null && dif > 0.5 && dif > difAnt
      // && dif > difSig) {
      String diffS = (dif == null) ? "null" : frmt.format(dif);
      System.out.println(diffS + " " + regs.get(idxReg.get(i)).getFecha());
      // }
    }
  }

  private Double verDiff(List<RegistroDiario> r1, List<RegistroDiario> r2, ProyectorRegistro proy) {
    int nullC1 = 0;
    int nullC2 = 0;
    for (RegistroDiario rd : r1) {
      if (proy.getValor(rd) == null) {
        nullC1++;
      }
    }
    for (RegistroDiario rd : r2) {
      if (proy.getValor(rd) == null) {
        nullC2++;
      }
    }

    if (Math.max(nullC1, nullC2) > 150) {
      return null;
    }

    int[] f1 = ResolucionHelper.acumuladosDecimales(r1, proy);
    int[] f2 = ResolucionHelper.acumuladosDecimales(r2, proy);

    return ChiSquareTest.sameDistribution(f1, f2, r1.size() - nullC1, r2.size() - nullC2);
  }

  /**
   * Devuelve los registros de la estacion parametro, catalogadas por año y manteniendo su
   * orden relativo.
   */
  private Map<Serializable, List<RegistroDiario>> clasificarRegistros(Estacion e,
      ClasificadorRegistroDiario clasificador) {
    List<RegistroDiario> regs = e.getRegistros();

    Map<Serializable, List<RegistroDiario>> rv = new TreeMap<Serializable, List<RegistroDiario>>();

    for (RegistroDiario rd : regs) {
      Serializable catRd = clasificador.clasificar(rd);

      if (rv.get(catRd) == null) {
        rv.put(catRd, new ArrayList<RegistroDiario>());
      }

      rv.get(catRd).add(rd);
    }

    return rv;
  }
}
