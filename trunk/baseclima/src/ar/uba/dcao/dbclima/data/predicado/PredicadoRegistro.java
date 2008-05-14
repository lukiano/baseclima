package ar.uba.dcao.dbclima.data.predicado;

import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Realiza una comprobacion sobre un registro y devuelve un valor de verdad. 
 * @author Luciano
 */
public interface PredicadoRegistro {
	
	boolean evaluar(RegistroDiario registroDiario);
	
	PredicadoRegistro TRUE = new PredicadoRegistro() {
		public boolean evaluar(RegistroDiario registroDiario) {
			return true;
		}
	};

}
