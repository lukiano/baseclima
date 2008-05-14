package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.predicado.NoPrecipitacionPredicado;
import ar.uba.dcao.dbclima.data.predicado.PredicadoRegistro;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Esta clase realiza el calculo de las sequias de manera similar a la clase que hereda,
 * pero estas sequias pueden contener registros faltantes.
 *
 */
public class ActualizarInfoSecuenciasSequiasONO extends ActualizarInfoSecuenciasSequias {

  public static interface DroughtLog {

    void log(Long estacionId, int contadorCerosAnteriores, int contadorNullsIntermedios, int contadorCerosPosteriores);

  }

  public static final DroughtLog getDummyDroughtLogInstance() {
    return new DroughtLog() {
      public void log(Long estacionId, int contadorCerosAnteriores, int contadorNullsIntermedios,
          int contadorCerosPosteriores) {
      }
    };
  }

  private DroughtLog droughtLog;

  public ActualizarInfoSecuenciasSequiasONO(boolean readOnly, DroughtLog droughtLog) {
    super(readOnly);
    this.droughtLog = droughtLog;
  }

  @Override
  protected void processStation(Session sess, Estacion estacion) {
    DAOFactory.getSequiaDAO(sess).deleteAllByStation(estacion);
    
    RegistroDiario primerRegistro = null;
    RegistroDiario ultimoRegistro = null;

    RegistroDiario primerCeroPosteriorRegistro = null;

    int contadorSecuencia = 0;

    int contadorCerosAnteriores = 0;
    int contadorCerosPosteriores = 0;
    int contadorNullsIntermedios = 0;

    PredicadoRegistro predicado = new NoPrecipitacionPredicado(NoPrecipitacionPredicado.Opcion.NULL_COMO_CERO);

    List<RegistroDiario> regs = estacion.getRegistros();
    for (RegistroDiario registroDiario : regs) {
      if (predicado.evaluar(registroDiario)) {
        // Continuo con la secuencia o empiezo una nueva.

        if (ultimoRegistro == null) { // si es null, el ultimo registro fue uno con
        // precipitacion
        // Empiezo una nueva secuencia.
        // Si hubo registros faltantes justo antes, los ignoro.
          primerRegistro = registroDiario;
          ultimoRegistro = primerRegistro;
          contadorSecuencia = 1;
          contadorCerosAnteriores = 1; // primer tanda de ceros antes de una secuencia
          // de registros faltantes.
          contadorCerosPosteriores = 0; // todavia no hay nulls intermedios involucrados
        } else {

          // sigo con la secuencia que se viene acumulando
          int diferencia = FechaHelper.dameDifereciaDeDias(ultimoRegistro.getFecha(), registroDiario.getFecha());
          if (diferencia == 0) {
            System.err.println("Error");
            FechaHelper.dameDifereciaDeDias(ultimoRegistro.getFecha(), registroDiario.getFecha());

          }
          if (diferencia == 1) {
            // no hubo nulls en el medio
            if (contadorCerosPosteriores == 0) {
              // estamos contando los ceros anteriores
              contadorCerosAnteriores++;
              contadorSecuencia++;
            } else {
              // estamos contando los ceros posteriores
              contadorCerosPosteriores++;
              // no sumo a la secuencia porque si los registros faltantes intermedios no
              // se incluyen,
              // pertenecen a una nueva secuencia.
            }

          } else {
            // SI hubo nulls en el medio
            if (contadorCerosPosteriores == 0) {
              // estamos contando los ceros anteriores => pasamos a contar los ceros
              // posteriores
              contadorCerosPosteriores++;
              primerCeroPosteriorRegistro = registroDiario;

              contadorNullsIntermedios = diferencia - 1; // el ultimo dia no es un
                                                          // registro faltante.
            } else {
              // estamos contando los ceros posteriores => me fijo si se incluye el gap
              // anterior de
              // registros faltantes.

              this.droughtLog.log(estacion.getId(), contadorCerosAnteriores, contadorNullsIntermedios,
                  contadorCerosPosteriores);

              if (this.incluirRegistrosFaltantes(contadorNullsIntermedios, contadorCerosAnteriores,
                  contadorCerosPosteriores)) {
                // si se incluyen
                contadorSecuencia += contadorNullsIntermedios; // los sumo a la secuencia
                contadorSecuencia += contadorCerosPosteriores; // los sumo a la secuencia
                contadorCerosAnteriores = contadorCerosPosteriores; // los posteriores
                                                                    // pasan a ser los
                // anteriores del nuevo gap.

                contadorCerosPosteriores = 1; // el registro actual es el primer posterior
                                              // del nuevo gap.
                primerCeroPosteriorRegistro = registroDiario;

                contadorNullsIntermedios = diferencia - 1; // el ultimo dia no es un
                                                            // registro faltante.
              } else {
                // No se incluyen los registros faltantes => los "ceros posteriores"
                // corresponden a una
                // nueva secuencia.
                // Guardo la secuencia hasta el gap anterior (sin incluirlo).
                this.guardarSecuencia(sess, primerRegistro, contadorSecuencia);

                // Los ceros posteriores pasan a ser los ceros anteriores de una nueva
                // secuencia.
                primerRegistro = primerCeroPosteriorRegistro;
                contadorSecuencia = contadorCerosPosteriores;
                contadorCerosAnteriores = contadorCerosPosteriores;

                contadorCerosPosteriores = 1; // el registro actual es el primer posterior
                                              // del nuevo gap.
                primerCeroPosteriorRegistro = registroDiario;

                contadorNullsIntermedios = diferencia - 1; // el ultimo dia no es un
                                                            // registro faltante.
              }
            }
          }

          ultimoRegistro = registroDiario;
        }
      } else {

        // Dia con precipitacion.
        // Termina la secuencia actual.
        if (primerRegistro != null) { // si es null, no hay secuencia para grabar (ej,
        // dos precipitaciones seguidas)
        // Si hubo registros faltantes entre el ultimo registro y este registro nuevo, los
        // ignoro.

          // Me fijo si el ultimo gap de registros faltantes (en caso de haber) debe ser
          // incluido.
          if (contadorNullsIntermedios > 0
              && this.incluirRegistrosFaltantes(contadorNullsIntermedios, contadorCerosAnteriores,
                  contadorCerosPosteriores)) {
            // si se incluyen
            contadorSecuencia += contadorNullsIntermedios; // los sumo a la secuencia
            contadorSecuencia += contadorCerosPosteriores; // los sumo a la secuencia

            // Guardo la secuencia.
            this.guardarSecuencia(sess, primerRegistro, contadorSecuencia);

          } else {
            // no se incluyen los registros faltantes => los "ceros posteriores"
            // corresponden a una
            // nueva secuencia (en caso de haber).
            // Guardo la secuencia hasta el gap anterior (sin incluirlo).
            this.guardarSecuencia(sess, primerRegistro, contadorSecuencia);
            if (contadorNullsIntermedios > 0) {
              // Los ceros posteriores pasan a ser los ceros anteriores de una nueva
              // secuencia.
              primerRegistro = primerCeroPosteriorRegistro;
              contadorSecuencia = contadorCerosPosteriores;
              contadorCerosAnteriores = contadorCerosPosteriores;

              // Guardo la secuencia.
              this.guardarSecuencia(sess, primerRegistro, contadorSecuencia);
            }
          }

          // Limpio los contadores.
          primerRegistro = null;
          contadorSecuencia = 0;
          ultimoRegistro = null;
          contadorCerosAnteriores = 0;
          contadorCerosPosteriores = 0;
          contadorNullsIntermedios = 0;
        }
      }
    }
  }

  private boolean incluirRegistrosFaltantes(int contadorNullsIntermedios, int contadorCerosAnteriores,
      int contadorCerosPosteriores) {
    if (contadorNullsIntermedios > 180) {
      return false;
    }
    double numerador = contadorNullsIntermedios;
    double denominador = contadorNullsIntermedios + contadorCerosAnteriores + contadorCerosPosteriores;
    // System.out.println(contadorCerosAnteriores + "-" + contadorNullsIntermedios + "-" +
    // contadorCerosPosteriores);
    return (numerador / denominador) < 0.5;
  }

}
