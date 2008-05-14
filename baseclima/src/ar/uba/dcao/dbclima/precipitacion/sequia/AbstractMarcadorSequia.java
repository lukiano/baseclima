package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.estacion.EstacionSatelital;

/**
 * Clase abstracta base para los marcadores disponibles. Aqui se reune el codigo comun.
 *
 */
public abstract class AbstractMarcadorSequia implements MarcadorSequia {

  public final void marcarSequia(Session sess, Estacion estacion, Sequia sequia, List<Sequia> sequiasAlternativas) {
    CodigoSequia.Codigo codigoLocal = this.obtenerCodigoLocal(estacion, sequia);
    CodigoSequia codigoSequiaVecino;
    List<DatosSequiaEnEstacionVecina> vecinos = this.obtenerVecinos(estacion, sequia);
    if (vecinos.isEmpty()) {
      if (sequiasAlternativas.isEmpty()) {
        codigoSequiaVecino = this.calculoSequiaConEstacionesSatelitales(sess, estacion, sequia, codigoLocal);
      } else {
        Sequia sequiaAlternativa = sequiasAlternativas.get(0); // va a haber una sola.
        List<DatosSequiaEnEstacionVecina> vecinosAlternativos = this.obtenerVecinos(estacion, sequiaAlternativa);
        if (vecinosAlternativos.isEmpty()) {
          codigoSequiaVecino = this.calculoSequiaConEstacionesSatelitales(sess, estacion, sequia, codigoLocal);
        } else {
          // calculo con vecinos alternativos
          codigoSequiaVecino = this.calculoConVecinos(sess, estacion, sequia, codigoLocal, vecinosAlternativos, NeighborType.ALTERNATE);
          if (codigoSequiaVecino == null) {
            codigoSequiaVecino = this.calculoSequiaConEstacionesSatelitales(sess, estacion, sequia, codigoLocal);  
          }
        }
      }
    } else {
      // calculo con vecinos
      codigoSequiaVecino = this.calculoConVecinos(sess, estacion, sequia, codigoLocal, vecinos, NeighborType.NORMAL);
      if (codigoSequiaVecino == null) {
        if (sequiasAlternativas.isEmpty()) {
          codigoSequiaVecino = this.calculoSequiaConEstacionesSatelitales(sess, estacion, sequia, codigoLocal);
        } else {
          Sequia sequiaAlternativa = sequiasAlternativas.get(0); // va a haber una sola.
          List<DatosSequiaEnEstacionVecina> vecinosAlternativos = this.obtenerVecinos(estacion, sequiaAlternativa);
          if (vecinosAlternativos.isEmpty()) {
            codigoSequiaVecino = this.calculoSequiaConEstacionesSatelitales(sess, estacion, sequia, codigoLocal);
          } else {
            // calculo con vecinos alternativos
            codigoSequiaVecino = this.calculoConVecinos(sess, estacion, sequia, codigoLocal, vecinosAlternativos, NeighborType.ALTERNATE);
            if (codigoSequiaVecino == null) {
              codigoSequiaVecino = this.calculoSequiaConEstacionesSatelitales(sess, estacion, sequia, codigoLocal);  
            }
          }
        }
      }
    }
    CodigoSequia codigoSequiaLocal = new CodigoSequia(codigoLocal);
    if (codigoSequiaVecino == null) {
      codigoSequiaLocal.add(CodigoSequia.Codigo.NO);
    } else {
      codigoSequiaLocal.add(codigoSequiaVecino);
    }
    this.marcar(sess, sequia, codigoSequiaLocal);
  }

  /**
   * Marca una sequia con un codigo local, codigo vecino y confianza determinados.
   * 
   * @param estacion
   * @param sequia
   * @param codigoVecino
   * @param codigoLocal
   * @param confianza
   */
  private void marcar(Session sess, Sequia sequia, CodigoSequia codigo) {
    System.out.println("Marcando Sequia: " + sequia + " como " + codigo.toString());
    RegistroDiario registroDiario = sequia.getRegistroComienzo();
//    RegistroDiario registroDiario = (RegistroDiario) sess.createQuery(
//        "FROM RegistroDiario WHERE estacion = ? AND fecha = ?").setParameter(0, sequia.getEstacion()).setDate(1,
//        sequia.getComienzo()).uniqueResult();
    registroDiario.setCodigoConfianzaDrought(codigo.toString());
    sess.saveOrUpdate(registroDiario);
  }

  protected abstract CodigoSequia.Codigo obtenerCodigoLocal(Estacion estacion, Sequia sequia);

  protected abstract CodigoSequia calculoConVecinos(Session sess, Estacion estacion, Sequia sequia,
      CodigoSequia.Codigo codigoLocal, List<DatosSequiaEnEstacionVecina> vecinos, NeighborType neighborType);

  protected final boolean calcularSiExisteVecinoSeco(List<DatosSequiaEnEstacionVecina> vecinos) {
    for (DatosSequiaEnEstacionVecina vecino : vecinos) {
      if (vecino.precipitacionVecina == 0) { // vecino seco
        return true;
      }
    }
    return false;
  }

  protected final void obtenerCodigoSegunTipoVecino(CodigoSequia codigoVecino, NeighborType neighborType) {
    if (codigoVecino == null) {
      return;
    }

    switch (neighborType) {
    case NORMAL:
      codigoVecino.add(CodigoSequia.Codigo.NORM);
      break;
    case ALTERNATE:
      codigoVecino.add(CodigoSequia.Codigo.ALTERN);
      break;
    case SATELLITAL:
      codigoVecino.add(CodigoSequia.Codigo.SATEL);
      break;
    }
  }

  protected final List<DatosSequiaEnEstacionVecina> obtenerVecinos(Estacion estacionBase, Sequia sequia) {
    List<Estacion> estacionesVecinas = ClasificadorSequiasHelper.dameEstacionesVecinasCercanas(estacionBase,
        UMBRAL_DISTANCIA_FISICA_VECINOS_SIN_CORRELACION);
    return this.obtenerVecinos(estacionBase, sequia, estacionesVecinas);
  }

  protected final List<DatosSequiaEnEstacionVecina> obtenerVecinosSatelitales(Estacion estacionBase, Sequia sequia) {
    List<EstacionSatelital> estacionesSatelitales = ClasificadorSequiasHelper.dameEstacionesSatelitalesCercanas(estacionBase,
        UMBRAL_DISTANCIA_FISICA_VECINOS_SIN_CORRELACION);
    return this.obtenerVecinos(estacionBase, sequia, estacionesSatelitales);
  }

  protected abstract List<DatosSequiaEnEstacionVecina> obtenerVecinos(Estacion estacionBase, Sequia sequia,
      List<? extends Estacion> estacionesVecinas);
  
  private CodigoSequia calculoSequiaConEstacionesSatelitales(Session sess, Estacion estacion, 
      Sequia sequia, CodigoSequia.Codigo codigoLocal) {
    // busco en las estaciones satelitales
    List<DatosSequiaEnEstacionVecina> vecinosSatelitales = this.obtenerVecinosSatelitales(estacion, sequia);
    if (vecinosSatelitales.isEmpty()) {
      return null;
    } else {
      // calculo con vecinos satelitales
      return this.calculoConVecinos(sess, estacion, sequia, codigoLocal, vecinosSatelitales, NeighborType.SATELLITAL);
    }
  }

}
