package ar.uba.dcao.dbclima.data.predicado;

import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Devuelve verdadero si el campo HayLluvia del Registro tiene un valor y ese valor indica verdadero.
 * @see RegistroDiario
 */
public class HayLluviaPredicado implements PredicadoRegistro {

	public boolean evaluar(RegistroDiario registroDiario) {
		return registroDiario.getHayLluvia() != null && registroDiario.getHayLluvia().booleanValue();
	}

}
