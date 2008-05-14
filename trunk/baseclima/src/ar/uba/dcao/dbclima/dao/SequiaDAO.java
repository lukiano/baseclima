package ar.uba.dcao.dbclima.dao;

import java.util.List;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;

/**
 * Clase DAO para realizar consultas sobre el objeto Sequia.
 * @see Sequia 
 *
 */
public class SequiaDAO extends AbstractDAO<Sequia> {

  public SequiaDAO() {
    super(Sequia.class);
  }

  @SuppressWarnings("unchecked")
  public List<Sequia> findAll() {
    return getSession().createQuery("FROM Sequia").list();
  }

  @SuppressWarnings("unchecked")
  public List<Sequia> findAllOrderedByLength() {
    return getSession().createQuery("FROM Sequia ORDER BY longitud DESC").list();
  }

  @SuppressWarnings("unchecked")
  public List<Sequia> findAllByStation(Estacion station) {
    return getSession().createQuery("FROM Sequia WHERE estacion = ?").setParameter(0, station).list();
  }

  @SuppressWarnings("unchecked")
  public List<Sequia> findAllByStationId(Long stationId) {
    return getSession().createQuery("FROM Sequia WHERE estacion.id = ?").setLong(0, stationId).list();
  }

  @SuppressWarnings("unchecked")
  public List<Sequia> findAllLongerThanByStation(int minimumLength, Estacion station) {
    return getSession().createQuery("FROM Sequia WHERE estacion = ? AND longitud >= ?")  
      .setParameter(0, station).setInteger(1, minimumLength).list();
  }

  @SuppressWarnings("unchecked")
  public List<Sequia> findAllLongerThanByStationId(int minimumLength, Long stationId) {
    return getSession().createQuery("FROM Sequia WHERE estacion.id = ? AND longitud >= ?")
      .setLong(0, stationId).setInteger(1, minimumLength).list();
  }

  public void deleteAllByStation(Estacion station) {
    getSession().createQuery("DELETE FROM Sequia WHERE estacion = ?").setParameter(0, station).executeUpdate();
  }

  public void deleteAllByStationId(Long stationId) {
    getSession().createQuery("DELETE FROM Sequia WHERE estacion.id = ?").setLong(0, stationId).executeUpdate();
  }

}
