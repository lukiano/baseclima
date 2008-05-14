package ar.uba.dcao.dbclima.casosDeUso.eventosExtremos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosEstacion;
import ar.uba.dcao.dbclima.eventosMeteo.DefinicionTemporadas;
import ar.uba.dcao.dbclima.eventosMeteo.EventoExtremo;
import ar.uba.dcao.dbclima.eventosMeteo.FiltroEvento;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeEventos;
import ar.uba.dcao.dbclima.utils.CollectionUtils;
import ar.uba.dcao.dbclima.utils.FechaHelper;

public class ReporteDistribucionEventoExtremo extends ReporteEventosExtremos {

  private static final double MIN_SOPORTE_EVEXT = 0.65d;

  public ReporteDistribucionEventoExtremo(DefinicionTemporadas temporada, ProyectorRegistro proyector,
      FiltroEvento filtro) {
    super(temporada, proyector, filtro);
  }

  @Override
  protected String printEstacionByAnalizador(AnalizadorEventosEstacion analizador, Estacion e,
      DefinicionTemporadas temporadaDef) {
    List<Double> primerosEventos = new ArrayList<Double>();
    List<Double> ultimosEventos = new ArrayList<Double>();

    int temporadasConEvs = 0;
    int totalEventos = 0;

    for (Integer anioTemporada : analizador.getTemporadasCubiertas()) {
      TemporadaDeEventos evsTemporada = analizador.getTemporadaEventos(anioTemporada);
      EventoExtremo primerEv = evsTemporada.getPrimerEvento();
      EventoExtremo ultimoEv = evsTemporada.getUltimoEvento();

      totalEventos += evsTemporada.getCantidadEventos();

      boolean temporadaTieneEvEx = false;
      if (primerEv != null && primerEv.getSoporte() > MIN_SOPORTE_EVEXT) {
        primerosEventos.add((double) primerEv.getDiaDelAnio());
        temporadaTieneEvEx = true;
      }
      if (ultimoEv != null && ultimoEv.getSoporte() > MIN_SOPORTE_EVEXT) {
        ultimosEventos.add((double) ultimoEv.getDiaDelAnio());
        temporadaTieneEvEx = true;
      }

      if (temporadaTieneEvEx) {
        temporadasConEvs++;
      }
    }

    Collections.sort(primerosEventos, temporadaDef.getComparadorDiaAsDouble());
    Collections.sort(ultimosEventos, temporadaDef.getComparadorDiaAsDouble());

    int aniosEstacion = FechaHelper.dameAnio(e.getFechaFin()) - FechaHelper.dameAnio(e.getFechaInicio()) + 1;

    String probEventoPorFecha = "";
    for (int diaAnio = 20; diaAnio < 366; diaAnio += 20) {
      Double probEventoEnDia = probEventoEnDiaAnio(diaAnio, primerosEventos, ultimosEventos);
      probEventoPorFecha += "," + (probEventoEnDia == null ? null : FRMT_D.format(probEventoEnDia));
    }

    String descEst = "" + e.getId() + "," + e.getAltura() + "," + e.getLatitud() + "," + e.getLongitud() + ","
        + temporadasConEvs + "," + aniosEstacion + ","
        + totalEventos + "," + procesarEvs(primerosEventos) + ","
        + procesarEvs(ultimosEventos) + probEventoPorFecha;

    return descEst;
  }

  @Override
  protected String printHeader() {
    String probEventoPorFecha = "";
    for (int diaAnio = 20; diaAnio < 366; diaAnio += 20) {
      probEventoPorFecha += ",P@" + diaAnio;
    }

    return "ID Est,Altura,Lat,Lon,Temps c/evs,Temps Estacion,Evs,p10 1er Ev,p50 1er Ev,p90 1er Ev,"
        + "p10 ult Ev,p50 ult Ev,p90 ult Ev" + probEventoPorFecha;
  }

  private static Double probEventoEnDiaAnio(int diaAnio, List<Double> primerosEventos, List<Double> ultimosEventos) {

    for (double perc = 1d; perc >= 0; perc -= 0.04d) {
      Double prEv = CollectionUtils.percentilOrderedList(primerosEventos, perc);
      Double ulEv = CollectionUtils.percentilOrderedList(ultimosEventos, 1 - perc);

      if (prEv == null || ulEv == null) {
        return null;
      } else if (prEv < diaAnio && diaAnio < ulEv) {
        return perc;
      }
    }

    return 0d;
  }

  private static String procesarEvs(List<Double> diasAniosEvs) {
    Double p10 = CollectionUtils.percentilOrderedList(diasAniosEvs, 0.1d);
    Double p50 = CollectionUtils.percentilOrderedList(diasAniosEvs, 0.5d);
    Double p90 = CollectionUtils.percentilOrderedList(diasAniosEvs, 0.9d);

    return "" + p10 + "," + p50 + "," + p90;
  }
}
