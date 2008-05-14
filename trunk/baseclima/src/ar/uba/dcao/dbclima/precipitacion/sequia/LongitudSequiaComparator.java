/**
 * 
 */
package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.Comparator;

import ar.uba.dcao.dbclima.data.Sequia;


/**
 * Compara y ordena sequias segun la duracion de las mismas.
 *
 */
public final class LongitudSequiaComparator implements Comparator<Sequia> {

  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Sequia s1, Sequia s2) {
    Integer longitud1 = s1.getLongitud();
    Integer longitud2 = s2.getLongitud();
    return longitud1.compareTo(longitud2);
  }

}