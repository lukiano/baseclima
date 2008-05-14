package ar.uba.dcao.dbclima.clasificacion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.utils.CollectionUtils;

public class CicloEstacional {

  private static final double SMOOTH_COEF_1 = 0.15;

  private static final double SMOOTH_COEF_2 = 0.15;

  private final Estacion estacion;

  private final ProyectorRegistro proyector;

  private double[] medias = new double[366];

  private double[] desvEst = new double[366];

  private double[] mediasSmooth = new double[366];

  private double[] desvSmooth = new double[366];

  public CicloEstacional(Estacion e, ProyectorRegistro proyector) {
    this.estacion = e;
    this.proyector = proyector;
    this.initDist();
  }

  public ProyectorRegistro getProyector() {
    return proyector;
  }

  public Estacion getEstacion() {
    return estacion;
  }

  public Double getValorNormalizado(RegistroDiario rd) {
    Double rv;
    Integer valor = this.proyector.getValor(rd);
    if (valor == null) {
      rv = null;
    } else {
      int diaAnio = this.getDiaAnio(rd);
      rv = (valor.doubleValue() - this.medias[diaAnio]) / this.desvEst[diaAnio];
    }

    return rv;
  }

  private void initDist() {
    List<List<Double>> valores = new ArrayList<List<Double>>();
    for (int i = 0; i <= 366; i++) {
      valores.add(new ArrayList<Double>());
    }

    for (RegistroDiario r : this.estacion.getRegistros()) {
      Integer valor = this.proyector.getValor(r);
      if (valor != null) {
        int diaAnio = getDiaAnio(r);
        valores.get(diaAnio).add(valor.doubleValue());
      }
    }

    for (int i = 0; i < 366; i++) {
      List<Double> valoresDia = valores.get(i);
      this.medias[i] = CollectionUtils.avg(valoresDia);
      this.desvEst[i] = CollectionUtils.stdv(valoresDia, medias[i]);
    }

    this.mediasSmooth = smooth(this.medias, SMOOTH_COEF_1, SMOOTH_COEF_2);
    this.desvSmooth = smooth(this.desvEst, SMOOTH_COEF_1, SMOOTH_COEF_2);
  }

  private int getDiaAnio(RegistroDiario rd) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(rd.getFecha());
    return cal.get(Calendar.DAY_OF_YEAR) - 1;
  }

  private double[] smooth(double[] series, double coef1, double coef2) {
    double[] rv = new double[series.length];

    for (int i = 0; i < series.length; i++) {

      double last = series[(i + series.length - 1) % series.length];
      double lastLast = series[(i + series.length - 2) % series.length];
      double next = series[(i + 1) % series.length];
      double nextNext = series[(i + 2) % series.length];

      double val = series[i];
      double mainCoef = 1 - coef1 * 2 - coef2 * 2;

      rv[i] = val * mainCoef + (last * coef1) + (next * coef1) + (lastLast * coef2 + nextNext * coef2);
    }

    return rv;
  }
}
