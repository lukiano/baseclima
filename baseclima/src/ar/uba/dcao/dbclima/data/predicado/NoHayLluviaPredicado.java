package ar.uba.dcao.dbclima.data.predicado;

import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Devuelve verdadero si el campo HayLluvia del Registro tiene un valor y ese valor indica verdadero.
 * Si el campo no tiene valor, el resultado dependera del parametro de entrada de esta clase.
 * @see RegistroDiario
 *
 */
public class NoHayLluviaPredicado implements PredicadoRegistro {
	
	private boolean tomarTambienNull;
	
	public NoHayLluviaPredicado(boolean tomarTambienNull) {
		this.tomarTambienNull = tomarTambienNull;
	}

	public boolean evaluar(RegistroDiario registroDiario) {
		if (registroDiario.getHayLluvia() == null) {
			return this.tomarTambienNull;
		}
		return !registroDiario.getHayLluvia().booleanValue();
	}

}
