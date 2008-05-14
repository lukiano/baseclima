package ar.uba.dcao.dbclima.casosDeUso.eventosExtremos;

import java.io.IOException;
import java.io.OutputStreamWriter;

import ar.uba.dcao.dbclima.casosDeUso.ReportePorEstacion;
import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.eventosMeteo.DefinicionTemporadas;
import ar.uba.dcao.dbclima.eventosMeteo.FiltroEvento;
import ar.uba.dcao.dbclima.eventosMeteo.FiltrosEvento;
import ar.uba.dcao.dbclima.eventosMeteo.ProyectoresConfiables;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

public class GeneradorReportesBase {

  private static final byte MAX_DESCONFIANZA = ConfianzaVariable.LIMITROFE;
  
  private static final DefinicionTemporadas TEMPORADA_ANIO = new DefinicionTemporadas(1, 366);

  private static final ProyectorRegistro PROY_TMIN = ProyectoresConfiables
      .buildProyectorTMinConfiable(MAX_DESCONFIANZA);

  private static final FiltroEvento FILTRO_HELADA = FiltrosEvento.buildFiltroPorTope(-1, false, true);

  public static void main(String[] args) {
//    ReportePorEstacion reporte = new ReporteTendenciaEvExt(TEMPORADA_ANIO, PROY_TMIN,
//        FILTRO_HELADA, false);
    ReportePorEstacion reporte = new Reporte25EstacionesCompletas(TEMPORADA_ANIO, PROY_TMIN,
        FILTRO_HELADA);

    try {
      reporte.writeReport(new OutputStreamWriter(System.out), DBSessionFactory.getInstance());

    } catch (IOException e) {
      throw new IllegalStateException("Error al escribir el reporte.");
    }
  }
}
