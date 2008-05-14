package ar.uba.dcao.dbclima;

import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Crea una conexion con la base de datos para que se levante automaticamente (en caso que se use Derby).
 *
 */
public class DBStart {
  
  public static void main(String[] args) {
     DBSessionFactory.getInstance().getCurrentSession();
     DBSessionFactory.getInstance().close();
  }
}
