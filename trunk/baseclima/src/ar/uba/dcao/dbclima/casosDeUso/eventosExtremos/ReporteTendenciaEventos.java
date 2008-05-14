package ar.uba.dcao.dbclima.casosDeUso.eventosExtremos;

import java.util.Calendar;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosEstacion;
import ar.uba.dcao.dbclima.eventosMeteo.DefinicionTemporadas;
import ar.uba.dcao.dbclima.eventosMeteo.FiltroEvento;

public abstract class ReporteTendenciaEventos extends ReporteEventosExtremos {

  private final String valorAnioNoOperado;

 
  public ReporteTendenciaEventos(DefinicionTemporadas temporada, ProyectorRegistro proyector, FiltroEvento filtro, String valorAnioNoOperado) {
    super(temporada, proyector, filtro);
    this.valorAnioNoOperado = valorAnioNoOperado;
  }

  @Override
  protected String printEstacionByAnalizador(AnalizadorEventosEstacion analizador, Estacion e,
      DefinicionTemporadas defTemporada) {
    String rv = e.getId() + "," + e.getAltura() + "," + e.getLatitud() + "," + e.getLongitud();

    CAL.setTime(e.getFechaInicio());
    int primerAnioEstacion = CAL.get(Calendar.YEAR);

    CAL.setTime(e.getFechaFin());
    int ultimoAnioEstacion = CAL.get(Calendar.YEAR);

    for (int i = PRIMER_ANIO_EVALUADO; i <= ULTIMO_ANIO_EVALUADO; i++) {
      if (this.valorAnioNoOperado != null && (primerAnioEstacion > i || ultimoAnioEstacion < i)) {
        rv += "," + this.valorAnioNoOperado;

      } else {
        rv += "," + this.getValorEstacionAnio(analizador, i);
      }
    }

    return rv;
  }

  @Override
  protected String printHeader() {
    return "Est,Altura,Lat,Lon," + HEADER_ANIOS;
  }

  protected abstract Double getValorEstacionAnio(AnalizadorEventosEstacion analizador, int anio);
}
