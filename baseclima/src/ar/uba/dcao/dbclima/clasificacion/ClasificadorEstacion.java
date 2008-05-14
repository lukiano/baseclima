package ar.uba.dcao.dbclima.clasificacion;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;

public abstract class ClasificadorEstacion {

  private static final int TRAIN_EPOCHS = 130;

  private static final int MAP_SIDE = 5;

  private final Estacion estacion;

  private ClasificadorSecuencias sequenceClasificator;

  public ClasificadorEstacion(Estacion e) {
    this.estacion = e;
    this.sequenceClasificator = new ClasificadorSecuencias(MAP_SIDE, MAP_SIDE, this.getNumeroCaracts());
  }

  public Map<RegistroDiario, Point> clasificar() {
    List<double[]> valores = new ArrayList<double[]>();

    for (RegistroDiario rd : this.estacion.getRegistros()) {
      if (this.registroAplica(rd)) {
        valores.add(getVectorCaracts(rd));
      }
    }

    /* Train. */
    double[][] valsArr = new double[valores.size()][2];
    valores.toArray(valsArr);
    this.sequenceClasificator.train(valsArr, TRAIN_EPOCHS);

    /* Query. */
    Map<RegistroDiario, Point> clasificacion = new HashMap<RegistroDiario, Point>();
    for (RegistroDiario rd : this.estacion.getRegistros()) {
      if (this.registroAplica(rd)) {
        double[] cv = getVectorCaracts(rd);
        Point neuronaActivada = this.sequenceClasificator.query(cv);
        clasificacion.put(rd, neuronaActivada);
      }
    }

    return clasificacion;
  }

  /**
   * Devuelve true sii el registro debe ser incluido en la clasificacion.
   * 
   * @param rd
   *            Registro a clasificar.
   */
  protected abstract boolean registroAplica(RegistroDiario rd);

  /**
   * Genera y devuelve el vector de caracteristicas para el registro diario indicado.
   */
  protected abstract double[] getVectorCaracts(RegistroDiario rd);

  /**
   * Cantidad de caracteristicas que tendran los vectores generados.
   */
  protected abstract int getNumeroCaracts();
}
