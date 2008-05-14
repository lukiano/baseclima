package ar.uba.dcao.dbclima.eventosMeteo;

import java.util.Date;

public class TemporadaDeEventos {

  private final TemporadaDeFeatures features;

  private final boolean[] eventos;

  private final int cantidadEventos;

  private EventoExtremo primerEvento;

  private EventoExtremo ultimoEvento;

  TemporadaDeEventos(TemporadaDeFeatures features, FiltroEvento filtroEvento) {
    this.features = features;
    int cantEventos = 0;
    Integer[] valoresRegistros = features.getValoresRegistros();

    /* Se computa el vector de eventos y la cantidad total. */
    this.eventos = new boolean[valoresRegistros.length];
    for (int i = 0; i < valoresRegistros.length; i++) {
      this.eventos[i] = filtroEvento.isEvento(valoresRegistros[i]);
      cantEventos += this.eventos[i] ? 1 : 0;
    }
    this.cantidadEventos = cantEventos;

    /* Se computan los eventos extremos (primero y ultimo). */
    this.computarExtremos();
  }

  private void computarExtremos() {
    Integer primerEv = null;
    Integer ultimoEv = null;

    for (int i = 0; i < this.eventos.length; i++) {
      if (this.eventos[i]) {
        if (primerEv == null) {
          primerEv = i;
        }
        ultimoEv = i;
      }
    }

    if (primerEv != null && ultimoEv != null) {
      Date fechaPrEv = this.features.getFechaPosicion(primerEv);
      int regsPrEv = this.features.getRegistrosPeriodo(fechaPrEv, true);

      Date fechaUlEv = this.features.getFechaPosicion(ultimoEv);
      int regsUlEv = this.features.getRegistrosPeriodo(fechaUlEv, false);

      this.primerEvento = new EventoExtremo(fechaPrEv, regsPrEv, primerEv, true);
      this.ultimoEvento = new EventoExtremo(fechaUlEv, regsUlEv, this.eventos.length - ultimoEv, false);
    }
  }

  public EventoExtremo getPrimerEvento() {
    return primerEvento;
  }

  public EventoExtremo getUltimoEvento() {
    return ultimoEvento;
  }

  public boolean[] getEventos() {
    return eventos;
  }

  public int getCantidadEventos() {
    return cantidadEventos;
  }

  public TemporadaDeFeatures getFeatures() {
    return features;
  }
}
