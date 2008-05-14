package ar.uba.dcao.dbclima.data.predicado;

import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Clase que encapsula a otro PredicadoRegistro y niega su resultado.
 * @see PredicadoRegistro
 *
 */
public final class NotPredicado implements PredicadoRegistro {
	
	private PredicadoRegistro predicadoDecorado;
	
	private NotPredicado(PredicadoRegistro predicadoDecorado) {
		this.predicadoDecorado = predicadoDecorado;
	}

	public boolean evaluar(RegistroDiario registroDiario) {
		return !this.predicadoDecorado.evaluar(registroDiario);
	}
	
	public static PredicadoRegistro decorar(PredicadoRegistro predicado) {
		return new NotPredicado(predicado);
	}

}
