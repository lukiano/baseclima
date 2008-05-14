package ar.uba.dcao.dbclima.correlation;

import java.util.Arrays;

/**
 * Clase simple que calcula el test de Kolmogorov-Smirnov para dos muestras.
 * Copiado del kstest2 del MatLab.
 * @see ar.uba.dcao.dbclima.qc.resolucion.KSTest
 * 
 */
public final class KSTest {
  
  private KSTest() {}
  
  public static double ksTest(double[] distrib1, double[] distrib2) {
    
    
    double[] ordenados = new double[distrib1.length + distrib2.length];
    for (int i = 0; i < distrib1.length; i++) {
      ordenados[i] = distrib1[i];
    }
    for (int i = 0; i < distrib2.length; i++) {
      ordenados[distrib1.length + i] = distrib2[i];
    }
    Arrays.sort(ordenados);
    
    double[] binEdges = new double[ordenados.length + 2];
    binEdges[0] = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < ordenados.length; i++) {
      binEdges[1 + i] = ordenados[i];
    }
    binEdges[1 + ordenados.length] = Double.POSITIVE_INFINITY;
    
    int[] binCounts1 = histc(distrib1, binEdges);
    int[] binCounts2 = histc(distrib2, binEdges); 

    double[] sumCounts1 = new double[binCounts1.length];
    sumCounts1[0] = binCounts1[0];
    for (int i = 1; i < sumCounts1.length; i++) {
      sumCounts1[i] = sumCounts1[i - 1] + binCounts1[i];
    }
    for (int i = 0; i < sumCounts1.length; i++) {
      sumCounts1[i] /= sum(binCounts1);
    }

    double[] sumCounts2 = new double[binCounts1.length];
    sumCounts2[0] = binCounts2[0];
    for (int i = 1; i < sumCounts2.length; i++) {
      sumCounts2[i] = sumCounts2[i - 1] + binCounts2[i];
    }
    for (int i = 0; i < sumCounts2.length; i++) {
      sumCounts2[i] /= sum(binCounts2);
    }

    double[] sampleCDF1 = new double[sumCounts1.length - 1];
    for (int i = 0; i < sampleCDF1.length; i++) {
      sampleCDF1[i] = sumCounts1[i];
    }
    double[] sampleCDF2 = new double[sumCounts2.length - 1];
    for (int i = 0; i < sampleCDF2.length; i++) {
      sampleCDF2[i] = sumCounts2[i];
    }

    double[] deltaCDF = new double[sampleCDF1.length];
    for (int i = 0; i < deltaCDF.length; i++) {
      deltaCDF[i] = Math.abs(sampleCDF1[i] - sampleCDF2[i]);
    }

    double KSstatistic = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < deltaCDF.length; i++) {
      if (deltaCDF[i] > KSstatistic) {
        KSstatistic = deltaCDF[i];
      }
    }
    
    int n1 = distrib1.length;
    int n2 = distrib2.length;
    double n = (double)(n1 * n2) / (double)(n1 + n2);
    
    double lambda = Math.max( (Math.sqrt(n) + 0.12 + 0.11/Math.sqrt(n)) * KSstatistic, 0);
    
    int[] j = new int[100];
    for (int i = 0; i < j.length; i++) {
      j[i] = i + 1; 
    }
    double pValue = 0;
    for (int i = 0; i < j.length; i++) {
      pValue += menosUnoALa(j[i] - 1) * Math.exp(-2 * lambda * lambda * Math.pow(j[i], 2d)); 
    }
    pValue *= 2d;
    
    pValue  = Math.min(Math.max(pValue, 0d), 1d);
    
    return pValue; 
  }

  /**
   * @param array
   * @param binEdges (requires binEdges to be sorted)
   * @return
   */
  private static int[] histc(double[] array, double[] binEdges) {
    int[] ret = new int[binEdges.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = 0;
      for (int j = 0; j < array.length; j++) {
        if (i == ret.length -1) {
          // ultimo elemento
          if (array[j] >= binEdges[i]) {
            ret[i]++;
          }
        } else {
          // elemento intermedio
          if (array[j] >= binEdges[i] && array[j] < binEdges[i + 1]) {
            ret[i]++;
          }
        }
      }
    }
    return ret;
  }
  
  private static int sum(int[] array) {
    int ret = 0;
    for (int i = 0; i < array.length; i++) {
      ret += array[i];
    }
    return ret;
  }

  private static int menosUnoALa(int potencia) {
    if (potencia % 2 == 0) {
      return 1;
    } else {
      return 0;
    }
  }
  
  public static void main(String[] args) {
    double[] test1 = new double[] {1, 2, 4, 5, 6, 6, 8, 12, 13, 14 };
    double[] test2 = new double[] {3, 5, 6, 6, 6, 9, 10, 11, 11, 11 };
    System.out.println(ksTest(test1, test2));
    
    
  }

}
