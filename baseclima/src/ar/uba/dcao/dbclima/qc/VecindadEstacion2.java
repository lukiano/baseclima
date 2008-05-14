package ar.uba.dcao.dbclima.qc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.FiltroRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.ResultadoTestQC;
import ar.uba.dcao.dbclima.qc.qc1.SequenceHomogeneityTest;

public class VecindadEstacion2 {

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
  private List<CorrelacionEstaciones> correlaciones;

  private Session sess;

  private FiltroRegistro filtro;

  private List<RegistroDiario> regsFecha;

  private List<CorrelacionEstaciones> corrsFecha;

  /* Estructura de datos para buscar registros eficientemente. */
  private List<List<Long>> idsEstacionesPorMes = new ArrayList<List<Long>>();

  /* Estructura de datos para buscar registros eficientemente. */
  private Map<String, CorrelacionEstaciones> corrsPorMesEstacion = new HashMap<String, CorrelacionEstaciones>();

  private Date fecha;

  public VecindadEstacion2(Estacion estacion, FiltroRegistro filtro, List<CorrelacionEstaciones> corrs, Session sess) {
    this.sess = sess;
    this.estacion = estacion;
    this.filtro = filtro;
    this.correlaciones = corrs;

    /* Listado de estaciones vecinas segun mes. */
    for (int i = 0; i < 12; i++) {
      this.idsEstacionesPorMes.add(new ArrayList<Long>());
    }
    for (CorrelacionEstaciones corr : this.correlaciones) {
      Estacion ev = corr.getCorrelacionado(this.estacion);
      this.idsEstacionesPorMes.get(corr.getMes()).add(ev.getId());
      String mapKey = corr.getMes() + " " + ev.getId();
      this.corrsPorMesEstacion.put(mapKey, corr);
    }
  }

  public double getAnguloCobertura(Date fecha) {
    this.seekFecha(fecha);
    return this.calcularCobertura();
  }

  public List<CorrelacionEstaciones> getCorrelaciones(Date fecha) {
    this.seekFecha(fecha);
    return this.corrsFecha;
  }

  public List<RegistroDiario> getRegistrosVecinos(Date fecha) {
    this.seekFecha(fecha);
    return this.regsFecha;
  }

  public Estacion getEstacion() {
    return estacion;
  }

  public double getAngulo(Estacion estVecina) {
    int lon = estVecina.getLongitud() - this.estacion.getLongitud();
    int lat = estVecina.getLatitud() - this.estacion.getLatitud();
    return (Math.atan2(lon, lat) / Math.PI / 2) + 0.5;
  }

  @SuppressWarnings("unchecked")
  private void seekFecha(Date fechaSeek) {
    if (this.fecha == null || fechaSeek.getTime() != this.fecha.getTime()) {
      cal.setTime(fechaSeek);
      int mes = cal.get(Calendar.MONTH);

      List<RegistroDiario> regs = new ArrayList<RegistroDiario>();
      List<CorrelacionEstaciones> corrs = new ArrayList<CorrelacionEstaciones>();

      List<RegistroDiario> regsFecha = Collections.emptyList();
      if (this.idsEstacionesPorMes.get(mes).size() > 0) {
        Query q = sess.createQuery("FROM RegistroDiario rd WHERE rd.estacion.id in (:idsEsts) AND rd.fecha = :fecha");
        q.setParameterList("idsEsts", this.idsEstacionesPorMes.get(mes));
        q.setDate("fecha", fechaSeek);
        regsFecha = q.list();
      }

      for (RegistroDiario vecino : regsFecha) {
        CorrelacionEstaciones corr = this.corrsPorMesEstacion.get(mes + " " + vecino.getEstacion().getId());
        if (this.filtro.aplicaA(vecino)) {
          corrs.add(corr);
          regs.add(vecino);
        }
      }

      this.fecha = fechaSeek;
      this.regsFecha = regs;
      this.corrsFecha = corrs;
    }
  }

  private double calcularCobertura() {
    List<Double> angulos = new ArrayList<Double>();
    for (RegistroDiario rd : this.regsFecha) {
      angulos.add(this.getAngulo(rd.getEstacion()));
    }

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
}