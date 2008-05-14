package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.hibernate.Session;

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.DistributionFactory;
import umontreal.iro.lecuyer.probdist.PiecewiseLinearEmpiricalDist;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.utils.DobleHelper;


/**
 * Esta es la pantalla asociada al frame VisorDistribucionSequias.
 * Aqui se realiza el grafico propiamente dicho. Para hacer los calculos
 * de la distribucion, se obtienen las sequias correspondientes
 * a la estacion desde la base de datos.
 *
 */
@SuppressWarnings("serial")
public class DistribucionSequiaPanel extends JPanel {
  
  private ContinuousDistribution distribution;
  
  private Estacion estacion;
  
  private Class<? extends ContinuousDistribution> claseDistribucion;
  
  private double p25, p50, p75, p90;
  
  private double umbralDistanciaSequia;
  
  private int umbralSaltoMinimo;
  
  private boolean incluirSequiaUnDia;
  
  private boolean cdf;
  
  private int cantidadSecuencias;
  
  private List<Sequia> sequiasMayores75 = new ArrayList<Sequia>();
  
  private List<Sequia> sequiasMayores75MayorUmbralDistancia = new ArrayList<Sequia>();
  
  private List<Sequia> sequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia = new ArrayList<Sequia>();
  
  public DistribucionSequiaPanel() {
    this.setBackground(Color.white);
  }

  public void setClaseDistribucion(Class<? extends ContinuousDistribution> nuevaClase) {
    this.claseDistribucion = nuevaClase;
  }

  public Class<? extends ContinuousDistribution> getClaseDistribucion() {
    return this.claseDistribucion;
  }

  public void setEstacion(Estacion nuevaEstacion) {
    this.estacion = nuevaEstacion;
  }

  public Estacion getEstacion() {
    return this.estacion;
  }

  public void setUmbralSaltoMinimo(int umbralSaltoMinimo) {
    this.umbralSaltoMinimo = umbralSaltoMinimo;
  }

  public void setUmbralDistanciaSequia(double umbralDistanciaSequia) {
    this.umbralDistanciaSequia = umbralDistanciaSequia;
  }

  public void setIncluirSequiaUnDia(boolean incluirSequiaUnDia) {
    this.incluirSequiaUnDia = incluirSequiaUnDia;
  }
  
  public void setCDF(boolean cdf) {
    this.cdf = cdf;
  }
  
  public List<Sequia> getSequiasMayores75MayorUmbralDistancia() {
    return this.sequiasMayores75MayorUmbralDistancia;
  }

  public List<Sequia> getSequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia() {
    return this.sequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia;
  }

  public void calcularDistribucion() {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    
    List<Sequia> resultados = DAOFactory.getSequiaDAO(sess).findAllLongerThanByStation(incluirSequiaUnDia?1:2, this.estacion);
    
    this.cantidadSecuencias = resultados.size();
    
    double[] valores = new double[cantidadSecuencias];
    
    for (int i = 0; i < cantidadSecuencias; i++) {
      valores[i] = resultados.get(i).getLongitud();
    }
    sess.getTransaction().commit();
    if (valores.length > 2) {
      if (this.claseDistribucion.equals(PiecewiseLinearEmpiricalDist.class)) {
        this.distribution = new PiecewiseLinearEmpiricalDist(valores);
      } else {
        this.distribution = DistributionFactory.getDistribution(this.claseDistribucion, valores, valores.length);
      }
      p25 = this.distribution.inverseF(0.25);
      p50 = this.distribution.inverseF(0.50);
      p75 = this.distribution.inverseF(0.75);
      p90 = this.distribution.inverseF(0.90);
      this.sequiasMayores75.clear();
      this.sequiasMayores75MayorUmbralDistancia.clear();
      this.sequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia.clear();
      for (Sequia sequia : resultados) {
        if (sequia.getLongitud().doubleValue() > p75) {
          this.sequiasMayores75.add(sequia);
        }
      }
      Collections.sort(this.sequiasMayores75, new LongitudSequiaComparator());
      int width = x(this.sequiasMayores75.get(this.sequiasMayores75.size() - 1).getLongitud()) + 20;
      int height = 200 + 20 * this.sequiasMayores75.size();
      this.setPreferredSize(new Dimension(width, height));
      this.repaint();
      double divisor = p75 - p50;
      int umbralDiferencia = Integer.MAX_VALUE;
      int umbralDistancia = Integer.MAX_VALUE;
      int diferenciaMayor = this.umbralSaltoMinimo;
      for (int i = 0; i < this.sequiasMayores75.size(); i++) {
        Sequia sequia = this.sequiasMayores75.get(i);
        int cantDias = sequia.getLongitud().intValue();
        double D = (cantDias - p75) / divisor;
        if (D >= this.umbralDistanciaSequia) {
          umbralDistancia = i;
        }
      }
      for (int i = 1; i < this.sequiasMayores75.size(); i++) {
        Sequia sequia = this.sequiasMayores75.get(i);
        int cantDias = sequia.getLongitud();
        int cantDiasAnterior = this.sequiasMayores75.get(i - 1).getLongitud();
        if (cantDiasAnterior != cantDias) {
          int diferencia = cantDias - cantDiasAnterior; 
          if (diferencia > diferenciaMayor) {
            diferenciaMayor = diferencia;
            umbralDiferencia = i;
          }
        }
      }
      for (int i = 0; i < this.sequiasMayores75.size(); i++) {
        if (i >= umbralDistancia || i >= umbralDiferencia) {
          Sequia sequia = this.sequiasMayores75.get(i);
          if (i >= umbralDiferencia) {
            this.sequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia.add(sequia);
          } else {
            this.sequiasMayores75MayorUmbralDistancia.add(sequia);
          }
        }
      }
      while (this.sequiasMayores75.size() > 0 && this.sequiasMayores75.size() > umbralDiferencia) {
        this.sequiasMayores75.remove(umbralDiferencia);
      }
      while (this.sequiasMayores75.size() > 0 && this.sequiasMayores75.size() > umbralDistancia) {
        this.sequiasMayores75.remove(umbralDistancia);
      }
    }
  }
  

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    //((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = g.getFont().deriveFont(11f);
    font = font.deriveFont(Font.PLAIN);
    g.setFont(font);
    if (this.distribution != null) {
      double longitud = 1000;
      g.setColor(Color.blue);
      g.drawLine(x(0), y(1), x(longitud), y(1));
      g.drawLine(x(0), y(0), x(longitud), y(0));
      g.setColor(Color.black);
      double intervalo = 1.0;
      for (double d = 0.0; d < longitud; d += intervalo) {
        int x1 = x(d);
        int x2 = x(d + intervalo);
        int y1, y2;
        if (this.cdf) {
          y1 = y(this.distribution.cdf(d));
          y2 = y(this.distribution.cdf(d + intervalo));
        } else {
          y1 = y(this.distribution.density(d));
          y2 = y(this.distribution.density(d + intervalo));
        }
        g.drawLine(x1, y1, x2, y2);
      }
      g.setColor(Color.blue);
      g.drawLine(x(p25), y(0), x(p25), y(1));
      g.drawLine(x(p50), y(0), x(p50), y(1));
      g.drawLine(x(p75), y(0), x(p75), y(1));
      g.drawLine(x(p90), y(0), x(p90), y(1));
      g.setColor(Color.magenta);
      g.drawString("25%", x(p25) + 1, y(0.9));
      g.drawString("50%", x(p50) + 1, y(0.9));
      g.drawString("75%", x(p75) + 1, y(0.9));
      g.drawString("90%", x(p90) + 1, y(0.9));
      g.drawString(String.valueOf((int)p25), x(p25) + 1, y(0.8));
      g.drawString(String.valueOf((int)p50), x(p50) + 1, y(0.8));
      g.drawString(String.valueOf((int)p75), x(p75) + 1, y(0.8));
      g.drawString(String.valueOf((int)p90), x(p90) + 1, y(0.8));
      
      g.setColor(Color.magenta);
      g.drawString(" Number of droughts:" + this.cantidadSecuencias, x(0), 150);
      
      double divisor = p75 - p50;
      Set<Integer> dias = new HashSet<Integer>();
      
      for (int i = 0; i < this.sequiasMayores75.size(); i++) {
        Sequia sequia = this.sequiasMayores75.get(i);
        int cantDias = sequia.getLongitud().intValue();
        
        double D = (cantDias - p75) / divisor;
        g.setColor(Color.black);
        int y;
        if (dias.contains(cantDias - 1)) {
          y = y(0.0);
        } else {
          y = y(-0.1);
          dias.add(cantDias);
        }
        g.drawString(String.valueOf(cantDias), x(cantDias), y);
        String stringD = DobleHelper.doble2String(D);
        String mensaje =" Start date:" + sequia.getComienzo() + "  |  Length:" + cantDias
        + "  |   (X - p75) / (p75 - p50): " + stringD;
        if (i > 0) {
          int cantDiasAnterior = this.sequiasMayores75.get(i - 1).getLongitud().intValue();
          if (cantDiasAnterior != cantDias) {
            mensaje += "   | Difference: " + (cantDias - cantDiasAnterior);
            double porc = ((double)(cantDias - cantDiasAnterior)) / (double)cantDiasAnterior;
            String stringPorc = DobleHelper.doble2String(porc*100);
            mensaje += " (" + stringPorc + "%)";
          }
        }
        g.drawString(mensaje, x(0), 200 + 20*i);
      }
      
      for (int i = 0; i < this.sequiasMayores75MayorUmbralDistancia.size(); i++) {
        Sequia sequia = this.sequiasMayores75MayorUmbralDistancia.get(i);
        int cantDias = sequia.getLongitud().intValue();
        double D = (cantDias - p75) / divisor;
        g.setColor(Color.cyan.darker());
        int y;
        if (dias.contains(cantDias - 1)) {
          y = y(0.0);
        } else {
          y = y(-0.1);
          dias.add(cantDias);
        }
        g.drawString(String.valueOf(cantDias), x(cantDias), y);
        String stringD = DobleHelper.doble2String(D);
        String mensaje = " Start date:" + sequia.getComienzo() + "  |  Length:" + cantDias
        + "  |   (X - p75) / (p75 - p50): " + stringD;
        int cantDiasAnterior;
        if (i == 0) {
          if (this.sequiasMayores75.size() == 0) {
            cantDiasAnterior = -1;
          } else {
            cantDiasAnterior = this.sequiasMayores75.get(this.sequiasMayores75.size() - 1).getLongitud().intValue();
          }
        } else {
          cantDiasAnterior = this.sequiasMayores75MayorUmbralDistancia.get(i - 1).getLongitud().intValue();
        }
        if (cantDiasAnterior != -1 && cantDiasAnterior != cantDias) {
          mensaje += "   | Difference: " + (cantDias - cantDiasAnterior);
          double porc = ((double)(cantDias - cantDiasAnterior)) / (double)cantDiasAnterior;
          String stringPorc = DobleHelper.doble2String(porc*100);
          mensaje += " (" + stringPorc + "%)";
        }
        g.drawString(mensaje, x(0), 200 + 20* (this.sequiasMayores75.size() + i));
      }
      
      for (int i = 0; i < this.sequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia.size(); i++) {
        Sequia sequia = this.sequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia.get(i);
        int cantDias = sequia.getLongitud().intValue();
        double D = (cantDias - p75) / divisor;
        g.setColor(Color.red);
        int y;
        if (dias.contains(cantDias - 1)) {
          y = y(0.0);
        } else {
          y = y(-0.1);
          dias.add(cantDias);
        }
        g.drawString(String.valueOf(cantDias), x(cantDias), y);
        String stringD = DobleHelper.doble2String(D);
        String mensaje = " Start date:" + sequia.getComienzo() + "  |  Length:" + cantDias
        + "  |   (X - p75) / (p75 - p50): " + stringD;
        int cantDiasAnterior;
        if (i == 0) {
          if (this.sequiasMayores75MayorUmbralDistancia.size() == 0) {
            if (this.sequiasMayores75.size() == 0) {
              cantDiasAnterior = -1;
            } else {
              cantDiasAnterior = this.sequiasMayores75.get(this.sequiasMayores75.size() - 1).getLongitud().intValue();  
            }
          } else {
            cantDiasAnterior = this.sequiasMayores75MayorUmbralDistancia.get(this.sequiasMayores75MayorUmbralDistancia.size() - 1).getLongitud().intValue();
          }
        } else {
          cantDiasAnterior = this.sequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia.get(i - 1).getLongitud().intValue();
        }
        if (cantDiasAnterior != -1 && cantDiasAnterior != cantDias) {
          mensaje += "   | Difference: " + (cantDias - cantDiasAnterior);
          double porc = ((double)(cantDias - cantDiasAnterior)) / (double)cantDiasAnterior;
          String stringPorc = DobleHelper.doble2String(porc*100);
          mensaje += " (" + stringPorc + "%)";
        }
        g.drawString(mensaje, x(0), 200 + 20 * (this.sequiasMayores75.size() + this.sequiasMayores75MayorUmbralDistancia.size() + i));
      }
    }
      
  }
  
  public int x(double x) {
    return ((int) Math.floor(x * 10));
  }
  
  public int y(double y) {
    return (-(int) Math.floor(y * 100)) + 120;
  }  

}
