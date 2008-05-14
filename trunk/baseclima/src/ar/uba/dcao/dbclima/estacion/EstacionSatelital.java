package ar.uba.dcao.dbclima.estacion;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.PuntoSatelital;

/**
 * Una extension de la clase estacion para dar soporte a los registros satelitales ubicados
 * en puntos satelitales.
 * @see PuntoSatelital
 *
 */
public class EstacionSatelital extends Estacion {

  public EstacionSatelital(Long id, Integer latitud, Integer longitud) {
    super("Satellite", null, null, null, latitud, longitud);
    this.setId(id);
  }

}
