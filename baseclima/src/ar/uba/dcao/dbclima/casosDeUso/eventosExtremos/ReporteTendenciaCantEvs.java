package ar.uba.dcao.dbclima.casosDeUso.eventosExtremos;

import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosEstacion;
import ar.uba.dcao.dbclima.eventosMeteo.DefinicionTemporadas;
import ar.uba.dcao.dbclima.eventosMeteo.FiltroEvento;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeEventos;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeFeatures;

public class ReporteTendenciaCantEvs extends ReporteTendenciaEventos {

  public static final int MODO_REPORTE_CANT_DIAS = 0;

  public static final int MODO_REPORTE_CANT_EVENTOS = 1;

  public static final int MODO_REPORTE_PROP_EVENTOS = 2;

  private static final double MIN_SOPORTE_EVEXT = 0.65d;

  private final int modoReporte;

  
  public ReporteTendenciaCantEvs(int modoReporte, DefinicionTemporadas temporada, ProyectorRegistro proyector, FiltroEvento filtro) {
    super(temporada, proyector, filtro, "");
    this.modoReporte = modoReporte;
  }

  @Override
  protected Double getValorEstacionAnio(AnalizadorEventosEstacion analizador, int anio) {

    TemporadaDeEventos eventos = analizador.getTemporadaEventos(anio);
    TemporadaDeFeatures features = analizador.getTemporadaFeatures(anio);
    int cantRegs = features == null ? 0 : features.getCantidadRegistrosTemporada();
    int cantEvs = eventos == null ? 0 : eventos.getCantidadEventos();

    Double rv;
    if (this.modoReporte == MODO_REPORTE_CANT_DIAS) {
      rv = (double) cantRegs;

    } else if (this.modoReporte == MODO_REPORTE_CANT_EVENTOS) {
      rv = (double) cantEvs;

    } else if (this.modoReporte == MODO_REPORTE_PROP_EVENTOS) {
      rv = cantRegs > 366 * MIN_SOPORTE_EVEXT ? (cantEvs / (double) cantRegs) : null;

    } else {
      throw new IllegalStateException("Modo de reporte no se reconoce.");
    }

    return rv;
  }
}
