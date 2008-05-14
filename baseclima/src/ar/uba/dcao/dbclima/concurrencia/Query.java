package ar.uba.dcao.dbclima.concurrencia;

/**
 * Un Query es un Request que recupera informacion de un repositorio.
 */
public interface Query extends Request {

  /**
   * @return resultado del query, solo si esta ya fue completada (null en caso
   *         contrario).
   */
  Object getResult();

}
