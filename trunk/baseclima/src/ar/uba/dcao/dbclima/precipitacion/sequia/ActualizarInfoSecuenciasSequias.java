package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.data.predicado.NoPrecipitacionPredicado;
import ar.uba.dcao.dbclima.data.predicado.PredicadoRegistro;
import ar.uba.dcao.dbclima.qc.StationBasedQualityCheck;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Realiza el calculo de las sequias leyendo los registros diarios para las estaciones seleccionadas.
 * Las sequias se guardan como objetos en la base de datos.
 * Una sequia es una sucesion de registros sin precipitacion.
 * Un registro faltante corta la sequia.
 * @see Sequia.
 * @see RegistroDiario
 *
 */
public class ActualizarInfoSecuenciasSequias extends StationBasedQualityCheck {
  
  private boolean readOnly;

  public ActualizarInfoSecuenciasSequias(boolean readOnly) {
    this.readOnly = readOnly;
  }
  
  @Override
  protected String startingDescription() {
    return "Building drought sequences...";
  }
  
  @Override
  protected String progressDescription(int processedStations, int totalStations) {
    return "Building drought sequences. " + processedStations + "/" + totalStations + " stations processed.";
  }
  
  @Override
  protected String finalDescription(int totalStations) {
    return "Drought sequences built. " + totalStations + " processes stations.";
  }

  @Override
  protected void processStation(Session sess, Estacion estacion) {
    DAOFactory.getSequiaDAO(sess).deleteAllByStation(estacion);
    
    RegistroDiario primerRegistro = null;
    RegistroDiario ultimoRegistro = null;
    int contadorSecuencia = 0;
    
    PredicadoRegistro predicado = new NoPrecipitacionPredicado(NoPrecipitacionPredicado.Opcion.NULL_COMO_CERO_SI_NO_HAY_LLUVIA);
    
    List<RegistroDiario> regs = estacion.getRegistros();
    for (RegistroDiario registroDiario : regs) {
      if (predicado.evaluar(registroDiario) && this.incluirNullORegistroContiguo(ultimoRegistro, registroDiario)) {
        
        // continuo con la secuencia o empiezo una nueva
        if (ultimoRegistro == null) {
          
          // empiezo una nueva
          primerRegistro = ultimoRegistro = registroDiario;
          contadorSecuencia = 1;
        } else {
          
          // continuo con la secuencia
          contadorSecuencia += FechaHelper.dameDifereciaDeDias(ultimoRegistro.getFecha(), registroDiario.getFecha());
          ultimoRegistro = registroDiario;
        }
      } else {
        
        // termina la secuencia actual
        if (primerRegistro != null) {
          // la grabo
          this.guardarSecuencia(sess, primerRegistro, contadorSecuencia);
          
          //reinicio los contadores
          primerRegistro = null;
          contadorSecuencia = 0;
          ultimoRegistro = null;
        }
      }
  }
  }

  private boolean incluirNullORegistroContiguo(RegistroDiario ultimoRegistro, RegistroDiario registroDiario) {
    if (ultimoRegistro == null) {
      // se empieza una nueva secuencia
      return true;
    }
    return FechaHelper.dameDifereciaDeDias(ultimoRegistro.getFecha(), registroDiario.getFecha()) == 1; // true solo si es un registro consecutivo
  }
  
  protected void guardarSecuencia(Session sess, RegistroDiario primerRegistro, int longitud) {
    Sequia sequia = new Sequia();
    sequia.setEstacion(primerRegistro.getEstacion());
    sequia.setComienzo(primerRegistro.getFecha());
    sequia.setLongitud(longitud);
    sequia.setRegistroComienzo(primerRegistro);
    if (!this.readOnly) {
      sess.saveOrUpdate(sequia);
    }
  }

}
