package ar.uba.dcao.dbclima.graficos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;

import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;

@SuppressWarnings("serial")
public class RepresentacionErroresXAnio implements RepresentacionEstacion {

  private static final ImageObserver nullObserver = new ImageObserver() {
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
      return false;
    }
  };

  private static final double CONST_DOUBT = 3.2;

  private static final double CONST_NCHECK = 39;

  private static final int BOX_WITDH = 10;

  private static final int BOX_HEIGHT = 20;

  private JFreeChart chart;

  private int[] conf = new int[4];

  private int lon;

  private int lat;

  public RepresentacionErroresXAnio() {
  }

  public Shape drawIcon(Graphics g, Point vertex) {
    double err = conf[3] * 100;
    double doubt = (int) (conf[2] / CONST_DOUBT * 100);
    double ncheck = (int) (conf[1] / CONST_NCHECK * 100);

    int width = (int) (BOX_WITDH * (err + doubt + ncheck) / 1300d);
    width = Math.max(4, width);

    double heightRatio = BOX_HEIGHT / (err + doubt + ncheck);
    int errorHeight = (int) (err * heightRatio);
    int doubtHeight = (int) (doubt * heightRatio);
    int ncHeight = (int) (ncheck * heightRatio);

    vertex = new Point(vertex.x - width / 2, vertex.y - BOX_HEIGHT / 2);
    g.setColor(Color.RED);
    g.fillRect(vertex.x, vertex.y, width, errorHeight);

    g.setColor(Color.ORANGE);
    g.fillRect(vertex.x, errorHeight + vertex.y, width, doubtHeight);

    g.setColor(Color.YELLOW);
    g.fillRect(vertex.x, errorHeight + doubtHeight + vertex.y, width, ncHeight);

    g.setColor(Color.BLACK);
    g.drawRect(vertex.x - 1, vertex.y - 1, width, BOX_HEIGHT);

    return new Rectangle(vertex.x - 1, vertex.y - 1, width, BOX_HEIGHT);
  }

  public void drawRepresentation(Graphics g, Rectangle bounds) {
    Image i = chart.createBufferedImage(bounds.width, bounds.height);
    g.drawImage(i, bounds.x, bounds.y, nullObserver);
  }

  public Point getPosition() {
    return new Point(this.lon, this.lat);
  }

  @SuppressWarnings("deprecation")
  public void init(Estacion e) {
    int inicio = e.getFechaInicio().getYear();
    double[][][] problemas = new double[5][2][e.getFechaFin().getYear() - inicio + 1];

    for (int i = 0; i < 5; i++) {
      for (int j = 0; j <= e.getFechaFin().getYear() - inicio; j++) {
        int anio = j + inicio;
        problemas[i][0][j] = new Date(anio, 0, 0).getTime();
      }
    }

    for (RegistroDiario d : e.getRegistros()) {
      ConfianzaVariable confianza = d.getConfianzaTempMin();
      if (confianza != null) {
        this.conf[confianza.getConfianza()]++;
        problemas[confianza.getConfianza()][1][d.getFecha().getYear() - inicio]++;
        problemas[4][1][d.getFecha().getYear() - inicio] += (1d / 36d);
      }
    }

    this.lon = e.getLongitud();
    this.lat = e.getLatitud();

    String timeAxisLabel = "Año";
    String valueAxisLabel = "Problemas";
    DefaultXYDataset dataset = new DefaultXYDataset();

    dataset.addSeries("Check (" + this.conf[1] + ")", problemas[1]);
    dataset.addSeries("Doubt (" + this.conf[2] + ")", problemas[2]);
    dataset.addSeries("Error (" + this.conf[3] + ")", problemas[3]);
    dataset.addSeries("#Regs/36", problemas[4]);

    this.chart = ChartFactory.createTimeSeriesChart(e.getNombre(), timeAxisLabel, valueAxisLabel, dataset, true, true,
        false);
    this.chart.setBackgroundImageAlpha(1);
  }
}
