package ar.uba.dcao.dbclima.casosDeUso.browsers;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.correlation.CatalogoCorrelacion;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.FiltroRegistro;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.qc.ResultadoVecindad;
import ar.uba.dcao.dbclima.qc.qc1.TestVecindad;
import ar.uba.dcao.dbclima.utils.CollectionUtils;

/**
 * Browser que releva la efectividad de las predicciones de vecindades.
 */
public final class PrediccionVecindadBrowser implements Browser {

  private static final ProyectorRegistro PROYECTOR = ProyectorRegistro.PROY_TMIN;

  private static DecimalFormat frmt = new DecimalFormat("0.00");

  private PrediccionVecindadBrowser() {
  }

  public static void main(String[] args) {
    FileWriter fw1 = null;
    FileWriter fw2 = null;

    try {
      fw1 = new FileWriter("predVecindad.detail.txt");
      fw2 = new FileWriter("predVecindad.table.csv");
      report(fw1, fw2);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    } finally {
      try {
        if (fw1 != null)
          fw1.close();
        if (fw2 != null)
          fw2.close();
      } catch (IOException e) {
        System.out.println("Couldn't close files");
      }
    }

  }

  private static void report(FileWriter fw1, FileWriter fw2) throws IOException {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    fw2.write("Estacion,Regs c/vecindad, Regs no nulos,Error medio pred,Cuartil#1 pred,Cuartil#2 pred,Cuartil#3 pred,"
        + "Perc90,Regs c/1 vecino,Regs c/2 vecinos,Regs c/3 vecinos,Regs c/4 vecinos,Regs c/5 vecinos,"
        + "Regs c/6 vecinos,Regs c/7 vecinos,Regs c/8 vecinos,Regs c/9 vecinos,Regs c/10+ vecinos\n");

    List<Long> idsEst = DAOFactory.getEstacionDAO(sess).findAllIDs().subList(0, 50);
    sess.close();

    int iEstacion = 0;
    for (Long id : idsEst) {
      estudiarEstacion(id, PROYECTOR, fw1, fw2);
      fw1.flush();
      fw2.flush();
      System.out.println("Estacion " + (++iEstacion) + "/" + idsEst.size() + " procesada");
    }
  }

  private static void estudiarEstacion(Long idEstacion, ProyectorRegistro proyector, FileWriter fwDetail,
      FileWriter fwTabla) throws IOException {
    Session session = DBSessionFactory.getInstance().getCurrentSession();
    session.beginTransaction();
    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(session);

    Estacion e = estacionDAO.findByID(idEstacion);
    CatalogoCorrelacion catCorr = new CatalogoCorrelacion(proyector.nombreVariable());
    List<CorrelacionEstaciones> cs = catCorr.getCorrelaciones(e, session, TestVecindad.MIN_CORR,
        TestVecindad.MAX_DESV_PRED, TestVecindad.NUM_VECINOS);

    FiltroRegistro filtro = proyector == (PROYECTOR) ? TestVecindad.FILTRO_QC_TMIN
        : TestVecindad.FILTRO_QC_TMAX;

    TestVecindad te = new TestVecindad(session, e, cs, proyector, true);
    List<Double> errores = new ArrayList<Double>();
    int[] cantVecinos = new int[TestVecindad.NUM_VECINOS + 1];

    int regsAplican = 0;
    for (RegistroDiario rd : e.getRegistros()) {
      if (filtro.aplicaA(rd)) {
        regsAplican++;
        ResultadoVecindad resVec = te.cotejarVecindad(rd);
        if (resVec != null) {
          int cv = resVec.getCantidadVecinos();
          cantVecinos[cv]++;
          double errorPred = Math.abs(proyector.getValor(rd) - resVec.getPrediccion());
          errores.add(errorPred);
        }
      }
    }

    Collections.sort(errores);
    double porcRegsConVecinos = errores.size() / (double) regsAplican * 100;
    String descEst = e.getId() + "/" + e.getNombre() + "/" + e.getProvincia();
    fwDetail.write("Estacion " + descEst + ": " + errores.size() + " registros cotejados sobre " + regsAplican
        + " registros posibles (" + frmt.format(porcRegsConVecinos) + "%)\n");

    fwTabla.write(descEst + "," + errores.size() + "," + regsAplican + ",");

    if (errores.size() > 0) {
      int cuartil = errores.size() / 4;
      double errorMean = CollectionUtils.avg(errores);
      double errorStdv = CollectionUtils.stdv(errores, errorMean);
      String cuartil1 = frmt.format(errores.get(cuartil));
      String cuartil2 = frmt.format(errores.get(cuartil * 2));
      String cuartil3 = frmt.format(errores.get(cuartil * 3));
      int iPerc90 = errores.size() * 9 / 10;
      String perc90 = frmt.format(errores.get(iPerc90));
      fwDetail.write("  Error medio: " + frmt.format(errorMean) + ", cuartiles: " + cuartil1 + "/" + cuartil2 + "/"
          + cuartil3 + ", stdv: " + frmt.format(errorStdv) + "\n");
      fwDetail.write("  Cantidad de vecinos: " + Arrays.toString(cantVecinos) + "\n");

      fwTabla.write(frmt.format(errorMean) + "," + cuartil1 + "," + cuartil2 + "," + cuartil3 + "," + perc90 + ",");
      for (int i = 1; i < 11; i++) {
        fwTabla.write(cantVecinos[i] + ",");
      }
    }

    fwDetail.write("\n");
    fwTabla.write("\n");
    session.close();
  }
}
