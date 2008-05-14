package ar.uba.dcao.dbclima.qc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.FiltroRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.ResultadoTestQC;
import ar.uba.dcao.dbclima.qc.qc1.SequenceHomogeneityTest;
import ar.uba.dcao.dbclima.utils.FechaHelper;

public class VecindadEstacion {

  /**
   * Devuelve true sii el test de homogeneidad no marca problemas de Homog ni hay
   * incoherencia entre tmin y tmax.
   */
  public static final FiltroRegistro filtroVecinoTn = new FiltroRegistro() {
    public boolean aplicaA(RegistroDiario rd) {
      ResultadoTestQC resHomg = rd.getResultadoByID(SequenceHomogeneityTest.TEST_PREFIX + "_Tn");
      boolean coh = rd.getTempMax() == null || rd.getTempMin() == null || rd.getTempMax() >= rd.getTempMin();
      return rd.getTempMin() != null && (resHomg == null || resHomg.getValor() <= 1) && coh;
    }
  };

  /**
   * Devuelve true sii el test de homogeneidad no marca problemas de Homog ni hay
   * incoherencia entre tmin y tmax.
   */
  public static final FiltroRegistro filtroVecinoTx = new FiltroRegistro() {
    public boolean aplicaA(RegistroDiario rd) {
      ResultadoTestQC resHomg = rd.getResultadoByID(SequenceHomogeneityTest.TEST_PREFIX + "_Tx");
      boolean coh = rd.getTempMax() == null || rd.getTempMin() == null || rd.getTempMax() >= rd.getTempMin();
      return rd.getTempMax() != null && (resHomg == null || resHomg.getValor() <= 1) && coh;
    }
  };

  private static final Calendar cal = Calendar.getInstance();

  /** Estacion para la cual se realiza el test */
  private Estacion estacion;

  /** Correlaciones que se usaran para predecir las variables de la estacion. */
  private List<CorrelacionEstaciones> corrs;

  /** Vecinos de la estacion. A mismo indice misma estacion q la lista de correlaciones. */
  private List<Estacion> vecinos = new ArrayList<Estacion>();

  /** Angulo del vector &lt;e, VECi&gt;. */
  private List<Double> angVecinos = new ArrayList<Double>();

  /** Indice para cada estacion vecina, del primer registro no anterior a la fecha. */
  private List<Integer> iRegs = new ArrayList<Integer>();

  private List<Boolean> validezIndices = new ArrayList<Boolean>();

  private Date lastSeekedDate = FechaHelper.dameFecha(1930, 1, 1);

  private FiltroRegistro filtro;

  public VecindadEstacion(Estacion e, FiltroRegistro filtro, List<CorrelacionEstaciones> corrs) {
    this.estacion = e;
    this.corrs = corrs;
    this.filtro = filtro;

    /* Se completan los datos que dependen de las estaciones y no de los vecinos. */
    for (CorrelacionEstaciones cc : this.corrs) {

      /* Angulo entre estaciones. */
      Estacion ev = cc.getCorrelacionado(this.estacion);
      this.angVecinos.add(this.getAngulo(ev));

      /* Se agrega la estacion a la lista de vecinos. */
      this.vecinos.add(ev);

      /* Agrega el indice que le corresponde a cada estacion. */
      iRegs.add(0);
      validezIndices.add(false);
    }
  }

  /**
   * Mueve el puntero usado en la lista de registros de cada estacion hasta la fecha
   * indicada. Concretamente este se mueve hasta el primer registro no anterior a la
   * fecha.
   */
  private void seek(Date fecha) {
    cal.setTime(fecha);
    int mes = cal.get(Calendar.MONTH);

    if (fecha.getTime() == this.lastSeekedDate.getTime()) {
      return;
    }

    for (int iVecino = 0; iVecino < this.vecinos.size(); iVecino++) {
      if (this.corrs.get(iVecino).getMes() != mes) {
        // Solo se usan correlaciones validas para el mes.
        this.validezIndices.set(iVecino, false);
        continue;
      }

      Integer idxReg = this.iRegs.get(iVecino);
      List<RegistroDiario> regsEstacionI = this.vecinos.get(iVecino).getRegistros();

      idxReg = Math.max(0, idxReg);
      idxReg = Math.min(regsEstacionI.size() - 1, idxReg);

      int sense = (int) Math.signum(fecha.getTime() - regsEstacionI.get(idxReg).getFecha().getTime());
      /*
       * Recorre la lista de registros de la estacion vecina hasta encontrar uno no
       * anterior a la fecha indicada
       */
      while (0 <= idxReg && idxReg < regsEstacionI.size() && sense != 0
          && Math.signum(fecha.getTime() - regsEstacionI.get(idxReg).getFecha().getTime()) == sense) {
        idxReg += sense;
      }
      this.iRegs.set(iVecino, idxReg);

      boolean b = (0 <= idxReg) && (idxReg < regsEstacionI.size())
          && regsEstacionI.get(idxReg).getFecha().equals(fecha) && filtro.aplicaA(regsEstacionI.get(idxReg));

      this.validezIndices.set(iVecino, b);
    }

    this.lastSeekedDate = fecha;
  }

  public List<RegistroDiario> getRegistrosVecinos(Date fecha) {
    List<RegistroDiario> rv = new ArrayList<RegistroDiario>();

    this.seek(fecha);

    for (int i = 0; i < this.corrs.size(); i++) {
      Integer iReg = this.iRegs.get(i);
      List<RegistroDiario> regsEst = this.vecinos.get(i).getRegistros();

      if (this.validezIndices.get(i)) {
        rv.add(regsEst.get(iReg));
      }
    }

    return rv;
  }

  public double getAnguloCobertura(Date fecha) {
    List<Double> angulos = new ArrayList<Double>();
    for (CorrelacionEstaciones c : this.getCorrelaciones(fecha)) {
      double ang = this.getAngulo(c.getCorrelacionado(this.estacion));
      angulos.add(ang);
    }

    return calcularCobertura(angulos);
  }

  public List<CorrelacionEstaciones> getCorrelaciones(Date fecha) {
    this.seek(fecha);

    List<CorrelacionEstaciones> rv = new ArrayList<CorrelacionEstaciones>();

    for (int i = 0; i < this.corrs.size(); i++) {
      if (this.validezIndices.get(i)) {
        rv.add(this.corrs.get(i));
      }
    }

    return rv;
  }

  public double getAngulo(Estacion estVecina) {
    int lon = estVecina.getLongitud() - this.getEstacion().getLongitud();
    int lat = estVecina.getLatitud() - this.getEstacion().getLatitud();
    return (Math.atan2(lon, lat) / Math.PI / 2) + 0.5;
  }

  private static double calcularCobertura(List<Double> angulos) {
    double rv;
    if (angulos.size() < 2) {
      rv = 0;

    } else {
      Collections.sort(angulos);
      angulos.add(angulos.get(0) + 1);

      double maxAngDescub = 0;

      for (int i = 0; i < angulos.size() - 1; i++) {
        double ang = angulos.get(i + 1) - angulos.get(i);
        maxAngDescub = Math.max(maxAngDescub, ang);
      }

      rv = 360d * (1 - maxAngDescub);
    }

    return rv;
  }

  public Estacion getEstacion() {
    return estacion;
  }
}
