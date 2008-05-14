package ar.uba.dcao.dbclima.graficos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.util.Calendar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosEstacion;
import ar.uba.dcao.dbclima.eventosMeteo.AnalizadorEventosFactory;
import ar.uba.dcao.dbclima.eventosMeteo.DefinicionTemporadas;
import ar.uba.dcao.dbclima.eventosMeteo.FiltroEvento;
import ar.uba.dcao.dbclima.eventosMeteo.FiltrosEvento;
import ar.uba.dcao.dbclima.eventosMeteo.ProyectoresConfiables;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeFeatures;

@SuppressWarnings("serial")
public class RepCantidadEventosActual implements RepresentacionEstacion {

  private static final ImageObserver nullObserver = new ImageObserver() {
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
      return false;
    }
  };

  private static final Calendar CAL = Calendar.getInstance();

  private static final int ANIO_INICIO = 1980;

  private static final int ANIO_FIN = 2005;

  private static final int MIN_TEMPS_VALIDO = 22;

  private static final double MIN_COBERT = 0.9d;

  private static final DefinicionTemporadas TEMPORADA_ANIO = new DefinicionTemporadas(1, 366);

  private JFreeChart chart;

  private int[] evsXAnio;

  private int maxEvsAnio;

  private int lon;

  private int lat;

  private int repHeight = 10;

  private boolean tieneSuficientesDatos;

  public Shape drawIcon(Graphics g, Point vertex) {
    int width;

    if (!this.tieneSuficientesDatos) {
      width = 2;
      repHeight = 2;
      g.setColor(new Color(1f, 0f, 0f, 0.3f));
      g.drawChars(new char[] { 'X' }, 0, 1, vertex.x, vertex.y);

    } else {

      double sumaEvs = 0;
      double aniosEvs = 0;
      for (int i = 0; i < evsXAnio.length; i++) {
        if (evsXAnio[i] > 0) {
          sumaEvs += evsXAnio[i];
          aniosEvs++;
        }
      }

      double meanEvsXAnio = sumaEvs / aniosEvs;

      width = (ANIO_FIN - ANIO_INICIO) / 2;
      vertex = new Point(vertex.x - width / 2, vertex.y - repHeight / 2);

      g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.5f));
      g.fillRect(vertex.x - 2, vertex.y - 3, evsXAnio.length / 2 + 2, repHeight + 2);

      for (int i = 2; i < evsXAnio.length - 1; i += 2) {
        double evsAnio = (evsXAnio[i - 2] + evsXAnio[i - 1] + evsXAnio[i] + evsXAnio[i + 1]) / 5d;

        int altura = (int) (evsAnio / meanEvsXAnio * repHeight);
        altura = Math.min(altura, repHeight);

        g.setColor(Color.BLUE);
        g.fillRect(vertex.x - 1 + i / 2, vertex.y - 1 + repHeight - altura, 1, altura);
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

    ProyectorRegistro proy = ProyectoresConfiables.buildProyectorTMinConfiable(ConfianzaVariable.LIMITROFE);
    AnalizadorEventosFactory aeFactory = new AnalizadorEventosFactory(e, proy, TEMPORADA_ANIO);
    FiltroEvento helada0 = FiltrosEvento.buildFiltroPorTope(-1, false, true);
    FiltroEvento helada2 = FiltrosEvento.buildFiltroPorTope(-21, false, true);
    FiltroEvento helada5 = FiltrosEvento.buildFiltroPorTope(-51, false, true);

    AnalizadorEventosEstacion ae0 = aeFactory.createAnalizadorEventos(helada0);
    AnalizadorEventosEstacion ae2 = aeFactory.createAnalizadorEventos(helada2);
    AnalizadorEventosEstacion ae5 = aeFactory.createAnalizadorEventos(helada5);

    this.maxEvsAnio = 0;
    evsXAnio = new int[ANIO_FIN - ANIO_INICIO + 1];
    double[][] eventosPorAnio0 = new double[2][ANIO_FIN - ANIO_INICIO + 1];
    double[][] eventosPorAnio2 = new double[2][ANIO_FIN - ANIO_INICIO + 1];
    double[][] eventosPorAnio5 = new double[2][ANIO_FIN - ANIO_INICIO + 1];

    double[][] soportePorAnio = new double[2][ANIO_FIN - ANIO_INICIO + 1];

    int temporadasValidas = 0;

    DefinicionTemporadas temporadaEventos = ae0.getTemporadaEventos(0, 0.65);
    if (temporadaEventos == null) {
      temporadaEventos = TEMPORADA_ANIO;
    }

    int primerDiaHeladas = temporadaEventos.getPrimerDiaTemporada();
    int ultimoDiaHeladas = temporadaEventos.getUltimoDiaTemporada();

    int eventos0 = 0;

    for (int i = ANIO_INICIO; i <= ANIO_FIN; i++) {
      int evs0EnAnio = ae0.getTemporadasCubiertas().contains(i) ? ae0.getTemporadaEventos(i).getCantidadEventos()
          : 0;
      int evs2EnAnio = ae2.getTemporadasCubiertas().contains(i) ? ae2.getTemporadaEventos(i).getCantidadEventos()
          : 0;
      int evs5EnAnio = ae5.getTemporadasCubiertas().contains(i) ? ae5.getTemporadaEventos(i).getCantidadEventos()
          : 0;

      eventos0 += evs0EnAnio;

      TemporadaDeFeatures featsTemp = ae0.getTemporadaFeatures(i);
      Integer soporteEnAnio = featsTemp == null ? 0 : featsTemp.getCantidadRegistrosTemporada();

      if (featsTemp != null && soporteEnAnio > 0 && featsTemp.getCoberturaEnPeriodo(primerDiaHeladas, ultimoDiaHeladas) > MIN_COBERT) {
        temporadasValidas++;
      }

      CAL.set(Calendar.YEAR, i);
      eventosPorAnio0[0][i - ANIO_INICIO] = CAL.getTimeInMillis();
      eventosPorAnio0[1][i - ANIO_INICIO] = evs0EnAnio;

      eventosPorAnio2[0][i - ANIO_INICIO] = CAL.getTimeInMillis();
      eventosPorAnio2[1][i - ANIO_INICIO] = evs2EnAnio;

      eventosPorAnio5[0][i - ANIO_INICIO] = CAL.getTimeInMillis();
      eventosPorAnio5[1][i - ANIO_INICIO] = evs5EnAnio;

      soportePorAnio[0][i - ANIO_INICIO] = CAL.getTimeInMillis();
      soportePorAnio[1][i - ANIO_INICIO] = soporteEnAnio;

      evsXAnio[i - ANIO_INICIO] = evs0EnAnio;
      this.maxEvsAnio = Math.max(this.maxEvsAnio, evs0EnAnio);
    }

    this.tieneSuficientesDatos = temporadasValidas >= MIN_TEMPS_VALIDO;

    if (this.tieneSuficientesDatos) {
      /* Inicializacion de los graficos.... */

      String timeAxisLabel = "Año";
      String valueAxisLabel = "Eventos";

      DefaultXYDataset eventosDS = new DefaultXYDataset();
      DefaultXYDataset soporteDS = new DefaultXYDataset();

      eventosDS.addSeries("Heladas", eventosPorAnio0);
      eventosDS.addSeries("Heladas a -2", eventosPorAnio2);
      eventosDS.addSeries("Heladas a -5", eventosPorAnio5);

      // eventosDS.addSeries("Dummy", dummyBar);
      soporteDS.addSeries("Soporte", soportePorAnio);

      String facts = " (Tmps: " + ae0.getTemporadasCubiertas().size();
      facts += ". Evs: " + eventos0 + ")";

      String title = (e.getNombre() == null ? "" : e.getNombre()) + facts;
      this.chart = ChartFactory.createTimeSeriesChart(title, timeAxisLabel, valueAxisLabel, eventosDS, true,
          false, false);

      NumberAxis timeAxis = new NumberAxis("Soporte");

      this.chart.setBackgroundImageAlpha(1);
      XYPlot plot = this.chart.getXYPlot();
      plot.setRangeAxis(1, timeAxis);
      plot.setDataset(1, soporteDS);
      plot.mapDatasetToRangeAxis(1, 1);
      plot.setRenderer(1, new StandardXYItemRenderer());
      plot.getRenderer(1).setSeriesPaint(0, Color.black);

      plot.getRangeAxis(0).setRange(0, this.maxEvsAnio + 2);
      plot.getRangeAxis(1).setRange(0, 370);

    }
  }
}
