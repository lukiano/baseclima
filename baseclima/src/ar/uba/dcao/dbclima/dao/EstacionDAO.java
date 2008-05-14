package ar.uba.dcao.dbclima.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.Estacion;

/**
 * Clase DAO para realizar consultas sobre el objeto Estacion.
 * 
 * @see Estacion
 * 
 * 
 */
public class EstacionDAO extends AbstractDAO<Estacion> {

  public EstacionDAO() {
    super(Estacion.class);
  }

  @SuppressWarnings("unchecked")
  public List<Estacion> findAllNamed() {
    return getSession().createQuery("FROM Estacion WHERE nombre IS NOT NULL").list();
  }

  @SuppressWarnings("unchecked")
  public List<Long> findAllNamedIDs() {
    return getSession().createQuery("SELECT id FROM Estacion WHERE nombre IS NOT NULL").list();
  }

  @SuppressWarnings("unchecked")
  public List<Estacion> findAllLocated() {
    return getSession().createQuery("FROM Estacion WHERE longitud != NULL AND latitud != NULL").list();
  }

  @SuppressWarnings("unchecked")
  public List<Integer> findAllLocatedIDs() {
    return getSession().createQuery("SELECT id FROM Estacion WHERE longitud != NULL AND latitud != NULL").list();
  }

  @SuppressWarnings("unchecked")
  public List<Long> findAllIDsByDatasetID(Long dsID) {
    return getSession().createQuery("SELECT id FROM Estacion WHERE dataset.id = :_id").setLong("_id", dsID).list();
  }

  @SuppressWarnings("unchecked")
  public List<Estacion> findAllByDataset(Dataset ds) {
    return getSession().createQuery("FROM Estacion WHERE dataset = :_ds").setParameter("_ds", ds).list();
  }

  @SuppressWarnings("unchecked")
  public List<Estacion> findAllByDataset(Long dsID) {
    return getSession().createQuery("FROM Estacion WHERE dataset.id = :_id").setLong("_id", dsID).list();
  }

  @SuppressWarnings("unchecked")
  public List<Long> findAllIDsForBDR() {
    return getSession().createQuery("SELECT id FROM Estacion WHERE dataset.referente = 1").list();
  }

  @SuppressWarnings("unchecked")
  public List<Estacion> findAllForBDR() {
    return getSession().createQuery("FROM Estacion WHERE dataset.referente = 1").list();
  }

  @SuppressWarnings( { "unchecked", "deprecation" })
  public List<Long> findAllIDsByActivityDate(int start, int end) {
    Date startD = new Date(start, 0, 1);
    Date endD = new Date(end, 11, 31);
    // String q = "SELECT id FROM Estacion e WHERE (e.fechaInicio BETWEEN :_ini AND :_fin)
    // OR (e.fechaFin BETWEEN :_ini AND :_fin)";
    String q = "SELECT id FROM Estacion e WHERE e.fechaInicio < :_fin AND e.fechaFin > :_ini";

    return getSession().createQuery(q).setDate("_ini", startD).setDate("_fin", endD).list();
  }

  @SuppressWarnings("unchecked")
  public List<Estacion> findNotQC1d(boolean includeUnnamed) {
    List<Integer> ids = getSession().createQuery(
        "SELECT DISTINCT rt.registro.estacion.id FROM ResultadoTestQC rt"
            + " WHERE rt.testID IN ('OUTL_Tx', 'OUTL_Tn', 'OUTL_Tr')").list();

    String q = "FROM Estacion WHERE id NOT IN (:ids)";
    if (!includeUnnamed) {
      q += " AND nombre IS NOT NULL";
    }

    return getSession().createQuery(q).setParameterList("ids", ids).list();
  }

  @SuppressWarnings("unchecked")
  public List<Long> findForCorrelationSearchAgainstDataset(Dataset ds) {
    String qa = "FROM Estacion e WHERE e.altura IS NOT null AND e.latitud IS NOT null AND e.longitud IS NOT NULL"
        + " AND (e.dataset.referente is true OR e.dataset.id = " + ds.getId() + ")";
    List<Estacion> estaciones = getSession().createQuery(qa).list();

    Map<String, Long> estsPorPos = new HashMap<String, Long>();
    for (Estacion e : estaciones) {
      String posStr = "" + e.getAltura() + "/" + e.getLatitud() + "/" + e.getLongitud();
      if (e.getDataset() != null || estsPorPos.get(posStr) == null) {
        estsPorPos.put(posStr, e.getId());
      }
    }

    return new ArrayList<Long>(estsPorPos.values());
  }

  @SuppressWarnings("unchecked")
  public List<Long> findForCorrelationSearchInDataset(Dataset ds) {
    String qa = "FROM Estacion e WHERE e.altura IS NOT null AND e.latitud IS NOT null AND e.longitud IS NOT NULL";
    if (!ds.isReferente()) {
      qa += " AND e.dataset.id = " + ds.getId();
    } else {
      qa += " AND e.dataset.referente is true";
    }

    List<Estacion> estaciones = getSession().createQuery(qa).list();

    Map<String, Long> estsPorPos = new HashMap<String, Long>();
    for (Estacion e : estaciones) {
      String posStr = "" + e.getAltura() + "/" + e.getLatitud() + "/" + e.getLongitud();
      if (e.getDataset() != null || estsPorPos.get(posStr) == null) {
        estsPorPos.put(posStr, e.getId());
      }
    }

    return new ArrayList<Long>(estsPorPos.values());
  }

  public List<Long> findNotQC1dIDs(boolean includeUnnamed) {
    return getIDs(this.findNotQC1d(includeUnnamed));
  }
}
