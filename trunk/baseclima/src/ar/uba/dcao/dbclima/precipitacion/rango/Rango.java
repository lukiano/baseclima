package ar.uba.dcao.dbclima.precipitacion.rango;

import java.util.Date;

/**
 * Contiene un periodo entre una fecha comienzo y una final, para la cual se guarda un valor y se le asocia un nombre.
 *
 */
public interface Rango {
  
  Date comienzo();
  
  Date fin();
  
  Double valor();
  
  String nombre();

}
