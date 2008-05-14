package ar.uba.dcao.dbclima.precipitacion.rango;

import java.util.List;

import ar.uba.dcao.dbclima.data.Estacion;

/**
 * Las clases que implementan esta interfaz realizan consultas sobre los registros de una estacion determinada,
 * y agrupan las respuestas en distintos rangos de registros, asociados a un valor.
 * Ejemplo: La cantidad de precipitacion caida del 1ro de Enero al 31 de Enero para todos los anios disponibles en la estacion. 
 *
 */
public interface ProyectorRango {
  
  List<Rango> proyectarRangos(Estacion estacion);

  String nombre();
  
}
