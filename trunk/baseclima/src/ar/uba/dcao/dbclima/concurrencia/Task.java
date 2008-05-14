package ar.uba.dcao.dbclima.concurrencia;


/**
 * Esta clase representa una tarea, definida por el usuario, y que debe ser realizada en
 * el thread de calculos para no interrumpir la GUI durante la ejecucion.
 */
public interface Task extends Request {

  /**
   * @return Descripcion de lo que esta haciendo la tarea EN EL MOMENTO. Puede cambiar
   *         durante la ejecucion.
   */
  String getProgressDescription();

  /**
   * @return el estado de completitud estimado de la tarea, en el rango [0;1].
   */
  double getCompletionState();

  TaskResult getResult();
}
