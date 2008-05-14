package ar.uba.dcao.dbclima.qc.qc1;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.EstacionHelper;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.ResultadoTestQC;
import ar.uba.dcao.dbclima.qc.ListValidator;
import ar.uba.dcao.dbclima.qc.ListValidatorFactory;

public class QC1 {

  public static final String[] testsQC1 = { "OUTL_Tx", "OUTL_Tn", "OUTL_Tr", "DIPT_Tx", "DIPT_Tn", "STEP_Tx", "STEP_Tn"/*, "DISP_Tx", "DISP_Tn"*/ };

  public static final String[] testsQC1Tn = { "OUTL_Tn", "DIPT_Tn", "STEP_Tn"};

  public static final String[] testsQC1Tx = { "OUTL_Tx", "DIPT_Tx", "STEP_Tx"};

  private Estacion estacion;

  public QC1(Estacion estacion) {
    this.estacion = estacion;
  }

  public void correrTests() {
    List<RegistroDiario> regs = this.estacion.getRegistros();

    List<RegistroDiario>[] regsXMes = EstacionHelper.getRegistrosPorMes(estacion);

    for (List<RegistroDiario> mes : regsXMes) {
      // Corro los tests que usan la distribucion mensual.
      for (ListValidator l : getMonthlyTests()) {
        l.validate(mes);
      }
    }

    for (ListValidator l : getSerialTests()) {
      // Corro los tests que usan los datos presentados secuencialmente.
      l.validate(regs);
    }
  }

  /**
   * Devuelve el conjunto de tests que se aplican en QC1 a la lista entera
   * 'plana' de registros de una estacion.
   */
  private Set<ListValidator> getSerialTests() {
    Set<ListValidator> rv = new HashSet<ListValidator>();
    ListValidator lv;

    lv = ListValidatorFactory.getMaxTempHomogeneityCheck();
    rv.add(lv);

    lv = ListValidatorFactory.getMinTempHomogeneityCheck();
    rv.add(lv);

    return rv;
  }

  /**
   * Devuelve el conjunto de tests que se aplican en QC1 a las listas
   * mensuales (por separado) de registros de una estacion.
   */
  private static Set<ListValidator> getMonthlyTests() {
    Set<ListValidator> rv = new HashSet<ListValidator>();
    ListValidator lv;

    lv = ListValidatorFactory.getMaxTempOutlierValidator();
    rv.add(lv);

    lv = ListValidatorFactory.getMinTempOutlierValidator();
    rv.add(lv);

    lv = ListValidatorFactory.getTempRangeOutlierValidator();
    rv.add(lv);

    lv = ListValidatorFactory.getMinTempDIPTest();
    rv.add(lv);

    lv = ListValidatorFactory.getMaxTempDIPTest();
    rv.add(lv);

    return rv;
  }

  /**
   * Asocia al registro r el resultado del test de nombre nombreTest con
   * valor scoreTest.
   */
  public static void marcarRegistro(RegistroDiario r, String nombreTest, double scoreTest) {
    ResultadoTestQC rt = new ResultadoTestQC();
    rt.setValor(scoreTest);
    rt.setTestID(nombreTest);
    r.registrarResultadoTestQC(rt);
  }
}