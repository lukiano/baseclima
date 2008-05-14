package ar.uba.dcao.dbclima.precipitacion.rango;

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;

import ar.uba.dcao.dbclima.precipitacion.PrecipitacionHelper;

/**
 * Obtiene la cantidad de precipitacion caida durante un cierto rango para cada anio de la estacion.
 *
 */
public class AcumPrecipitacionAnualProyectorRango extends AbstractAnualProyectorRango {
  
  private boolean incluir0mm = true;
  
  public AcumPrecipitacionAnualProyectorRango() {
    super();
  }

  public AcumPrecipitacionAnualProyectorRango(Date comienzo, Date fin) {
    super(comienzo, fin);
  }

  public AcumPrecipitacionAnualProyectorRango(int diaComienzo, int mesComienzo, int diaFin, int mesFin) {
    super(diaComienzo, mesComienzo, diaFin, mesFin);
  }

  @Override
  protected Query crearQuery(Session sess) {
    return sess.createQuery("SELECT sum(precipitacion) FROM RegistroDiario WHERE estacion = ? AND fecha >= ? AND fecha < ?");
  }
  
  public String nombre() {
    return "AcumPrecipAnio";
  }

  @Override
  protected Double procesarResultado(Date comienzo, Date fin, Object resultadoCrudo) {
    if (resultadoCrudo == null) {
      return this.incluir0mm ? 0d:null;
    }
    Number precip = (Number)resultadoCrudo;
    if (precip.intValue() == 0) {
      return this.incluir0mm ? 0d:null;
    }
    
    double valor = precip.doubleValue();
    valor = PrecipitacionHelper.ajustarPrecipitacion(valor);
    return valor;
  }

  public void setIncluir0mm(boolean incluir0mm) {
    this.incluir0mm = incluir0mm;
  }

}
