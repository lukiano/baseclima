package ar.uba.dcao.dbclima.casosDeUso.reportes.precip;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.EstacionHelper;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.utils.CollectionUtils;

public class PrecipitacionMesEstaciones {

  public static final int MODO_NORM_NADA = 0;

  public static final int MODO_NORM_LINEAL = 1;

  public static final int MODO_NORM_CLASSIC = 2;

  private static double[] DIAS_POR_MES = { 31, 28.7, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

  private static final int ANIO_INICIO = 1980;

  private static final int ANIO_FIN = 2005;

  private static final double RATIO_MIN_REGS = 0.85;

  private static final double RATIO_MIN_REGS_TEMP = 0.8;

  private static final DecimalFormat FRMT = new DecimalFormat("0.00");

  private static final int MIN_DIAS_VALIDEZ_ESTAD = 50;

  private int minDiasConReg;

  private int minDiasTemp;

  private int mesAnio;

  private List<PrecipitacionMesEstacion> precipitacionesMes = new ArrayList<PrecipitacionMesEstacion>();

  private List<Double> ratiosPrecip = new ArrayList<Double>();

  /* Arreglo con 10 posiciones para percentiles, 1 para cociente y 3 para coordenadas */
  private ArrayList[] features;

  public PrecipitacionMesEstaciones(int mesAnio) {
    this.mesAnio = mesAnio;
    this.minDiasConReg = (int) ((ANIO_FIN - ANIO_INICIO + 1) * DIAS_POR_MES[mesAnio] * RATIO_MIN_REGS);
    this.minDiasTemp = (int) ((ANIO_FIN - ANIO_INICIO + 1) * DIAS_POR_MES[mesAnio] * RATIO_MIN_REGS_TEMP);

    try {
      this.features = new ArrayList[34];
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    for (int i = 0; i < this.features.length; i++) {
      this.features[i] = new ArrayList<Double>();
    }
  }

  public void emitirReporte(int modo) {
    for (int i = 0; i < this.features.length; i++) {
      Collections.sort((List<Double>) this.features[i]);
    }

    List<Double> alturasLog = new ArrayList<Double>();
    for (Double alt : (List<Double>) this.features[33]) {
      alturasLog.add(Math.log10(alt));
    }

    for (PrecipitacionMesEstacion p : this.precipitacionesMes) {
      double ratioPrec = p.getDiasPrecipitacion() / (double) p.getRegistros();

      int modoPos = Math.min(MODO_NORM_LINEAL, modo);

      double latNorm = normalizar(p.getLatitud(), this.features[31], modoPos);
      double lonNorm = normalizar(p.getLongitud(), this.features[32], modoPos);
      double logAltura = Math.log10(p.getAltura());
      double altNorm = normalizar(logAltura, alturasLog, modoPos);

      double ratioPrecNorm = normalizar(ratioPrec, this.features[30], modo);

      String nombre = p.getNombreEstacion() + " <" + p.getLatitud() + ":" + p.getLongitud() + ":" + p.getAltura()
          + ">";
      if (p.getDiasPrecipitacion() < MIN_DIAS_VALIDEZ_ESTAD) {
        nombre = "* " + nombre;
      }

      Double latO = p.getLatitud() / -100d;
      Double lonO = p.getLongitud() / -100d;

      String rep = nombre + "," + FRMT.format(latO) + "," + FRMT.format(lonO) + "," + this.mesAnio + ","
          + FRMT.format(latNorm) + "," + FRMT.format(lonNorm) + "," + FRMT.format(altNorm);

      rep += "," + FRMT.format(ratioPrecNorm);

      for (int i = 0; i < 10; i++) {
        double percI = p.getDistribucionPrecipitacion()[i];
        double percINorm = normalizar(percI, this.features[i], modo);
        rep += "," + FRMT.format(percINorm);
      }

      for (int i = 10; i < 20; i++) {
        double percI = p.getDistTmin()[i - 10];
        double percINorm = normalizar(percI, this.features[i], modo);
        rep += "," + FRMT.format(percINorm);
      }

      for (int i = 20; i < 30; i++) {
        double percI = p.getDistTmax()[i - 20];
        double percINorm = normalizar(percI, this.features[i], modo);
        rep += "," + FRMT.format(percINorm);
      }

      System.out.println(rep);
    }
  }

  private double normalizar(double val, List<Double> muestra, int modoNorm) {
    double rv;
    if (modoNorm == MODO_NORM_NADA) {
      rv = val;

    } else if (modoNorm == MODO_NORM_LINEAL) {
      double min = Collections.min(muestra);
      double max = Collections.max(muestra);
      rv = ((val - min) / (max - min) * 2) - 1;

    } else if (modoNorm == MODO_NORM_CLASSIC) {
      double mean = CollectionUtils.avg(muestra);
      double stdv = CollectionUtils.stdv(muestra, mean);
      rv = (val - mean) / stdv;

    } else {
      throw new UnsupportedOperationException();
    }

    return rv;
  }

  public void registrarMesEstacion(Estacion e) {
    List<RegistroDiario>[] registrosPorMes = EstacionHelper.getRegistrosPorMes(e);

    int regsTotal = 0;
    List<Double> precipitaciones = new ArrayList<Double>();
    List<Double> tmins = new ArrayList<Double>();
    List<Double> tmaxs = new ArrayList<Double>();

    Calendar cal = Calendar.getInstance();
    for (RegistroDiario rd : registrosPorMes[this.mesAnio]) {
      cal.setTime(rd.getFecha());
      int anio = cal.get(Calendar.YEAR);

      if (anio >= ANIO_INICIO && anio <= ANIO_FIN) {
        regsTotal++;
        Integer prec = rd.getPrecipitacion();
        if (prec != null && prec > 0) {
          precipitaciones.add(prec.doubleValue());
        }
        if (rd.getTempMin() != null && rd.getConfianzaTempMin() != null
            && rd.getConfianzaTempMin().getConfianza() == 0) {
          tmins.add(rd.getTempMin().doubleValue());
        }
        if (rd.getTempMax() != null && rd.getConfianzaTempMax() != null
            && rd.getConfianzaTempMax().getConfianza() == 0) {
          tmaxs.add(rd.getTempMax().doubleValue());
        }
      }
    }

    Collections.sort(precipitaciones);
    Collections.sort(tmins);
    Collections.sort(tmaxs);

    double[] distPrec = new double[10];
    double[] distTmin = new double[10];
    double[] distTmax = new double[10];

    int i = 0;
    for (double per = 0.05; per < 1; per += 0.1d) {
      Double percentilIPrecip = CollectionUtils.percentilOrderedList(precipitaciones, per);
      distPrec[i++] = percentilIPrecip == null ? 0 : percentilIPrecip;
    }

    i = 0;
    for (double per = 0.05; per < 1; per += 0.1d) {
      Double percentilI = CollectionUtils.percentilOrderedList(tmins, per);
      distTmin[i++] = percentilI == null ? 0 : percentilI;
    }

    i = 0;
    for (double per = 0.05; per < 1; per += 0.1d) {
      Double percentilI = CollectionUtils.percentilOrderedList(tmaxs, per);
      distTmax[i++] = percentilI == null ? 0 : percentilI;
    }

    if (regsTotal >= this.minDiasConReg && tmins.size() >= this.minDiasTemp && tmaxs.size() >= this.minDiasTemp) {
      double ratioPrecip = precipitaciones.size() / (double) regsTotal;
      this.ratiosPrecip.add(ratioPrecip);
      for (i = 0; i < 10; i++) {
        this.features[i].add(distPrec[i]);
      }
      for (i = 10; i < 20; i++) {
        this.features[i].add(distTmin[i - 10]);
      }
      for (i = 20; i < 30; i++) {
        this.features[i].add(distTmax[i - 20]);
      }
      this.features[30].add(ratioPrecip);
      this.features[31].add(e.getLatitud().doubleValue());
      this.features[32].add(e.getLongitud().doubleValue());
      this.features[33].add(e.getAltura().doubleValue());

      PrecipitacionMesEstacion precipEst = new PrecipitacionMesEstacion(e.getNombre(), e.getLatitud(), e
          .getLongitud(), e.getAltura(), distPrec, distTmin, distTmax, precipitaciones.size(), regsTotal,
          this.mesAnio);

      this.precipitacionesMes.add(precipEst);
    }
  }
}