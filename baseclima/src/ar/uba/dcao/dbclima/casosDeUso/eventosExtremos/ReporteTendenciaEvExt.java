package ar.uba.dcao.dbclima.casosDeUso.eventosExtremos;

import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosEstacion;
import ar.uba.dcao.dbclima.eventosMeteo.DefinicionTemporadas;
import ar.uba.dcao.dbclima.eventosMeteo.EventoExtremo;
import ar.uba.dcao.dbclima.eventosMeteo.FiltroEvento;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeEventos;

public class ReporteTendenciaEvExt extends ReporteTendenciaEventos {

  private static final double MIN_SOPORTE_EVEXT = 0.65d;

  private static final int MAX_PERIODO_IGNOR_SOPORTE = 10;

  private final boolean primerEvento;

  
  public ReporteTendenciaEvExt(DefinicionTemporadas temporada, ProyectorRegistro proyector, FiltroEvento filtro, boolean primerEvento) {
    super(temporada, proyector, filtro, "");
    this.primerEvento = primerEvento;
  }

  @Override
  protected Double getValorEstacionAnio(AnalizadorEventosEstacion analizador, int anio) {
    TemporadaDeEventos evs = analizador.getTemporadaEventos(anio);
    
    EventoExtremo evExt = this.primerEvento ? evs.getPrimerEvento() : evs.getUltimoEvento();

    Double rv;

    if (evExt == null
        || (evExt.getDiasUltraEvento() > MAX_PERIODO_IGNOR_SOPORTE && evExt.getSoporte() < MIN_SOPORTE_EVEXT)) {
      rv = null;

    } else {
      rv = (double) evExt.getDiaDelAnio();
    }

    return rv;
  }
}
