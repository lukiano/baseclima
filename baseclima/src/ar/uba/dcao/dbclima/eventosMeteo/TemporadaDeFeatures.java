package ar.uba.dcao.dbclima.eventosMeteo;

import java.util.Calendar;
import java.util.Date;

public class TemporadaDeFeatures {

  private static final long MILLIS_IN_DAY = 1000 * 3600 * 24;

  private Integer[] valoresRegistros;

  private Integer cantidadRegistros;

  private final int anio;

  private final Date fechaComienzo;

  TemporadaDeFeatures(DefinicionTemporadas serieTemporadas, int anio) {
    /* XXX: La long de la serie deberia ser 365 para los anios no bisiestos, se esta muy generico. */
    this.valoresRegistros = new Integer[serieTemporadas.getDuracionTemporadas()];
    this.anio = anio;

    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, anio);
    cal.set(Calendar.DAY_OF_YEAR, serieTemporadas.getPrimerDiaTemporada());
    this.fechaComienzo = cal.getTime();
  }

  public double getCoberturaPeriodo(Date fechaLimite, boolean fechaEsTopeSuperior) {
    int posLimite = this.getPosicionFecha(fechaLimite);
    return fechaEsTopeSuperior ? getCoberturaEnPeriodo(0, posLimite) : getCoberturaEnPeriodo(posLimite,
        valoresRegistros.length);
  }

  public int getRegistrosPeriodo(Date fechaLimite, boolean fechaEsTopeSuperior) {
    int posLimite = this.getPosicionFecha(fechaLimite);
    return fechaEsTopeSuperior ? getRegistrosEnRango(0, posLimite) : getRegistrosEnRango(posLimite,
        valoresRegistros.length);
  }

  public int getRegistrosEnRango(int indiceStart, int indiceEnd) {
    int valoresPresentes = 0;
    for (int i = indiceStart; i < indiceEnd; i++) {
      if (this.valoresRegistros[i] != null) {
        valoresPresentes++;
      }
    }

    return valoresPresentes;
  }

  public double getCoberturaEnPeriodo(int indiceStart, int indiceEnd) {
    return getRegistrosEnRango(indiceStart, indiceEnd) / (double) (indiceEnd - indiceStart);
  }

  public Integer[] getValoresRegistros() {
    return valoresRegistros;
  }

  public int getAnio() {
    return this.anio;
  }

  public int getCantidadRegistrosTemporada() {
    if (this.cantidadRegistros == null) {
      this.cantidadRegistros = 0;

      for (Integer val : this.valoresRegistros) {
        if (val != null) {
          this.cantidadRegistros++;
        }
      }
    }

    return this.cantidadRegistros;
  }

  void addFeat(Date fecha, Integer valorRegistro) {
    int posicion = getPosicionFecha(fecha);
    this.valoresRegistros[posicion] = valorRegistro;
  }

  Date getFechaPosicion(int posicion) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(this.fechaComienzo);
    calendar.add(Calendar.DAY_OF_YEAR, posicion);
    return calendar.getTime();
  }

  private int getPosicionFecha(Date fecha) {
    long dayDiff = (fecha.getTime() - fechaComienzo.getTime()) / MILLIS_IN_DAY;
    if (0 > dayDiff && dayDiff >= this.valoresRegistros.length) {
      throw new IllegalArgumentException("La fecha " + fecha + " no pertenece a la temporada");
    }

    return (int) dayDiff;
  }
}
