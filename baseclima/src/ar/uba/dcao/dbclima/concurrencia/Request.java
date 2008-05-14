package ar.uba.dcao.dbclima.concurrencia;

import org.hibernate.SessionFactory;

/**
 * Un request encapsula un comando que realiza acceso a la base de datos 
 * (a traves de Hibernate). Puede ser ejecutado de manera sincrona o asincrona.
 *
 */
public interface Request {

  /**
   * Metodo que ejecuta efectivamente la tarea. Debe ser invocado desde el thread de
   * calculos y mantener actualizado el resultado a devolver por el metodo
   * getCompletionState.
   * 
   * @param runningFactory
   * @return true sii la tarea finalizo con exito.
   */
  boolean run(SessionFactory runningFactory);
  
  /**
   * @return true sii la tarea ya fue completada.
   */
  boolean isComplete();

  /**
   * Each request knows how to update the GUI when it ends.
   */
  void updateGUIWhenCompleteSuccessfully();
}
