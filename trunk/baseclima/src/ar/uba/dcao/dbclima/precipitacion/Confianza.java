/**
 * 
 */
package ar.uba.dcao.dbclima.precipitacion;

/**
 * Enumeracion de los niveles de confianza asignados a distintas clasificaciones.
 * Generalmente, varias clasificaciones distintas van a resultar en una misma confianza.
 *
 */
public enum Confianza {
  
  OK, NEED_CHECK, DOUBTFUL, ERROR;
}