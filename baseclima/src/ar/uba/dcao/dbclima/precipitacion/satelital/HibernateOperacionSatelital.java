package ar.uba.dcao.dbclima.precipitacion.satelital;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ar.uba.dcao.dbclima.data.PuntoSatelital;
import ar.uba.dcao.dbclima.data.RegistroSatelital;
import ar.uba.dcao.dbclima.estacion.EstacionSatelital;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.precipitacion.PrecipitacionHelper;
import ar.uba.dcao.dbclima.utils.FechaHelper;

class HibernateOperacionSatelital implements OperacionSatelital {
  
  public static final int GRADOS_DESVIACION = PrecipitacionHelper.ajustarGrados(360d);

  @SuppressWarnings("unchecked")
  public Map<Integer, Integer> calcularPeriodo(EstacionSatelital estacion, int mesComienzo, int diaComienzo,
      int mesFin, int diaFin, int anioComienzo, int rangoAnios, boolean excluirAnioCentral, int deltaAnio) {
    PuntoSatelital puntoSatelital = this.damePuntoSatelital(estacion);

    Map<Integer, Integer> mapa = new HashMap<Integer, Integer>(); // aqui se van acumulando los
                                                                  // valores para cada anio (anio es
                                                                  // la clave)

    int[] anios = this.dameAnios(puntoSatelital, anioComienzo, rangoAnios, deltaAnio);

    for (int anio : anios) {
      if (excluirAnioCentral && anio == anioComienzo + deltaAnio) {
        continue;
      }

      List<RegistroSatelital> registros;
      {
        Date comienzo = Helper.dameFecha(diaComienzo, mesComienzo, anio);
        Date fin = Helper.dameFecha(diaFin, mesFin, anio);
        Session sess = DBSessionFactory.getInstance().getCurrentSession();
        Transaction transaction = sess.getTransaction();
        if (transaction != null && transaction.isActive()) {
          transaction = null;
        } else {
          transaction = sess.beginTransaction();
        }

        Query querySatelites = sess
            .createQuery("FROM RegistroSatelital WHERE puntoSatelital = ? AND fecha >= ? AND fecha <= ?");
        querySatelites = querySatelites.setParameter(0, puntoSatelital);
        querySatelites = querySatelites.setDate(1, comienzo).setDate(2, fin);
        // System.out.println("Obteniendo registros satelitales para punto " + puntoSatelital + "con
        // fechas de " + comienzo + " a " + fin);
        registros = querySatelites.list();
        if (transaction != null) {
          transaction.commit();
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

      for (int i = 0; i < registros.size(); i++) {
        RegistroSatelital registro = registros.get(i);

        int mm = registro.getLluvia();
        Date comienzo = registro.getFecha();
        Date fin;

        if (i < (registros.size() - 1)) {
          RegistroSatelital proxRegistro = registros.get(i + 1);
          fin = proxRegistro.getFecha();
        } else {
          outliers += FechaHelper.dameDifereciaDeDias(comienzo, Helper.dameFecha(comienzo, diaFin, mesFin));
          continue;
        }

        if (i == 0 && Helper.comparaFecha(comienzo, diaComienzo, mesComienzo) == 1) {
          outliers += FechaHelper
              .dameDifereciaDeDias(Helper.dameFecha(comienzo, diaComienzo, mesComienzo), comienzo);
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
  }

  @SuppressWarnings("unchecked")
  public List<EstacionSatelital> dameEstacionesSatelitalesCercanas(int estacionBaseLatitud, int estacionBaseLongitud,
      double distanciaMaximaEnGrados) {
    
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    Transaction transaction = sess.getTransaction();
    if (transaction != null && transaction.isActive()) {
      transaction = null;
    } else {
      transaction = sess.beginTransaction();
    }
  
    boolean desviacionEnGrados = false;
    if (estacionBaseLongitud < 0) {
      boolean satelitesConLongitudDe0a360 = satelitesConLongitudDe0a360(sess);
      if (satelitesConLongitudDe0a360) {
        estacionBaseLongitud += GRADOS_DESVIACION;
        desviacionEnGrados = true;
      }
    }
  
    Query querySatelites = sess
        .createQuery("FROM PuntoSatelital WHERE ((latitud - ?) * (latitud - ?) + (longitud - ?) * (longitud - ?)) <= ?)");
    querySatelites = querySatelites.setInteger(0, estacionBaseLatitud).setInteger(1, estacionBaseLatitud);
    querySatelites = querySatelites.setInteger(2, estacionBaseLongitud).setInteger(3, estacionBaseLongitud);
    int umbralDistancia = PrecipitacionHelper.ajustarGrados(distanciaMaximaEnGrados);
    int umbralDistanciaCuadrado = umbralDistancia * umbralDistancia; 
    querySatelites = querySatelites.setInteger(4, umbralDistanciaCuadrado);
    List<PuntoSatelital> puntosSatelitales = querySatelites.list();
    List<EstacionSatelital> estacionesSatelitales = new ArrayList<EstacionSatelital>(puntosSatelitales.size());
    for (PuntoSatelital satelite : puntosSatelitales) {
      int sateliteLatitud = satelite.getLatitud().intValue();
      int sateliteLongitud = satelite.getLongitud().intValue();
      if (desviacionEnGrados) {
        sateliteLongitud -= GRADOS_DESVIACION;
      }
      EstacionSatelital estacionSatelital = new EstacionSatelital(satelite.getId(), sateliteLatitud,
          sateliteLongitud);
      estacionesSatelitales.add(estacionSatelital);
    }
  
    if (transaction != null) {
      transaction.commit();
    }
    if(estacionesSatelitales.isEmpty()) {
      System.out.println("Sin estaciones satelitales cercanas!");
    }
    return estacionesSatelitales;
  }

  private int[] dameAnios(PuntoSatelital puntoSatelital, int anioComienzo, int rangoAnios, int deltaAnio) {
    int inicio = FechaHelper.dameAnio(puntoSatelital.getFechaInicio());
    int fin = FechaHelper.dameAnio(puntoSatelital.getFechaFin()) + deltaAnio;
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


  private PuntoSatelital damePuntoSatelital(EstacionSatelital estacion) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    Transaction transaction = sess.getTransaction();
    if (transaction != null && transaction.isActive()) {
      transaction = null;
    } else {
      transaction = sess.beginTransaction();
    }
    Query querySatelites = sess.createQuery("FROM PuntoSatelital WHERE id = ?");
    querySatelites = querySatelites.setLong(0, estacion.getId());
    PuntoSatelital puntoSatelital = (PuntoSatelital) querySatelites.uniqueResult();

    if (transaction != null) {
      transaction.commit();
    }
    return puntoSatelital;
  }

  /**
   * Devuelve TRUE si los puntos satelitales en la base de datos tienen sus longitudes entre 0 y 360 grados.
   * (La otra opcion es que esten entre -180 y 180).
   */
  private boolean satelitesConLongitudDe0a360(Session session) {
    Query querySatelites = session
        .createQuery("SELECT count(*) FROM PuntoSatelital WHERE longitud < 0");
    Number resultado = (Number) querySatelites.uniqueResult();
    return resultado.intValue() == 0;
  }


}
