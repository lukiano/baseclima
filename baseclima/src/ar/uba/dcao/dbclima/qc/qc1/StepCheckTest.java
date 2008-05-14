package ar.uba.dcao.dbclima.qc.qc1;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.RegistroDiario;

public abstract class StepCheckTest extends DistributionBasedTest {

  public static final String TEST_PREFIX = "STEP";

  public StepCheckTest(Double maxDistValida) {
    super(true, true, maxDistValida, maxDistValida);
  }

  @Override
  protected List<ValorRegistro> valoresRegistros(List<RegistroDiario> registros) {
    List<ValorRegistro> rv = new ArrayList<ValorRegistro>();

    for (int i = 0; i < registros.size(); i++) {
      RegistroDiario rd = registros.get(i);

      Integer valorD = getProyector().getValor(rd);
      Integer valorAnt = rd.getAyer() == null ? null : getProyector().getValor(rd.getAyer());

      if (valorD != null && valorAnt != null) {
        rv.add(new ValorRegistro(rd, (double) (valorD - valorAnt)));
      }
    }

    return rv;
  }
}