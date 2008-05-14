package ar.uba.dcao.dbclima.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionUtils {

  public static double stdv(Collection<Double> vals, double avg) {
    double rv = 0;
    for (double val : vals) {
      rv += Math.pow(Math.abs(val - avg), 2);
    }

    return Math.sqrt(rv / (vals.size() - 1));
  }

  public static double avg(Collection<Double> vals) {
    double suma = 0;
    int tam = 0;
    for (Double val : vals) {
      if (val != null) {
        suma += val;
        tam++;
      }
    }

    return suma / tam;
  }

  public static <T extends Number> T percentilOrderedList(List<T> vals, double percentil) {
    T rv;
    if (vals.size() == 0) {
      rv = null;
    } else {
      int pos = (int) Math.ceil(percentil * vals.size()) - 1;
      pos = Math.max(pos, 0);
      rv = vals.get(pos);
    }

    return rv;
  }

  public static Double percentil(Collection<Double> vals, double percentil) {
    List<Double> valsL = new ArrayList<Double>(vals);
    Collections.sort(valsL);

    return percentilOrderedList(valsL, percentil);
  }
}