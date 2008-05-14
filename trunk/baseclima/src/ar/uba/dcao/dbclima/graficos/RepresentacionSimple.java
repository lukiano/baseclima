package ar.uba.dcao.dbclima.graficos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import ar.uba.dcao.dbclima.data.Estacion;

@SuppressWarnings("serial")
public class RepresentacionSimple implements RepresentacionEstacion {

  private int lon;

  private int lat;

  private static final int AREA_RECT_SIDE = 6;

  public RepresentacionSimple() {
  }

  public Shape drawIcon(Graphics g, Point vertex) {
    vertex = new Point(vertex.x - AREA_RECT_SIDE/2, vertex.y - AREA_RECT_SIDE/2);
    g.setColor(Color.BLACK);
    g.fillOval(vertex.x, vertex.y, AREA_RECT_SIDE, AREA_RECT_SIDE);
    return new Rectangle(vertex.x, vertex.y, AREA_RECT_SIDE, AREA_RECT_SIDE);
  }

  public void drawRepresentation(Graphics g, Rectangle bounds) {
  }

  public Point getPosition() {
    return new Point(this.lon, this.lat);
  }

  public void init(Estacion e) {
    this.lon = e.getLongitud();
    this.lat = e.getLatitud();
  }
}
