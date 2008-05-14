package ar.uba.dcao.dbclima.precipitacion.rango;

import java.util.Comparator;

public final class ComienzoRangoComparator implements Comparator<Rango> {

  public int compare(Rango r1, Rango r2) {
    return r1.comienzo().compareTo(r2.comienzo());
  }

}
