package ar.uba.dcao.dbclima.precipitacion.rango;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.LRUMap;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.precipitacion.PrecipitacionHelper;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Proyecta el promedio de precipitacion caida para cada anio de una estacion correspondido entre una fecha comienzo
 * (dia y mes) y una fecha fin determinadas.
 * <P>El anio que figura en la fecha comienzo se toma como anio central. Existe una opcion para excluir el mismo.
 * <P>Tambien se puede especificar que se tomen solamente los anios en una determinada distancia al anio central (ej, 
 * entre los diez anios anteriores y los diez posteriores).
 * <P>Otra opcion es no incluir aquellos anios para los cuales la cantidad de registros entre las fechas de comienzo y
 * fin no sean mayores a un porcentaje determinado (O sea, los registros que faltan si son mayores al opuesto de ese porcentaje).
 * <P> Para optimizar las cuentas, esta clase cuenta con una cache interna donde se guardan resultados, y es actualizada de manera acorde.
 */
public class PromedioPrecipitacionAnualProyectorRango implements ProyectorRango {
  
  private static class ResultadoQuery {
    
    public int count;
    
    public double sum;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + count;
      long temp;
      temp = Double.doubleToLongBits(sum);
      result = prime * result + (int) (temp ^ (temp >>> 32));
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
      final ResultadoQuery other = (ResultadoQuery) obj;
      if (count != other.count)
        return false;
      if (Double.doubleToLongBits(sum) != Double.doubleToLongBits(other.sum))
        return false;
      return true;
    }
    
  }
  
  private static class CacheEntry {
    
    public Long estacionId;
    
    public Date comienzo;
    
    public Date fin;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((comienzo == null) ? 0 : comienzo.hashCode());
      result = prime * result + ((estacionId == null) ? 0 : estacionId.hashCode());
      result = prime * result + ((fin == null) ? 0 : fin.hashCode());
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
      if (comienzo == null) {
        if (other.comienzo != null)
          return false;
      } else if (!comienzo.equals(other.comienzo))
        return false;
      if (estacionId == null) {
        if (other.estacionId != null)
          return false;
      } else if (!estacionId.equals(other.estacionId))
        return false;
      if (fin == null) {
        if (other.fin != null)
          return false;
      } else if (!fin.equals(other.fin))
        return false;
      return true;
    }
    
  }
  
  private static Map<CacheEntry, ResultadoQuery> cache = new LRUMap<CacheEntry, ResultadoQuery>(1024);

  protected int diaComienzo;
  protected int mesComienzo;
  protected int anioComienzo;
  protected int diaFin;
  protected int mesFin;
  protected int sumaAnioFin;
  
  protected List<Integer> aniosPosibles = null;
  protected int rangoAnios = 100;
  
  private double umbralNulos = 0.0;
  
  private boolean incluirNulos = false;
  
  protected boolean excluirAnioCentral = false;
  
  public PromedioPrecipitacionAnualProyectorRango(Date comienzo, Date fin) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(comienzo);
    this.diaComienzo = calendar.get(Calendar.DAY_OF_MONTH);
    this.mesComienzo = calendar.get(Calendar.MONTH) + 1; // los meses de Calendar empiezan en 0;
    this.anioComienzo = calendar.get(Calendar.YEAR);
    calendar.setTime(fin);
    this.diaFin = calendar.get(Calendar.DAY_OF_MONTH);
    this.mesFin = calendar.get(Calendar.MONTH) + 1; // los meses de Calendar empiezan en 0;
    int anioFin = calendar.get(Calendar.YEAR);
    this.sumaAnioFin = anioFin - this.anioComienzo;
  }
  
  public List<Rango> proyectarRangos(Estacion estacion) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    Transaction transaction = sess.getTransaction();
    if (transaction != null && transaction.isActive()) {
      transaction = null;
    } else {
      transaction = sess.beginTransaction();
    }
    
    List<Integer> anios = this.dameAnios(estacion);
    
    List<Rango> resultado = new ArrayList<Rango>();
    for (int anio : anios) {
      if (this.excluirAnioCentral && anio == this.anioComienzo) {
        continue;
      }
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.DAY_OF_MONTH, this.diaComienzo);
      calendar.set(Calendar.MONTH, this.mesComienzo - 1);
      calendar.set(Calendar.YEAR, anio);
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MINUTE , 0);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      Date comienzo = calendar.getTime();

      calendar.set(Calendar.DAY_OF_MONTH, this.diaFin);
      calendar.set(Calendar.MONTH, this.mesFin - 1);
      calendar.set(Calendar.YEAR, anio + this.sumaAnioFin);
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MINUTE , 0);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      Date fin = calendar.getTime();


      long totalRegistros = FechaHelper.dameDifereciaDeDias(comienzo, fin);
      
      ResultadoQuery resultadoQuery = this.realizarQuery(sess, estacion, comienzo, fin);
      
      int valorDiasNoNulos = resultadoQuery.count;
      
      if ((totalRegistros - valorDiasNoNulos) <= this.umbralNulos * totalRegistros) {
        double precipitacion = resultadoQuery.sum;
        precipitacion /= valorDiasNoNulos; // promedio
        precipitacion = PrecipitacionHelper.ajustarPrecipitacion(precipitacion);
        Rango rango = new RangoImpl(comienzo, fin, nombre(), Double.valueOf(precipitacion));
        resultado.add(rango);
      } else if (this.incluirNulos) {
        Rango rango = new RangoImpl(comienzo, fin, nombre(), null);
        resultado.add(rango);
      }
    }
    if (transaction != null) {
      transaction.commit();
    }
    Collections.sort(resultado, new ComienzoRangoComparator());
    return resultado;
  }
  
  private ResultadoQuery realizarQuery(Session sess, Estacion estacion, Date comienzo, Date fin) {
    CacheEntry cacheEntry = new CacheEntry();
    cacheEntry.estacionId = estacion.getId();
    cacheEntry.comienzo = comienzo;
    cacheEntry.fin = fin;
    
    ResultadoQuery resultadoQuery;
    if (cache.containsKey(cacheEntry)) {
      //System.out.println("Obteniendo de cache promedio para estacion " + estacion + "con fechas de " + comienzo + " a " + fin);
      resultadoQuery = cache.get(cacheEntry);
    } else {
      resultadoQuery = this.realizarQuery2(sess, estacion, comienzo, fin);
      cache.put(cacheEntry, resultadoQuery);
    }
    return resultadoQuery;
  }
  
  private ResultadoQuery realizarQuery2(Session sess, Estacion estacion, Date comienzo, Date fin) {
    Query queryPrecip = sess.createQuery("SELECT COUNT(*), SUM(precipitacion) FROM RegistroDiario WHERE estacion = ? AND fecha >= ? AND fecha < ?")
    .setParameter(0, estacion);
    queryPrecip = queryPrecip.setDate(1, comienzo).setDate(2, fin);
    //System.out.println("Calculando promedio para estacion " + estacion + "con fechas de " + comienzo + " a " + fin);
    Object[] objects = (Object[])queryPrecip.uniqueResult();
    
    ResultadoQuery resultadoQuery = new ResultadoQuery();
    resultadoQuery.count = (objects == null || objects[0] == null)?0:((Number)objects[0]).intValue();
    resultadoQuery.sum = (objects == null || objects[1] == null)?0d:((Number)objects[1]).doubleValue();
    
    return resultadoQuery;
  }

  private List<Integer> dameAnios(Estacion estacion) {
    int inicio = FechaHelper.dameAnio(estacion.getFechaInicio());
    int fin = FechaHelper.dameAnio(estacion.getFechaFin());
    if (inicio < (this.anioComienzo - this.rangoAnios)) {
      inicio = this.anioComienzo - this.rangoAnios;
    }
    if (fin > (this.anioComienzo + this.rangoAnios)) {
      fin = this.anioComienzo + this.rangoAnios;
    }

    
    List<Integer> resultado = new ArrayList<Integer>();
    for (int i = inicio; i <= fin; i++) {
      resultado.add(i);
    }
    if (this.aniosPosibles != null) {
      resultado.retainAll(this.aniosPosibles);
    }
    return resultado;
  }

  public void setUmbralNulos(double umbralNulos) {
    this.umbralNulos = umbralNulos;
  }

  public void setRangoAnios(int rangoAnios) {
    this.rangoAnios = rangoAnios;
  }

  public void setIncluirNulos(boolean incluirNulos) {
    this.incluirNulos = incluirNulos;
  }

  public void setExcluirAnioCentral(boolean excluirAnioCentral) {
    this.excluirAnioCentral = excluirAnioCentral;
  }

  public String nombre() {
    return "PromPrecipAnio";
  }

  public void setAnioPosibles(List<Integer> aniosPosibles) {
    this.aniosPosibles = new ArrayList<Integer>(aniosPosibles);
  }
  
  public static void clearCache() {
    cache.clear();
  }

}
