package ar.uba.dcao.dbclima.data;

import java.text.DecimalFormat;
import java.util.Collection;

import ar.uba.dcao.dbclima.qc.qc1.DIPTest;
import ar.uba.dcao.dbclima.qc.qc1.OutlierTest;
import ar.uba.dcao.dbclima.qc.qc1.StepCheckTest;

public class RegistroHelper {

  public static final DecimalFormat formatter = new DecimalFormat(" 00.0;-00.0");

  private static final DecimalFormat DFRMT = new DecimalFormat("0.0");

  public static int regsConValor(Collection<RegistroDiario> regs, ProyectorRegistro p) {
    int rv = 0;

    for (RegistroDiario rd : regs) {
      if (p.getValor(rd) != null) {
        rv++;
      }
    }

    return rv;
  }

  public static String getDescr(RegistroDiario rd, ProyectorRegistro pr) {
    String postfijo = "_" + pr.nombreVariable();
    ResultadoTestQC resOutl = rd.getResultadoByID(OutlierTest.TEST_PREFIX + postfijo);
    ResultadoTestQC resStep = rd.getResultadoByID(StepCheckTest.TEST_PREFIX + postfijo);
    ResultadoTestQC resDipt = rd.getResultadoByID(DIPTest.TEST_PREFIX + postfijo);

    double outl = resOutl == null ? 0 : resOutl.getValor();
    double step = resStep == null ? 0 : resStep.getValor();
    double dipt = resDipt == null ? 0 : resDipt.getValor();

    String temps = RegistroHelper.getTemp(rd.getAyer(), pr) + " -> " + RegistroHelper.getTemp(rd, pr) + " -> "
        + RegistroHelper.getTemp(rd.getManiana(), pr);

    String testRes = RegistroHelper.formatter.format(outl) + " " + RegistroHelper.formatter.format(step) + " "
        + RegistroHelper.formatter.format(dipt);

    String ref = rd.getEstacion().getNombre() + " " + rd.getFecha();
    return testRes + " [" + temps + "] (" + ref + ")";
  }

  public static String getDescrCorta(RegistroDiario rd, ProyectorRegistro pr) {
    return RegistroHelper.getTemp(rd.getAyer(), pr) + " -> " + RegistroHelper.getTemp(rd, pr) + " -> "
        + RegistroHelper.getTemp(rd.getManiana(), pr);
  }

  public static String getSecuFrmt(RegistroDiario rd, ProyectorRegistro pr) {
    Short tAyer = RegistroHelper.getTemp(rd.getAyer(), pr);
    Short tHoy = RegistroHelper.getTemp(rd, pr);
    Short tMan = RegistroHelper.getTemp(rd.getManiana(), pr);

    return formatTemp(tAyer) + " -> " + formatTemp(tHoy) + " -> " + formatTemp(tMan);
  }

  private static String formatTemp(Short temp) {
    String rv = "(x)";
    if (temp != null) {
      double dTemp = temp.doubleValue();
      rv = DFRMT.format(dTemp / 100d);
    }

    return rv;
  }

  public static int getPrecipAcum(RegistroDiario rd) {
    return (short) (RegistroHelper.getPrecipitacion(rd) + RegistroHelper.getPrecipitacion(rd.getAyer()));
  }

  public static int getPrecipitacion(RegistroDiario rd) {
    return (rd == null || rd.getPrecipitacion() == null) ? 0 : rd.getPrecipitacion();
  }

  private static Short getTemp(RegistroDiario rd, ProyectorRegistro proy) {
    Short rv = null;
    if (rd != null && proy.getValor(rd) != null) {
      rv = proy.getValor(rd).shortValue();
    }

    return rv;
  }
}
