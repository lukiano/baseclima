package ar.uba.dcao.dbclima.clasificacion;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import ar.uba.dcao.dbclima.data.RegistroDiario;

@SuppressWarnings("serial")
public class GraficoClasificacion extends JComponent {

  private final int[][] neuronas;

  private final int[][] registros;

  private int minX;

  private int minY;

  private int maxX;

  private int maxY;

  public GraficoClasificacion(Map<Point, List<RegistroDiario>> neuronas, int width, int height) {

    /* Inicializo los arreglos de registros y neuronas */
    int cantRegs = 0;
    for (Map.Entry<Point, List<RegistroDiario>> en : neuronas.entrySet()) {
      cantRegs += en.getValue().size();
    }

    this.neuronas = new int[neuronas.size()][2];
    this.registros = new int[cantRegs][2];

    this.minX = 10000;
    this.minY = 10000;

    this.maxX = -10000;
    this.maxY = -10000;

    int itR = 0;
    int itN = 0;
    for (Map.Entry<Point, List<RegistroDiario>> en : neuronas.entrySet()) {
      int sumMin = 0;
      int sumMax = 0;
      
      for (RegistroDiario rd : en.getValue()) {
        sumMin += rd.getTempMin();
        sumMax += rd.getTempMax();
        this.registros[itR][0] = rd.getTempMin();
        this.registros[itR++][1] = rd.getTempMax();
      }

      this.neuronas[itN][0] = (sumMin / en.getValue().size());
      this.neuronas[itN++][1] = (sumMax / en.getValue().size());
    }

    for (int i = 0; i < this.registros.length; i++) {
      this.minX = Math.min(this.registros[i][0], this.minX);
      this.maxX = Math.max(this.registros[i][0], this.maxX);
      this.minY = Math.min(this.registros[i][1], this.minY);
      this.maxY = Math.max(this.registros[i][1], this.maxY);
    }
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);

    g.setColor(Color.RED);
    for (int i = 0; i < this.neuronas.length; i++) {
      Point p = locatePoint(this.neuronas[i][0], this.neuronas[i][1]);
      g.fillOval(p.x - 1, p.y - 1, 3, 3);
    }

    g.setColor(Color.BLACK);
    for (int i = 0; i < this.registros.length; i++) {
      Point p = locatePoint(this.registros[i][0], this.registros[i][1]);
      g.fillOval(p.x - 1, p.y - 1, 2, 2);
    }
  }

  private Point locatePoint(int x, int y) {
    int newX = (x - this.minX) * this.getWidth() / (this.maxX - this.minX);
    int newY = (y - this.minY) * this.getHeight() / (this.maxY - this.minY);

    return new Point(newX, newY);
  }
}
