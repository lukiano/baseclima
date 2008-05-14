package ar.uba.dcao.dbclima.qc.qc1;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.RegistroDiario;

public abstract class OutlierTest extends DistributionBasedTest {

  public static final String TEST_PREFIX = "OUTL";

  public OutlierTest(boolean checkMin, boolean checkMax, Double maxDistValidaPos, Double maxDistValidaNeg) {
    super(checkMin, checkMax, maxDistValidaPos, maxDistValidaNeg);
  }

  
  @Override
  protected List<ValorRegistro> valoresRegistros(List<RegistroDiario> registros) {
    List<ValorRegistro> rv = new ArrayList<ValorRegistro>();

    for (RegistroDiario r : registros) {
      Integer vi = getProyector().getValor(r);

      if (vi != null) {
        rv.add(new ValorRegistro(r, vi.doubleValue()));
      }
    }

    return rv;
  }
}
