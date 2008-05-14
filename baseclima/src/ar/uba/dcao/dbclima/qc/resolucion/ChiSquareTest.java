package ar.uba.dcao.dbclima.qc.resolucion;

import umontreal.iro.lecuyer.probdist.GammaDist;

public class ChiSquareTest {

  public static boolean sameDistribution(int[] muestra1, int[] muestra2, int sum1, int sum2, double confidence) {
    return sameDistribution(muestra1, muestra2, sum1, sum2) < confidence;
  }

  public static double sameDistribution(int[] muestra1, int[] muestra2, int sum1, int sum2) {
    if (muestra1.length != muestra2.length) {
      throw new IllegalArgumentException();
    }

    double sqr12 = Math.sqrt(sum1/(double) sum2);
    double sqr21 = Math.sqrt(sum2/(double) sum1);

    double chi = 0;
    int freedomDeg = muestra1.length;

    for (int i = 0; i < muestra1.length; i++) {

      if (muestra1[i] == 0 && muestra2[i] == 0) {
        freedomDeg--;

      } else {
        double diff = sqr21 * muestra1[i] - sqr12 * muestra2[i];
        double sum = muestra2[i] + muestra1[i];
        chi += Math.pow(diff, 2) / sum;
      }
    }

    GammaDist gamma = new GammaDist(freedomDeg/2d);
    return gamma.cdf(chi * 0.5);
  }
}
