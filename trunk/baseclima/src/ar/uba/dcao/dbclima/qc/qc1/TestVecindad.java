package ar.uba.dcao.dbclima.qc.qc1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.EstacionHelper;
import ar.uba.dcao.dbclima.data.FiltroRegistro;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.qc.ResultadoVecindad;
import ar.uba.dcao.dbclima.qc.VecindadEstacion;
import ar.uba.dcao.dbclima.qc.ResultadoVecindad.EstimacionVecino;

public class TestVecindad {

  public static FiltroRegistro FILTRO_QC_TMAX = new FiltroRegistro() {
    public boolean aplicaA(RegistroDiario rd) {
      byte conf = rd.getConfianzaTempMax() == null ? 0 : rd.getConfianzaTempMax().getConfianza();
      return rd.getTempMax() != null && conf < 1
          && (rd.getCodigoConfianzaTempRange() == null || rd.getConfianzaTempRange().getConfianza() < 2);
    }
  };

  public static FiltroRegistro FILTRO_QC_TMIN = new FiltroRegistro() {
    public boolean aplicaA(RegistroDiario rd) {
      byte conf = rd.getConfianzaTempMin() == null ? 0 : rd.getConfianzaTempMin().getConfianza();
      return rd.getTempMin() != null && conf < 1
          && (rd.getCodigoConfianzaTempRange() == null || rd.getConfianzaTempRange().getConfianza() < 2);
    }
  };

  public static final int MAX_VEC_SIZE = 5;

  public static final int MAX_DESV_PRED = 40;

  public static final int NUM_VECINOS = 10;

  public static final double MIN_CORR = 0.8d;

  private static final double POND_RANGE = 1.5d;

  private static final Calendar calendar = Calendar.getInstance();

  /** Proyeccion (variable) a testear. */
  private ProyectorRegistro proy;

  /** Desviacion estandar de los valores por mes. */
  private double[] stdvPorMes;

  private VecindadEstacion vcnd;

  private final boolean filling;

  public TestVecindad(Session sess, Estacion e, List<CorrelacionEstaciones> corrs, ProyectorRegistro proy,
      boolean filling) {
    this.filling = filling;

    FiltroRegistro f = null;
    if (filling) {
      f = (proy == ProyectorRegistro.PROY_TMAX) ? VecindadEstacion.filtroVecinoTx
          : VecindadEstacion.filtroVecinoTn;
    } else {
      f = (proy == ProyectorRegistro.PROY_TMAX) ? FILTRO_QC_TMAX : FILTRO_QC_TMIN;
    }
    this.vcnd = new VecindadEstacion(e, f, corrs);
    this.proy = proy;

    this.stdvPorMes = EstacionHelper.getStdvPorMes(e, proy);
  }

  public ResultadoVecindad cotejarVecindad(RegistroDiario rd) {
    Integer valorReg = proy.getValor(rd);
    if (valorReg == null || vcnd.getRegistrosVecinos(rd.getFecha()).size() == 0) {
      /* No se puede testear el registro. Tiene val = null o no hay vecinos. */
      return null;
    }

    /* Calculo de angulo cubierto */
    double anguloCubiertoXVecinos = vcnd.getAnguloCobertura(rd.getFecha());

    List<RegistroDiario> regsVecinos = vcnd.getRegistrosVecinos(rd.getFecha());
    List<CorrelacionEstaciones> corrsVecinos = vcnd.getCorrelaciones(rd.getFecha());

    ResultadoVecindad rv = new ResultadoVecindad();

    double sumaPredicciones = 0;

    /*
     * En este ciclo se obtienen las estimaciones de los vecinos que mejor predicen a la estacion.
     */
    int size = Math.min(regsVecinos.size(), MAX_VEC_SIZE);
    List<Double> ponderacion = this.ponderar(corrsVecinos);
    double sumaPonderacion = 0;

    for (int i = 0; i < size; i++) {
      RegistroDiario regVecino = regsVecinos.get(i);
      CorrelacionEstaciones corrVecino = corrsVecinos.get(i);

      double valRegVecino = proy.getValor(regVecino);
      double pred = corrVecino.predecirEstacion(vcnd.getEstacion(), valRegVecino);
      double desvNorm = (valorReg - pred) / corrVecino.getDesviacionEstimacion();

      EstimacionVecino estimVecino = new EstimacionVecino(regVecino, pred, desvNorm, vcnd.getAngulo(regVecino
          .getEstacion()));
      rv.getEstimaciones().add(estimVecino);

      sumaPonderacion += ponderacion.get(i);
      sumaPredicciones += pred * ponderacion.get(i);
    }

    /* Get desvStdv del mes para la variable en la estacion. */
    calendar.setTime(rd.getFecha());
    double desvStdVariable = stdvPorMes[calendar.get(Calendar.MONTH)];

    /* Configura y devuelve el resultado de la vecindad. */
    rv.setValorReal(valorReg);
    rv.setPrediccion(sumaPredicciones / sumaPonderacion);
    rv.setDesvEstandard(desvStdVariable);
    rv.setAnguloCubierto(anguloCubiertoXVecinos);

    return rv;
  }

  private List<Double> ponderar(List<CorrelacionEstaciones> corrs) {
    List<Double> rv = new ArrayList<Double>();
    for (int i = 0; i < corrs.size(); i++) {
      if (this.filling) {
        double ratio = (MAX_DESV_PRED - corrs.get(i).getDesviacionEstimacion()) / MAX_DESV_PRED;
        rv.add(ratio * POND_RANGE + 1);
      } else {
        rv.add(1d);
      }
    }

    return rv;
  }
}
