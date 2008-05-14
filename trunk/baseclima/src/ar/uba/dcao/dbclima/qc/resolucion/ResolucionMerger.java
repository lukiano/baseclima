package ar.uba.dcao.dbclima.qc.resolucion;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.RegistroHelper;
import ar.uba.dcao.dbclima.utils.Merger;

public class ResolucionMerger extends Merger<List<RegistroDiario>> {

  private final ProyectorRegistro proy;

  public ResolucionMerger(ProyectorRegistro proy) {
    this.proy = proy;
  }

  @Override
  public List<RegistroDiario> merge(List<RegistroDiario> e1, List<RegistroDiario> e2) {
    List<RegistroDiario> rv = new ArrayList<RegistroDiario>(e1);
    rv.addAll(e2);

    return rv;
  }

  @Override
  public boolean shouldMerge(List<RegistroDiario> e1, List<RegistroDiario> e2) {
    int vals1 = RegistroHelper.regsConValor(e1, proy);
    int vals2 = RegistroHelper.regsConValor(e2, proy);
    boolean anyNonSignificant = (vals1 < e1.size() / 3d || vals2 < e2.size() / 3d);

    int[] as1 = ResolucionHelper.acumuladosDecimales(e1, proy);
    int[] as2 = ResolucionHelper.acumuladosDecimales(e2, proy);
//    double[] ds1 = ResolucionHelper.frecuenciasDecimales(e1, proy);
//    double[] ds2 = ResolucionHelper.frecuenciasDecimales(e2, proy);

//    double ksTestResult = KSTest.ksTestOnResolution(ds1, ds2, vals1, vals2);
//    double chiTestResultD = ChiSquareTest.sameDistribution(as1, as2, vals1, vals2);

    boolean chiTestResult = ChiSquareTest.sameDistribution(as1, as2, vals1, vals2, 0.99);
    return anyNonSignificant || chiTestResult;
  }
}
