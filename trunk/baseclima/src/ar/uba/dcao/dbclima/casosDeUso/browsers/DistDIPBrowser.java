package ar.uba.dcao.dbclima.casosDeUso.browsers;

import java.util.Arrays;

import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class DistDIPBrowser {

  /* 7.000.000 de muestras requieren 100MB asignados a la JVM */
  private static final int SAMPLE_SIZE = 30 * 1000 * 1000;

  private static MRG32k3a randGenerator = new MRG32k3a();
  static {
    randGenerator.setSeed(randomSeed());
  }

  private EmpiricalDist dipDist;

  public static void main(String[] args) {
    float[] autoCorr = { 0.1f, 0.18f, 0.75f, 0.75f };
    float[] varErr = { 1f, 0.95f, 0.75f, 0.65f };

    for (int j = 0; j < autoCorr.length; j++) {
      DistDIPBrowser browser = new DistDIPBrowser(autoCorr[j], varErr[j]);

      System.out.println("\nAutoCorr=" + autoCorr[j] + " epsilon=" + varErr[j]);
      for (double i = 1; i <= 6; i += 0.5) {
        double prob = Math.abs(browser.f(i) - 0.5) * 2;
        double oneIn = 1 / (1 - prob);
        System.out.println("DIP=" + i + " en perc. " + 100 * prob + " => 1 en " + oneIn);
      }
    }
  }

  public DistDIPBrowser(float a, float stdv) {
    double[] dipSample = getDIPSample(SAMPLE_SIZE, a, stdv);
    this.dipDist = new EmpiricalDist(dipSample);
  }

  public double f(double dip) {
    return this.dipDist.cdf(dip);
  }

  private double[] getDIPSample(int cant, float a, float stdv) {
    float[] steps = new float[cant];

    float prev = generateNormal(stdv);

    for (int i = 0; i < cant; i++) {
      /* Se generan samples, se calculan los saltos con el sample anterior y se guarda. */
      float now = generateNormalForSeries(prev, a, stdv);
      steps[i] = now - prev;
      prev = now;
    }

    float[] stepsOrd = new float[steps.length];
    System.arraycopy(steps, 0, stepsOrd, 0, steps.length);
    Arrays.sort(stepsOrd);

    double p25 = stepsOrd[(int) Math.round(stepsOrd.length * 0.25)];
    double p50 = stepsOrd[(int) Math.round(stepsOrd.length * 0.5)];
    double p75 = stepsOrd[(int) Math.round(stepsOrd.length * 0.75)];

    int dipsNumber = 1;
    for (int i = 1; i < steps.length; i++) {
      if ((steps[i - 1]) * (-steps[i]) >= 0) {
        dipsNumber++;
      }
    }

    double[] dips = new double[dipsNumber];
    int dipN = 0;
    for (int i = 1; i < steps.length; i++) {
      if ((steps[i - 1]) * (-steps[i]) >= 0) {
        double step1 = stepTest(steps[i - 1], p25, p50, p75);
        double step2 = stepTest(steps[i], p25, p50, p75);
        dips[dipN++] = Math.sqrt(step1 * step2 * -1) * Math.signum(step1);
      }
    }

    Arrays.sort(dips);
    return dips;
  }

  private double stepTest(double step, double p25, double p50, double p75) {
    if (step < p25) {
      return (p25 - step) / (p50 - p25);
    } else if (step > p75) {
      return (p75 - step) / (p75 - p50);
    } else {
      return 0;
    }
  }

  private float generateNormalForSeries(float prev, float a, float stdv) {
    return prev * a + generateNormal(stdv);
  }

  /**
   * Generates an array with six long values. Hopefully, not 2 of them zero (or might
   * fail).
   */
  private static long[] randomSeed() {
    long[] rv = new long[6];
    for (int i = 0; i < 6; i++) {
      rv[i] = Math.round(Math.random() * Integer.MAX_VALUE);
    }

    return rv;
  }

  private float generateNormal(float stdv) {
    return (float) NormalGen.nextDouble(randGenerator, 0, stdv);
  }
}
