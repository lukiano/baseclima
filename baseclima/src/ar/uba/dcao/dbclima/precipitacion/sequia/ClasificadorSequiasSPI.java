package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.PiecewiseLinearEmpiricalDist;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.precipitacion.rango.Rango;
import ar.uba.dcao.dbclima.qc.StationBasedQualityCheck;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Chequeo de calidad para las sequias. Es realizado estacion por estacion.
 * Aqui se realiza la primera parte del algoritmo de decision.
 * Luego, dependiendo de si hay suficientes datos para armar el SPI o no,
 * se llama al MarcadorSequia correspondiente.
 * @see MarcadorSequia
 * @author Luciano
 *
 */
public class ClasificadorSequiasSPI extends StationBasedQualityCheck {

  private static final double UMBRAL_DISTANCIA = 10d;

  private static final double UMBRAL_DEMASIADA_DISTANCIA = 100d;

  @Override
  protected String finalDescription(int totalStations) {
    return "Drought sequences classified. " + totalStations + " processes stations.";
  }

  @Override
  protected String progressDescription(int processedStations, int totalStations) {
    return "Classifying drought sequences. " + processedStations + "/" + totalStations + " stations processed.";
  }

  /**
   * Comienzo del recorrido para cada estacion
   * 
   * @param estacion
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void processStation(Session sess, Estacion estacion) {
//    System.out.println("Poniendo codigo sequias en NULL");
    sess.createQuery("UPDATE RegistroDiario SET codigoConfianzaDrought = NULL WHERE estacion = ?").setParameter(0,
        estacion).executeUpdate();
  
    List<Sequia> resultados = DAOFactory.getSequiaDAO(sess).findAllByStation(estacion);
  
    int cantidadSequias = resultados.size();
  
    if (resultados.size() < 2) {
      // muy pocas observaciones => la estacion no sirve
      return;
    }
  
    double[] valores = new double[cantidadSequias];
  
    for (int i = 0; i < cantidadSequias; i++) {
      valores[i] = resultados.get(i).getLongitud().doubleValue();
    }
  
    // Calculo la distribucion de las sequias de la estacion
    ContinuousDistribution distribution = new PiecewiseLinearEmpiricalDist(valores);
  
    double p50 = distribution.inverseF(0.50);
    double p75 = distribution.inverseF(0.75);
  
    List<Sequia> sequiasMayores75 = new ArrayList<Sequia>();
  
    for (Sequia sequia : resultados) {
      if (sequia.getLongitud().doubleValue() > p75) {
        sequiasMayores75.add(sequia);
      } else {
        this.marcar(sess, sequia, new CodigoSequia(CodigoSequia.Codigo.LP75));
      }
    }
  
    // --------
  
    Collections.sort(sequiasMayores75, new LongitudSequiaComparator());
  
    double divisor = p75 - p50;
    int umbralDistancia = Integer.MAX_VALUE;
    for (int i = 0; i < sequiasMayores75.size(); i++) {
      Sequia sequia = sequiasMayores75.get(i);
      int cantDias = sequia.getLongitud();
      double D = (cantDias - p75) / divisor;
      if (D >= UMBRAL_DISTANCIA) {
        umbralDistancia = i;
        break;
      }
    }
    if (umbralDistancia == Integer.MAX_VALUE) {
      umbralDistancia = sequiasMayores75.size();
    }
  
    int saltoMayor = 1;
    int umbralSaltoMayor = 0;
    for (int i = 1; i < umbralDistancia; i++) {
      Sequia sequia = sequiasMayores75.get(i);
      int cantDias = sequia.getLongitud();
      int cantDiasAnterior = sequiasMayores75.get(i - 1).getLongitud();
      if (cantDiasAnterior != cantDias) {
        int salto = cantDias - cantDiasAnterior;
        if (salto > saltoMayor) {
          saltoMayor = salto;
          umbralSaltoMayor = i;
        }
      }
    }
  
    for (int i = 0; i < umbralSaltoMayor; i++) { // decision 1
      Sequia sequia = sequiasMayores75.get(i);
      this.marcar(sess, sequia, new CodigoSequia(CodigoSequia.Codigo.SLD));
    }
  
    int umbralDemasiadaDistancia = Integer.MAX_VALUE;
    for (int i = umbralDistancia; i < sequiasMayores75.size(); i++) {
      Sequia sequia = sequiasMayores75.get(i);
      int cantDias = sequia.getLongitud();
      double D = (cantDias - p75) / divisor;
      if (D >= UMBRAL_DEMASIADA_DISTANCIA) {
        umbralDemasiadaDistancia = i;
        break;
      }
    }
    if (umbralDemasiadaDistancia == Integer.MAX_VALUE) {
      umbralDemasiadaDistancia = sequiasMayores75.size();
    }
  
    // para cada sequia entre el umbral de Salto Mayor y el de DEMASIADA DISTANCIA, la realizo un
    // analisis para marcarla
    for (int i = umbralSaltoMayor; i < umbralDemasiadaDistancia; i++) {
      Sequia sequia = sequiasMayores75.get(i);
      this.marcarSequia(sess, estacion, sequia);
    }
  
    // para cada sequia que pasa el umbral de DEMASIADA DISTANCIA, la marco como tal sin realizar
    // mayor analisis
    for (int i = umbralDemasiadaDistancia; i < sequiasMayores75.size(); i++) {
      Sequia sequia = sequiasMayores75.get(i);
      this.marcar(sess, sequia, new CodigoSequia(CodigoSequia.Codigo.LLD));
    }
  
  }

  @Override
  protected String startingDescription() {
    return "Classifying drought sequences...";
  }

  /**
   * Convierte una lista de rangos en una lista de sequias.
   * 
   * @param rangos
   * @return
   */
  private List<Sequia> dameSequias(Sequia sequiaOriginal, List<Rango> rangos) {
    List<Sequia> resultado = new ArrayList<Sequia>();
    for (Rango rango : rangos) {
      if (rango.valor().doubleValue() == 0d) {
        int cantidadDias = FechaHelper.dameDifereciaDeDias(rango.comienzo(), rango.fin());
        Sequia sequia = new Sequia();
        sequia.setEstacion(sequiaOriginal.getEstacion());
        sequia.setComienzo(rango.comienzo());
        sequia.setLongitud(cantidadDias);
        resultado.add(sequia);
      }
    }
    return resultado;
  }

  /**
   * Marca una sequia con un codigo local determinado.
   * 
   * @param estacion
   * @param sequia
   * @param codigoLocal
   * @param confianza
   */
  private void marcar(Session sess, Sequia sequia, CodigoSequia codigoLocal) {
    //System.out.println("Marcando Sequia: " + sequia + " como " + codigoLocal.toString());
    RegistroDiario registroDiario = sequia.getRegistroComienzo();
    registroDiario.setCodigoConfianzaDrought(codigoLocal.toString());
    sess.saveOrUpdate(registroDiario);
  }

  /**
   * Analisis de una sequia particular de una estacion
   * 
   * @param estacion
   * @param sequia
   */
  private void marcarSequia(Session sess, Estacion estacion, Sequia sequia) {
  
    // Calculo la SPI propia
    List<Rango> rangos = ClasificadorSequiasHelper.dameRangos(estacion, sequia, MarcadorSequia.UMBRAL_NULOS);
    
    List<Sequia> sequiasAlternativas = this.dameSequias(sequia, rangos);
    sequiasAlternativas.remove(sequia);
    if (sequiasAlternativas.size() >= 2) {
      // si existen dos o mas anios con precipitacion cero (sequia) ademas del que estoy
      // analizando, entonces asumo que está bien
      this.marcar(sess, sequia, new CodigoSequia(CodigoSequia.Codigo.MD));
      return;
    }
    // me queda uno o ningun evento de sequia alternativa
    
  
    // Cada rango contiene la precipitacion caida para para cada anio de la estacion, segun el
    // comienzo y fin de la sequia actual
    if (rangos.size() < SPIHelper.CANTIDAD_MINIMA_ANIOS) {
      // muy pocas observaciones para realizar la distribucion sobre la estacion
      MarcadorSequia marcadorSequia = new MarcadorSequiaSinCorrelacion();
      marcadorSequia.marcarSequia(sess, estacion, sequia, sequiasAlternativas);
    } else {
      // tengo suficientes anios para realizar la distribucion
      MarcadorSequia marcadorSequia = new MarcadorSequiaConCorrelacion();
      marcadorSequia.marcarSequia(sess, estacion, sequia, sequiasAlternativas);
    }
  }

}