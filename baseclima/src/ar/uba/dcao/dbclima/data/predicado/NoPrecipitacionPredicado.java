package ar.uba.dcao.dbclima.data.predicado;

import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Devuelve verdadero si el campo Precipitacion de un registro tiene un valor y ese valor es 0.
 * Si el campo tiene valor, el resultado dependera de la opcion elegida.
 * @see RegistroDiario
 */
public class NoPrecipitacionPredicado implements PredicadoRegistro {
  
    /**
     * Enumeracion de los pasos a seguir si el registro tiene vacio
     * el campo Precipitacion.
     *
     */
    public static enum Opcion { 
      NULL_COMO_CERO,
      NULL_COMO_CERO_SI_NO_HAY_LLUVIA,
      NULL_COMO_INCIERTO
    };
	
	private PredicadoRegistro hayLluvia = new HayLluviaPredicado();
	
	private Opcion valorDeNull;
	
	public NoPrecipitacionPredicado(Opcion valorDeNull) {
		this.valorDeNull = valorDeNull;
	}

	public boolean evaluar(RegistroDiario registroDiario) {
		if (registroDiario.getPrecipitacion() == null) {
          switch (this.valorDeNull) {
          case NULL_COMO_CERO:
            return true;
          case NULL_COMO_CERO_SI_NO_HAY_LLUVIA:
            return !this.hayLluvia.evaluar(registroDiario); // si no hay lluvia se asume que no hay precipitacion
          case NULL_COMO_INCIERTO:
            return false;
          }
		}
		return registroDiario.getPrecipitacion().shortValue() == 0;
	}

}
