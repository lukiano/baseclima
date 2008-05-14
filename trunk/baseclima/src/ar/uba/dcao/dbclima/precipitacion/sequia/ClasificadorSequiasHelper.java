package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.PiecewiseLinearEmpiricalDist;
import umontreal.iro.lecuyer.util.MathFunction;
import ar.uba.dcao.dbclima.correlation.CalculadorCorrelacionEnRangos;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.estacion.EstacionSatelital;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.precipitacion.PrecipitacionHelper;
import ar.uba.dcao.dbclima.precipitacion.rango.PromedioPrecipitacionAnualConSatelitesProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.PromedioPrecipitacionAnualProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.Rango;
import ar.uba.dcao.dbclima.precipitacion.rango.RangoImpl;
import ar.uba.dcao.dbclima.precipitacion.satelital.OperacionSatelital;
import ar.uba.dcao.dbclima.precipitacion.satelital.OperacionSatelitalFactory;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Clase con metodos auxiliares para la clasificacion de sequias.
 * @author luciano
 *
 */
public final class ClasificadorSequiasHelper {
  
  
  private ClasificadorSequiasHelper() {}

  /**
   * Calcula una distribucion empirica tomando como valores el resultado de la proyeccion 
   * del rango de las fechas para cada anio disponible en la estacion.
   * @see PromedioPrecipitacionAnualConSatelitesProyectorRango
   */
  public static Distribution calcularEmpirica(Date comienzoSequia, Date finSequia,
      Estacion estacion, double umbralNulos) {
    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzoSequia, finSequia);
    proyectorRango.setUmbralNulos(umbralNulos);
    proyectorRango.setIncluirNulos(false);
    List<Rango> rangos = proyectorRango.proyectarRangos(estacion);
  
    List<Double> valores = SPIHelper.obtenerValores(rangos);
    double[] dobles = new double[valores.size()];
    for (int i = 0; i < dobles.length; i++) {
      dobles[i] = valores.get(i);
    }
    Arrays.sort(dobles);
    Distribution neighborDistribution = new PiecewiseLinearEmpiricalDist(dobles);
    return neighborDistribution;
  }

  /**
   * Calcula una distribucion empirica tomando como valores el resultado de la proyeccion 
   * del rango de las fechas para cada anio disponible en la estacion y que se encuentre en los anios posibles.
   * @see PromedioPrecipitacionAnualConSatelitesProyectorRango
   */
  public static Distribution calcularEmpirica(List<Integer> aniosPosibles, Date comienzoSequia, Date finSequia,
      Estacion estacion, double umbralNulos) {
    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzoSequia, finSequia);
    proyectorRango.setUmbralNulos(umbralNulos);
    proyectorRango.setIncluirNulos(false);
    proyectorRango.setAnioPosibles(aniosPosibles);
    List<Rango> rangos = proyectorRango.proyectarRangos(estacion);
  
    List<Double> valores = SPIHelper.obtenerValores(rangos);
    double[] dobles = new double[valores.size()];
    for (int i = 0; i < dobles.length; i++) {
      dobles[i] = valores.get(i);
    }
    Arrays.sort(dobles);
    Distribution neighborDistribution = new PiecewiseLinearEmpiricalDist(dobles);
    return neighborDistribution;
  }

  /**
   * Calcula la distribucion SPI tomando como valores el resultado de la proyeccion 
   * del rango de las fechas para cada anio disponible en la estacion.
   * @see PromedioPrecipitacionAnualConSatelitesProyectorRango
   */
  public static MathFunction calcularSPI(Date comienzoSequia, Date finSequia, Estacion estacion, double umbralNulos) {
    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzoSequia, finSequia);
    proyectorRango.setUmbralNulos(umbralNulos);
    proyectorRango.setIncluirNulos(false);
    List<Rango> rangos = proyectorRango.proyectarRangos(estacion);
  
    List<Double> valores = SPIHelper.obtenerValores(rangos);
    MathFunction spiF = SPIHelper.funcionSPI(valores);
  
    return spiF;
  }

  /**
   * Calcula la distribucion SPI tomando como valores el resultado de la proyeccion 
   * del rango de las fechas para cada anio disponible en la estacion y que se encuentre en los anios posibles.
   * @see PromedioPrecipitacionAnualConSatelitesProyectorRango
   */
  public static MathFunction calcularSPI(List<Integer> aniosPosibles, Date comienzoSequia, Date finSequia, Estacion estacion, double umbralNulos) {
    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzoSequia, finSequia);
    proyectorRango.setUmbralNulos(umbralNulos);
    proyectorRango.setIncluirNulos(false);
    proyectorRango.setAnioPosibles(aniosPosibles);
    List<Rango> rangos = proyectorRango.proyectarRangos(estacion);
  
    List<Double> valores = SPIHelper.obtenerValores(rangos);
    MathFunction spiF = SPIHelper.funcionSPI(valores);
  
    return spiF;
  }

  /**
   * Obtiene los puntos satelitales cercanos a la estacion.
   * 
   * @param estacionBase
   * @return
   */
  @SuppressWarnings("unchecked")
  public static List<EstacionSatelital> dameEstacionesSatelitalesCercanas(Estacion estacionBase,
      double distanciaMaximaEnGrados) {
  
    int estacionBaseLatitud = estacionBase.getLatitud();
    int estacionBaseLongitud = estacionBase.getLongitud();
    
    OperacionSatelital operacionSatelital = OperacionSatelitalFactory.getInstance();
    return operacionSatelital.dameEstacionesSatelitalesCercanas(estacionBaseLatitud, estacionBaseLongitud, distanciaMaximaEnGrados);
  }

//  public static List<Estacion> dameEstacionesVecinas(Session sess, Estacion estacionBase) {
//    estacionBase = (Estacion) sess.merge(estacionBase);
//    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);
//    if (estacionBase.getDataset().isReferente()) {
//      return new ArrayList<Estacion>(estacionDAO.findAllForBDR());
//    } else {
//      List<Estacion> estaciones = new ArrayList<Estacion>(estacionDAO.findAllByDataset(estacionBase.getDataset()));
//      List<Estacion> estacionesBDR = estacionDAO.findAllForBDR();
//      for (Estacion estacionBDR : estacionesBDR) {
//        if (!estaciones.contains(estacionBDR)) {
//          estaciones.add(estacionBDR);
//        }
//      }
//      return estaciones;
//    }
//  }

  /**
   * Obtiene las estaciones cercanas a la estacion base 
   * segun distancia euclideana medida en grados.
   */
  @SuppressWarnings("unchecked")
  public static List<Estacion> dameEstacionesVecinasCercanas(Estacion estacionBase,
      double distanciaMaximaEnGrados) {
    
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    Transaction transaction = sess.getTransaction();
    if (transaction != null && transaction.isActive()) {
      transaction = null;
    } else {
      transaction = sess.beginTransaction();
    }

    int estacionBaseLatitud = estacionBase.getLatitud();
    int estacionBaseLongitud = estacionBase.getLongitud();

    Query querySatelites = sess
        .createQuery("FROM Estacion WHERE ((latitud - ?) * (latitud - ?) + (longitud - ?) * (longitud - ?)) <= ?");
    querySatelites = querySatelites.setInteger(0, estacionBaseLatitud).setInteger(1, estacionBaseLatitud);
    querySatelites = querySatelites.setInteger(2, estacionBaseLongitud).setInteger(3, estacionBaseLongitud);
    int umbralDistancia = PrecipitacionHelper.ajustarGrados(distanciaMaximaEnGrados);
    int umbralDistanciaCuadrado = umbralDistancia * umbralDistancia; 
    querySatelites = querySatelites.setInteger(4, umbralDistanciaCuadrado);
    List<Estacion> estacionesVecinas = querySatelites.list();

    estacionesVecinas.remove(estacionBase);
  
    if (transaction != null) {
      transaction.commit();
    }

    return estacionesVecinas;
  }

  /**
   * Obtiene para un determinado rango conformado por las fechas de comienzo y fin (y solo
   * para el anio indicado en ellas) la precipitacion caida en la estacion especificada.
   */
  public static Double damePrecipitacionCaidaEnPeriodoParaEstacion(Estacion estacion, Date comienzo, Date fin, double umbralNulos) {
    PromedioPrecipitacionAnualProyectorRango proyectorRangoParaSaberSiLaEstacionTieneDatosEnElPeriodoDeLaSequia = 
      new PromedioPrecipitacionAnualConSatelitesProyectorRango(comienzo, fin);
    proyectorRangoParaSaberSiLaEstacionTieneDatosEnElPeriodoDeLaSequia.setUmbralNulos(umbralNulos);
    proyectorRangoParaSaberSiLaEstacionTieneDatosEnElPeriodoDeLaSequia.setIncluirNulos(true);
    proyectorRangoParaSaberSiLaEstacionTieneDatosEnElPeriodoDeLaSequia.setRangoAnios(0);
    List<Rango> rangos = proyectorRangoParaSaberSiLaEstacionTieneDatosEnElPeriodoDeLaSequia
        .proyectarRangos(estacion);
  
    if (rangos.size() == 0) {
      return null;
    } else {
      return rangos.get(0).valor();
    }
  }

  /**
   * Para una sequia de una determinada estacion, devuelve los rangos correspondientes al mismo
   * comienzo y fin de la sequia, pero en los distintos anios disponibles en la estacion. Sirve para
   * armar la distribucion SPI si es que hay suficientes anios.
   * 
   * @param estacion
   * @param sequia
   * @return
   */
  public static List<Rango> dameRangos(Estacion estacion, Sequia sequia, double umbralNulos) {
    Date comienzo = sequia.getComienzo();
    Date fin = FechaHelper.dameFechaSumada(comienzo, sequia.getLongitud());
    return dameRangos(estacion, comienzo, fin, umbralNulos);
  }

  public static List<Rango> dameRangos(Estacion estacion, Date comienzo, Date fin, double umbralNulos) {
    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzo, fin);
    proyectorRango.setUmbralNulos(umbralNulos);
    proyectorRango.setIncluirNulos(false);
  
    List<Rango> rangos = proyectorRango.proyectarRangos(estacion);
  
    // El anio en cuestion puede tener mas del porcentaje de nulls, pero como estan aceptados, le
    // pongo un cero.
    boolean anioCentralEsNull = true;
    for (Rango rango : rangos) {
      if (FechaHelper.dameAnio(rango.comienzo()) == FechaHelper.dameAnio(comienzo)) {
        anioCentralEsNull = false;
        break;
      }
    }
  
    if (anioCentralEsNull) {
      rangos.add(new RangoImpl(comienzo, fin, proyectorRango.nombre(), 0d));
    }
  
    return rangos;
  }

  /**
   * Obtiene el promedio de la muestra y desplaza sus valores de tal manera que
   * el nuevo promedio sea cero.
   */
  public static void moverACero(double[] muestra) {
    double promedio = 0;
    for (int i = 0; i < muestra.length; i++) {
      promedio += muestra[i];
    }
    promedio /= muestra.length;
    for (int i = 0; i < muestra.length; i++) {
      muestra[i] -= promedio;
    }
  }

  /**
   * Calcula la cantidad de anios en comun que tienen dos estaciones, tomando en cuenta la disponibilidad
   * de anios en las mismas para el rango determinado por las fechas de comienzo y fin.
   */
  public static List<Integer> obtenerAniosEnComun(Estacion e1, Estacion e2, Date comienzo, Date fin, double umbralNulos) {
    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzo, fin);
    proyectorRango.setUmbralNulos(umbralNulos);
    proyectorRango.setIncluirNulos(false);
  
    return CalculadorCorrelacionEnRangos.obtenerAniosEnComun(e1, e2, proyectorRango);
  }

  /**
   * Obtiene el resultado de la proyeccion 
   * del rango de las fechas para cada anio disponible en la estacion.
   * @see PromedioPrecipitacionAnualConSatelitesProyectorRango
   */
  public static double[] obtenerMuestra(List<Integer> aniosPosibles, Date comienzoSequia, Date finSequia,
      Estacion estacion, double umbralNulos) {
    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzoSequia, finSequia);
    proyectorRango.setUmbralNulos(umbralNulos);
    proyectorRango.setIncluirNulos(false);
    proyectorRango.setAnioPosibles(aniosPosibles);
    List<Rango> rangos = proyectorRango.proyectarRangos(estacion);
  
    List<Double> valores = SPIHelper.obtenerValores(rangos);
    double[] dobles = new double[valores.size()];
    for (int i = 0; i < dobles.length; i++) {
      dobles[i] = valores.get(i);
    }
    Arrays.sort(dobles);
    return dobles;
  }
  
  

}
