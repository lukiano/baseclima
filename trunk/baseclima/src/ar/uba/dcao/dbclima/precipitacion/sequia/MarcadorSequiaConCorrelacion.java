package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.util.MathFunction;
import ar.uba.dcao.dbclima.correlation.CalculadorCorrelacionEnRangos;
import ar.uba.dcao.dbclima.correlation.KSTest;
import ar.uba.dcao.dbclima.correlation.SignificanciaCorrelacionHelper;
import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.precipitacion.rango.PromedioPrecipitacionAnualConSatelitesProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.PromedioPrecipitacionAnualProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.Rango;
import ar.uba.dcao.dbclima.precipitacion.sequia.DatosSequiaEnEstacionVecina.ConfidenceType;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * En este camino existen suficientes datos para armar una distribucion SPI en la estacion base
 * para la sequia que esta siendo analizada, y se realizan los calculos con los vecinos
 * tomando en cuenta correlaciones y tests KS.
 *
 */
public class MarcadorSequiaConCorrelacion extends AbstractMarcadorSequia {

  protected CodigoSequia calculoConVecinos(Session sess, Estacion estacion, Sequia sequia,
      CodigoSequia.Codigo codigoLocal, List<DatosSequiaEnEstacionVecina> vecinos,
      NeighborType neighborType) {
    CodigoSequia codigoVecino;
    if (vecinos.isEmpty()) {
      // no hay vecinos
      codigoVecino = null;
    } else if (this.calcularSiExisteVecinoSeco(vecinos)) {
      // hay un vecino con sequia en el mismo periodo
      codigoVecino = new CodigoSequia(CodigoSequia.Codigo.OV);
    } else {
      // hay vecinos pero ninguno tiene sequia en el mismo periodo
      codigoVecino = this.calcularMenorDiferenciaConSPIVecinos(codigoLocal, vecinos);
    }
    this.obtenerCodigoSegunTipoVecino(codigoVecino, neighborType);
    return codigoVecino;
  }

  protected List<DatosSequiaEnEstacionVecina> obtenerVecinos(Estacion estacionBase, Sequia sequia,
      List<? extends Estacion> estacionesVecinas) {
    Date comienzoSequia = sequia.getComienzo();
    Date finSequia = FechaHelper.dameFechaSumada(comienzoSequia, sequia.getLongitud());
    
    List<DatosSequiaEnEstacionVecina> resultado = new ArrayList<DatosSequiaEnEstacionVecina>();
    for (Estacion estacionVecina : estacionesVecinas) {
      Double valor = ClasificadorSequiasHelper.damePrecipitacionCaidaEnPeriodoParaEstacion(estacionVecina,
          comienzoSequia, finSequia, UMBRAL_NULOS);
      if (valor == null) {
        // se ignora la estacion
      } else {
        double precipitacionVecina = valor.doubleValue();
        DatosSequiaEnEstacionVecina datos = this.obtenerDatosEstacionVecina(estacionBase, 
            comienzoSequia, finSequia, estacionVecina, precipitacionVecina);
        resultado.add(datos);
      }
    }
    return resultado;
  }

  private CodigoSequia calcularMenorDiferenciaConSPIVecinos(CodigoSequia.Codigo codigoLocal,
      List<DatosSequiaEnEstacionVecina> vecinos) {
    CodigoSequia codigoSequiaVecino = null;
    double diferenciaMasChica = Double.POSITIVE_INFINITY;

    // primero comprobamos los vecinos que tengan buena correlacion
    for (DatosSequiaEnEstacionVecina vecino : vecinos) {
      if (vecino.distributionConfidenceType == ConfidenceType.CORR) {
        double diferencia = Math.abs(vecino.spiVecino - vecino.spiLocalAcotado);
        if (diferencia < diferenciaMasChica) {
          CodigoSequia.Codigo codigoVecino = SPIHelper.spi2Codigo(vecino.spiVecino);
          codigoVecino = this.dameNuevoCodigoVecino(codigoVecino, diferencia, codigoLocal);
          codigoSequiaVecino = this.componerCodigoVecino(codigoVecino, vecino.distributionConfidenceType);
          diferenciaMasChica = diferencia;
        }
      }
    }
    if (codigoSequiaVecino != null) {
      return codigoSequiaVecino;
    }

    // si no hay, comprobamos los vecinos que tengan buen KS Test
    for (DatosSequiaEnEstacionVecina vecino : vecinos) {
      if (vecino.distributionConfidenceType == ConfidenceType.KS) {
        double diferencia = Math.abs(vecino.spiVecino - vecino.spiLocalAcotado);
        if (diferencia < diferenciaMasChica) {
          CodigoSequia.Codigo codigoVecino = SPIHelper.spi2Codigo(vecino.spiVecino);
          codigoVecino = this.dameNuevoCodigoVecino(codigoVecino, diferencia, codigoLocal);
          codigoSequiaVecino = this.componerCodigoVecino(codigoVecino, vecino.distributionConfidenceType);
          diferenciaMasChica = diferencia;
        }
      }
    }
    return codigoSequiaVecino;
  }
  
  private CodigoSequia componerCodigoVecino(CodigoSequia.Codigo codigoVecino, ConfidenceType confidenceType) {
    CodigoSequia codigoSequiaVecino = new CodigoSequia(codigoVecino);
    switch (confidenceType) {
    case CORR:
      codigoSequiaVecino.add(CodigoSequia.Codigo.CORR);
      break;
    case KS:
      codigoSequiaVecino.add(CodigoSequia.Codigo.KS);
      break;
    }
    return codigoSequiaVecino;
  }

  /**
   * Devuelve un nuevo codigo de vecino segun el codigo de vecino actual y el codigo local, tomando
   * en cuenta la Matriz.
   * 
   * @param codigoVecino
   * @param diferencia
   * @param codigoLocal
   * @return
   */
  private CodigoSequia.Codigo dameNuevoCodigoVecino(CodigoSequia.Codigo codigoVecino, double diferencia, CodigoSequia.Codigo codigoLocal) {
    CodigoSequia.Codigo nuevoCodigoVecino = codigoVecino;
    switch (codigoLocal) {
    case SPI_ED:
      switch (codigoVecino) {
      case SPI_SD:
        if (diferencia < 0.5) {
          nuevoCodigoVecino = CodigoSequia.Codigo.SPI_ED;
        }
        break;
      case SPI_MD:
        if (diferencia < 1) {
          nuevoCodigoVecino = CodigoSequia.Codigo.SPI_SD;
        }
        break;
      case SPI_NN:
        if (diferencia < 1.5) {
          nuevoCodigoVecino = CodigoSequia.Codigo.SPI_MD;
        }
        break;
      }
      break;
    case SPI_SD:
      switch (codigoVecino) {
      case SPI_MD:
        if (diferencia < 0.5) {
          nuevoCodigoVecino = CodigoSequia.Codigo.SPI_SD;
        }
        break;
      case SPI_NN:
        if (diferencia < 1) {
          nuevoCodigoVecino = CodigoSequia.Codigo.SPI_MD;
        }
        break;
      }
      break;
    case SPI_MD:
      switch (codigoVecino) {
      case SPI_SD:
      case SPI_NN:
        if (diferencia < 0.5) {
          nuevoCodigoVecino = CodigoSequia.Codigo.SPI_MD;
        }
        break;
      }
      break;
    }
    return nuevoCodigoVecino;
  }

  private Double obtenerBuena_KS_SiExiste(Estacion estacionBase, Date comienzoSequia, Date finSequia,
      Estacion estacionVecina, List<Integer> aniosEnComun) {
    double[] muestraBase = ClasificadorSequiasHelper.obtenerMuestra(aniosEnComun, comienzoSequia, finSequia,
        estacionBase, UMBRAL_NULOS);
    ClasificadorSequiasHelper.moverACero(muestraBase);
    double[] muestraVecina = ClasificadorSequiasHelper.obtenerMuestra(aniosEnComun, comienzoSequia, finSequia,
        estacionVecina, UMBRAL_NULOS);
    ClasificadorSequiasHelper.moverACero(muestraVecina);
    Double ksTest = KSTest.ksTest(muestraBase, muestraVecina);
    if (ksTest < UMBRAL_KS_TEST_VECINOS) {
      ksTest = null;
    }
    return ksTest;
  }

  private Double obtenerBuenaCorrelacionSiExiste(Estacion estacionBase, Date comienzoSequia, Date finSequia,
      Estacion estacionVecina) {
    PromedioPrecipitacionAnualProyectorRango proyectorRangoParaCorrelaciones = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzoSequia, finSequia);
    proyectorRangoParaCorrelaciones.setUmbralNulos(UMBRAL_NULOS);
    proyectorRangoParaCorrelaciones.setExcluirAnioCentral(true);
    
    CorrelacionEstaciones correlacionEstaciones = CalculadorCorrelacionEnRangos.getCorr(estacionBase, estacionVecina,
        proyectorRangoParaCorrelaciones, true);
    Double correlacion = correlacionEstaciones.getCorrelacion();
    boolean buenaCorrelacion = correlacion != null && correlacion >= UMBRAL_CORRELACION_VECINOS &&
    SignificanciaCorrelacionHelper.
    obtenerSignificancia(correlacion, correlacionEstaciones.getNumRegsUsados()) <= UMBRAL_SOC_VECINOS;
    if (!buenaCorrelacion) {
      correlacion = null;
    }
    return correlacion;
  }

  protected CodigoSequia.Codigo obtenerCodigoLocal(Estacion estacion, Sequia sequia) {
    List<Rango> rangos = ClasificadorSequiasHelper.dameRangos(estacion, sequia, UMBRAL_NULOS);
    MathFunction spiF = SPIHelper.funcionSPI(SPIHelper.obtenerValores(rangos));
    double spiLocal = spiF.evaluate(0);
    CodigoSequia.Codigo codigoLocal = SPIHelper.spi2Codigo(spiLocal);
    return codigoLocal;
  }

  private DatosSequiaEnEstacionVecina obtenerDatosEstacionVecina(Estacion estacionBase, Date comienzoSequia,
      Date finSequia, Estacion estacionVecina, double precipitacionVecina) {
    DatosSequiaEnEstacionVecina datos;
    List<Integer> aniosEnComun = ClasificadorSequiasHelper.obtenerAniosEnComun(estacionBase, estacionVecina,
        comienzoSequia, finSequia, UMBRAL_NULOS);
    if (aniosEnComun.size() < UMBRAL_ANIOS_EN_COMUN) {
      datos = new DatosSequiaEnEstacionVecina(precipitacionVecina, 
            null, // sin SPI vecino
            null, // sin SPI local acotado 
            aniosEnComun, 
            null, // sin correlacion 
            null);
    } else {
      MathFunction spiF = ClasificadorSequiasHelper.calcularSPI(comienzoSequia, finSequia,
          estacionBase, UMBRAL_NULOS);
      double spiLocalAcotado = spiF.evaluate(0d);
      double linearValue = this.obtenerPercentilYRegresionLineal(estacionBase, comienzoSequia, finSequia,
          estacionVecina, precipitacionVecina, aniosEnComun);
      double spiVecino = spiF.evaluate(linearValue);
      
      Double correlacion = this.obtenerBuenaCorrelacionSiExiste(estacionBase, comienzoSequia, finSequia, estacionVecina);
      if (correlacion != null) {
          datos = new DatosSequiaEnEstacionVecina(precipitacionVecina, 
                spiVecino,
                spiLocalAcotado, 
                aniosEnComun, 
                ConfidenceType.CORR, 
                correlacion);
      } else {
        Double ksTest = this.obtenerBuena_KS_SiExiste(estacionBase, comienzoSequia, finSequia, estacionVecina,
            aniosEnComun);
        if (ksTest != null) {
          datos = new DatosSequiaEnEstacionVecina(precipitacionVecina, 
              spiVecino,
              spiLocalAcotado, 
              aniosEnComun, 
              ConfidenceType.KS, 
              ksTest);
          
        } else {
          datos = new DatosSequiaEnEstacionVecina(precipitacionVecina, 
                spiVecino,
                spiLocalAcotado, 
                aniosEnComun, 
                null, // sin correlacion 
                null);
        }
      }
    }
    return datos;
  }

  private double obtenerPercentilYRegresionLineal(Estacion estacionBase, Date comienzoSequia, Date finSequia,
      Estacion estacionVecina, double precipitacionVecina, List<Integer> aniosEnComun) {
    Distribution neighborDistribution = ClasificadorSequiasHelper.calcularEmpirica(aniosEnComun,
        comienzoSequia, finSequia, estacionVecina, UMBRAL_NULOS);
    double percent = neighborDistribution.cdf(precipitacionVecina);
    Distribution localDistribution = ClasificadorSequiasHelper.calcularEmpirica(aniosEnComun, comienzoSequia,
        finSequia, estacionBase, UMBRAL_NULOS);
    double linearValue = localDistribution.inverseF(percent);
    return linearValue;
  }

}
