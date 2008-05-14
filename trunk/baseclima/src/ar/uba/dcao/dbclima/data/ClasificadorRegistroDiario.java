package ar.uba.dcao.dbclima.data;

import java.io.Serializable;

/**
 * Las instancias de esta interfaz definen un criterio de clasificacion de registros
 * diarios.
 */
public interface ClasificadorRegistroDiario {

  public Serializable clasificar(RegistroDiario rd);

}
