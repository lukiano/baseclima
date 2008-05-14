package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.hibernate.Session;

import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;
import umontreal.iro.lecuyer.util.MathFunction;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.precipitacion.rango.PromedioPrecipitacionAnualProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.Rango;
import ar.uba.dcao.dbclima.utils.DobleHelper;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Aqui se realizan los calculos y la grafica propiamente dicha del VisorDistribucionPrecipitacion 
 * @see PromedioPrecipitacionAnualProyectorRango
 *
 */
@SuppressWarnings("serial")
public class DistribucionPrecipitacionPanel extends JPanel {

  protected Distribution distribution;

  protected MathFunction funcionH;

  protected Estacion estacionBase;

  protected double p25, p50, p75, p90;

  protected double umbralDistancia;

  protected double umbralNulos;

  protected Sequia sequia;

  private List<Double> valores;

  protected Map<Number, Double> precipitacionAnios;

  private double mayorValor;

  private double multiplicadorHorizontal = 1;

  public DistribucionPrecipitacionPanel() {
    this.setBackground(Color.white);
  }

  public void setEstacion(Estacion nuevaEstacion) {
    this.estacionBase = nuevaEstacion;
  }

  public void setSequia(Sequia sequia) {
    this.sequia = sequia;
  }

  public void setUmbralDistancia(double umbral) {
    this.umbralDistancia = umbral;
  }

  public void setUmbralNulos(double umbralNulos) {
    this.umbralNulos = umbralNulos;
  }

  public void calcularPrecipitacion() {

    Date comienzo = this.sequia.getComienzo();
    Date fin = FechaHelper.dameFechaSumada(comienzo, this.sequia.getLongitud());

    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualProyectorRango(comienzo,
        fin);
    proyectorRango.setUmbralNulos(this.umbralNulos);
    proyectorRango.setIncluirNulos(true);
    
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    sess.getTransaction().commit();
    List<Rango> rangos = proyectorRango.proyectarRangos(this.estacionBase);

    valores = new ArrayList<Double>();
    precipitacionAnios = new TreeMap<Number, Double>();

    mayorValor = 0.0;

    for (Rango rango : rangos) {
      Number valor = rango.valor();
      // El anio en cuestion puede tener mas del porcentaje de nulls, pero como estan aceptados, le pongo un cero.
      if (FechaHelper.dameAnio(rango.comienzo()) == FechaHelper.dameAnio(comienzo)) {
        if (valor == null) {
          valor = Double.valueOf(0);
        }
      }
      if (valor == null) {
        this.precipitacionAnios.put(FechaHelper.dameAnio(rango.comienzo()), null);
      } else {
        double precipitacion = valor.doubleValue();
        if (precipitacion > mayorValor) {
          mayorValor = precipitacion;
        }
        this.precipitacionAnios.put(FechaHelper.dameAnio(rango.comienzo()), precipitacion);
        valores.add(precipitacion);
      }
    }

    this.establecerEscalaHorizontalSegunPrecipitacionPromedio();

    int width = x(mayorValor) + 30;
    int height = 200 + 20 + 20 * this.precipitacionAnios.size() / 5;
    this.setPreferredSize(new Dimension(width, height));
    this.repaint();
  }

  private void establecerEscalaHorizontalSegunPrecipitacionPromedio() {
    if (mayorValor > 100) {
      multiplicadorHorizontal = 5.0;
    } else if (mayorValor > 75) {
      multiplicadorHorizontal = 10.0;
    } else if (mayorValor > 50) {
      multiplicadorHorizontal = 15.0;
    } else if (mayorValor > 25) {
      multiplicadorHorizontal = 20.0;
    } else if (mayorValor > 10) {
      multiplicadorHorizontal = 40.0;
    } else if (mayorValor > 5) {
      multiplicadorHorizontal = 80.0;
    } else if (mayorValor > 2) {
      multiplicadorHorizontal = 160.0;
    } else if (mayorValor > 1) {
      multiplicadorHorizontal = 320.0;
    } else if (mayorValor > 0.5) {
      multiplicadorHorizontal = 480.0;
    } else if (mayorValor > 0.1) {
      multiplicadorHorizontal = 640.0;
    } else {
      multiplicadorHorizontal = 800.0;
    }
  }

  public void calcularDistribucion() {
    if (valores.size() < SPIHelper.CANTIDAD_MINIMA_ANIOS) {
      return;
    }

    double[] valoresDefinitivos = new double[valores.size()];

    for (int i = 0; i < valores.size(); i++) {
      valoresDefinitivos[i] = valores.get(i).doubleValue();
    }
    
    Arrays.sort(valoresDefinitivos);
    this.distribution = new EmpiricalDist(valoresDefinitivos);
    /*
    if (this.claseDistribucion.equals(PiecewiseLinearEmpiricalDist.class)) {
      this.distribution = new PiecewiseLinearEmpiricalDist(valoresDefinitivos);
    } else {
      this.distribution = DistributionFactory.getDistribution(this.claseDistribucion, valoresDefinitivos,
          valoresDefinitivos.length);
    }
    */
    if (this.distribution != null) {
      p25 = this.distribution.inverseF(0.25);
      p50 = this.distribution.inverseF(0.50);
      p75 = this.distribution.inverseF(0.75);
      p90 = this.distribution.inverseF(0.90);

      this.funcionH = SPIHelper.funcionH(valores);
      
    }
  }


  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    // ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    // RenderingHints.VALUE_ANTIALIAS_ON);

    if (this.distribution == null) {
      Font font = g.getFont().deriveFont(11f);
      font = font.deriveFont(Font.PLAIN);
      g.setFont(font);
      g.setColor(Color.black);
      g.drawString("Could not get enough data for analysis.", x(1), y(0));
      int y = 200;
      int count = 0;
      for (Map.Entry<Number, Double> entry : this.precipitacionAnios.entrySet()) {
        if (entry.getValue() == null) {
          g.drawString("Discarded year: " + entry.getKey(), 150 * count, y);
        } else {
          g.drawString("Year " + entry.getKey() + " : " + DobleHelper.doble2String(entry.getValue()), 150 * count, y);
        }
        count++;
        if (count == 5) {
          y += 20;
          count = 0;
        }
      }
    } else {
      double longitud = mayorValor;
      g.setColor(Color.blue);
      g.drawLine(x(0), y(1), x(longitud), y(1));
      g.drawLine(x(0), y(0), x(longitud), y(0));
      g.setColor(Color.gray);
      double intervalo = 1 / multiplicadorHorizontal;
      for (double d = 0.0; d < longitud; d += intervalo) {
        int x1 = x(d);
        int x2 = x(d + intervalo);
        int y1, y2;
        y1 = y(this.distribution.cdf(d));
        y2 = y(this.distribution.cdf(d + intervalo));

        /*
        if (this.cdf) {
          y1 = y(this.distribution.cdf(d));
          y2 = y(this.distribution.cdf(d + intervalo));
        } else {
           // y1 = y(this.distribution.cdf(d) - this.distribution.cdf(d - intervalo)); y2 =
           // y(this.distribution.cdf(d + intervalo) - this.distribution.cdf(d));
          y1 = y(this.distribution.density(d));
          y2 = y(this.distribution.density(d + intervalo));
        }
        */
        g.drawLine(x1, y1, x2, y2);
      }
      g.setColor(Color.orange);
      for (double d = 0.0; d < longitud; d += intervalo) {
        int x1 = x(d);
        int x2 = x(d + intervalo);
        int y1 = y(this.funcionH.evaluate(d));
        int y2 = y(this.funcionH.evaluate(d + intervalo));
        g.drawLine(x1, y1, x2, y2);
      }
      g.setColor(Color.blue);
      g.drawLine(x(p25), y(0), x(p25), y(1));
      g.drawLine(x(p50), y(0), x(p50), y(1));
      g.drawLine(x(p75), y(0), x(p75), y(1));
      g.drawLine(x(p90), y(0), x(p90), y(1));
      g.setColor(Color.magenta);

      Font font = g.getFont().deriveFont(11f);
      font = font.deriveFont(Font.PLAIN);
      g.setFont(font);
      g.drawString("25%", x(p25) + 1, y(0.9));
      g.drawString("50%", x(p50) + 1, y(0.9));
      g.drawString("75%", x(p75) + 1, y(0.9));
      g.drawString("90%", x(p90) + 1, y(0.9));
      g.drawString(DobleHelper.doble2String(p25), x(p25) + 1, y(0.8));
      g.drawString(DobleHelper.doble2String(p50), x(p50) + 1, y(0.8));
      g.drawString(DobleHelper.doble2String(p75), x(p75) + 1, y(0.8));
      g.drawString(DobleHelper.doble2String(p90), x(p90) + 1, y(0.8));
      /*
       * g.setColor(Color.black); for (Double valor : valores.uniqueSet()) { double
       * posicion = valor.doubleValue(); if (incluirCero || posicion > 0d) { //double
       * porcentaje = ((double)valores.getCount(valor)) / valores.size(); String string =
       * doble2String(valor); double porc; if (this.cdf) { porc =
       * this.distribution.cdf(valor); } else { porc = this.distribution.density(valor); }
       * g.drawString(string, x(valor), y(porc)); } }
       * 
       * font = g.getFont().deriveFont(11f); font = font.deriveFont(Font.PLAIN);
       * g.setFont(font);
       */
      double D = (0.0 - p25) / (p25 - p50);
      double spi = SPIHelper.spi(this.funcionH.evaluate(0d));
      if (D >= umbralDistancia) {
        g.setColor(Color.red);
      } else {
      }
      g.drawString("(0 - p25) / (p25 - p50): " + DobleHelper.doble2String(D), 0, 150);
      g.drawString("SPI: " + DobleHelper.doble2String(spi) + " (" + SPIHelper.spi2Codigo(spi) + ")", 300, 150);
      g.setColor(Color.black);
      g.drawString("Scale: 1 to " + multiplicadorHorizontal, 0, 170);
      int y = 200;
      int count = 0;
      for (Map.Entry<Number, Double> entry : this.precipitacionAnios.entrySet()) {
        if (entry.getValue() == null) {
          g.drawString("Discarded year: " + entry.getKey(), 150 * count, y);
        } else {
          g.drawString("Year " + entry.getKey() + " : " + DobleHelper.doble2String(entry.getValue()), 150 * count, y);
        }
        count++;
        if (count == 5) {
          y += 20;
          count = 0;
        }
      }
    }
  }

  public int x(double x) {
    return ((int) Math.floor(x * multiplicadorHorizontal));
  }

  public int y(double y) {
    return (-(int) Math.floor(y * 100)) + 120;
  }

}
