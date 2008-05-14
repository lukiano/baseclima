package ar.uba.dcao.dbclima.precipitacion.satelital;

import java.util.List;
import java.util.Map;

import ar.uba.dcao.dbclima.estacion.EstacionSatelital;

public interface OperacionSatelital {

  List<EstacionSatelital> dameEstacionesSatelitalesCercanas(int latitud, int longitud,
      double distanciaMaximaEnGrados);

  Map<Integer, Integer> calcularPeriodo(EstacionSatelital estacion, int mesComienzo, int diaComienzo, int mesFin,
      int diaFin, int anioComienzo, int rangoAnios, boolean excluirAnioCentral, int deltaAnio);

}
