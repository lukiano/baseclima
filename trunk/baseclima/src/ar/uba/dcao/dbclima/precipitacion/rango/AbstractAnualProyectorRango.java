package ar.uba.dcao.dbclima.precipitacion.rango;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Clase abstracta que ejecuta una consulta determinada sobre un rango de dias para todos los anios
 * de la estacion. Las subclases indicaran cual es la consulta y que se hace con el resultado.
 *
 */
public abstract class AbstractAnualProyectorRango implements ProyectorRango {
  
  private int diaComienzo;
  private int mesComienzo;
  private int diaFin;
  private int mesFin;
  private int sumaAnioFin;
  
  public AbstractAnualProyectorRango(Date comienzo, Date fin) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(comienzo);
    this.diaComienzo = calendar.get(Calendar.DAY_OF_MONTH);
    this.mesComienzo = calendar.get(Calendar.MONTH) + 1; // los meses de Calendar empiezan en 0;
    int anioComienzo = calendar.get(Calendar.YEAR);
    calendar.setTime(fin);
    this.diaFin = calendar.get(Calendar.DAY_OF_MONTH);
    this.mesFin = calendar.get(Calendar.MONTH) + 1; // los meses de Calendar empiezan en 0;
    int anioFin = calendar.get(Calendar.YEAR);
    this.sumaAnioFin = anioFin - anioComienzo;
  }

  public AbstractAnualProyectorRango(int diaComienzo, int mesComienzo, int diaFin, int mesFin) {
    this.diaComienzo = diaComienzo;
    this.diaFin = diaFin;
    this.mesComienzo = mesComienzo;
    this.mesFin = mesFin;
  }

  public AbstractAnualProyectorRango() {
    this(1, 1, 31, 12);
  }

  @SuppressWarnings("unchecked")
  public List<Rango> proyectarRangos(Estacion estacion) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    Transaction transaction = sess.getTransaction();
    if (transaction != null && transaction.isActive()) {
      transaction = null;
    } else {
      transaction = sess.beginTransaction();
    }
    List<Number> anios = sess.createQuery("SELECT DISTINCT year(fecha) FROM RegistroDiario WHERE estacion = ?")
      .setParameter(0, estacion).list();
    Query query = crearQuery(sess).setParameter(0, estacion);
    
    List<Rango> resultado = new ArrayList<Rango>();
    for (Number anio : anios) {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.DAY_OF_MONTH, this.diaComienzo);
      calendar.set(Calendar.MONTH, this.mesComienzo - 1);
      calendar.set(Calendar.YEAR, anio.intValue());
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      Date comienzo = calendar.getTime();
      query.setDate(1, comienzo);

      calendar.set(Calendar.DAY_OF_MONTH, this.diaFin);
      calendar.set(Calendar.MONTH, this.mesFin - 1);
      calendar.set(Calendar.YEAR, anio.intValue() + this.sumaAnioFin);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      Date fin = calendar.getTime();
      query.setDate(2, fin);

      Double valor = procesarResultado(comienzo, fin, query.uniqueResult());
      
      if (valor != null) {
        Rango rango = new RangoImpl(comienzo, fin, nombre(), valor);
        resultado.add(rango);
      }
      
    }
    if (transaction != null) {
      transaction.commit();
    }
    Collections.sort(resultado, new ComienzoRangoComparator());
    return resultado;
  }

  protected abstract Double procesarResultado(Date comienzo, Date fin, Object resultadoCrudo);

  protected abstract Query crearQuery(Session sess);
  
}
