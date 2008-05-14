package ar.uba.dcao.dbclima.qc.resolucion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Kolmogorov-Smirnov test para comparar dos muestras extraidas de distribuciones
 * continuas.
 */
public class KSTest {

  private static final double DOUBLE_OP_ERROR = 0.00001;

  private static final double EPS1 = 0.001;

  private static final double EPS2 = 1.0e-8;

  private static final Integer[] ceroANueve = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

  public static boolean sameDistribution(final double[] dist1, final double[] dist2, int longMuestra1,
      int longMuestra2, double confidence) {
    return ksTestOnResolution(dist1, dist2, longMuestra1, longMuestra2) < confidence;
  }

  public static double ksTestOnResolution(final double[] dist1, final double[] dist2, int longMuestra1, int longMuestra2) {

    /*
     * Generador de permutacion. Este creara la permutacion segun la diferencia entre las
     * muestras. Ubicando primero los digitos decimales con mas diferencia entre muestras.
     */
    Comparator<Integer> permCreator = new Comparator<Integer>() {
      public int compare(Integer o1, Integer o2) {
        return (int) Math.signum((dist1[o1] - dist2[o1]) - (dist1[o2] - dist2[o2]));
      }
    };

    /* Permutador. */
    List<Integer> perm = new ArrayList<Integer>(Arrays.asList(ceroANueve));
    Collections.sort(perm, permCreator);

    double[] d1ord = new double[dist1.length];
    double[] d2ord = new double[dist1.length];

    /* Creo las permutaciones de las distribuciones en arreglos nuevos. */
    for (int i = 0; i < perm.size(); i++) {
      int destI = perm.get(i);
      d1ord[i] = dist1[destI];
      d2ord[i] = dist2[destI];
    }

    /* Genero muestras que representen a las distribuciones. */
    List<Integer> muestraD1 = generateSample(d1ord, longMuestra1);
    List<Integer> muestraD2 = generateSample(d2ord, longMuestra2);

    /* Obtengo el primer resultado. */
    return 1 - ksTest(muestraD1, muestraD2);
  }

  private static double ksTest(List<Integer> data1, List<Integer> data2) {
    /* Las listas ya deberian estar ordenadas. */
    //Collections.sort(data1);
    //Collections.sort(data2);

    int j1 = 0;
    int j2 = 0;
    double fn1 = 0;
    double fn2 = 0;
    double dtM = 0;

    while (j1 < data1.size() && j2 < data2.size()) {
      double d1 = data1.get(j1).doubleValue();
      double d2 = data2.get(j2).doubleValue();

      if (d1 <= d2) {
        fn1 = j1++ / (double) data1.size();
      }
      if (d2 <= d1) {
        fn2 = j2++ / (double) data2.size();
      }
      double dt = Math.abs(fn2 - fn1);
      if (dt > dtM) {
        dtM = dt;
      }
    }

    double en = Math.sqrt(data1.size() * data2.size() / (data1.size() + data2.size()));
    return probks((en + 0.12 + 0.11 / en) * dtM);
  }

  private static double probks(double d) {
    double fac = 2;
    double sum = 0;
    double termbf = 0;
    double a2 = -2.0 * d * d;

    for (int j = 1; j <= 100; j++) {
      double term = fac * Math.exp(a2 * j * j);
      sum += term;
      if (Math.abs(term) <= EPS1 * termbf || Math.abs(term) <= EPS2 * sum) {
        return sum;
      }
      fac = -fac;
      termbf = Math.abs(term);
    }

    return 1;
  }

  private static List<Integer> generateSample(double[] dist, int size) {
    List<Integer> rv = new ArrayList<Integer>();

    double acum = 0;
    int j = 0;
    for (int i = 0; i < size; i++) {
      while (i / (double) size >= acum + dist[j] - DOUBLE_OP_ERROR) {
        acum += dist[j++];
      }
      rv.add(j);
    }

    return rv;
  }
}
