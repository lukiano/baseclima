package ar.uba.dcao.dbclima.qc.qc1;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.RegistroDiario;

public abstract class DIPTest extends DistributionBasedTest {

  public DIPTest(Double maxDistValida) {
    super(true, true, maxDistValida, maxDistValida);
  }

  public static final String TEST_PREFIX = "DIPT";

  @Override
  protected List<ValorRegistro> valoresRegistros(List<RegistroDiario> registros) {
    List<ValorRegistro> rv = new ArrayList<ValorRegistro>();

    for (int i = 0; i < registros.size(); i++) {
      RegistroDiario rd = registros.get(i);
      Double val = this.stepValue(rd);

      if (val != null) {
        rv.add(new ValorRegistro(rd, val));
      }
    }

    return rv;
  }

  private Double stepValue(RegistroDiario rd) {
    Double rv;
    Integer valorD = null;
    Integer valorAyer = null;

    if (rd != null && rd.getAyer() != null) {
      valorD = getProyector().getValor(rd);
      valorAyer = getProyector().getValor(rd.getAyer());
    }

    if (valorD != null && valorAyer != null) {
      rv = (double) valorD - valorAyer;
    } else {
      rv = null;
    }

    return rv;
  }

  /**
   * Se calcula el dipValue del registro usando el stepValue del registro
   * hoy y el stepValue del registro maniana (si existe).<br>
   * 
   * dipVHoy = sqrt(stepVHoy * stepVManiana) sii estos step tienen signos
   * opuestos, y 0 si tienen mismo signo.
   */
  @Override
  protected double desviacionDato(ValorRegistro vr) {
    double rv = 0d;

    RegistroDiario maniana = vr.r.getManiana();

    double stepHoy = vr.v;
    Double stepManiana = (maniana == null) ? null : stepValue(maniana);

    if (stepManiana != null && Math.signum(stepHoy) * Math.signum(stepManiana) == -1d) {
      // Tiene sentido calcular la desviacion.
      ValorRegistro valorRegManiana = new ValorRegistro(maniana, stepManiana);

      double desvHoy = super.desviacionDato(vr);
      double desvManiana = super.desviacionDato(valorRegManiana);

      rv = Math.sqrt(desvHoy * desvManiana * -1d) * Math.signum(stepHoy);
    }

    return rv;
  }
}
