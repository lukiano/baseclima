package ar.uba.dcao.dbclima.graficos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;

@SuppressWarnings("serial")
public class RepresentacionErroresTemp implements RepresentacionEstacion {

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

  public RepresentacionErroresTemp() {
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

    vertex = new Point(vertex.x - width/2, vertex.y - BOX_HEIGHT/2);
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

  public void init(Estacion e) {
    for (RegistroDiario d : e.getRegistros()) {
      ConfianzaVariable confianza = d.getConfianzaTempMin();
      if (confianza != null) {
        this.conf[confianza.getConfianza()]++;
      }
    }

    this.lon = e.getLongitud();
    this.lat = e.getLatitud();

    DefaultKeyedValues ar = new DefaultKeyedValues();
    ar.addValue("NeedCheck(" + conf[1] + ")", conf[1] / CONST_NCHECK);
    ar.addValue("Doubt(" + conf[2] + ")", conf[2] / CONST_DOUBT);
    ar.addValue("Error(" + conf[3] + ")", conf[3]);
    PieDataset ds = new DefaultPieDataset(ar);
    chart = ChartFactory.createPieChart(e.getNombre(), ds, true, true, false);
  }
}
