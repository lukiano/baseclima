package ar.uba.dcao.dbclima.data.predicado;

import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Devuelve verdadero si el campo Precipitacion de un registro tiene un valor y ese valor es mayor al minimo
 * requerido (o mayor a 0 si no se especifico ninguno).
 *
 */
public class PrecipitacionPredicado implements PredicadoRegistro {
	
	private int valorMinimo;
	
	public PrecipitacionPredicado() {
		this(0);
	}
	
	public PrecipitacionPredicado(int valorMinimo) {
		this.valorMinimo = valorMinimo;
	}

	public boolean evaluar(RegistroDiario registroDiario) {
		return registroDiario.getPrecipitacion() != null && registroDiario.getPrecipitacion().shortValue() > this.valorMinimo;
	}

}
