package ar.uba.dcao.dbclima.graficos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.text.DecimalFormat;
import java.util.Calendar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
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
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeEventos;
import ar.uba.dcao.dbclima.eventosMeteo.TemporadaDeFeatures;

@SuppressWarnings("serial")
public class RepTendenciaCantidadEventos implements RepresentacionEstacion {

  private static final ImageObserver nullObserver = new ImageObserver() {
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
      return false;
    }
  };

  private static final DecimalFormat FRMT = new DecimalFormat("0.000");

  private static final Calendar CAL = Calendar.getInstance();

  private static final int ANIO_INICIO = 1959;

  private static final int ANIO_FIN = 2005;

  private static final int MIN_TEMPS_VALIDO = 40;

  private static final double MIN_RATIO_DIAS_TEMP = 0.85d;

  private static final byte MAX_DESCONFIANZA = ConfianzaVariable.LIMITROFE;

  private static final DefinicionTemporadas TEMPORADA_ANIO = new DefinicionTemporadas(1, 366);

  private static final ProyectorRegistro PROY_TMIN_CONFIABLE = ProyectoresConfiables
      .buildProyectorTMinConfiable(MAX_DESCONFIANZA);

  private static final FiltroEvento FILTRO_HELADA_0 = FiltrosEvento.buildFiltroPorTope(-1, false, true);
  private static final FiltroEvento FILTRO_HELADA_2 = FiltrosEvento.buildFiltroPorTope(-21, false, true);
  private static final FiltroEvento FILTRO_HELADA_5 = FiltrosEvento.buildFiltroPorTope(-51, false, true);

  private JFreeChart chart;

  private int[] evsXAnio;

  private int maxEvsAnio;

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

    AnalizadorEventosFactory aef = new AnalizadorEventosFactory(e, PROY_TMIN_CONFIABLE, TEMPORADA_ANIO);
    AnalizadorEventosEstacion analizadorHeladas0 = aef.createAnalizadorEventos(FILTRO_HELADA_0);
    AnalizadorEventosEstacion analizadorHeladas2 = aef.createAnalizadorEventos(FILTRO_HELADA_2);
    AnalizadorEventosEstacion analizadorHeladas5 = aef.createAnalizadorEventos(FILTRO_HELADA_5);

    this.maxEvsAnio = 0;
    evsXAnio = new int[ANIO_FIN - ANIO_INICIO + 1];
    double[][] eventosPorAnio0 = new double[2][ANIO_FIN - ANIO_INICIO + 1];
    double[][] eventosPorAnio2 = new double[2][ANIO_FIN - ANIO_INICIO + 1];
    double[][] eventosPorAnio5 = new double[2][ANIO_FIN - ANIO_INICIO + 1];
    double[][] soportePorAnio = new double[2][ANIO_FIN - ANIO_INICIO + 1];

    int temporadasValidas = 0;

    int PRIMER_DIA_TEMP_HELADA = 90;
    int ULT_DIA_TEMP_HELADA = 300;

    int eventosEstacion = 0;
    for (int i = ANIO_INICIO; i <= ANIO_FIN; i++) {
      TemporadaDeFeatures tempFeats = analizadorHeladas0.getTemporadaFeatures(i);
      Integer soporteEnAnio = tempFeats != null ? tempFeats.getCantidadRegistrosTemporada() : 0;

      TemporadaDeEventos tempEventos0 = analizadorHeladas0.getTemporadaEventos(i);
      Integer evsEnAnio0 = tempEventos0 != null ? tempEventos0.getCantidadEventos() : 0;
      TemporadaDeEventos tempEventos2 = analizadorHeladas2.getTemporadaEventos(i);
      Integer evsEnAnio2 = tempEventos2 != null ? tempEventos2.getCantidadEventos() : 0;
      TemporadaDeEventos tempEventos5 = analizadorHeladas5.getTemporadaEventos(i);
      Integer evsEnAnio5 = tempEventos5 != null ? tempEventos5.getCantidadEventos() : 0;

      eventosEstacion += tempEventos0 == null ? 0 : tempEventos0.getCantidadEventos();

      if (tempFeats != null && soporteEnAnio > 0) {
        int cantRegsTemporada = tempFeats.getRegistrosEnRango(PRIMER_DIA_TEMP_HELADA, ULT_DIA_TEMP_HELADA);

        if (cantRegsTemporada > (ULT_DIA_TEMP_HELADA - PRIMER_DIA_TEMP_HELADA) * MIN_RATIO_DIAS_TEMP) {
          temporadasValidas++;
        }
      }

      CAL.set(Calendar.YEAR, i);
      eventosPorAnio0[0][i - ANIO_INICIO] = CAL.getTimeInMillis();
      eventosPorAnio0[1][i - ANIO_INICIO] = evsEnAnio0;
      eventosPorAnio2[0][i - ANIO_INICIO] = CAL.getTimeInMillis();
      eventosPorAnio2[1][i - ANIO_INICIO] = evsEnAnio2;
      eventosPorAnio5[0][i - ANIO_INICIO] = CAL.getTimeInMillis();
      eventosPorAnio5[1][i - ANIO_INICIO] = evsEnAnio5;

      soportePorAnio[0][i - ANIO_INICIO] = CAL.getTimeInMillis();
      soportePorAnio[1][i - ANIO_INICIO] = soporteEnAnio;

      evsXAnio[i - ANIO_INICIO] = evsEnAnio0;
      this.maxEvsAnio = Math.max(this.maxEvsAnio, evsEnAnio0);
    }

    this.tieneSuficientesDatos = temporadasValidas >= MIN_TEMPS_VALIDO;

    if (this.tieneSuficientesDatos) {
      /* Inicializacion de los graficos.... */
      double dayRatio = this.maxEvsAnio / 366d;
      for (int i = ANIO_INICIO; i <= ANIO_FIN; i++) {
        soportePorAnio[1][i - ANIO_INICIO] *= dayRatio;
      }

      String timeAxisLabel = "Año";
      String valueAxisLabel = "Eventos";

      DefaultXYDataset dataset = new DefaultXYDataset();
      dataset.addSeries("Heladas 0", eventosPorAnio0);
      dataset.addSeries("Heladas -2", eventosPorAnio2);
      dataset.addSeries("Heladas -5", eventosPorAnio5);
      dataset.addSeries("Soporte * " + FRMT.format(dayRatio), soportePorAnio);

      String facts = " (Tmps: " + analizadorHeladas0.getTemporadasCubiertas().size();
      facts += ". Evs: " + eventosEstacion + ")";

      String title = (e.getNombre() == null ? "" : e.getNombre()) + facts;
      this.chart = ChartFactory.createTimeSeriesChart(title, timeAxisLabel, valueAxisLabel, dataset, true, false,
          false);
      this.chart.setBackgroundImageAlpha(1);
    }
  }
}
