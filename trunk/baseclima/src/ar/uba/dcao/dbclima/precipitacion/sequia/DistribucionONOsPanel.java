package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;

import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.precipitacion.sequia.ActualizarInfoSecuenciasSequiasONO.DroughtLog;


/**
 * Este es el panel asociado al frame VisorDistribucionONOs.
 * Aqui se realizan los graficos propiamente dichos.
 * Fue usado para decidir si se incluian registros faltantes dentro de una sequia,
 * y en caso de ser asi, hasta que cantidad de registros faltantes serian soportadas.
 * (ONO significa que vienen uno o mas registros sin precipitacion, seguido una alternacion entre registros faltantes y otros registros sin precipitacion).
 *
 */
@SuppressWarnings("serial")
public class DistribucionONOsPanel extends JPanel {
  
  private static class Dato {
    volatile int total300 = 0;
    volatile int sup300 = 0;
    volatile int med300 = 0;
    volatile int inf300 = 0;
    
    volatile int total100 = 0;
    volatile int sup100 = 0;
    volatile int med100 = 0;
    volatile int inf100 = 0;
    
    volatile int totalTotal = 0;
    volatile int supTotal = 0;
    volatile int medTotal = 0;
    volatile int infTotal = 0;
    
    Map<Pair, Integer> matriz = new ConcurrentHashMap<Pair, Integer>();
    
  }
  
  private Dato mins;
  private Dato maxs;
  private Dato sumas;
  
  public DistribucionONOsPanel() {
    this.setBackground(Color.white);
  }

  public void calcular() {
    mins = new Dato();
    maxs = new Dato();
    sumas = new Dato();
    
    final ActualizarInfoSecuenciasSequiasONO.DroughtLog droughtLog = new DroughtLog() {
      
      private Long id = null;
      
      private void agregar(int p1, int p2, Dato dato) {
        if (p1 < 300 && p2 < 300) {
          dato.total300++;
          if (p1 > p2) {
            dato.inf300++;
          } else if (p1 < p2) {
            dato.sup300++;
          } else {
            dato.med300++;
          }
        }
        if (p1 < 100 && p2 < 100) {
          dato.total100++;
          if (p1 > p2) {
            dato.inf100++;
          } else if (p1 < p2) {
            dato.sup100++;
          } else {
            dato.med100++;
          }
        }
        dato.totalTotal++;
        if (p1 > p2) {
          dato.infTotal++;
        } else if (p1 < p2) {
          dato.supTotal++;
        } else {
          dato.medTotal++;
        }
        Pair pair = new Pair(p1, p2);
        if (dato.matriz.containsKey(pair)) {
          int valorActual = dato.matriz.get(pair);
          valorActual++;
          dato.matriz.put(pair, valorActual);
        } else {
          dato.matriz.put(pair, 1);
        }
      }
      
      public void log(Long estacionId, int contadorCerosAnteriores, int contadorNullsIntermedios,
          int contadorCerosPosteriores) {
        
        
        this.agregar(Math.min(contadorCerosAnteriores, contadorCerosPosteriores), contadorNullsIntermedios, mins);
        this.agregar(Math.max(contadorCerosAnteriores, contadorCerosPosteriores), contadorNullsIntermedios, maxs);
        this.agregar(contadorCerosAnteriores + contadorCerosPosteriores, contadorNullsIntermedios, sumas);
        if (estacionId != this.id) {
          repaint();
          this.id = estacionId;
        }
      }
    };
    
    Thread thread = new Thread("CalculadorSecuencias") {
      @Override
      public void run() {
        ActualizarInfoSecuenciasSequiasONO actualizarInfoSecuenciasSequiasONO = 
          new ActualizarInfoSecuenciasSequiasONO(true, droughtLog);
        actualizarInfoSecuenciasSequiasONO.run(DBSessionFactory.getInstance());
      }
    };
    
    thread.start();
    
    int width = (300 + 20) * 3;
    int height = (300 + 20) * 2;
    this.setPreferredSize(new Dimension(width, height));
    this.repaint();
  }
  
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    //((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = g.getFont().deriveFont(11f);
    font = font.deriveFont(Font.PLAIN);
    g.setFont(font);
    
    for (int i = 0 ; i < 256; i += 8) {
      g.setColor(this.dameColor(i));
      g.drawString(String.valueOf(i), 1000, i*2+20);
    }
    for (int i = 256 ; i < 512; i += 8) {
      g.setColor(this.dameColor(i));
      g.drawString(String.valueOf(i), 1050, i*2-512+20);
    }
    
    Color eje = Color.black;
    Color diagonal = Color.yellow;
    Color texto = Color.blue;
    
    g.setColor(eje);
    
    g.drawLine(x1(0), y1(0), x1(300), y1(0));
    g.drawLine(x1(50), y1(2), x1(50), y1(-2));
    g.drawLine(x1(100), y1(2), x1(100), y1(-2));
    g.drawLine(x1(150), y1(2), x1(150), y1(-2));
    g.drawLine(x1(200), y1(2), x1(200), y1(-2));
    g.drawLine(x1(250), y1(2), x1(250), y1(-2));
    
    g.drawLine(x1(0), y1(0), x1(0), y1(300));
    g.drawLine(x1(-2), y1(50), x1(2), y1(50));
    g.drawLine(x1(-2), y1(100), x1(2), y1(100));
    g.drawLine(x1(-2), y1(150), x1(2), y1(150));
    g.drawLine(x1(-2), y1(200), x1(2), y1(200));
    g.drawLine(x1(-2), y1(250), x1(2), y1(250));

    g.drawLine(x4(0), y4(0), x4(300), y4(0));
    g.drawLine(x4(150), y4(2), x4(150), y4(-2));
    g.drawLine(x4(0), y4(0), x4(0), y4(300));
    g.drawLine(x4(-2), y4(150), x4(2), y4(150));

    g.setColor(diagonal);
    g.drawLine(x1(0), y1(0), x1(300), y1(300));
    g.drawLine(x4(0), y4(0), x4(300), y4(300));
    
    g.setColor(texto);
    g.drawString("0", x1(0), y1(-10));
    g.drawString("300", x1(290), y1(-10));
    g.drawString("min(x1,x2)", x1(150), y1(-10));
    g.drawString(mins.sup300 + " (" + porc(mins.sup300, mins.total300)+"%)", x1(50), y1(250));
    g.drawString(mins.inf300 + " (" + porc(mins.inf300, mins.total300)+"%)", x1(250), y1(50));
    g.drawString(mins.med300 + " (" + porc(mins.med300, mins.total300)+"%)", x1(150), y1(150));
    g.drawString("0", x4(0), y4(-10));
    g.drawString("100", x4(290), y4(-10));
    g.drawString("min(x1,x2)", x4(150), y4(-10));
    g.drawString(mins.sup100 + " (" + porc(mins.sup100, mins.total100)+"%)", x4(50), y4(250));
    g.drawString(mins.inf100 + " (" + porc(mins.inf100, mins.total100)+"%)", x4(250), y4(50));
    g.drawString(mins.med100 + " (" + porc(mins.med100, mins.total100)+"%)", x4(150), y4(150));

    for (Map.Entry<Pair, Integer> entry : mins.matriz.entrySet()) {
      Color color = this.dameColor(entry.getValue());
      g.setColor(color);
      Pair pair = entry.getKey();
      if (pair.p1 < 300 && pair.p2 < 300) {
        g.drawOval(x1(pair.p1), y1(pair.p2), 0, 0);
      }
      if (pair.p1 < 100 && pair.p2 < 100) {
        g.fillOval(x4(pair.p1*3-1), y4(pair.p2*3-1), 2, 2);
      }
    }

    
    g.setColor(eje);
    
    g.drawLine(x2(0), y2(0), x2(300), y2(0));
    g.drawLine(x2(50), y2(2), x2(50), y2(-2));
    g.drawLine(x2(100), y2(2), x2(100), y2(-2));
    g.drawLine(x2(150), y2(2), x2(150), y2(-2));
    g.drawLine(x2(200), y2(2), x2(200), y2(-2));
    g.drawLine(x2(250), y2(2), x2(250), y2(-2));
    
    g.drawLine(x2(0), y2(0), x2(0), y2(300));
    g.drawLine(x2(-2), y2(50), x2(2), y2(50));
    g.drawLine(x2(-2), y2(100), x2(2), y2(100));
    g.drawLine(x2(-2), y2(150), x2(2), y2(150));
    g.drawLine(x2(-2), y2(200), x2(2), y2(200));
    g.drawLine(x2(-2), y2(250), x2(2), y2(250));

    g.drawLine(x5(0), y5(0), x5(300), y5(0));
    g.drawLine(x5(150), y5(2), x5(150), y5(-2));
    g.drawLine(x5(0), y5(0), x5(0), y5(300));
    g.drawLine(x5(-2), y5(150), x5(2), y5(150));
    
    g.setColor(diagonal);
    g.drawLine(x2(0), y2(0), x2(300), y2(300));
    g.drawLine(x5(0), y5(0), x5(300), y5(300));
    
    g.setColor(texto);
    g.drawString("0", x2(0), y2(-10));
    g.drawString("300", x2(290), y2(-10));
    g.drawString("max(x1,x2)", x2(150), y2(-10));
    g.drawString(maxs.sup300 + " (" + porc(maxs.sup300, maxs.total300)+"%)", x2(50), y2(250));
    g.drawString(maxs.inf300 + " (" + porc(maxs.inf300, maxs.total300)+"%)", x2(250), y2(50));
    g.drawString(maxs.med300 + " (" + porc(maxs.med300, maxs.total300)+"%)", x2(150), y2(150));
    g.drawString("0", x5(0), y5(-10));
    g.drawString("100", x5(290), y5(-10));
    g.drawString("max(x1,x2)", x5(150), y5(-10));
    g.drawString(maxs.sup100 + " (" + porc(maxs.sup100, maxs.total100)+"%)", x5(50), y5(250));
    g.drawString(maxs.inf100 + " (" + porc(maxs.inf100, maxs.total100)+"%)", x5(250), y5(50));
    g.drawString(maxs.med100 + " (" + porc(maxs.med100, maxs.total100)+"%)", x5(150), y5(150));
    
    for (Map.Entry<Pair, Integer> entry : maxs.matriz.entrySet()) {
      Color color = this.dameColor(entry.getValue());
      g.setColor(color);
      Pair pair = entry.getKey();
      if (pair.p1 < 300 && pair.p2 < 300) {
        g.drawOval(x2(pair.p1), y2(pair.p2), 0, 0);
      }
      if (pair.p1 < 100 && pair.p2 < 100) {
        g.fillOval(x5(pair.p1*3-1), y5(pair.p2*3-1), 2, 2);
      }
    }
    
    g.setColor(eje);
    g.drawLine(x3(0), y3(0), x3(300), y3(0));
    g.drawLine(x3(50), y3(2), x3(50), y3(-2));
    g.drawLine(x3(100), y3(2), x3(100), y3(-2));
    g.drawLine(x3(150), y3(2), x3(150), y3(-2));
    g.drawLine(x3(200), y3(2), x3(200), y3(-2));
    g.drawLine(x3(250), y3(2), x3(250), y3(-2));
    
    g.drawLine(x3(0), y3(0), x3(0), y2(300));
    g.drawLine(x3(-2), y3(50), x3(2), y2(50));
    g.drawLine(x3(-2), y3(100), x3(2), y2(100));
    g.drawLine(x3(-2), y3(150), x3(2), y2(150));
    g.drawLine(x3(-2), y3(200), x3(2), y2(200));
    g.drawLine(x3(-2), y3(250), x3(2), y2(250));

    g.drawLine(x6(0), y6(0), x6(300), y6(0));
    g.drawLine(x6(150), y6(2), x6(150), y6(-2));
    g.drawLine(x6(0), y6(0), x6(0), y6(300));
    g.drawLine(x6(-2), y6(150), x6(2), y6(150));

    g.setColor(diagonal);
    g.drawLine(x3(0), y3(0), x3(300), y3(300));
    g.drawLine(x6(0), y6(0), x6(300), y6(300));
    
    g.setColor(texto);
    g.drawString("0", x3(0), y3(-10));
    g.drawString("300", x3(290), y3(-10));
    g.drawString("x1+x2", x3(150), y3(-10));
    g.drawString(sumas.sup300 + " (" + porc(sumas.sup300, sumas.total300)+"%)", x3(50), y3(250));
    g.drawString(sumas.inf300 + " (" + porc(sumas.inf300, sumas.total300)+"%)", x3(250), y3(50));
    g.drawString(sumas.med300 + " (" + porc(sumas.med300, sumas.total300)+"%)", x3(150), y3(150));
    
    g.drawString("0", x6(0), y6(-10));
    g.drawString("100", x6(290), y6(-10));
    g.drawString("x1+x2", x6(150), y6(-10));
    
    g.drawString(sumas.sup100 + " (" + porc(sumas.sup100, sumas.total100)+"%)", x6(50), y6(250));
    g.drawString(sumas.inf100 + " (" + porc(sumas.inf100, sumas.total100)+"%)", x6(250), y6(50));
    g.drawString(sumas.med100 + " (" + porc(sumas.med100, sumas.total100)+"%)", x6(150), y6(150));

    for (Map.Entry<Pair, Integer> entry : sumas.matriz.entrySet()) {
      Color color = this.dameColor(entry.getValue());
      g.setColor(color);
      Pair pair = entry.getKey();
      if (pair.p1 < 300 && pair.p2 < 300) {
        g.drawOval(x3(pair.p1), y3(pair.p2), 0, 0);
      }
      if (pair.p1 < 100 && pair.p2 < 100) {
        g.fillOval(x6(pair.p1*3-1), y6(pair.p2*3-1), 2, 2);
      }
    }

  }
  
  private Color dameColor(int i) {
//    if (0 <= i && i <= 63) {
//      return new Color(0,i*4,0);
//    }
//    if (64 <= i && i <= 127) {
//      return new Color(0,255,(i-64)*4);
//    }
//    if (128 <= i && i <= 191) {
//      return new Color(0,(191-i)*4,255);
//    }
//    if (191 <= i && i <= 255) {
//      return new Color((i-191)*4,0,255);
//    }
//    if (256 <= i && i <= 319) {
//      return new Color(255,0,(319-i)*4);
//    }
//    if (320 <= i) {
//      return new Color(255,0,0);
//    }
    if (0 <= i && i <= 15) {
      return new Color(0,i*16,0);
    }
    if (16 <= i && i <= 31) {
      return new Color(0,255,(i-16)*16);
    }
    if (32 <= i && i <= 63) {
      return new Color(0,(63-i)*8,255);
    }
    if (64 <= i && i <= 127) {
      return new Color((i-64)*4,0,255);
    }
    if (128 <= i && i <= 255) {
      return new Color(255,0,(255-i)*2);
    }
    if (256 <= i) {
      return new Color(255,0,0);
    }
    
    return Color.yellow;
  }
  
  private int porc(int x, int total) {
    if (total == 0) {
      return 0;
    }
    float f = x * 100f;
    return Math.round(f / total);
  }
  
  private int x1(int i) { return 10 + i; }
  
  private int y1(int i) { return 310 - i; }
  
  private int x2(int i) { return 320 + x1(i); }

  private int y2(int i) { return y1(i); }
  
  private int x3(int i) { return 640 + x1(i); }
  
  private int y3(int i) { return y1(i); }
  
  private int x4(int i) { return x1(i); }
  
  private int y4(int i) { return 630 - i; }
  
  private int x5(int i) { return x2(i); }

  private int y5(int i) { return y4(i); }
  
  private int x6(int i) { return x3(i); }
  
  private int y6(int i) { return y4(i); }

}
