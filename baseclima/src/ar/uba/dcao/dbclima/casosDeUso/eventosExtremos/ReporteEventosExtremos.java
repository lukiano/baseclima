package ar.uba.dcao.dbclima.casosDeUso.eventosExtremos;

import java.text.DecimalFormat;
import java.util.Calendar;

import ar.uba.dcao.dbclima.casosDeUso.ReportePorEstacion;
import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosEstacion;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosFactory;
import ar.uba.dcao.dbclima.eventosMeteo.DefinicionTemporadas;
import ar.uba.dcao.dbclima.eventosMeteo.FiltroEvento;

public abstract class ReporteEventosExtremos extends ReportePorEstacion {

  protected static final int PRIMER_ANIO_EVALUADO = 1959;

  protected static final int ULTIMO_ANIO_EVALUADO = 2006;

  protected static final Calendar CAL = Calendar.getInstance();

  protected static final DecimalFormat FRMT_D = new DecimalFormat("0.##");

  protected static final byte MAX_DESCONFIANZA = ConfianzaVariable.LIMITROFE;

  protected static final String HEADER_ANIOS = getHeaderAnios();

  private final DefinicionTemporadas temporada;

  private final ProyectorRegistro proyector;

  private final FiltroEvento filtro;

  private static String getHeaderAnios() {
    String rv = "";
    for (int i = PRIMER_ANIO_EVALUADO; i <= ULTIMO_ANIO_EVALUADO; i++) {
      rv += "," + i;
    }

    return rv.substring(1);
  }

  public ReporteEventosExtremos(DefinicionTemporadas temporada, ProyectorRegistro proyector, FiltroEvento filtro) {
    this.temporada = temporada;
    this.proyector = proyector;
    this.filtro = filtro;
  }

  @Override
  protected String printEstacion(Estacion estacion) {
    AnalizadorEventosEstacion analizador = new AnalizadorEventosFactory(estacion, this.proyector, this.temporada)
        .createAnalizadorEventos(this.filtro);
    return this.printEstacionByAnalizador(analizador, estacion, this.temporada);
  }

  protected abstract String printEstacionByAnalizador(AnalizadorEventosEstacion analizador, Estacion estacion,
      DefinicionTemporadas defTemporada);
}
