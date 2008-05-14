package ar.uba.dcao.dbclima.casosDeUso.browsers;

import umontreal.iro.lecuyer.probdist.NormalDist;

public class SigmaBrowser {
  private static NormalDist nd = new NormalDist(0, 1);

  public static void main(String[] args) {
    for (double i = 0; i <= 6; i+=0.5) {
      double prob = prob(i);
      int oneIn = (int) (1 / (1 - prob));
      System.out.println(i + " sigmas con prob " + prob + ". Uno cada " + oneIn);
    }
  }

  private static double prob(double sigma) {
    return 2 * (nd.cdf(sigma) - 0.5);
  }
}
