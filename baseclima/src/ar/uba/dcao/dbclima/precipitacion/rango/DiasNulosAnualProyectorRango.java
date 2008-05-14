package ar.uba.dcao.dbclima.precipitacion.rango;

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Obtiene la cantidad de registros faltantes durante un cierto rango para cada anio de la estacion.
 *
 */
public class DiasNulosAnualProyectorRango extends AbstractAnualProyectorRango {
  
  /**
   * 
   */
  public DiasNulosAnualProyectorRango() {
    super();
  }

  /**
   * @param comienzo
   * @param fin
   */
  public DiasNulosAnualProyectorRango(Date comienzo, Date fin) {
    super(comienzo, fin);
  }

  /**
   * @param diaComienzo
   * @param mesComienzo
   * @param diaFin
   * @param mesFin
   */
  public DiasNulosAnualProyectorRango(int diaComienzo, int mesComienzo, int diaFin, int mesFin) {
    super(diaComienzo, mesComienzo, diaFin, mesFin);
  }

  @Override
  protected Double procesarResultado(Date comienzo, Date fin, Object resultadoCrudo) {
    long cantidadDias = this.dameDifereciaDeDias(comienzo, fin);
    //cantidadDias--;
    if (resultadoCrudo == null) {
      return (double)cantidadDias;
    }
    Number diasNoNulos = (Number)resultadoCrudo;
    return Double.valueOf(cantidadDias - diasNoNulos.longValue());
  }
  
  private long dameDifereciaDeDias(Date comienzo, Date fin) {
    long diferenciaMillis = fin.getTime() - comienzo.getTime();
    long divisor = 1000*60*60*24; // un dia
    return diferenciaMillis / divisor;
  }
  
  @Override
  protected Query crearQuery(Session sess) {
    return sess.createQuery("SELECT count(*) FROM RegistroDiario WHERE estacion = ? AND fecha >= ? AND fecha < ?");
  }
  
  public String nombre() {
    return "DiasNulosAnio";
  }

}
