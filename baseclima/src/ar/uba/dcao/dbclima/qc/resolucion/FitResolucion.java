package ar.uba.dcao.dbclima.qc.resolucion;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.qc.resolucion.ModeloResolucion.AjusteModelo;

public class FitResolucion {

  private static final double[] ENTERO = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

  private static final double[] MEDIO = { 0.5, 0, 0, 0, 0, 0.5, 0, 0, 0, 0 };

  private static final double[] QUINTO = { 0.2, 0, 0.2, 0, 0.2, 0, 0.2, 0, 0.2, 0 };

  private static final double[] DECIMO = { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 };

  /** Clase utilitario, no se instancia. */
  private FitResolucion() {
  }

  public static ModeloResolucion fit(double[] datos, int sizeMuestra) {
    List<AjusteModelo> results = new ArrayList<AjusteModelo>();
    results.addAll(fitRotaciones("NTRO", ENTERO, datos, 10));
    results.addAll(fitRotaciones("MDIO", MEDIO, datos, 5));
    results.addAll(fitRotaciones("QNTO", QUINTO, datos, 2));
    results.addAll(fitRotaciones("DCMO", DECIMO, datos, 1));

    return new ModeloResolucion(results, sizeMuestra);
  }

  private static List<AjusteModelo> fitRotaciones(String fitLabel, double[] modelo, double[] datos, int uniquePositions) {
    List<AjusteModelo> rv = new ArrayList<AjusteModelo>();

    for (int i = 0; i < uniquePositions; i++) {
      double[] r = rotar(datos, i);
      double fit = fit(modelo, r);
      String nombre = fitLabel + "-" + i;
      rv.add(new AjusteModelo(fit, nombre));
    }

    return rv;
  }

  private static double[] rotar(double[] datos, int posicionesRotacion) {
    double[] rv = new double[datos.length];
    posicionesRotacion = Math.min(posicionesRotacion, datos.length);

    if (posicionesRotacion == 0) {
      rv = datos;
    } else {
      for (int i = 0; i < datos.length; i++) {
        int iOrigen = (i + posicionesRotacion) % datos.length;
        rv[i] = datos[iOrigen];
      }
    }

    return rv;
  }

  /**
   * Indica cuanto se parecen las distribuciones parametro.
   */
  public static double fit(double[] distr1, double[] distr2) {
    double fit = 0;

    for (int i = 0; i < distr1.length; i++) {
      fit += Math.pow(distr1[i] - distr2[i], 2);
    }

    return Math.sqrt(fit);
  }

  /**
   * Indica cuanto se parecen las distribuciones parametro.
   */
  public static double fitMaxDiff(double[] distr1, double[] distr2) {
    double fit = 0;

    for (int i = 0; i < distr1.length; i++) {
      double diff = Math.abs(distr1[i] - distr2[i]);
      fit = Math.max(fit, diff);
    }

    return fit;
  }

}