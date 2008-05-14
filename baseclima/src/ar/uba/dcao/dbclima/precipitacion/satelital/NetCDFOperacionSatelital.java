package ar.uba.dcao.dbclima.precipitacion.satelital;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ar.uba.dcao.dbclima.estacion.EstacionSatelital;
import ar.uba.dcao.dbclima.precipitacion.PrecipitacionHelper;
import ar.uba.dcao.dbclima.utils.FechaHelper;

class NetCDFOperacionSatelital implements OperacionSatelital {

  private enum Keys {
    starttime, time, timetype, precipitation, longitude, latitude, file;
  }
  
  private static final double GRADOS_DESVIACION = 360d;

  private static AtomicLong key = new AtomicLong(1);

  private Properties properties = new Properties();

  private static enum TimeTypes {
    Months, Days, Hours, Minutes, Seconds, Milliseconds
  };

  public NetCDFOperacionSatelital(Properties properties) throws IllegalArgumentException {
    this.checkProperties(properties);
    this.properties = properties;
  }

  private void checkProperties(Properties properties) throws IllegalArgumentException {
    for (Keys key : Keys.values()) {
      if (!properties.containsKey(key.name())) {
        throw new IllegalArgumentException("Property not found '" + key.name() + "'");
      }
    }
    String filename = properties.getProperty(Keys.file.name());
    try {
      NetcdfDataset.openDataset(filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error opening NetCDF", e);
    }
  }

  private long dameTiempoInicial() {
    DateTimeFormatter formatter = ISODateTimeFormat.basicDateTimeNoMillis();
    DateTime dateTimeStart = formatter.parseDateTime(properties.getProperty("starttime").trim());
    return dateTimeStart.getMillis();
  }

  public Map<Integer, Integer> calcularPeriodo(EstacionSatelital estacion, int mesComienzo, int diaComienzo,
      int mesFin, int diaFin, int anioComienzo, int rangoAnios, boolean excluirAnioCentral, int deltaAnio) {

    try {
      NetcdfDataset dataset = NetcdfDataset.openDataset(properties.getProperty(Keys.file.name()));
      try {
        double[] lats = this.getVariable(properties.getProperty(Keys.latitude.name()), dataset);
        double[] lons = this.getVariable(properties.getProperty(Keys.longitude.name()), dataset);

        long[] times = this.obtenerTiempos(dataset);

        double puntoLat = estacion.getLatitud() / 100d;
        double puntoLon = estacion.getLongitud() / 100d;

        if (estacion.getId() < 0) {
          puntoLon += GRADOS_DESVIACION;
        }

        int realLat = -1;
        for (int i = 0; i < lats.length; i++) {
          if (lats[i] == puntoLat) {
            realLat = i;
            break;
          }
        }
        
        int realLon = java.util.Arrays.binarySearch(lons, puntoLon);

        Variable variable = dataset.findVariable(properties.getProperty(Keys.precipitation.name()));
        variable = variable.slice(2, realLon).slice(1, realLat);
        Array varArray = variable.read();
        int[] precips = new int[times.length];
        IndexIterator indexIterator = varArray.getIndexIterator();
        for (int i = 0; i < precips.length; i++) {
          double next = indexIterator.getDoubleNext();
          if (Double.isNaN(next) || Double.isInfinite(next) || next < 0) {
            precips[i] = -1;
          } else {
            precips[i] = (int)Math.round(next * 100); // porque se espera que esten en centesimas de mm
          }
        }

        Map<Integer, Integer> mapa = new HashMap<Integer, Integer>(); // aqui se van acumulando los
        // valores para cada anio (anio es
        // la clave)

        int[] anios = this.dameAnios(times, anioComienzo, rangoAnios, deltaAnio);

        for (int anio : anios) {
          if (excluirAnioCentral && anio == anioComienzo + deltaAnio) {
            continue;
          }
          
          int comIndex = 0;
          int finIndex = times.length;
          {
            Date comienzo = Helper.dameFecha(diaComienzo, mesComienzo, anio);
            Date fin = Helper.dameFecha(diaFin, mesFin, anio);
            for (int i = 0; i < times.length; i++) {
              Date fecha = new Date(times[i]);
              if (fecha.after(comienzo)) {
                comIndex = i;
                break;
              }
            }
            for (int i = 0; i < times.length; i++) {
              Date fecha = new Date(times[i]);
              if (fecha.after(fin)) {
                finIndex = i==0?0:i-1;
                break;
              }
            }
          }


          int outliers = 0;
          int sumaDias = 0;
          anio = anio - deltaAnio;
          int valorActual;
          if (mapa.containsKey(anio)) {
            valorActual = mapa.get(anio);
          } else {
            valorActual = 0;
          }

          for (int i = comIndex; i < finIndex; i++) {
            int mm = precips[i];
            Date comienzo = new Date(times[i]);
            Date fin;

            if (i < (finIndex - 1)) {
              fin = new Date(times[i + 1]);
            } else {
              outliers += FechaHelper.dameDifereciaDeDias(comienzo, Helper.dameFecha(comienzo, diaFin, mesFin));
              continue;
            }

            if (i == comIndex && Helper.comparaFecha(comienzo, diaComienzo, mesComienzo) == 1) {
              outliers += FechaHelper.dameDifereciaDeDias(Helper.dameFecha(comienzo, diaComienzo, mesComienzo),
                  comienzo);
            }

            if (Helper.comparaFecha(comienzo, diaComienzo, mesComienzo) == -1) {
              comienzo = Helper.dameFecha(comienzo, diaComienzo, mesComienzo);
            }
            if (Helper.comparaFecha(comienzo, diaFin, mesFin) == 1) {
              continue;
            }
            if (Helper.comparaFecha(fin, diaFin, mesFin) == 1) {
              continue;
            }
            int difDias = FechaHelper.dameDifereciaDeDias(comienzo, fin);
            mm *= difDias;
            sumaDias += difDias;
            valorActual += mm;
          }
          if (sumaDias > 0) {
            valorActual += ((valorActual * outliers) / sumaDias);
          }

          mapa.put(anio, valorActual);

        }

        return mapa;
      } catch (InvalidRangeException e) {
        e.printStackTrace();
        return java.util.Collections.emptyMap();
      } finally {
        dataset.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
      return java.util.Collections.emptyMap();
    }
  }

  private int[] dameAnios(long[] times, int anioComienzo, int rangoAnios, int deltaAnio) {

    int inicio = FechaHelper.dameAnio(new Date(times[0]));
    int fin = FechaHelper.dameAnio(new Date(times[times.length - 1])) + deltaAnio;
    if (inicio < (anioComienzo - rangoAnios + deltaAnio)) {
      inicio = anioComienzo - rangoAnios + deltaAnio;
    }
    if (fin > (anioComienzo + rangoAnios + deltaAnio)) {
      fin = anioComienzo + rangoAnios + deltaAnio;
    }
    if (inicio > fin) {
      return new int[0];
    }
    int[] resultado = new int[fin - inicio + 1];
    for (int i = inicio; i <= fin; i++) {
      resultado[i - inicio] = i;
    }
    return resultado;
  }

  private long[] obtenerTiempos(NetcdfDataset dataset) throws IOException {

    TimeTypes timeType = TimeTypes.valueOf(properties.getProperty(Keys.timetype.name()).trim());
    final long multiplicador;
    switch (timeType) {
    case Months:
      multiplicador = 30 * 24 * 60 * 60 * 1000;
      break;
    case Days:
      multiplicador = 24 * 60 * 60 * 1000;
      break;
    case Hours:
      multiplicador = 60 * 60 * 1000;
      break;
    case Minutes:
      multiplicador = 60 * 1000;
      break;
    case Seconds:
      multiplicador = 1000;
      break;
    case Milliseconds:
      multiplicador = 1;
      break;
    default:
      multiplicador = 1;
      break;
    }
    final long tiempoInicial = this.dameTiempoInicial();
    Variable variable = dataset.findVariable(properties.getProperty(Keys.time.name()));
    Array varArray = variable.read();
    int size = (int) varArray.getSize();

    long[] ret = new long[size];
    IndexIterator indexIterator = varArray.getIndexIterator();
    for (int i = 0; i < ret.length; i++) {
      long next = indexIterator.getLongNext();
      next = multiplicador * next;
      next += tiempoInicial;
      ret[i] = next;
    }

    return ret;
  }

  private Pair[] damePuntosCercanos(Pair pair, double[] lat, double[] lon, double distanciaMaximaEnGrados) {
    ArrayList<Pair> pairs = new ArrayList<Pair>();
    for (int i = 0; i < lat.length; i++) {
      for (int j = 0; j < lon.length; j++) {
        double dist = Math.sqrt((lat[i] - pair.lat) * (lat[i] - pair.lat) + (lon[j] - pair.lon) * (lon[j] - pair.lon));
        if (dist <= distanciaMaximaEnGrados) {
          Pair p = new Pair(lat[i], lon[j]);
          p.indexLan = i;
          p.indexLon = j;
          pairs.add(p);
        }

      }
    }
    return pairs.toArray(new Pair[pairs.size()]);
  }

  public List<EstacionSatelital> dameEstacionesSatelitalesCercanas(int latitud, int longitud,
      double distanciaMaximaEnGrados) {

    double baseLat = latitud / 100d;
    double baseLon = longitud / 100d;
    boolean desviacionEnGrados = false;
    if (baseLon < 0) {
      baseLon += GRADOS_DESVIACION;
      desviacionEnGrados = true;
    }

    try {
      NetcdfDataset dataset = NetcdfDataset.openDataset(properties.getProperty(Keys.file.name()));
      try {
        double[] lats = this.getVariable(properties.getProperty(Keys.latitude.name()), dataset);
        double[] lons = this.getVariable(properties.getProperty(Keys.longitude.name()), dataset);
        Pair[] puntos = this.damePuntosCercanos(new Pair(baseLat, baseLon), lats, lons, distanciaMaximaEnGrados);
        List<EstacionSatelital> estaciones = new ArrayList<EstacionSatelital>(puntos.length);
        for (Pair punto : puntos) {
          double puntoLat = punto.lat;
          double puntoLon = punto.lon;
          Long id = this.dameId();
          if (desviacionEnGrados) {
            puntoLon -= GRADOS_DESVIACION;
            id = -id;
          }
          EstacionSatelital estacionSatelital = new EstacionSatelital(id, PrecipitacionHelper.ajustarGrados(puntoLat),
              PrecipitacionHelper.ajustarGrados(puntoLon));
          estaciones.add(estacionSatelital);
        }

        return estaciones;

      } finally {
        dataset.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
      return java.util.Collections.emptyList();
    }

  }

  private synchronized Long dameId() {
    return key.incrementAndGet();
  }

  private double[] getVariable(String variableName, NetcdfDataset dataset) throws IOException {
    Variable variable = dataset.findVariable(variableName);
    Array varArray = variable.read();
    double var[] = new double[(int) varArray.getSize()];
    IndexIterator indexIterator = varArray.getIndexIterator();
    for (int i = 0; i < var.length; i++) {
      var[i] = indexIterator.getDoubleNext();
    }
    return var;
  }

}
