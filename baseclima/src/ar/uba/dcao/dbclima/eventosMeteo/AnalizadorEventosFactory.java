package ar.uba.dcao.dbclima.eventosMeteo;

import java.util.HashMap;
import java.util.Map;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;

public class AnalizadorEventosFactory {

  private Map<Integer, TemporadaDeFeatures> featuresPorTemporada;
  private final DefinicionTemporadas definicionTemporadas;

  public AnalizadorEventosFactory(Estacion e, ProyectorRegistro feature, DefinicionTemporadas definicionTemporadas) {
    this.definicionTemporadas = definicionTemporadas;
    this.featuresPorTemporada = buildFeaturesPorTemporada(e, feature, definicionTemporadas);
  }

  public AnalizadorEventosEstacion createAnalizadorEventos(FiltroEvento filtroEvento) {
    Map<Integer, TemporadaDeEventos> eventosPorTemporada = new HashMap<Integer, TemporadaDeEventos>();
    for (Integer tempFeat : this.featuresPorTemporada.keySet()) {
      TemporadaDeFeatures feats = this.featuresPorTemporada.get(tempFeat);
      eventosPorTemporada.put(tempFeat, new TemporadaDeEventos(feats, filtroEvento));
    }

    return new AnalizadorEventosEstacion(this.featuresPorTemporada, eventosPorTemporada, this.definicionTemporadas);
  }

  private static Map<Integer, TemporadaDeFeatures> buildFeaturesPorTemporada(Estacion e,
      ProyectorRegistro feature, DefinicionTemporadas definicionTemporadas) {
    Map<Integer, TemporadaDeFeatures> rv = new HashMap<Integer, TemporadaDeFeatures>();

    for (RegistroDiario rd : e.getRegistros()) {
      Integer temporada = definicionTemporadas.getTemporada(rd.getFecha());

      if (temporada != null) {
        /* El registro pertenence a una temporada. */
        Integer valorRegistro = feature.getValor(rd);

        TemporadaDeFeatures temporadaFeat = rv.get(temporada);

        if (temporadaFeat == null) {
          temporadaFeat = new TemporadaDeFeatures(definicionTemporadas, temporada);
          rv.put(temporada, temporadaFeat);
        }

        temporadaFeat.addFeat(rd.getFecha(), valorRegistro);
      }
    }

    return rv;
  }
}
