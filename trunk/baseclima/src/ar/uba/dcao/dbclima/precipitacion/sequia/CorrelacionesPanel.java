package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import umontreal.iro.lecuyer.util.MathFunction;
import ar.uba.dcao.dbclima.data.CorrelacionNormalizadaEstaciones;
import ar.uba.dcao.dbclima.utils.DobleHelper;

/**
 * Panel asociado al frame VisorCorrelaciones. Las correlaciones entre las estaciones
 * vecinas se obtienen de la clase CorrelacionNormalizadaEstaciones.
 * @see CorrelacionNormalizadaEstaciones.
 *
 */
@SuppressWarnings("serial")
public class CorrelacionesPanel extends JPanel {
  
  private double ordenadaOrigen;
  
  private double pendiente;
  
  private List<Double> valores1, valores2;
  
  private List<Integer> pares1, pares2;
  
  private Map<Number, Double> precipitacionAnios;
  
  private static final int ESPACIO_EJE = 40;
  
  private static final int ESPACIO_EN_BLANCO = 50;

  public void setCorrelacion(CorrelacionNormalizadaEstaciones corr) {
    this.ordenadaOrigen = corr.getOrdenadaOrigen();
    this.pendiente = corr.getPendiente();
    this.valores1 = new ArrayList<Double>(corr.getValoresEstacion1());
    this.valores2 = new ArrayList<Double>(corr.getValoresEstacion2());
    this.pares1 = new ArrayList<Integer>(corr.getParesEstacion1());
    this.pares2 = new ArrayList<Integer>(corr.getParesEstacion2());
    this.setSize(ESPACIO_EN_BLANCO + valores1.size() * ESPACIO_EJE + ESPACIO_EJE + ESPACIO_EN_BLANCO,
        (ESPACIO_EN_BLANCO * 5) + valores2.size() * ESPACIO_EJE + ESPACIO_EJE + ESPACIO_EN_BLANCO);
    this.setPreferredSize(this.getSize());
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Font font = g.getFont().deriveFont(11f);
    font = font.deriveFont(Font.PLAIN);
    g.setFont(font);
    
    g.setColor(Color.black);
    g.drawLine(ESPACIO_EN_BLANCO, 
        ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE + ESPACIO_EJE, 
        ESPACIO_EN_BLANCO + valores1.size() * ESPACIO_EJE + ESPACIO_EJE, 
        ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE + ESPACIO_EJE);
    g.drawLine(ESPACIO_EN_BLANCO,
        ESPACIO_EN_BLANCO,
        ESPACIO_EN_BLANCO,
        ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE + ESPACIO_EJE);
    
    g.setColor(Color.blue);
    for (int i = 0; i < valores1.size(); i++) {
      Double valor1 = valores1.get(i);
      g.drawString(DobleHelper.doble2String(valor1), 
          ESPACIO_EN_BLANCO + ESPACIO_EJE + i*ESPACIO_EJE, 
          ESPACIO_EN_BLANCO + ESPACIO_EJE + valores2.size() * ESPACIO_EJE);
    }
    for (int i = 0; i < valores2.size(); i++) {
      Double valor2 = valores2.get(i);
      g.drawString(DobleHelper.doble2String(valor2), 
          ESPACIO_EN_BLANCO, 
          ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE - ESPACIO_EJE * i);
    }

    g.setColor(Color.green.darker());
    for (int i = 0; i < valores1.size(); i++) {
      Double valor1 = valores1.get(i);
      int yaxis = ESPACIO_EN_BLANCO;
      for (Map.Entry<Number, Double> entrada : this.precipitacionAnios.entrySet()) {
        if (valor1.equals(entrada.getValue())) {
          g.drawString(entrada.getKey().toString(), 
              ESPACIO_EN_BLANCO + ESPACIO_EJE + i*ESPACIO_EJE, 
              yaxis + ESPACIO_EN_BLANCO + ESPACIO_EJE + valores2.size() * ESPACIO_EJE);
          yaxis += ESPACIO_EN_BLANCO / 2;
        }
      }
    }
    
    
    List<Double> valoresRepetidos1 = new ArrayList<Double>(pares1.size());
    for (Integer par : pares1) {
      valoresRepetidos1.add(this.valores1.get(par - 1));
    }
    List<Double> valoresRepetidos2 = new ArrayList<Double>(pares2.size());
    for (Integer par : pares2) {
      valoresRepetidos2.add(this.valores2.get(par - 1));
    }

    MathFunction s1 = SPIHelper.funcionSPI(valoresRepetidos1);
    MathFunction s2 = SPIHelper.funcionSPI(valoresRepetidos2);

    g.setColor(Color.magenta);
    for (int i = 0; i < valores1.size(); i++) {
      Double valor1 = valores1.get(i);
      g.drawString(DobleHelper.doble2String(s1.evaluate(valor1)), 
          ESPACIO_EN_BLANCO + ESPACIO_EJE + i*ESPACIO_EJE, 
          (ESPACIO_EN_BLANCO / 2) + ESPACIO_EN_BLANCO + ESPACIO_EJE + valores2.size() * ESPACIO_EJE);
    }
    for (int i = 0; i < valores2.size(); i++) {
      Double valor2 = valores2.get(i);
      g.drawString(DobleHelper.doble2String(s2.evaluate(valor2)), 
          0, 
          ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE - ESPACIO_EJE * i);
    }

    g.setColor(Color.red);
    for (int i = 0; i < pares1.size(); i++) {
      int c1 = pares1.get(i) - 1;
      int c2 = pares2.get(i) - 1;
      g.fillRect(ESPACIO_EN_BLANCO + ESPACIO_EJE + c1 * ESPACIO_EJE - ESPACIO_EJE / 4, 
          ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE - ESPACIO_EJE * c2 - ESPACIO_EJE / 4, 
          ESPACIO_EJE / 2, 
          ESPACIO_EJE / 2);
      g.drawLine(ESPACIO_EN_BLANCO,
          ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE - ESPACIO_EJE * c2,
          ESPACIO_EN_BLANCO + ESPACIO_EJE + c1 * ESPACIO_EJE,
          ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE - ESPACIO_EJE * c2);
      g.drawLine(ESPACIO_EN_BLANCO + ESPACIO_EJE + c1 * ESPACIO_EJE,
          ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE - ESPACIO_EJE * c2,
          ESPACIO_EN_BLANCO + ESPACIO_EJE + c1 * ESPACIO_EJE,
          ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE + ESPACIO_EJE);
    }
    
    g.setColor(Color.green);
    int x1 = ESPACIO_EN_BLANCO;
    int y1 = (int)Math.floor(ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE - ESPACIO_EJE * ordenadaOrigen);
    int x2 = ESPACIO_EN_BLANCO + valores1.size() * ESPACIO_EJE + ESPACIO_EJE;
    int y2 = (int)Math.floor(ESPACIO_EN_BLANCO + valores2.size() * ESPACIO_EJE - ESPACIO_EJE * (ordenadaOrigen + valores1.size() * pendiente));
    g.drawLine(x1, y1, x2, y2);
    
  }

  public void setPrecipitacionAnios(Map<Number, Double> precipitacionAnios) {
    this.precipitacionAnios = precipitacionAnios;
  }
  

}
