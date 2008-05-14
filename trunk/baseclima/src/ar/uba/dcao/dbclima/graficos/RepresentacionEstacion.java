package ar.uba.dcao.dbclima.graficos;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;

import ar.uba.dcao.dbclima.data.Estacion;

public interface RepresentacionEstacion extends Serializable {

  /**
   * Inicializa la instancia con la estacion e. A partir de ahora la instancia
   * representara a esta estacion.
   */
  void init(Estacion e);

  /**
   * Dibuja el icono que representa a la estacion, dentro del los limites indicados.
   * Devuelve la figura que realmente se utilizo para dibujar el icono (contenida en los
   * limites).
   * 
   * @param g
   *            Grafico sobre el cual se debe dibujar el icono.
   * @param bounds
   *            Limites dentro de los cuales se debe dibujar el icono.
   * @return Area utilizada para graficar el icono
   */
  Shape drawIcon(Graphics g, Point bounds);

  /**
   * Se representa a la estacion, dentro del los limites indicados.
   * 
   * @param g
   *            Grafico sobre el cual se debe representar a la estacion.
   * @param bounds
   *            Limites dentro de los cuales se debe graficar.
   */
  void drawRepresentation(Graphics g, Rectangle bounds);

  /**
   * @return Posicion de la estacion representada, en centecima de puntos de coordenada
   *         (lon y lat).
   */
  Point getPosition();
}
