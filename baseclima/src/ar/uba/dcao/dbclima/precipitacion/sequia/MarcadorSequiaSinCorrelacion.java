package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.PiecewiseLinearEmpiricalDist;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.precipitacion.rango.PromedioPrecipitacionAnualConSatelitesProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.PromedioPrecipitacionAnualProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.Rango;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Este es el caso en donde la estacion no tiene suficientes anios para realizar la distribucion
 * de la sequia (y saber si es sospechosamente seca).
 */
public class MarcadorSequiaSinCorrelacion extends AbstractMarcadorSequia {

  /**
   * 
   * @param estacion
   * @param sequia
   * @param vecinos
   */
  protected CodigoSequia calculoConVecinos(Session sess, Estacion estacion, Sequia sequia,
      CodigoSequia.Codigo codigoLocal, List<DatosSequiaEnEstacionVecina> vecinos, NeighborType neighborType) {
    CodigoSequia codigoVecino;
    if (vecinos.isEmpty()) {
      // no hay vecinos
      codigoVecino = null;
    } else if (this.calcularSiExisteVecinoSeco(vecinos)) {
      // hay un vecino con sequia en el mismo periodo
      codigoVecino = new CodigoSequia(CodigoSequia.Codigo.OV);
    } else {
      // hay vecinos pero ninguno tiene sequia en el mismo periodo
      Double menorPorcentaje = null;
      for (DatosSequiaEnEstacionVecina vecino : vecinos) {
        int cantidadAnios = vecino.aniosEnComun.size();
        if (cantidadAnios >= UMBRAL_ANIOS_EN_COMUN && vecino.spiVecino != null) {
          if (menorPorcentaje == null || vecino.spiVecino < menorPorcentaje) {
            menorPorcentaje = vecino.spiVecino;
          }
        }
      }
      if (menorPorcentaje == null) {
        // no hay buenos vecinos
        codigoVecino = null;
      } else if (Double.isInfinite(menorPorcentaje)) {
        codigoVecino = new CodigoSequia(CodigoSequia.Codigo.MIN);
      } else {
        codigoVecino = new CodigoSequia(this.obtenerCodigoSegunPorcentaje(menorPorcentaje));
      }
    }
    this.obtenerCodigoSegunTipoVecino(codigoVecino, neighborType);
    return codigoVecino;
  }

  protected CodigoSequia.Codigo obtenerCodigoLocal(Estacion estacion, Sequia sequia) {
    return CodigoSequia.Codigo.NO;
  }

  protected List<DatosSequiaEnEstacionVecina> obtenerVecinos(Estacion estacionBase, Sequia sequia,
      List<? extends Estacion> estacionesVecinas) {
    Date comienzoSequia = sequia.getComienzo();
    Date finSequia = FechaHelper.dameFechaSumada(comienzoSequia, sequia.getLongitud());

    List<DatosSequiaEnEstacionVecina> resultado = new ArrayList<DatosSequiaEnEstacionVecina>();
    for (Estacion estacionVecina : estacionesVecinas) {
      Double valor = ClasificadorSequiasHelper.damePrecipitacionCaidaEnPeriodoParaEstacion(estacionVecina, comienzoSequia, finSequia, UMBRAL_NULOS);
      if (valor == null) {
        // se ignora la estacion
      } else {
        double precipitacionVecina = valor.doubleValue();
        
        PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
            comienzoSequia, finSequia);
        proyectorRango.setUmbralNulos(UMBRAL_NULOS);
        proyectorRango.setIncluirNulos(false);
        List<Rango> rangos = proyectorRango.proyectarRangos(estacionVecina);
        List<Integer> anios = this.obtenerAnios(rangos);
        Double spiVecino;
        if (rangos.size() >= UMBRAL_ANIOS_EN_COMUN) {
          List<Double> valores = SPIHelper.obtenerValores(rangos);
          double[] dobles = new double[valores.size()];
          for (int i = 0; i < dobles.length; i++) {
            dobles[i] = valores.get(i);
          }
          Arrays.sort(dobles);
          if (precipitacionVecina == dobles[0]) {
            // es el minimo
            spiVecino = Double.NEGATIVE_INFINITY;
          } else {
            Distribution neighborDistribution = new PiecewiseLinearEmpiricalDist(dobles);
            double porcentaje = neighborDistribution.cdf(precipitacionVecina);
            spiVecino = porcentaje;
          }
        } else {
          spiVecino = null;
        }
        DatosSequiaEnEstacionVecina datos = 
          new DatosSequiaEnEstacionVecina(precipitacionVecina, 
            spiVecino,
            null, // sin SPI local acotado,
            anios,
            null, // sin KS test
            null // sin KS test
        );
        resultado.add(datos);
      }
    }
    return resultado;
  }
  
  private CodigoSequia.Codigo obtenerCodigoSegunPorcentaje(double porcentaje) {
    if (porcentaje <= 0.1) {
      return CodigoSequia.Codigo.P1;
    } else if (porcentaje <= 0.2) {
      return CodigoSequia.Codigo.P2;
    } else {
      return CodigoSequia.Codigo.P3;
    }
  }

  private List<Integer> obtenerAnios(List<Rango> rangos) {
    List<Integer> anios = new ArrayList<Integer>(rangos.size());
    for (Rango rango : rangos) {
      anios.add(FechaHelper.dameAnio(rango.comienzo()));
    }
    return anios;
  }

}
