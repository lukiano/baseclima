package ar.uba.dcao.dbclima.utils;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class MultiList<E> extends AbstractList<E> {

  private List<List<E>> elems;

  public MultiList(List<List<E>> elems) {
    this.elems = elems;
  }

  @Override
  public E get(int pos) {
    int iPos = pos;
    for (List<E> l : this.elems) {
      if (l.size() > iPos) {
        return l.get(iPos);
      } else {
        iPos -= l.size();
      }
    }

    throw new IndexOutOfBoundsException("Current size: X. Requested element at position " + pos);
  }

  @Override
  public int size() {
    int rv = 0;
    for (List<E> l : this.elems) {
      rv += l.size();
    }
    return rv;
  }

  @Override
  public Iterator<E> iterator() {
    return new MultiListIterator<E>(this);
  }
  
  private static class MultiListIterator<E> implements Iterator<E> {

    private int pos;
    private int currentList;
    private MultiList<E> list;

    public MultiListIterator(MultiList<E> list) {
      this.list = list;
      this.currentList = 0;
      this.pos = 0;
      this.goToNextLegalPos();
    }

    public boolean hasNext() {
      goToNextLegalPos();
      return currentList >= 0;
    }

    public E next() {
      goToNextLegalPos();
      E rv = this.list.elems.get(currentList).get(pos);
      this.pos++;
      return rv;
    }

    public void remove() {
      throw new UnsupportedOperationException("No se pueden borrar elementos");
    }

    private void goToNextLegalPos() {
      while (currentList < this.list.elems.size() && (this.list.elems.get(this.currentList) == null || this.list.elems.get(this.currentList).size() == this.pos)) {
        currentList++;
        pos = 0;
      }

      if (currentList >= this.list.elems.size()) {
        this.pos = -1;
        this.currentList = -1;
      }
    }
  }
}
