package ar.uba.dcao.dbclima.casosDeUso.eventosExtremos;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ar.uba.dcao.dbclima.casosDeUso.ReportePorEstacion;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosEstacion;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosFactory;
import ar.uba.dcao.dbclima.eventosMeteo.DefinicionTemporadas;
import ar.uba.dcao.dbclima.eventosMeteo.EventoExtremo;
import ar.uba.dcao.dbclima.eventosMeteo.FiltroEvento;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeEventos;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeFeatures;
import ar.uba.dcao.dbclima.utils.CollectionUtils;

public class Reporte25EstacionesCompletas extends ReportePorEstacion {

  protected static final int PRIMER_ANIO_EVALUADO = 1960;

  protected static final int ULTIMO_ANIO_EVALUADO = 1985;

  protected static final int MIN_TEMPORADAS = 23;

  protected static final double MIN_SOPORTE_TEMP = 0.85;

  protected static final double MIN_SOPORTE_EVEX = 0.65;

  protected static final Calendar CAL = Calendar.getInstance();

  protected static final DecimalFormat FRMT_D = new DecimalFormat("0.##");

  private final DefinicionTemporadas temporada;

  private final ProyectorRegistro proyector;

  private final FiltroEvento filtro;

  public Reporte25EstacionesCompletas(DefinicionTemporadas temporada, ProyectorRegistro proyector,
      FiltroEvento filtro) {
    this.temporada = temporada;
    this.proyector = proyector;
    this.filtro = filtro;
  }

  @Override
  protected String printEstacion(Estacion e) {
    AnalizadorEventosEstacion analizador = new AnalizadorEventosFactory(e, this.proyector, this.temporada)
        .createAnalizadorEventos(filtro);

    List<Double> primerosEventos = new ArrayList<Double>();
    List<Double> ultimosEventos = new ArrayList<Double>();
    List<Double> eventosPorAnio = new ArrayList<Double>();
    int aniosCubiertos = 0;

    for (int anio = PRIMER_ANIO_EVALUADO; anio <= ULTIMO_ANIO_EVALUADO; anio++) {
      TemporadaDeFeatures feats = analizador.getTemporadaFeatures(anio);
      TemporadaDeEventos evs = analizador.getTemporadaEventos(anio);

      if (feats == null) {
        continue;
      }

      /* Calculos cantidad eventos */
      double minDias = this.temporada.getDuracionTemporadas() * MIN_SOPORTE_TEMP;
      if (feats.getCantidadRegistrosTemporada() >= minDias) {
        eventosPorAnio.add((double) evs.getCantidadEventos());
        aniosCubiertos++;
      }

      /* Calculos eventos extremos */
      EventoExtremo primerEvento = evs.getPrimerEvento();
      EventoExtremo ultimoEvento = evs.getUltimoEvento();

      if (primerEvento != null && primerEvento.getSoporte() > MIN_SOPORTE_EVEX) {
        primerosEventos.add((double) primerEvento.getDiaDelAnio());
      }
      if (ultimoEvento != null && ultimoEvento.getSoporte() > MIN_SOPORTE_EVEX) {
        ultimosEventos.add((double) ultimoEvento.getDiaDelAnio());
      }
    }

    /* Reporte */
    String descEst;
    if (aniosCubiertos > MIN_TEMPORADAS) {
      Collections.sort(primerosEventos, this.temporada.getComparadorDiaAsDouble());
      Collections.sort(ultimosEventos, this.temporada.getComparadorDiaAsDouble());

      double cantEvsAvg = CollectionUtils.avg(eventosPorAnio);

      Double prEvp10 = CollectionUtils.percentilOrderedList(primerosEventos, 0.1);
      Double prEvpMn = CollectionUtils.avg(primerosEventos);
      Double prEvp90 = CollectionUtils.percentilOrderedList(primerosEventos, 0.9);
      Double ulEvp10 = CollectionUtils.percentilOrderedList(ultimosEventos, 0.1);
      Double ulEvpMn = CollectionUtils.avg(ultimosEventos);
      Double ulEvp90 = CollectionUtils.percentilOrderedList(ultimosEventos, 0.9);

      descEst = e.getId() + "," + e.getAltura() + "," + e.getLatitud() + "," + e.getLongitud() + ","
          + FRMT_D.format(cantEvsAvg);

      descEst += "," + FRMT_D.format(prEvp10) + "," + FRMT_D.format(prEvpMn) + "," + FRMT_D.format(prEvp90);
      descEst += "," + FRMT_D.format(ulEvp10) + "," + FRMT_D.format(ulEvpMn) + "," + FRMT_D.format(ulEvp90);

    } else {
      descEst = null;
    }

    return descEst;
  }

  @Override
  protected String printHeader() {
    return "ID,Altura,Lat,Lon,#Evs/Anio,PrEv p10,PrEv mean,PrEv p90,UlEv p10,UlEv mean,UlEv p90";
  }

}
