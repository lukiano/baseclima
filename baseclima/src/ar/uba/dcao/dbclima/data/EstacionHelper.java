package ar.uba.dcao.dbclima.data;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.utils.CollectionUtils;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Metodos utiles para tratar con estaciones.
 */
public class EstacionHelper {

  @SuppressWarnings("unchecked")
  public static List<RegistroDiario>[] getRegistrosPorMes(Estacion e) {
    List<RegistroDiario>[] rv = new ArrayList[12];
    for (int i = 0; i < 12; i++) {
      rv[i] = new ArrayList<RegistroDiario>();
    }

    for (RegistroDiario rd : e.getRegistros()) {
      rv[FechaHelper.dameMes0a11(rd.getFecha())].add(rd);
    }

    return rv;
  }

  public static double[] getAvgPorMes(Estacion e, ProyectorRegistro proy) {
    double[] rv = new double[12];

    List<Double>[] valoresPorMes = getValoresPorMes(e, proy);

    for (int i = 0; i < 12; i++) {
      double m = CollectionUtils.avg(valoresPorMes[i]);
      rv[i] = m;
    }

    return rv;
  }

  public static double[] getStdvPorMes(Estacion e, ProyectorRegistro proy) {
    List<Double>[] vals = getValoresPorMes(e, proy);
    double[] mediasPorMes = getAvgPorMes(e, proy);

    double[] rv = new double[12];
    for (int i = 0; i < 12; i++) {
      rv[i] = CollectionUtils.stdv(vals[i], mediasPorMes[i]);
    }

    return rv;
  }

  @SuppressWarnings("unchecked")
  private static List<Double>[] getValoresPorMes(Estacion e, ProyectorRegistro proy) {
    List<RegistroDiario>[] registrosPorMes = getRegistrosPorMes(e);

    List<Double>[] rv = new ArrayList[12];

    for (int i = 0; i < 12; i++) {
      rv[i] = new ArrayList<Double>();
      List<RegistroDiario> regsMes = registrosPorMes[i];

      for (RegistroDiario rd : regsMes) {
        Integer valor = proy.getValor(rd);
        if (valor != null) {
          rv[i].add(valor.doubleValue());
        }
      }
    }

    return rv;
  }
}