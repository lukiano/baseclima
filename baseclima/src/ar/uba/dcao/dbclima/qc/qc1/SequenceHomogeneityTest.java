package ar.uba.dcao.dbclima.qc.qc1;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.qc.ListValidator;
import ar.uba.dcao.dbclima.qc.resolucion.ResolucionHelper;

public abstract class SequenceHomogeneityTest implements ListValidator {

  public static final String TEST_PREFIX = "HOMG";

  /** Registros vecinos usados para determinar la resolucion de una secuencia. */
  private static final int CANT_VECINOS_RES = 2;

  public SequenceHomogeneityTest() {
  }

  public void validate(List<RegistroDiario> regs) {
    int i = 1;
    while (i < regs.size()) {
      List<RegistroDiario> diasIguales = new ArrayList<RegistroDiario>();
      diasIguales.add(regs.get(i - 1));

      /* Se recorre la lista mientras haya dias consecutivos con el mismo valor. */
      while (i < regs.size() && regs.get(i) == regs.get(i - 1).getManiana() && igualValor(regs.get(i - 1), regs.get(i))) {
        diasIguales.add(regs.get(i));
        i++;
      }
      i++;

      /*
       * Al llegar aca la lista diasIguales es maximal, e "i" apunta al segundo dia afuera
       * de esta (podria ser igual a i-1).
       */

      if (diasIguales.size() >= 2) {
        RegistroDiario rd1 = diasIguales.get(0);
        RegistroDiario rdN = diasIguales.get(diasIguales.size() - 1);
        Integer res1 = ResolucionHelper.definirResolucionRegistro(rd1, getProyector(), CANT_VECINOS_RES);
        Integer resN = ResolucionHelper.definirResolucionRegistro(rdN, getProyector(), CANT_VECINOS_RES);
        int res = Math.min(res1, resN);
        double maxDiasAceptables = (res == 1) ? 1.2d : 2d;
        double resultadoTest = diasIguales.size() / maxDiasAceptables;

        if (resultadoTest > 1) {
          /*
           * La sec. de valores iguales se considera potencialmente erronea. Se registra.
           */
          for (RegistroDiario rc : diasIguales) {
            QC1.marcarRegistro(rc, this.getNombreTest(), resultadoTest);
          }
        }
      }
    }
  }

  private boolean igualValor(RegistroDiario rd1, RegistroDiario rd2) {
    Integer v1 = this.getProyector().getValor(rd1);
    Integer v2 = this.getProyector().getValor(rd2);

    return v1 != null && v2 != null && v1.equals(v2);
  }
}