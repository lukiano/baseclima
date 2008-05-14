package ar.uba.dcao.dbclima.casosDeUso.batchs;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.casosDeUso.browsers.Browser;
import ar.uba.dcao.dbclima.correlation.CatalogoCorrelacion;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.RegistroHelper;
import ar.uba.dcao.dbclima.data.ResultadoTestQC;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.qc.ResultadoVecindad;
import ar.uba.dcao.dbclima.qc.qc1.DIPTest;
import ar.uba.dcao.dbclima.qc.qc1.OutlierTest;
import ar.uba.dcao.dbclima.qc.qc1.TestVecindad;
import ar.uba.dcao.dbclima.utils.TimeDisplayUtil;

public class ConfianzaUseCase2 implements Browser {

  private static DateFormat dfrmt = new SimpleDateFormat("MM/dd/yyyy");

  private static DecimalFormat frmt = new DecimalFormat("0.00");

  private static final int NUM_VECINOS = 10;

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException {
    Session session = DBSessionFactory.getInstance().getCurrentSession();
    session.beginTransaction();

    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(session);
    List<Long> ids = estacionDAO.findAllIDs();
    session.clear();

    /* Variables para el reporte. */
    FileWriter fw = new FileWriter("Reporte_3vars.csv");
    fw.append("Estacion,Posicion,Fecha,SecuMin,SecuMax,Outl_Tn,DIP_Tn,Outl_Tx,DIP_Tx,OutlRng,");
    fw.append("ConfTn,CodConfTn,ConfTx,CodConfTx,CodConfTr,PredTn,DesvPredTn,PredTx,DesvPredTx\n");

    /* Variables para el informe de avance. */
    long start = new Date().getTime();
    int estI = 1;
    System.out.println("Asignando confianza");

    /* Iteracion en las estaciones. */
    for (Long id : ids) {
      session = DBSessionFactory.getInstance().getCurrentSession();
      session.beginTransaction();
      estacionDAO = DAOFactory.getEstacionDAO(session);

      Estacion e = estacionDAO.findByID(id);

      /* Reporte. */
      reportarEstacion(session, e, fw);

      session.getTransaction().commit();

      /* Informe avance. */
      if (estI % 3 == 0) {
        String elapsed = TimeDisplayUtil.displayLapse(new Date().getTime() - start);
        System.out.println("Estacion " + estI + "/" + ids.size() + " procesada. Tiempo parcial: " + elapsed);
      }

      estI++;
    }

    fw.close();
    System.out.println("\n\n");
    
    DBSessionFactory.getInstance().close();
  }

  public static void reportarEstacion(Session sess, Estacion e, FileWriter fw) throws IOException {
    CatalogoCorrelacion catCorrTn = new CatalogoCorrelacion(ProyectorRegistro.PROY_TMIN.nombreVariable());
    CatalogoCorrelacion catCorrTx = new CatalogoCorrelacion(ProyectorRegistro.PROY_TMAX.nombreVariable());

    List<CorrelacionEstaciones> csTn = catCorrTn.getCorrelaciones(e, sess, 0.85, 30, NUM_VECINOS);
    List<CorrelacionEstaciones> csTx = catCorrTx.getCorrelaciones(e, sess, 0.85, 30, NUM_VECINOS);
    TestVecindad testVcndTn = new TestVecindad(sess, e, csTn, ProyectorRegistro.PROY_TMIN, false);
    TestVecindad testVcndTx = new TestVecindad(sess, e, csTx, ProyectorRegistro.PROY_TMAX, false);

    for (RegistroDiario rd : e.getRegistros()) {
      ResultadoTestQC resOutlTn = rd.getResultadoByID(OutlierTest.TEST_PREFIX + "_Tn");
      ResultadoTestQC resOutlTx = rd.getResultadoByID(OutlierTest.TEST_PREFIX + "_Tx");
      double outlTn = resOutlTn != null ? Math.abs(resOutlTn.getValor()) : 0;
      double outlTx = resOutlTx != null ? Math.abs(resOutlTx.getValor()) : 0;

      ConfianzaVariable confianzaTn = getConfianzaRelevante(ProyectorRegistro.PROY_TMIN, rd);
      ConfianzaVariable confianzaTx = getConfianzaRelevante(ProyectorRegistro.PROY_TMAX, rd);
      ConfianzaVariable confianzaRange = rd.getConfianzaTempRange();

      boolean tnAReportar = (confianzaTn != null && (confianzaTn.getConfianza() > 0)) || outlTn > 6.5;
      boolean txAReportar = (confianzaTx != null && (confianzaTx.getConfianza() > 0)) || outlTx > 6.5;
      boolean trAReportar = confianzaRange != null && (confianzaRange.getConfianza() > 0);

      if (tnAReportar || txAReportar || trAReportar) {
        String rep = reportarRegistro(rd, testVcndTn, testVcndTx);
        fw.append(rep + "\n");
      }
    }
  }

  public static String reportarRegistro(RegistroDiario rd, TestVecindad testVcndTn, TestVecindad testVcndTx) {
    StringBuilder rv = new StringBuilder();

    Estacion est = rd.getEstacion();

    /* Datos de la estacion. */
    rv.append(est.getNombre() + "/" + est.getId() + ",");
    rv.append((est.getLatitud() / -100d) + " x " + (est.getLongitud() / -100d) + " x " + est.getAltura() + ",");

    /* Datos del registro. */
    rv.append(dfrmt.format(rd.getFecha()) + "," + RegistroHelper.getDescrCorta(rd, ProyectorRegistro.PROY_TMIN) + ","
        + RegistroHelper.getDescrCorta(rd, ProyectorRegistro.PROY_TMAX) + ",");

    /* Datos de los tests de DIP y Outl. */
    ResultadoTestQC outlTN = rd.getResultadoByID(OutlierTest.TEST_PREFIX + "_" + ProyectorRegistro.PROY_TMIN.nombreVariable());
    ResultadoTestQC dipTN = rd.getResultadoByID(DIPTest.TEST_PREFIX + "_" + ProyectorRegistro.PROY_TMIN.nombreVariable());

    ResultadoTestQC outlTX = rd.getResultadoByID(OutlierTest.TEST_PREFIX + "_" + ProyectorRegistro.PROY_TMAX.nombreVariable());
    ResultadoTestQC dipTX = rd.getResultadoByID(DIPTest.TEST_PREFIX + "_" + ProyectorRegistro.PROY_TMAX.nombreVariable());

    ResultadoTestQC routlTr = rd.getResultadoByID("OUTL_Tr");

    String outlTNS = (outlTN == null) ? "" : frmt.format(outlTN.getValor());
    String dipTNS = (dipTN == null) ? "" : frmt.format(dipTN.getValor());

    String outlTXS = (outlTX == null) ? "" : frmt.format(outlTX.getValor());
    String dipTXS = (dipTX == null) ? "" : frmt.format(dipTX.getValor());

    String outlTr = (routlTr == null) ? "" : frmt.format(routlTr.getValor());

    rv.append(outlTNS + "," + dipTNS + "," + outlTXS + "," + dipTXS + "," + outlTr + ",");

    /* Datos de confianza. */
    ConfianzaVariable confianzaTn = getConfianzaRelevante(ProyectorRegistro.PROY_TMIN, rd);
    ConfianzaVariable confianzaTx = getConfianzaRelevante(ProyectorRegistro.PROY_TMAX, rd);
    ConfianzaVariable confianzaRange = rd.getConfianzaTempRange();

    String confRangeStr = confianzaRange == null ? "" : confianzaRange.getCodigo();
    String confTn = confianzaTn != null ? confianzaTn.getConfianza() + "," + confianzaTn.getCodigo() + "," : ",,";
    String confTx = confianzaTx != null ? confianzaTx.getConfianza() + "," + confianzaTx.getCodigo() + "," : ",,";
    rv.append(confTn);
    rv.append(confTx + confRangeStr + ",");

    ResultadoVecindad resVcndTn = (testVcndTn == null) ? null : testVcndTn.cotejarVecindad(rd);
    ResultadoVecindad resVcndTx = (testVcndTx == null) ? null : testVcndTx.cotejarVecindad(rd);

    /* Datos del test de Vecindad. */
    if (resVcndTn != null) {
      rv.append(frmt.format(resVcndTn.getPrediccion()) + "," + frmt.format(resVcndTn.getDesviacionEstimacion()) + ",");
    } else {
      rv.append(",,");
    }
    if (resVcndTx != null) {
      rv.append(frmt.format(resVcndTx.getPrediccion()) + "," + frmt.format(resVcndTx.getDesviacionEstimacion()));
    }

    return rv.toString();
  }

  private static ConfianzaVariable getConfianzaRelevante(ProyectorRegistro proy, RegistroDiario rd) {
    return (proy == ProyectorRegistro.PROY_TMIN) ? rd.getConfianzaTempMin() : rd.getConfianzaTempMax();
  }
}