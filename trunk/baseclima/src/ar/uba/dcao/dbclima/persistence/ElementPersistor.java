package ar.uba.dcao.dbclima.persistence;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;


public class ElementPersistor<T>  {

  protected int maxUncommitedElems;

  protected List<T> uncommited;

  private final SessionFactory factory;

  public ElementPersistor(int maxUncommitedElems, SessionFactory factory) {
    this.maxUncommitedElems = maxUncommitedElems;
    this.factory = factory;
    this.uncommited = new ArrayList<T>(maxUncommitedElems);
  }

  public void queue(T elem) {
    this.uncommited.add(elem);
    if (this.uncommited.size() >= this.maxUncommitedElems) {
      this.commit();
    }
  }

  public void commit() {
    Session sess = this.factory.getCurrentSession();
    sess.beginTransaction();
    for (T e : this.uncommited) {
      sess.saveOrUpdate(e);
    }

    sess.getTransaction().commit();

    this.uncommited.clear();
  }
}