package ar.uba.dcao.dbclima.correlation;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Calcula correlacion entre dos series de variables (una por estacion).
 */
public class CalculadorCorrelacion {

  /** Minima longitud de una lista para buscar correlacion. */
  public static final int MIN_LEN_BASE_CORR = 250;

  public static List<CorrelacionEstaciones> getCorr(Estacion e1, Estacion e2, ProyectorRegistro proy) {

    List<CorrelacionEstaciones> corrs = new ArrayList<CorrelacionEstaciones>();
    for (int i = 0; i < 12; i++) {
      CorrelacionEstaciones corr = new CorrelacionEstaciones(e1, e2, i);
      corr.setVariable(proy.nombreVariable());
      corrs.add(corr);
    }

    List<List<ValuePair>> valsLists = buildSeries(e1, e2, corrs, proy);

    for (int i = 0; i < 12; i++) {
      List<ValuePair> vals = valsLists.get(i);
      CorrelacionEstaciones corr = corrs.get(i);
      calcularCorrelacion(corr, vals);
    }

    return corrs;
  }

  private static void calcularCorrelacion(CorrelacionEstaciones rv, List<ValuePair> vals) {
    double sumXY = 0;
    double sumX = 0;
    double sumY = 0;
    double sumX2 = 0;
    double sumY2 = 0;

    if (vals.size() > MIN_LEN_BASE_CORR) {

      for (ValuePair vp : vals) {
        double v1n = vp.v1.doubleValue();
        double v2n = vp.v2.doubleValue();

        sumXY += v1n * v2n;
        sumX += v1n;
        sumY += v2n;
        sumX2 += v1n * v1n;
        sumY2 += v2n * v2n;
      }

      /**
       * E: sumatoria<br>
       * E(y) = na + b E(x)<br>
       * E(xy) = aE(x) + b * E(pow(x,2))
       */

      /*
       * Calculo de correlacion
       */
      int n = vals.size();
      double rvUp = n * sumXY - sumX * sumY;
      double rvDownSqr = (n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY);

      double corr = rvUp / Math.sqrt(rvDownSqr);
      corr = Math.round(corr * 1000) / 1000d;
      rv.setCorrelacion(corr);

      /*
       * Calculo de relacion lineal x minimos cuadrados
       */
      double b = (sumXY - sumX * sumY / n) / (sumX2 - sumX * sumX / n);
      double a = (sumY - b * sumX) / n;

      double desvio = 0;

      for (ValuePair vp : vals) {
        double v2est = vp.v1.doubleValue() * b + a;
        desvio += Math.pow(vp.v2.doubleValue() - v2est, 2);
      }
      desvio = Math.sqrt(desvio / (n + 1));

      a = Math.round(a * 100) / 100d;
      b = Math.round(b * 100) / 100d;
      desvio = Math.round(desvio * 100) / 100d;

      rv.setPendiente(b);
      rv.setOrdenadaOrigen(a);
      rv.setDesviacionEstimacion(desvio);

    } else {
      rv.setCorrelacion(null);
    }
  }

  /**
   * Actualiza la lista de correlaciones por mes (con inicio, fin y
   * cantidad de registros usados) y devuelve una lista de pares de valor (el valor de cada estacion)
   * discriminadas por mes.
   * 
   * @param e1
   * @param e2
   * @param corrs
   * @param proy
   * @return
   */
  private static List<List<ValuePair>> buildSeries(Estacion e1, Estacion e2, List<CorrelacionEstaciones> corrs, ProyectorRegistro proy) {
    List<List<ValuePair>> rv = new ArrayList<List<ValuePair>>();
    for (int i = 0; i < 12; i++) {
      rv.add(new ArrayList<ValuePair>());
    }

    int i = 0;
    int j = 0;

    List<RegistroDiario> regs1 = e1.getRegistros();
    List<RegistroDiario> regs2 = e2.getRegistros();

    Number vi;
    Number vj;

    while (i < regs1.size() && j < regs2.size()) {
      RegistroDiario r1 = regs1.get(i);
      RegistroDiario r2 = regs2.get(j);

      if (r1.getFecha().equals(r2.getFecha())) {
        // Si ambos registros corresponden al mismo dia, se agregan a la
        // lista.
        vi = proy.getValor(r1);
        vj = proy.getValor(r2);

        if (vi != null && vj != null) {
          int mes = FechaHelper.dameMes0a11(r1.getFecha());
          rv.get(mes).add(new ValuePair(vi, vj));

          CorrelacionEstaciones corr = corrs.get(mes);
          if (corr.getComienzo() == null) {
            corr.setComienzo(r1.getFecha());
          }

          corr.setFin(r1.getFecha());
          corr.setNumRegsUsados(corr.getNumRegsUsados() + 1);
        }
      }

      if (!r1.getFecha().before(r2.getFecha())) {
        // Si el 1er registro no es anterior al 2do, se avanza la 2da
        // lista.
        j++;
      }

      if (!r2.getFecha().before(r1.getFecha())) {
        // Si el 2do registro no es anterior al 1ero, se avanza la 1era
        // lista.
        i++;
      }
    }

    return rv;
  }

  private static class ValuePair {
    Number v1;

    Number v2;

    public ValuePair(Number v1, Number v2) {
      this.v1 = v1;
      this.v2 = v2;
    }
  }
}
