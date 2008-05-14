package ar.uba.dcao.dbclima.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Merger<T> {

  public abstract T merge(T e1, T e2);

  public abstract boolean shouldMerge(T e1, T e2);

  public int merge(List<T> l) {
    List<Integer> quiebres = new ArrayList<Integer>();
    int sizeOrig = l.size();

    /*
     * Se compara cada par de segmentos de registros y mientras haya segmentos parecidos,
     * se unen.
     */
    for (int i = 1; i < l.size(); i++) {
      T li = l.get(i);
      T lant = l.get(i - 1);

      if (!shouldMerge(lant, li)) {
        quiebres.add(i);
        /* Hay que juntar i e i-1 */
      }
    }

    quiebres.add(0, 0);
    quiebres.add(l.size());
    Collections.reverse(quiebres);

    for (int i = 1; i < quiebres.size(); i++) {
      for (int j = quiebres.get(i); j < quiebres.get(i - 1) - 1; j++) {
        int mPos = quiebres.get(i);
        T m = this.merge(l.get(mPos), l.get(mPos + 1));
        l.set(mPos, m);
        l.remove(mPos + 1);
      }
    }

    return sizeOrig - l.size();
  }

  public int mergeOld(List<T> l) {
    List<Integer> chunksAJuntar = new ArrayList<Integer>();
    int merges = 0;

    /*
     * Se compara cada par de segmentos de registros y mientras haya segmentos parecidos,
     * se unen.
     */
    do {
      chunksAJuntar.clear();

      for (int i = 1; i < l.size(); i++) {
        T li = l.get(i);
        T lant = l.get(i - 1);

        if (shouldMerge(lant, li)) {
          chunksAJuntar.add(i);
          i += 1;
          /* Hay que juntar i e i-1 */
        }
      }

      Collections.reverse(chunksAJuntar);
      for (int i : chunksAJuntar) {
        T m = this.merge(l.get(i - 1), l.get(i));
        l.set(i - 1, m);
        l.remove(i);
      }
      merges += chunksAJuntar.size();

    } while (chunksAJuntar.size() > 0);

    return merges;
  }
}
