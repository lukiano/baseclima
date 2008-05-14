package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;

/**
 * Interfaz para la segunda parte del algoritmo de decision. Segun si la sequia
 * tiene suficientes anios como para calcular su SPI, se llamara a la implementacion
 * correspondiente. (Cada implementacion representa un camino distinto).
 * @see ClasificadorSequiasSPI
 *
 */
public interface MarcadorSequia {
  
  double UMBRAL_NULOS = 0.1;

  double UMBRAL_DISTANCIA_FISICA_VECINOS = 2;

  double UMBRAL_DISTANCIA_FISICA_VECINOS_SIN_CORRELACION = 2;

  double UMBRAL_CORRELACION_VECINOS = 0.8d;

  double UMBRAL_SOC_VECINOS = 0.01d; // 1 - 0.99
  
  double UMBRAL_KS_TEST_VECINOS = 0.9d;
  
  int UMBRAL_ANIOS_EN_COMUN = 5;

  void marcarSequia(Session session, Estacion estacion, Sequia sequia, List<Sequia> sequiasAlternativas);

  enum NeighborType {
    NORMAL, ALTERNATE, SATELLITAL;
  }

}
