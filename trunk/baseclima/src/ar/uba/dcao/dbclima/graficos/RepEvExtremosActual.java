package ar.uba.dcao.dbclima.graficos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosEstacion;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosFactory;
import ar.uba.dcao.dbclima.eventosMeteo.DefinicionTemporadas;
import ar.uba.dcao.dbclima.eventosMeteo.EventoExtremo;
import ar.uba.dcao.dbclima.eventosMeteo.FiltroEvento;
import ar.uba.dcao.dbclima.eventosMeteo.FiltrosEvento;
import ar.uba.dcao.dbclima.eventosMeteo.ProyectoresConfiables;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeEventos;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeFeatures;

@SuppressWarnings("serial")
public class RepEvExtremosActual implements RepresentacionEstacion {

  private static final ImageObserver nullObserver = new ImageObserver() {
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
      return false;
    }
  };

  private static final Calendar CAL = Calendar.getInstance();

  private static final int ANIO_INICIO = 1980;

  private static final int ANIO_FIN = 2005;

  private static final int MIN_TEMPS_VALIDO = 22;

  private static final double MIN_SOPORTE_TEMP = 0.9d;

  private static final double MIN_SOPORTE_EV = 0.65d;

  private static final byte MAX_DESCONFIANZA = ConfianzaVariable.LIMITROFE;

  private static final DefinicionTemporadas TEMPORADA_ANIO = new DefinicionTemporadas(1, 366);

  private static final ProyectorRegistro PROY_TMIN_CONFIABLE = ProyectoresConfiables
      .buildProyectorTMinConfiable(MAX_DESCONFIANZA);

  private static final FiltroEvento FILTRO_HELADA_0 = FiltrosEvento.buildFiltroPorTope(-1, false, true);

  private static final FiltroEvento FILTRO_HELADA_2 = FiltrosEvento.buildFiltroPorTope(-21, false, true);

  private static final int MIN_PERIODO_UNCHECKED = 20;

  private JFreeChart chart;

  private double[] priEvPorAnio;

  private double[] ultEvPorAnio;

  private int lon;

  private int lat;

  private int repHeight = 9;

  private boolean tieneSuficientesDatos;

  public Shape drawIcon(Graphics g, Point vertex) {
    int width;

    if (!this.tieneSuficientesDatos) {
      width = 2;
      repHeight = 2;
      g.setColor(new Color(1f, 0f, 0f, 0.3f));
      g.drawChars(new char[] { 'X' }, 0, 1, vertex.x, vertex.y);

    } else {

      width = (ANIO_FIN - ANIO_INICIO);
      vertex = new Point(vertex.x - width / 2, vertex.y - repHeight / 2);

      g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.7f));
      g.fillRect(vertex.x - 2, vertex.y - 3, width, repHeight + 2);

      for (int i = ANIO_INICIO; i < ANIO_FIN; i++) {
        int alturaPr = (int) (this.priEvPorAnio[i - ANIO_INICIO] / 366d * repHeight);
        int alturaUl = (int) (this.ultEvPorAnio[i - ANIO_INICIO] / 366d * repHeight);

        if (this.priEvPorAnio[i - ANIO_INICIO] > TEMPORADA_ANIO.getPrimerDiaTemporada()) {
          g.setColor(Color.BLUE);
          g.fillRect(vertex.x - 1 + (i - ANIO_INICIO), vertex.y - 1 + repHeight - alturaPr, 1, 1);
        }

        if (this.ultEvPorAnio[i - ANIO_INICIO] < TEMPORADA_ANIO.getUltimoDiaTemporada()) {
          g.setColor(Color.GREEN);
          g.fillRect(vertex.x - 1 + (i - ANIO_INICIO), vertex.y - 1 + repHeight - alturaUl, 1, 1);
        }
      }
    }

    return new Rectangle(vertex.x - 1, vertex.y - 1, width, repHeight);
  }

  public void drawRepresentation(Graphics g, Rectangle bounds) {
    if (this.tieneSuficientesDatos) {
      Image i = chart.createBufferedImage(bounds.width, bounds.height);
      g.drawImage(i, bounds.x, bounds.y, nullObserver);
    }
  }

  public Point getPosition() {
    return new Point(this.lon, this.lat);
  }

  public void init(Estacion e) {
    this.lon = e.getLongitud();
    this.lat = e.getLatitud();

    this.priEvPorAnio = new double[ANIO_FIN - ANIO_INICIO + 1];
    this.ultEvPorAnio = new double[ANIO_FIN - ANIO_INICIO + 1];

    AnalizadorEventosFactory aef = new AnalizadorEventosFactory(e, PROY_TMIN_CONFIABLE, TEMPORADA_ANIO);
    AnalizadorEventosEstacion analizadorHeladas0 = aef.createAnalizadorEventos(FILTRO_HELADA_0);
    AnalizadorEventosEstacion analizadorHeladas2 = aef.createAnalizadorEventos(FILTRO_HELADA_2);

    int temporadasValidas = 0;

    /* Si la estacion no tiene actividad suficiente durante la temporada estudiada, se marca. */
    CAL.setTime(e.getFechaInicio());
    int anioIniEst = Math.max(ANIO_INICIO, CAL.get(Calendar.YEAR));

    CAL.setTime(e.getFechaFin());
    int anioFinEst = Math.min(ANIO_FIN, CAL.get(Calendar.YEAR));
    this.tieneSuficientesDatos = Math.min(anioFinEst, ANIO_FIN) - Math.max(anioIniEst, ANIO_INICIO) >= MIN_TEMPS_VALIDO;

    DefinicionTemporadas epocaEventos = analizadorHeladas0.getTemporadaEventos(0, 0.65);
    if (epocaEventos == null) {
      epocaEventos = TEMPORADA_ANIO;
    }

    int primerDiaHeladas = epocaEventos.getPrimerDiaTemporada();
    int ultimoDiaHeladas = epocaEventos.getUltimoDiaTemporada();
    SerieEventos series = new SerieEventos(5);
    
    for (int i = ANIO_INICIO; i <= ANIO_FIN && tieneSuficientesDatos; i++) {
      TemporadaDeFeatures featsTemp = analizadorHeladas0.getTemporadaFeatures(i);
      Integer soporteEnAnio = featsTemp == null ? 0 : featsTemp.getCantidadRegistrosTemporada();

      if (featsTemp != null && soporteEnAnio > 0
          && featsTemp.getCoberturaEnPeriodo(primerDiaHeladas, ultimoDiaHeladas) > MIN_SOPORTE_TEMP) {
        temporadasValidas++;
      }

      CAL.set(Calendar.DAY_OF_YEAR, 1);
      CAL.set(Calendar.YEAR, i);

      if (!analizadorHeladas0.getTemporadasCubiertas().contains(i)) {
        continue;
      }

      TemporadaDeEventos temporadaEvs0 = analizadorHeladas0.getTemporadaEventos(i);
      TemporadaDeEventos temporadaEvs2 = analizadorHeladas2.getTemporadaEventos(i);

      EventoExtremo priEv0 = temporadaEvs0.getPrimerEvento();
      EventoExtremo ultEv0 = temporadaEvs0.getUltimoEvento();

      EventoExtremo priEv2 = temporadaEvs2.getPrimerEvento();
      EventoExtremo ultEv2 = temporadaEvs2.getUltimoEvento();

      Date date = CAL.getTime();
      if (priEv0 != null) {
        this.priEvPorAnio[i - ANIO_INICIO] = priEv0.getDiaDelAnio() - 1;
        series.registrarEvento(date, 0, tieneCobertura(priEv0, featsTemp), priEv0.getDiaDelAnio() - 1);
      }
      if (ultEv0 != null) {
        this.ultEvPorAnio[i - ANIO_INICIO] = ultEv0.getDiaDelAnio() - 1;
        series.registrarEvento(date, 1, tieneCobertura(ultEv0, featsTemp), ultEv0.getDiaDelAnio() - 1);
      }
      if (priEv2 != null) {
        series.registrarEvento(date, 2, tieneCobertura(priEv2, featsTemp), priEv2.getDiaDelAnio() - 1);
      }
      if (ultEv2 != null) {
        series.registrarEvento(date, 3, tieneCobertura(ultEv2, featsTemp), ultEv2.getDiaDelAnio() - 1);
      }
      series.registrarEvento(date, 4, true, soporteEnAnio);
    }

    this.tieneSuficientesDatos = this.tieneSuficientesDatos && temporadasValidas >= MIN_TEMPS_VALIDO;

    if (this.tieneSuficientesDatos) {
      /* Inicializacion de los graficos.... */

      String timeAxisLabel = "Año";
      String valueAxisLabel = "Evento Extr";

      DefaultXYDataset dataset = new DefaultXYDataset();
      dataset.addSeries("1era Helada", series.getSerie(0, true));
      dataset.addSeries("1era Heladas a -2", series.getSerie(2, true));
      dataset.addSeries("Ult Helada", series.getSerie(1, true));
      dataset.addSeries("Ult Heladas a -2", series.getSerie(3, true));

      dataset.addSeries("Cant Registros", series.getSerie(4, true));

      String title = (e.getNombre() == null ? "" : e.getNombre());
      this.chart = ChartFactory.createTimeSeriesChart(title, timeAxisLabel, valueAxisLabel, dataset, true, false,
          false);

      this.chart.setBackgroundImageAlpha(1);
      XYPlot plot = this.chart.getXYPlot();
      plot.getRangeAxis().setRange(0, 370);

      Color tBlue = new Color(0, 0, 255, 128);
      Color tGreen = new Color(0, 255, 0, 128);
      Color tRed = new Color(255, 0, 0, 128);
      // Color tBlack = new Color(0, 0, 0, 128);
      // Color tOrange = new Color(255, 200, 0, 128);

      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(0);
      renderer.setShapesVisible(true);
      renderer.setLinesVisible(false);

      renderer.setSeriesPaint(0, tRed);
      renderer.setSeriesPaint(1, tBlue);
      renderer.setSeriesPaint(2, tGreen);
      renderer.setSeriesPaint(3, Color.BLACK);
      renderer.setSeriesPaint(4, Color.ORANGE);
    }
  }

  private static boolean tieneCobertura(EventoExtremo evEx, TemporadaDeFeatures featTemp) {
    boolean rv;

    if (evEx == null) {
      rv = false;

    } else {
      int diaEvento = TEMPORADA_ANIO.diaTemporada(evEx.getFecha());
      int primerDiaCheck;
      int ultimoDiaCheck;

      if (evEx.isPrimerEvento()) {
        primerDiaCheck = Math.max(TEMPORADA_ANIO.getPrimerDiaTemporada(), diaEvento - 30);
        ultimoDiaCheck = diaEvento;

      } else {
        primerDiaCheck = diaEvento + 1;
        ultimoDiaCheck = Math.min(TEMPORADA_ANIO.getUltimoDiaTemporada(), diaEvento + 31);
      }

      boolean periodoCorto = (ultimoDiaCheck - primerDiaCheck < MIN_PERIODO_UNCHECKED);
      rv = periodoCorto || featTemp.getCoberturaEnPeriodo(primerDiaCheck, ultimoDiaCheck) >= MIN_SOPORTE_EV;
    }

    return rv;
  }

  private static class SerieEventos implements Serializable {

    List<Map<Long, Integer>> series = new ArrayList<Map<Long, Integer>>();

    public SerieEventos(int cantSeries) {
      for (int i = 0; i < cantSeries * 2; i++) {
        series.add(new HashMap<Long, Integer>());
      }
    }

    public void registrarEvento(Date fecha, int serie, boolean tieneCobertura, int valor) {
      int sNum = serie * 2 + (tieneCobertura ? 1 : 0);
      series.get(sNum).put(fecha.getTime(), valor);
    }

    public double[][] getSerie(int serie, boolean conCobertura) {
      Map<Long, Integer> unaSerie = this.series.get(serie * 2 + (conCobertura ? 1 : 0));
      double[][] rv = new double[2][unaSerie.size()];

      int i = 0;
      for (Map.Entry<Long, Integer> entry : unaSerie.entrySet()) {
        rv[0][i] = entry.getKey();
        rv[1][i] = entry.getValue();
        i++;
      }

      return rv;
    }
  }
}
