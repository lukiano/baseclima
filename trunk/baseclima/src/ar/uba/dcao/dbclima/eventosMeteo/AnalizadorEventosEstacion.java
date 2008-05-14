package ar.uba.dcao.dbclima.eventosMeteo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.uba.dcao.dbclima.utils.CollectionUtils;

public class AnalizadorEventosEstacion {

  private Map<Integer, TemporadaDeFeatures> featuresPorTemporada = new HashMap<Integer, TemporadaDeFeatures>();

  private Map<Integer, TemporadaDeEventos> eventosPorTemporada = new HashMap<Integer, TemporadaDeEventos>();

  private final DefinicionTemporadas definicionTemporadas;

  AnalizadorEventosEstacion(Map<Integer, TemporadaDeFeatures> featuresPorTemporada,
      Map<Integer, TemporadaDeEventos> eventosPorTemporada, DefinicionTemporadas definicionTemporadas) {
    this.featuresPorTemporada = featuresPorTemporada;
    this.eventosPorTemporada = eventosPorTemporada;
    this.definicionTemporadas = definicionTemporadas;
  }

  public Set<Integer> getTemporadasCubiertas() {
    return this.featuresPorTemporada.keySet();
  }

  public TemporadaDeFeatures getTemporadaFeatures(int temporada) {
    return this.featuresPorTemporada.get(temporada);
  }

  public TemporadaDeEventos getTemporadaEventos(int temporada) {
    return this.eventosPorTemporada.get(temporada);
  }

  public DefinicionTemporadas getTemporadaEventos(double probEvento, double soporteMinEvEx) {
    List<Integer> primerosEventos = new ArrayList<Integer>();
    List<Integer> ultimosEventos = new ArrayList<Integer>();

    for (TemporadaDeEventos evs : this.eventosPorTemporada.values()) {
      EventoExtremo primerEvento = evs.getPrimerEvento();
      EventoExtremo ultimoEvento = evs.getUltimoEvento();

      if (primerEvento != null && primerEvento.getSoporte() > soporteMinEvEx) {
        primerosEventos.add(primerEvento.getDiaDelAnio());
      }

      if (ultimoEvento != null && ultimoEvento.getSoporte() > soporteMinEvEx) {
        ultimosEventos.add(ultimoEvento.getDiaDelAnio());
      }
    }

    DefinicionTemporadas definicionTemporada;

    if (primerosEventos.size() > 0 && ultimosEventos.size() > 0) {
      Comparator<Integer> comparadorDia = this.definicionTemporadas.getComparadorDia();
      Collections.sort(primerosEventos, comparadorDia);
      Collections.sort(ultimosEventos, comparadorDia);

      Integer prEv = CollectionUtils.percentilOrderedList(primerosEventos, probEvento);
      Integer ulEv = CollectionUtils.percentilOrderedList(ultimosEventos, 1 - probEvento);

      definicionTemporada = new DefinicionTemporadas(prEv, ulEv);
    } else {
      definicionTemporada = null;
    }

    return definicionTemporada;
  }
}
