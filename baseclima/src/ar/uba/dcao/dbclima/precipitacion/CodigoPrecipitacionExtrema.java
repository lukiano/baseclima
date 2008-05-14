/**
 * 
 */
package ar.uba.dcao.dbclima.precipitacion;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeracion de los codigos disponibles para la clasificacion de una precipitacion diaria.
 *
 */
public enum CodigoPrecipitacionExtrema {
  
  POK, P75, PJUMP, P90;
  
  private static final Map<CodigoPrecipitacionExtrema, Confianza> CODIGO2CONFIANZA = new HashMap<CodigoPrecipitacionExtrema, Confianza>();
  static {
    CODIGO2CONFIANZA.put(POK, Confianza.OK);
    CODIGO2CONFIANZA.put(P75, Confianza.OK);
    CODIGO2CONFIANZA.put(PJUMP, Confianza.OK);
    CODIGO2CONFIANZA.put(P90, Confianza.NEED_CHECK);
  }
  
  public static Confianza dameConfianza(CodigoPrecipitacionExtrema codigo) {
    return CODIGO2CONFIANZA.get(codigo);
  }
  
}