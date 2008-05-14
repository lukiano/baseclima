package ar.uba.dcao.dbclima.gui.report;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.RegistroHelper;
import ar.uba.dcao.dbclima.data.ResultadoTestQC;
import ar.uba.dcao.dbclima.qc.DatasetPorEstacionTask;
import ar.uba.dcao.dbclima.qc.qc1.DIPTest;
import ar.uba.dcao.dbclima.qc.qc1.OutlierTest;

public class ReporteConfTempTask extends DatasetPorEstacionTask {

  private static DateFormat dfrmt = new SimpleDateFormat("MM/dd/yyyy");

  private static DecimalFormat frmt = new DecimalFormat("0.00");

  private final Long dsID;

  private final String filename;

  public ReporteConfTempTask(String filename, Long dsID) {
    this.filename = filename;
    this.dsID = dsID;

    try {
      FileWriter fw = null;
      fw = new FileWriter(filename);
      fw.append("Estacion,Posicion,Fecha,SecuMin,SecuMax,Outl Tn,Outl Tx,Outl Rng, DIP Tn, DIP Tx,");
      fw.append("Confianza Tn,CodigoConf Tn,Confianza Tx,CodigoConf Tx,ConfianzaRange\n");
      fw.close();

    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void processEstacion(Estacion estacion, Session session) {
    String descEst = estacion.getUbicacion() + " - " + estacion.getNombre();
    descEst += "," + estacion.getAltura() + "mts | " + estacion.getLatitud() + " | " + estacion.getLongitud();

    for (RegistroDiario rd : estacion.getRegistros()) {
      if (isProblematico(rd)) {
        String repReg = descEst + "," + reportarRegistro(rd);

        try {
          FileWriter fw = null;
          fw = new FileWriter(filename, true);
          fw.append(repReg + "\n");
          fw.close();

        } catch (IOException e1) {
          throw new IllegalStateException(e1);
        }
      }
    }
  }

  private String reportarRegistro(RegistroDiario rd) {
    /* Datos de los tests de DIP y Outl. */
    ResultadoTestQC routlTn = rd.getResultadoByID(OutlierTest.TEST_PREFIX + "_Tn");
    ResultadoTestQC routlTr = rd.getResultadoByID(OutlierTest.TEST_PREFIX + "_Tr");
    ResultadoTestQC routlTx = rd.getResultadoByID(OutlierTest.TEST_PREFIX + "_Tx");

    ResultadoTestQC rdipTn = rd.getResultadoByID(DIPTest.TEST_PREFIX + "_Tn");
    ResultadoTestQC rdipTx = rd.getResultadoByID(DIPTest.TEST_PREFIX + "_Tx");

    String outlTn = (routlTn == null) ? "" : frmt.format(routlTn.getValor());
    String outlTr = (routlTr == null) ? "" : frmt.format(routlTr.getValor());
    String outlTx = (routlTx == null) ? "" : frmt.format(routlTx.getValor());

    String dipTn = (rdipTn == null) ? "" : frmt.format(rdipTn.getValor());
    String dipTx = (rdipTx == null) ? "" : frmt.format(rdipTx.getValor());

    ConfianzaVariable cTn = rd.getConfianzaTempMin();
    ConfianzaVariable cTx = rd.getConfianzaTempMax();
    ConfianzaVariable cTr = rd.getConfianzaTempRange();

    byte nivelConfTn = cTn == null ? 0 : cTn.getConfianza();
    byte nivelConfTx = cTx == null ? 0 : cTx.getConfianza();
    byte nivelConfTr = cTr == null ? 0 : cTr.getConfianza();

    String codConfTn = cTn == null ? "" : cTn.getCodigo();
    String codConfTx = cTx == null ? "" : cTx.getCodigo();

    String rv = dfrmt.format(rd.getFecha()) + "," + RegistroHelper.getSecuFrmt(rd, ProyectorRegistro.PROY_TMIN)
        + "," + RegistroHelper.getSecuFrmt(rd, ProyectorRegistro.PROY_TMAX) + "," + outlTn + "," + outlTx + ","
        + outlTr + "," + dipTn + "," + dipTx + "," + nivelConfTn + "," + codConfTn + "," + nivelConfTx + ","
        + codConfTx + "," + nivelConfTr;

    return rv;
  }

  private static boolean isProblematico(RegistroDiario rd) {
    return (rd.getConfianzaTempMax() != null && rd.getConfianzaTempMax().getConfianza() > 0)
        || (rd.getConfianzaTempMin() != null && rd.getConfianzaTempMin().getConfianza() > 0);
  }

  @Override
  public boolean estacionIsToBeProcessed(Estacion estacion) {
    return true;
  }

  @Override
  public Long getDatasetId() {
    return this.dsID;
  }

  @Override
  public void updateGUIWhenCompleteSuccessfully() {
  }

  public String getProgressDescription() {
    return "Procesando estacion";
  }
}
