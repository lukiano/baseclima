package ar.uba.dcao.dbclima.dao;

import org.hibernate.Session;

/**
 * Factory para obtener las distintas clases DAO disponibles.
 *
 */
public class DAOFactory {

  private static final EstacionDAO estacionDAO = new EstacionDAO();
  
  private static final DatasetDAO datasetDAO = new DatasetDAO();
  
  private static final SequiaDAO sequiaDAO = new SequiaDAO();
  
  private DAOFactory() {}

  public static EstacionDAO getEstacionDAO(Session session) {
    estacionDAO.setSession(session);
    return estacionDAO;
  }
  
  public static DatasetDAO getDatasetDAO(Session session) {
    datasetDAO.setSession(session);
    return datasetDAO;
  }

  public static SequiaDAO getSequiaDAO(Session session) {
    sequiaDAO.setSession(session);
    return sequiaDAO;
  }

}
