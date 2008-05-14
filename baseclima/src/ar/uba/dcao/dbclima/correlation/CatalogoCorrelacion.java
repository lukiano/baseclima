package ar.uba.dcao.dbclima.correlation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;

/**
 * Un catalogo de correlaciones ofrece acceso a las correlaciones
 * almacenadas en la base de datos. Como parte de esto permite
 * consultas a las correlaciones existentes e inserciones de nuevas
 * correlaciones.
 */
public class CatalogoCorrelacion {

  private Map<Estacion, List<CorrelacionEstaciones>> corrs;

  private String variable;

  public CatalogoCorrelacion(String variable) {
    this.variable = variable;
    this.corrs = new HashMap<Estacion, List<CorrelacionEstaciones>>();
  }

  @SuppressWarnings("unchecked")
  public void initialize(Session sess) {
    List<CorrelacionEstaciones> corrTemp = sess.createQuery(
        "FROM CorrelacionEstaciones WHERE variable = '" + variable + "' ORDER BY e1.id, e2.id").list();

    for (CorrelacionEstaciones c : corrTemp) {
      addCorrelacion(c);
    }
  }

  public void addCorrelaciones(List<CorrelacionEstaciones> corrs) {
    for (CorrelacionEstaciones corr : corrs) {
      this.addCorrelacion(corr);
    }
  }

  public void addCorrelacion(CorrelacionEstaciones c) {
    List<CorrelacionEstaciones> corrsEst = this.corrs.get(c.getE1());
    if (corrsEst == null) {
      corrsEst = new ArrayList<CorrelacionEstaciones>();
      this.corrs.put(c.getE1(), corrsEst);
    }

    corrsEst.add(c);
  }

  public List<CorrelacionEstaciones> getCorrelaciones(Estacion e1, Estacion e2) {
    Estacion eLt = (e2.getId() > e1.getId()) ? e1 : e2;
    Estacion eGt = (e2.getId() > e1.getId()) ? e2 : e1;
    List<CorrelacionEstaciones> rv = new ArrayList<CorrelacionEstaciones>();

    List<CorrelacionEstaciones> corrsPosibles = this.corrs.get(eLt);
    if (corrsPosibles != null) {
      for (CorrelacionEstaciones corr : corrsPosibles) {
        if (corr.getE1().equals(eLt) && corr.getE2().equals(eGt)) {
          rv.add(corr);
        }
      }
    }

    return rv;
  }

  public CorrelacionEstaciones[] getCorrelacionesPorMes(Estacion e1, Estacion e2) {
    CorrelacionEstaciones[] rv = new CorrelacionEstaciones[12];
    List<CorrelacionEstaciones> corrsAsList = this.getCorrelaciones(e1, e2);

    for (CorrelacionEstaciones c : corrsAsList) {
      if (c.getCorrelacion() != null) {
        rv[c.getMes()] = c;
      }
    }

    return rv;
  }

  public Map<Estacion, List<CorrelacionEstaciones>> getCorrs() {
    return corrs;
  }

  public String getVariable() {
    return variable;
  }

  public void saveTo(Session sess) {
    for (Entry<Estacion, List<CorrelacionEstaciones>> entry : this.corrs.entrySet()) {
      for (CorrelacionEstaciones c : entry.getValue()) {
        sess.saveOrUpdate(c);
      }
    }

    this.corrs.clear();
  }

  /**
   * Devuelve la coleccion de correlaciones de la estacion e con
   * correlacion >= minCorr y desvio estandard ( de su estimacion
   * derivada ) <= maxDesv.
   */
  @SuppressWarnings("unchecked")
  public List<CorrelacionEstaciones> getCorrelaciones(Estacion e, Session s, double minCorr, double maxDesv,
      int maxNumCorrsPorMes) {
    List<CorrelacionEstaciones> rv = new ArrayList<CorrelacionEstaciones>();

    for (int i = 0; i < 12; i++) {
      String q = "FROM CorrelacionEstaciones" + " WHERE (e1.id = " + e.getId() + " OR e2.id = " + e.getId() + ")"
          + " AND variable = '" + this.variable + "'" + " AND correlacion >= " + minCorr
          + " AND desviacionEstimacion <= " + maxDesv + " AND mes = " + i + "ORDER BY desviacionEstimacion";
      Query qCorrsMes = s.createQuery(q).setMaxResults(maxNumCorrsPorMes);
      List<CorrelacionEstaciones> corrsMes = qCorrsMes.list();
      rv.addAll(corrsMes);
    }

    return rv;
  }
}