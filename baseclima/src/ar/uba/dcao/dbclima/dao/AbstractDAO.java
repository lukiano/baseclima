package ar.uba.dcao.dbclima.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.PersistentObject;

/**
 * Clase abstracta base para clases DAO. Encapsula algunos metodos comunes.
 *
 * @param <T>
 */
public abstract class AbstractDAO<T extends PersistentObject> {

  private Session session;

  private Class<T> claseDAO;

  public AbstractDAO(Class<T> claseDAO) {
    this.claseDAO = claseDAO;
  }

  @SuppressWarnings("unchecked")
  public List<T> findAll() {
    return this.session.createQuery("FROM " + this.claseDAO.getName()).list();
  }

  @SuppressWarnings("unchecked")
  public List<Long> findAllIDs() {
    List<T> pobs = this.session.createQuery("FROM " + this.claseDAO.getName()).list();
    return getIDs(pobs);
  }

  public static List<Long> getIDs(List<? extends PersistentObject> pobs) {
    List<Long> rv = new ArrayList<Long>();
    for (PersistentObject po : pobs) {
      rv.add(po.getId());
    }

    return rv;
  }

  @SuppressWarnings("unchecked")
  public T findByID(Long id) {
    return (T) this.session.createQuery("FROM " + this.claseDAO.getName() + " WHERE id = " + id).uniqueResult();
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public Session getSession() {
    return session;
  }
}
