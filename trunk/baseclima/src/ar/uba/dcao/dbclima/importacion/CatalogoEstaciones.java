package ar.uba.dcao.dbclima.importacion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.util.DatasetDuplicationUtils;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

public class CatalogoEstaciones {

  private static final long BDR_DS_PFIX = 0L;

  private Map<String, Estacion> estaciones = new HashMap<String, Estacion>();

  private Dataset dataset;

  public void init(Dataset dataset, Session sess) {
    this.dataset = dataset;
    List<Estacion> es = DAOFactory.getEstacionDAO(sess).findAll();
    for (Estacion e : es) {
      if (e.getDataset().isReferente() || e.getDataset().equals(dataset)) {
        this.estaciones.put(getKey(e), e);
      }
    }
  }

  public Estacion findEstacion(Integer codOMM, Integer codPais, String codNac) {
    String key = this.getKey(codPais, codOMM, codNac, dataset);
    Estacion estacion = this.estaciones.get(key);

    if (estacion == null && !this.dataset.isReferente()) {
      /*
       * La estacion no existe para el dataset. Se procede a buscarlo en la bdr porq de
       * existir se debe duplicar para el dataset que se esta importando.
       */
      Estacion forCopy = this.estaciones.get(key);
      if (forCopy != null) {
        forCopy = (Estacion) DBSessionFactory.getInstance().getCurrentSession().load(Estacion.class, forCopy.getId());
        estacion = DatasetDuplicationUtils.duplicarEstacion(forCopy, this.dataset);
        this.estaciones.put(key, estacion);
      }
    }

    return estacion;
  }

  public Estacion createEstacion(Integer codOMM, Integer codPais, String codNac, String nombre, String ubicacion,
      Integer latitud, Integer longitud, Integer altura) {

    Estacion estacion = new Estacion(nombre, ubicacion, codPais, codOMM, codNac, altura, latitud, longitud);
    //Dataset eDataset = this.dataset.isReferente() ? null : this.dataset;
    estacion.setDataset(this.dataset);
    this.estaciones.put(getKey(estacion), estacion);

    return estacion;
  }
  
  private String dameValor(Object obj) {
    return obj==null?"":obj.toString();
  }

  private String getKey(Estacion e) {
    return getKey(e.getCodigoPais(), e.getCodigoOMM(), e.getCodigoNacional(), e.getDataset());
  }

  private String getKey(Integer codigoPais, Integer codigoOMM, String codigoNacional, Dataset dataset) {
    return dameValor(codigoPais) + "," + dameValor(codigoOMM) + ","
        + dameValor(codigoNacional) + "," + getDatasetRep(dataset);
  }

  private static String getDatasetRep(Dataset ds) {
    long dsRep = (ds == null || ds.isReferente()) ? BDR_DS_PFIX : ds.getFechaCreacion().getTime();
    return String.valueOf(dsRep);
  }
}
