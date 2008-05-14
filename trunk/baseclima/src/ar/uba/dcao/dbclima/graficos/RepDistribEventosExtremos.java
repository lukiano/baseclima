package ar.uba.dcao.dbclima.graficos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

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
import ar.uba.dcao.dbclima.utils.CollectionUtils;

@SuppressWarnings("serial")
public class RepDistribEventosExtremos implements RepresentacionEstacion {

  private static final ImageObserver nullObserver = new ImageObserver() {
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
      return false;
    }
  };

  private static Calendar CAL = Calendar.getInstance();

  private static final double SOPORTE_MIN = 0.65d;

  private static final byte MAX_DESCONFIANZA = ConfianzaVariable.LIMITROFE;

  private static final DefinicionTemporadas TEMPORADA_ANIO = new DefinicionTemporadas(1, 366);

  private static final ProyectorRegistro PROY_TMIN_CONFIABLE = ProyectoresConfiables
      .buildProyectorTMinConfiable(MAX_DESCONFIANZA);

  private static final FiltroEvento FILTRO_HELADA = FiltrosEvento.buildFiltroPorTope(-1, false, true);

  private JFreeChart chart;

  private Double[] probsEnDia;

  private int temporadasConRegs;

  private int lon;

  private int lat;

  public Shape drawIcon(Graphics g, Point vertex) {
    int width;

    width = probsEnDia.length;
    vertex = new Point(vertex.x - width / 2, vertex.y - width / 2);

    if (temporadasConRegs < 5) {
      g.setColor(Color.RED);
      g.drawChars(new char[] { 'X' }, 0, 1, vertex.x, vertex.y);

    } else {
      for (int i = 0; i < probsEnDia.length; i++) {
        Double probEv = probsEnDia[i];

        Color color = probEv != null ? new Color((float) (1 - probEv), (float) (1 - probEv), 1f) : Color.BLACK;

        g.setColor(color);
        g.fillRect(vertex.x - 1 + i, vertex.y - 1, 1, 6);
      }
    }

    return new Rectangle(vertex.x - 1, vertex.y - 1, width, 6);
  }

  public void drawRepresentation(Graphics g, Rectangle bounds) {
    Image i = chart.createBufferedImage(bounds.width, bounds.height);
    g.drawImage(i, bounds.x, bounds.y, nullObserver);
  }

  public Point getPosition() {
    return new Point(this.lon, this.lat);
  }

  public void init(Estacion e) {
    this.lon = e.getLongitud();
    this.lat = e.getLatitud();

    AnalizadorEventosFactory aef = new AnalizadorEventosFactory(e, PROY_TMIN_CONFIABLE, TEMPORADA_ANIO);
    AnalizadorEventosEstacion analizadorHeladas = aef.createAnalizadorEventos(FILTRO_HELADA);

    List<Double> primerosEventos = new ArrayList<Double>();
    List<Double> ultimosEventos = new ArrayList<Double>();

    int eventosEstacion = 0;
    for (Integer anioTemp : analizadorHeladas.getTemporadasCubiertas()) {
      TemporadaDeEventos unaTemporada = analizadorHeladas.getTemporadaEventos(anioTemp);
      eventosEstacion += unaTemporada.getCantidadEventos();

      if (unaTemporada.getPrimerEvento() != null && unaTemporada.getPrimerEvento().getSoporte() > SOPORTE_MIN) {
        primerosEventos.add((double) unaTemporada.getPrimerEvento().getDiaDelAnio());
      }

      if (unaTemporada.getUltimoEvento() != null && unaTemporada.getUltimoEvento().getSoporte() > SOPORTE_MIN) {
        ultimosEventos.add((double) unaTemporada.getUltimoEvento().getDiaDelAnio());
      }
    }

    Collections.sort(primerosEventos, TEMPORADA_ANIO.getComparadorDiaAsDouble());
    Collections.sort(ultimosEventos, TEMPORADA_ANIO.getComparadorDiaAsDouble());

    this.temporadasConRegs = analizadorHeladas.getTemporadasCubiertas().size();

    probsEnDia = new Double[24];
    double[][] probGrafico = new double[2][26];
    for (int i = 1; i <= 24; i++) {
      Double probEventoEnDiaAnio = probEventoEnDiaAnio(i * 15, primerosEventos, ultimosEventos);

      CAL.set(Calendar.YEAR, 2000);
      CAL.set(Calendar.DAY_OF_YEAR, i * 15);

      probGrafico[0][i - 1] = CAL.getTimeInMillis();
      probGrafico[1][i - 1] = probEventoEnDiaAnio == null ? 0 : probEventoEnDiaAnio;

      probsEnDia[i - 1] = probEventoEnDiaAnio;
    }

    CAL.set(Calendar.YEAR, 2000);
    CAL.set(Calendar.DAY_OF_YEAR, 366);
    probGrafico[0][24] = CAL.getTimeInMillis();
    probGrafico[1][24] = probGrafico[1][23];
    probGrafico[0][25] = CAL.getTimeInMillis();
    probGrafico[1][25] = 1d;

    String timeAxisLabel = "Dia del año";
    String valueAxisLabel = "Prob Evento";

    DefaultXYDataset dataset = new DefaultXYDataset();
    dataset.addSeries("", probGrafico);

    String facts = " (Tmps: " + analizadorHeladas.getTemporadasCubiertas().size();
    facts += ". Evs: " + eventosEstacion + ")";

    String title = (e.getNombre() == null ? "" : e.getNombre()) + facts;
    this.chart = ChartFactory.createTimeSeriesChart(title, timeAxisLabel, valueAxisLabel, dataset, true, false,
        false);
    this.chart.setBackgroundImageAlpha(1);
  }

  private static Double probEventoEnDiaAnio(int diaAnio, List<Double> primerosEventos, List<Double> ultimosEventos) {

    for (double perc = 1d; perc >= 0; perc -= 0.04d) {
      Double prEv = CollectionUtils.percentilOrderedList(primerosEventos, perc);
      Double ulEv = CollectionUtils.percentilOrderedList(ultimosEventos, 1 - perc);

      if (prEv == null || ulEv == null) {
        return null;
      } else if (prEv < diaAnio && diaAnio < ulEv) {
        return perc;
      }
    }

    return 0d;
  }

}
