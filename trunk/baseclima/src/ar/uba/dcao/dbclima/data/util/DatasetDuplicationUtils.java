package ar.uba.dcao.dbclima.data.util;

import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Clase de ayuda para la duplicacion de estaciones o registros al insertar un Dataset de prueba
 * que contiene estaciones ya existentes en los Datasets de referencia.
 *
 */
public final class DatasetDuplicationUtils {

  private DatasetDuplicationUtils() {
  }

  public static Estacion duplicarEstacion(Estacion srcEst, Dataset destDataset) {
    Estacion destEst = new Estacion(srcEst.getNombre(), srcEst.getUbicacion(), srcEst.getCodigoPais(), srcEst
        .getCodigoOMM(), srcEst.getCodigoNacional(), srcEst.getAltura(), srcEst.getLatitud(), srcEst.getLongitud());

    if (!destDataset.isReferente()) {
      destEst.setDataset(destDataset);
    }

    for (RegistroDiario srcRd : srcEst.getRegistros()) {
      RegistroDiario destRd = duplicarRegistro(srcRd, destEst, destDataset);
      destEst.getRegistros().add(destRd);
    }

    return destEst;
  }

  public static RegistroDiario duplicarRegistro(RegistroDiario srcRd, Estacion destEst, Dataset dataset) {
    RegistroDiario dupRd = new RegistroDiario();
    dupRd.setDataset(dataset);
    dupRd.setEstacion(destEst);

    dupRd.setTempMin(srcRd.getTempMin());
    dupRd.setTempMax(srcRd.getTempMax());
    dupRd.setPrecipitacion(srcRd.getPrecipitacion());
    dupRd.setFecha(srcRd.getFecha());

    return dupRd;
  }
}
