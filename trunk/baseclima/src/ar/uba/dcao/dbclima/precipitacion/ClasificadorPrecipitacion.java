package ar.uba.dcao.dbclima.precipitacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.PiecewiseLinearEmpiricalDist;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.precipitacion.DistribucionPrecipitacionDelMesPanel.RegistroDiarioPrecipitacionComparator;
import ar.uba.dcao.dbclima.precipitacion.rango.AcumPrecipitacionAnualProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.Rango;
import ar.uba.dcao.dbclima.qc.StationBasedQualityCheck;


/**
 * Chequeo de calidad para las precipitaciones. Es realizado estacion por estacion.
 * Aqui se implementa el algoritmo de decision para clasificar las precipitaciones.
 * Se procesa sobre los registros diarios, leyendo el campo precipitacion.
 * @see CodigoPrecipitacionExtrema
 *
 */
public class ClasificadorPrecipitacion extends StationBasedQualityCheck {

  @Override
  protected String finalDescription(int totalStations) {
    return "Precipitation classified for " + totalStations + " stations.";
  }

  @Override
  protected String progressDescription(int processedStations, int totalStations) {
    return "Precipitation classified for " + processedStations + "/" + totalStations + " stations";
  }
  
  @Override
  protected void processStation(Session sess, Estacion station) {
    String progressDescription = this.getProgressDescription();
    for (int mes = 1; mes <= 12; mes++) {
      this.procesarMes(sess, station, mes);
      this.setProgressDescription(progressDescription + " (month " + (mes + 1) + ")");
    }
  }
  
  @Override
  protected String startingDescription() {
    return "Classifying precipitation...";
  }

  private void borrarCodigosActuales(Session sess, Estacion estacion, int mes) {
    sess.createQuery("UPDATE RegistroDiario SET codigoConfianzaPrecip = NULL WHERE estacion = ? AND month(fecha) = ?")
      .setParameter(0, estacion).setInteger(1, mes)
      .executeUpdate();
  }

  private ContinuousDistribution dameDistribucion(Estacion estacion, int mes) {
    int finDia = DistribucionPrecipitacionDelMesPanel.dameFinDia(mes);
    
    AcumPrecipitacionAnualProyectorRango proyectorRango = new AcumPrecipitacionAnualProyectorRango(1, mes, finDia, mes);

    proyectorRango.setIncluir0mm(false);
    List<Rango> rangos = proyectorRango.proyectarRangos(estacion);
    
    List<Double> lista = new ArrayList<Double>(); 
    for (Rango rango : rangos) {
      if (rango.valor() != null) {
        lista.add(rango.valor().doubleValue());
      }
    }
    
    if (lista.size() < 2) {
      // no se puede procesar el mes, no hay suficientes anios en la estacion para el mismo.
      return null;
    }
    
    double[] valores = new double[lista.size()];
    for (int i = 0; i < valores.length; i++) {
      valores[i] = lista.get(i);
    }
    
    ContinuousDistribution distribution = new PiecewiseLinearEmpiricalDist(valores);
    return distribution;
  }

  private void marcar(Session session, RegistroDiario registro, CodigoPrecipitacionExtrema codigo) {
    registro.setCodigoConfianzaPrecip(codigo.toString());
    session.saveOrUpdate(registro);
  }

  @SuppressWarnings("unchecked")
  private void procesarMes(Session sess, Estacion estacion, int mes) {
    this.borrarCodigosActuales(sess, estacion, mes);

    ContinuousDistribution distribution = this.dameDistribucion(estacion, mes);
    if (distribution == null) {
      return;
    }
    
    double p75 = distribution.inverseF(0.75);
    int percentil75 = (int)Math.floor(p75 * 100);
    double p90 = distribution.inverseF(0.90);
    
    sess.createQuery("UPDATE RegistroDiario SET codigoConfianzaPrecip = ? WHERE estacion = ? AND month(fecha) = ? AND precipitacion < ?")
      .setString(0, CodigoPrecipitacionExtrema.POK.toString()).setParameter(1, estacion).setInteger(2, mes).setInteger(3, percentil75).executeUpdate();

    List<RegistroDiario> registrosMayoresP75 = new ArrayList<RegistroDiario>();

    Query query = sess.createQuery("FROM RegistroDiario WHERE estacion = ? AND month(fecha) = ? AND precipitacion >= ?").
      setParameter(0, estacion).setInteger(1, mes);
    List<RegistroDiario> registros = query.setInteger(2, percentil75).list();
    if (registros != null) {
      for (RegistroDiario registro : registros) {
        //sess.evict(registro);
        registrosMayoresP75.add(registro);
      }
    }
    
    RegistroDiarioPrecipitacionComparator comparator = new RegistroDiarioPrecipitacionComparator();
    Collections.sort(registrosMayoresP75, comparator);
    
    Double ultimaPrecip = null;
    boolean huboJumpMayor100 = false;
    for (RegistroDiario registro : registrosMayoresP75) {
      double precip = registro.getPrecipitacion().doubleValue();
      precip = PrecipitacionHelper.ajustarPrecipitacion(precip);
      
      if (!huboJumpMayor100) {
        if (ultimaPrecip != null) {
          double diferencia = precip - ultimaPrecip;
          if (diferencia > ultimaPrecip) {
            huboJumpMayor100 = true;
          }
        }
        ultimaPrecip = precip;
      }

      if (huboJumpMayor100) { // decision 2
        if (precip < p90) { // decision 3
          this.marcar(sess, registro, CodigoPrecipitacionExtrema.PJUMP);
        } else {
          this.marcar(sess, registro, CodigoPrecipitacionExtrema.P90);
        }
      } else {
        if (precip < p90) { // decision 1
          this.marcar(sess, registro, CodigoPrecipitacionExtrema.P75);
        } else {
          this.marcar(sess, registro, CodigoPrecipitacionExtrema.P90);
        }
      }
    }

  }

}
