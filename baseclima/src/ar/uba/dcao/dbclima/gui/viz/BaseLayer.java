package ar.uba.dcao.dbclima.gui.viz;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import ar.uba.dcao.dbclima.utils.InputLineReader;

@SuppressWarnings("serial")
public class BaseLayer extends JComponent {

  private static final Rectangle bounds = new Rectangle(-90, -60, 40, 40);

  private Map<Shape, RepresentacionEstacion> iconosEstacion = new HashMap<Shape, RepresentacionEstacion>();

  private static final String ARCHIVO_MAPA = "LayerPoliticoArg.csv";

  private InputLineReader reader;

  private Rectangle display;

  private List<RepresentacionEstacion> reps;

  public BaseLayer(Rectangle display) {
    super();
    this.display = display;

    try {
      String layerMapaFile = BaseLayer.class.getClassLoader().getResource(ARCHIVO_MAPA).getFile();
      reader = new InputLineReader(new FileReader(layerMapaFile));
    } catch (FileNotFoundException e) {
      throw new IllegalStateException(e);
    }

  }

  public void init(List<RepresentacionEstacion> representaciones) {
    this.reps = representaciones;
    this.addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
      }

      public void mouseMoved(MouseEvent e) {
        BaseLayer.this.updateMousePosition(e.getPoint());
      }
    });
  }

  protected void updateMousePosition(Point point) {
    for (Map.Entry<Shape, RepresentacionEstacion> iconoEstacion : this.iconosEstacion.entrySet()) {
      if (iconoEstacion.getKey().contains(point)) {
        this.drawRep(iconoEstacion.getValue());
        break;
      }
    }
  }

  private void drawRep(RepresentacionEstacion value) {
    Rectangle bounds = new Rectangle(0, 0, 350, 350);
    value.drawRepresentation(this.getGraphics(), bounds);
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    Polygon p = new Polygon();

    while (!reader.eof()) {
      String line = reader.getLine();
      String[] vals = line.split(",");

      double lat = Double.valueOf(vals[0]);
      double lon = Double.valueOf(vals[1]);
      if (Double.isNaN(lat)) {
        g.drawPolygon(p);
        p = new Polygon();
      } else {
        Point point = coordToPosition(new Point((int) (lon * -100), (int) (lat * -100)));
        p.addPoint(point.x, point.y);
      }
    }

    if (this.reps != null) {
      for (RepresentacionEstacion rep : this.reps) {
        Point point = coordToPosition(rep.getPosition());
        Shape icono = rep.drawIcon(g, new Point((int) point.getX(), (int) point.getY()));
        this.iconosEstacion.put(icono, rep);
      }
    }
  }

  /**
   * Transforma coordenadas geograficas en valores representables en el grafico
   * 
   * @param coords
   *            coordenada en formato <lat * -100, lon * -100>. E.g. <6300, 3930>
   */
  private Point coordToPosition(Point coords) {
    double lon = coords.getX() / -100;
    double lat = coords.getY() / -100;
    double xRatio = (lon - bounds.x) / bounds.width;
    double yRatio = (lat - bounds.y) / bounds.height;

    double newX = display.x + display.width * xRatio;
    double newY = display.y + display.height * (1 - yRatio);

    return new Point((int) newX, (int) newY);
  }
}
