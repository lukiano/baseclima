package ar.uba.dcao.dbclima.precipitacion.rango;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.LRUMap;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.estacion.EstacionSatelital;
import ar.uba.dcao.dbclima.precipitacion.PrecipitacionHelper;
import ar.uba.dcao.dbclima.precipitacion.satelital.OperacionSatelital;
import ar.uba.dcao.dbclima.precipitacion.satelital.OperacionSatelitalFactory;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Clase de funcionamiento similar a la que hereda, pero ademas tiene soporte para registros
 * satelitales. (Los mismos son representados por la clase EstacionSatelital, que hereda de
 * Estacion).
 * 
 * @See EstacionSatelital.
 * 
 */
public class PromedioPrecipitacionAnualConSatelitesProyectorRango extends PromedioPrecipitacionAnualProyectorRango {

  private static Map<CacheEntry, Map<Integer, Integer>> cache = new LRUMap<CacheEntry, Map<Integer, Integer>>(1024);

  private static class CacheEntry {

    public Long id;

    public int mesComienzo;

    public int diaComienzo;

    public int mesFin;

    public int diaFin;

    public int deltaAnio;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + deltaAnio;
      result = prime * result + diaComienzo;
      result = prime * result + diaFin;
      result = prime * result + mesComienzo;
      result = prime * result + mesFin;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final CacheEntry other = (CacheEntry) obj;
      if (deltaAnio != other.deltaAnio)
        return false;
      if (diaComienzo != other.diaComienzo)
        return false;
      if (diaFin != other.diaFin)
        return false;
      if (mesComienzo != other.mesComienzo)
        return false;
      if (mesFin != other.mesFin)
        return false;
      if (id == null) {
        if (other.id != null)
          return false;
      } else if (!id.equals(other.id))
        return false;
      return true;
    }

  }

  public PromedioPrecipitacionAnualConSatelitesProyectorRango(Date comienzo, Date fin) {
    super(comienzo, fin);
  }

  @Override
  public List<Rango> proyectarRangos(Estacion estacion) {
    if (estacion instanceof EstacionSatelital) {
      return this.proyectarRangoSatelital((EstacionSatelital) estacion);
    } else {
      return super.proyectarRangos(estacion);
    }
  }

  private List<Rango> proyectarRangoSatelital(EstacionSatelital estacion) {

    List<Rango> rangos = new ArrayList<Rango>();
    int diferenciaDias;
    {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.DAY_OF_MONTH, this.diaComienzo);
      calendar.set(Calendar.MONTH, this.mesComienzo - 1);
      calendar.set(Calendar.YEAR, this.anioComienzo);
      Date comienzo = calendar.getTime();
      calendar.set(Calendar.DAY_OF_MONTH, this.diaFin);
      calendar.set(Calendar.MONTH, this.mesFin - 1);
      calendar.set(Calendar.YEAR, this.anioComienzo + this.sumaAnioFin);
      Date fin = calendar.getTime();
      diferenciaDias = FechaHelper.dameDifereciaDeDias(comienzo, fin);
    }

    if (this.sumaAnioFin == 0) { // es en el mismo anio

      Map<Integer, Integer> mapa = this.calcularPeriodo(estacion, this.mesComienzo, this.diaComienzo, this.mesFin,
          this.diaFin, 0);

      for (Map.Entry<Integer, Integer> entrada : mapa.entrySet()) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.DAY_OF_MONTH, this.diaComienzo);
        calendar.set(Calendar.MONTH, this.mesComienzo - 1);
        calendar.set(Calendar.YEAR, entrada.getKey().intValue());
        Date comienzo = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, this.diaFin);
        calendar.set(Calendar.MONTH, this.mesFin - 1);
        calendar.set(Calendar.YEAR, entrada.getKey().intValue());
        Date fin = calendar.getTime();

        double valor = entrada.getValue().doubleValue() / diferenciaDias;
        valor = PrecipitacionHelper.ajustarPrecipitacionSatelital(valor);

        Rango rango = new RangoImpl(comienzo, fin, nombre(), valor);
        rangos.add(rango);
      }

    } else { // se reparte en mas de un anio

      Map<Integer, Integer> mapaGlobal = new HashMap<Integer, Integer>();
      int diferenciaMeses = 0;

      {
        Map<Integer, Integer> mapa = this.calcularPeriodo(estacion, this.mesComienzo, this.diaComienzo, 12, 31, 0);
        this.agregarEntradasAlMapaGlobal(mapaGlobal, mapa);
        diferenciaMeses += (12 - this.mesComienzo + 1);
      }
      for (int i = 1; i < this.sumaAnioFin; i++) {
        Map<Integer, Integer> mapa = this.calcularPeriodo(estacion, 1, 1, 12, 31, i);
        this.agregarEntradasAlMapaGlobal(mapaGlobal, mapa);
        diferenciaMeses += 12;
      }
      {
        Map<Integer, Integer> mapa = this.calcularPeriodo(estacion, 1, 1, this.mesFin, this.diaFin,
            this.sumaAnioFin);
        this.agregarEntradasAlMapaGlobal(mapaGlobal, mapa);
        diferenciaMeses += this.mesFin;
      }

      for (Map.Entry<Integer, Integer> entrada : mapaGlobal.entrySet()) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.DAY_OF_MONTH, this.diaComienzo);
        calendar.set(Calendar.MONTH, this.mesComienzo - 1);
        calendar.set(Calendar.YEAR, entrada.getKey().intValue());
        Date comienzo = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, this.diaFin);
        calendar.set(Calendar.MONTH, this.mesFin - 1);
        calendar.set(Calendar.YEAR, entrada.getKey().intValue());
        Date fin = calendar.getTime();

        double valor = entrada.getValue().doubleValue() / diferenciaDias;
        valor = PrecipitacionHelper.ajustarPrecipitacionSatelital(valor);

        Rango rango = new RangoImpl(comienzo, fin, nombre(), valor);
        rangos.add(rango);
      }

    }

    java.util.Collections.sort(rangos, new Comparator<Rango>() {

      public int compare(Rango r1, Rango r2) {
        return r1.comienzo().compareTo(r2.comienzo());
      }

    });

    return rangos;
  }

  private void agregarEntradasAlMapaGlobal(Map<Integer, Integer> mapaGlobal, Map<Integer, Integer> mapa) {
    for (Map.Entry<Integer, Integer> entrada : mapa.entrySet()) {
      int valor;
      if (mapaGlobal.containsKey(entrada.getKey())) {
        valor = mapaGlobal.get(entrada.getKey());
      } else {
        valor = 0;
      }
      mapaGlobal.put(entrada.getKey(), valor + entrada.getValue().intValue());
    }
  }

  private Map<Integer, Integer> calcularPeriodo(EstacionSatelital estacion, int mesComienzo, int diaComienzo,
      int mesFin, int diaFin, int deltaAnio) {

    CacheEntry cacheEntry = new CacheEntry();
    cacheEntry.id = estacion.getId();
    cacheEntry.mesComienzo = mesComienzo;
    cacheEntry.diaComienzo = diaComienzo;
    cacheEntry.mesFin = mesFin;
    cacheEntry.diaFin = diaFin;
    cacheEntry.deltaAnio = deltaAnio;

    Map<Integer, Integer> resultadoQuery;
    if (cache.containsKey(cacheEntry)) {
      // System.out.println("Obteniendo de cache promedio para estacion " + estacion + "con fechas
      // de " + comienzo + " a " + fin);
      resultadoQuery = cache.get(cacheEntry);
    } else {
      resultadoQuery = this.calcularPeriodo2(estacion, mesComienzo, diaComienzo, mesFin, diaFin, deltaAnio);
      cache.put(cacheEntry, resultadoQuery);
    }
    return resultadoQuery;
  }

  @SuppressWarnings("unchecked")
  private Map<Integer, Integer> calcularPeriodo2(EstacionSatelital estacion, int mesComienzo, int diaComienzo,
      int mesFin, int diaFin, int deltaAnio) {
    OperacionSatelital operacionSatelital = OperacionSatelitalFactory.getInstance();
    return operacionSatelital.calcularPeriodo(estacion, mesComienzo, diaComienzo, mesFin, diaFin,
        this.anioComienzo, this.rangoAnios, this.excluirAnioCentral, deltaAnio);
  }

  public static void clearCache() {
    cache.clear();
  }

}
