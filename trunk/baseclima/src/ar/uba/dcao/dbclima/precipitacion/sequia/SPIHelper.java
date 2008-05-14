package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.precipitacion.rango.Rango;

import umontreal.iro.lecuyer.probdist.GammaDist;
import umontreal.iro.lecuyer.probdist.PiecewiseLinearEmpiricalDist;
import umontreal.iro.lecuyer.util.MathFunction;

/**
 * Esta clase provee metodos auxiliares para el calculo de las funciones SPI.
 *
 */
public final class SPIHelper {
  
  /**
   * Cantidad minima de anios (muestras) que debe haber para que haya una distribucion SPI coherente.
   */
  public static final int CANTIDAD_MINIMA_ANIOS = 20;
  
  /**
   * Cantidad recomendada de anios (muestras) que debe haber para que haya una buena distribucion SPI.
   */
  public static final int CANTIDAD_RECOMENDADA_ANIOS = 44;
  
  private SPIHelper() {}
  
  /**
   * Devuelve la funcion H utilizada para el calculo SPI (es una Gamma modificada)
   * @see GammaDist
   */
  public static MathFunction funcionH(List<Double> dobles) {

    double[] valores = new double[dobles.size()];
    int contador = 0;
    for (Double doble : dobles) {
      double valor = doble.doubleValue();
      if (valor > 0) {
        valores[contador] = valor;
        contador++;
      }
    }
    
    final MathFunction h;
    if (contador == 0) {
      h = new MathFunction() {
        public double evaluate(double x) { // solamente hay ceros
          return 1d; // es distribucion acumulada, ya empieza en el valor maximo
        }
      };
    } else if (contador == 1) {
      final PiecewiseLinearEmpiricalDist empiricalDist = new PiecewiseLinearEmpiricalDist(valores);
      h = new MathFunction() {
        public double evaluate(double x) {
          return empiricalDist.cdf(x);
        }
      };
    } else {
      final double q = (double) contador / (double) valores.length;
      final GammaDist gammaDist = GammaDist.getInstanceFromMLE(valores, contador);
      h = new MathFunction() {
        public double evaluate(double x) {
          return (1d - q) + q * gammaDist.cdf(x);
        }
      };
    }
    
    return h;
  }
  
  /**
   * Devuelve la funcion SPI calculada tomando en cuenta una muestra de valores.
   */
  public static MathFunction funcionSPI(List<Double> dobles) {
    final MathFunction h = funcionH(dobles);
    return new MathFunction() {
      public double evaluate(double x) {
        return spi(h.evaluate(x));
      }
    };
  }

  /**
   * Transformacion de la funcion Gamma a la normal SPI.
   */
  public static double spi(double x) {
    if (x < 0d || x > 1d) {
      throw new IllegalArgumentException("X must be between 0.0 and 1.0 (both inclusive)");
    }
    double t;
    if (x <= 0.5d) {
      t = StrictMath.sqrt(StrictMath.log(1d / StrictMath.pow(x, 2)));
    } else {
      t = StrictMath.sqrt(StrictMath.log(1d / StrictMath.pow(1d - x, 2)));
    }
    double c0 = 2.515517;
    double c1 = 0.802853;
    double c2 = 0.010328;
    double d1 = 1.432788;
    double d2 = 0.189269;
    double d3 = 0.001308;
    double spi;
    if (x <= 0.5d) {
      spi = (c0 + c1 * t + c2 * t * t) / (1 + d1 * t + d2 * t * t + d3 * t * t * t) - t;
    } else {
      spi = t - (c0 + c1 * t + c2 * t * t) / (1 + d1 * t + d2 * t * t + d3 * t * t * t);
    }
    return spi;
  }

  /**
   * Convierte una lista de valores encapsulados en Rangos a una lista de valores propiamente dichos.
   * @see Rango
   */
  public static List<Double> obtenerValores(List<Rango> rangos) {
    List<Double> resultado = new ArrayList<Double>(rangos.size());
    for (Rango rango : rangos) {
      resultado.add(rango.valor());
    }
    return resultado;
  }

  /**
   * Devuelve la clasificacion SPI correspondiente segun el valor SPI dentro de la distribucion Normal.
   */
  public static CodigoSequia.Codigo spi2Codigo(double spi) {
    if (spi <= -2.0) {
      return CodigoSequia.Codigo.SPI_ED;
    }
    if (spi <= -1.5) {
      return CodigoSequia.Codigo.SPI_SD;
    }
    if (spi <= -1.0) {
      return CodigoSequia.Codigo.SPI_MD;
    }
    if (spi < 1.0) {
      return CodigoSequia.Codigo.SPI_NN;
    }
    if (spi < 1.5) {
      return CodigoSequia.Codigo.SPI_MW;
    }
    if (spi < 2.0) {
      return CodigoSequia.Codigo.SPI_VW;
    }
    return CodigoSequia.Codigo.SPI_EW;
  }


}
