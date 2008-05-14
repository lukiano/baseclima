package ar.uba.dcao.dbclima.dao;

import java.util.List;

import ar.uba.dcao.dbclima.data.Dataset;

/**
 * Clase DAO para realizar consultas sobre el objeto Dataset.
 * @see Dataset 
 *
 */
public class DatasetDAO extends AbstractDAO<Dataset> {

  public DatasetDAO() {
    super(Dataset.class);
  }

  @SuppressWarnings("unchecked")
  public List<Dataset> findAll() {
    return getSession().createQuery("FROM Dataset").list();
  }

  @SuppressWarnings("unchecked")
  public List<Long> findAllNamedIDs() {
    return getSession().createQuery("SELECT id FROM Dataset").list();
  }

  @SuppressWarnings("unchecked")
  public List<Dataset> findAllByUsername(String username) {
    return getSession().createQuery("FROM Dataset WHERE usuario = :_us").setParameter("_us", username).list();
  }

}
