package ar.uba.dcao.dbclima.precipitacion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.hibernate.Query;
import org.hibernate.Session;

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.DistributionFactory;
import umontreal.iro.lecuyer.probdist.PiecewiseLinearEmpiricalDist;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.precipitacion.rango.AcumPrecipitacionAnualProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.Rango;
import ar.uba.dcao.dbclima.utils.DobleHelper;
import ar.uba.dcao.dbclima.utils.FechaHelper;


/**
 * Este es el panel de Java que acompania al frame. Aqui se realiza el proceso de visualizacion, tomando
 * los datos especificados en el frame.
 * @see VisorDistribucionPrecipitacionDelMes
 */
@SuppressWarnings("serial")
public class DistribucionPrecipitacionDelMesPanel extends JPanel {
  
  static class RegistroDiarioPrecipitacionComparator implements Comparator<RegistroDiario> {

    public int compare(RegistroDiario rd1, RegistroDiario rd2) {
      return rd1.getPrecipitacion().compareTo(rd2.getPrecipitacion());
    }
    
  }
  
  protected ContinuousDistribution distribution;
  
  protected Long estacionId;
  
  protected Class<? extends ContinuousDistribution> claseDistribucion;
  
  protected double p25, p50, p75, p90;
  
  protected double umbralDistancia;
  
  protected boolean cdf;
  
  protected int mes;
  
  private Bag<Double> valores;
  
  protected Map<Number, Double> precipitacionAnios;
  
  private double mayorValor;
  
  private double multiplicadorHorizontal = 1;
  
  private List<RegistroDiario> registrosMayoresP50;
  
  private List<RegistroDiario> registrosMayoresP75;
  
  public DistribucionPrecipitacionDelMesPanel() {
    this.setBackground(Color.white);
  }

  public void setClaseDistribucion(Class<? extends ContinuousDistribution> nuevaClase) {
    this.claseDistribucion = nuevaClase;
  }
  
  public void setEstacion(Long nuevaEstacionId) {
    this.estacionId = nuevaEstacionId;
  }

  public void setMes(int mes) {
    this.mes = mes;
  }

  public void setUmbralDistancia(double umbral) {
    this.umbralDistancia = umbral;
  }

  public void setCDF(boolean cdf) {
    this.cdf = cdf;
  }
  
  public void calcularPrecipitacion() {
    if (this.mes == -1) {
      return;
    }

    int finDia = dameFinDia(this.mes);
    
    AcumPrecipitacionAnualProyectorRango proyectorRango = new AcumPrecipitacionAnualProyectorRango(1, this.mes, finDia, this.mes);

    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    Estacion estacion =  DAOFactory.getEstacionDAO(sess).findByID(this.estacionId);
    sess.getTransaction().commit();
    
    proyectorRango.setIncluir0mm(false);
    List<Rango> rangos = proyectorRango.proyectarRangos(estacion);

    valores = new HashBag<Double>();
    precipitacionAnios = new TreeMap<Number, Double>();
    
    mayorValor = 0.0;
    
    for (Rango rango : rangos) {
      if (rango.valor() == null) {
        this.precipitacionAnios.put(FechaHelper.dameAnio(rango.comienzo()), null);
      } else {
        double precipitacion = rango.valor().doubleValue();
        if (precipitacion > mayorValor) {
          mayorValor = precipitacion;
        }
        this.precipitacionAnios.put(FechaHelper.dameAnio(rango.comienzo()), precipitacion);
        valores.add(precipitacion);  
      }
    }

    this.establecerEscalaHorizontalSegunPrecipitacionAcumulada();
    
    int width = x(mayorValor) + 30;
    int height = 200 + 20 + 20 * this.precipitacionAnios.size() / 5;
    this.setPreferredSize(new Dimension(width, height));
    this.revalidate();
  }
  
  public static int dameFinDia(int mes) {
    if (mes == 1 || mes == 3 || mes == 5 || mes == 7 || mes == 8 || mes == 10 || mes == 12) {
      return 31;
    }
    if (mes == 4 || mes == 6 || mes == 9 || mes == 11) {
      return 30;
    }
    return 28;
  }

//  private void establecerEscalaHorizontalSegunPrecipitacionPromedio() {
//    if (mayorValor > 100) {
//      multiplicadorHorizontal = 5.0;
//    } else if (mayorValor > 75) {
//      multiplicadorHorizontal = 10.0;
//    } else if (mayorValor > 50) {
//        multiplicadorHorizontal = 15.0;
//    } else if (mayorValor > 25) {
//      multiplicadorHorizontal = 20.0;
//    } else if (mayorValor > 10) {
//      multiplicadorHorizontal = 40.0;
//    } else if (mayorValor > 5) {
//      multiplicadorHorizontal = 80.0;
//    } else if (mayorValor > 2) {
//      multiplicadorHorizontal = 160.0;
//    } else if (mayorValor > 1) {
//      multiplicadorHorizontal = 320.0;
//    } else if (mayorValor > 0.5) {
//      multiplicadorHorizontal = 480.0;
//    } else if (mayorValor > 0.1) {
//      multiplicadorHorizontal = 640.0;
//    } else {
//      multiplicadorHorizontal = 800.0;
//    }
//  }
  
  private void establecerEscalaHorizontalSegunPrecipitacionAcumulada() {
    if (mayorValor > 2500) {
      multiplicadorHorizontal = 0.2;
    } else if (mayorValor > 1500) {
      multiplicadorHorizontal = 0.3;
    } else if (mayorValor > 1250) {
        multiplicadorHorizontal = 0.5;
    } else if (mayorValor > 1000) {
      multiplicadorHorizontal = 0.7;
    } else if (mayorValor > 500) {
      multiplicadorHorizontal = 1.5;
    } else if (mayorValor > 250) {
      multiplicadorHorizontal = 3.0;
    } else if (mayorValor > 100) {
      multiplicadorHorizontal = 5.0;
    } else if (mayorValor > 50) {
      multiplicadorHorizontal = 7.0;
    } else if (mayorValor > 10) {
      multiplicadorHorizontal = 10.0;
    } else if (mayorValor > 5) {
      multiplicadorHorizontal = 15.0;
    } else {
      multiplicadorHorizontal = 20.0;
    }
  }

  public void calcularDistribucion() {
    if (this.mes == -1) {
      return;
    }

    double[] valoresDefinitivos;

    valoresDefinitivos = new double[valores.size()];
    int i = 0;
    for (Double valor : valores) {
      valoresDefinitivos[i] = valor.doubleValue();
      i++;
    }
    
    if (valoresDefinitivos.length < 2) {
      return;
    }
    
    if (this.claseDistribucion.equals(PiecewiseLinearEmpiricalDist.class)) {
      this.distribution = new PiecewiseLinearEmpiricalDist(valoresDefinitivos);
    } else {
      this.distribution = DistributionFactory.getDistribution(this.claseDistribucion, valoresDefinitivos, valoresDefinitivos.length);
    }
    if (this.distribution != null) {
      p25 = this.distribution.inverseF(0.25);
      p50 = this.distribution.inverseF(0.50);
      p75 = this.distribution.inverseF(0.75);
      p90 = this.distribution.inverseF(0.90);
    }
    this.rellenarRegistros();
  }

  @SuppressWarnings("unchecked")
  private void rellenarRegistros() {
    this.registrosMayoresP50 = new ArrayList<RegistroDiario>();
    this.registrosMayoresP75 = new ArrayList<RegistroDiario>();
    int percentil50 = (int)Math.floor(p50 * 10); // multiplicado por 10 porque en la base estan en decimas de milimetro
    int percentil75 = (int)Math.floor(p75 * 10);
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    Query query = sess.createQuery("FROM RegistroDiario WHERE estacion.id = ? AND month(fecha) = ? AND precipitacion >= ?").
      setLong(0, this.estacionId).setInteger(1, this.mes);
    List<RegistroDiario> registros = query.setInteger(2, percentil50).list();
    if (registros != null) {
      for (RegistroDiario registro : registros) {
        double precip = registro.getPrecipitacion();
        precip /= 10;
        sess.evict(registro);
        if (registro.getPrecipitacion() > percentil75) {
          this.registrosMayoresP75.add(registro);
        } else {
          this.registrosMayoresP50.add(registro);
        }
      }
    }
    sess.getTransaction().commit();
    RegistroDiarioPrecipitacionComparator comparator = new RegistroDiarioPrecipitacionComparator();
    Collections.sort(this.registrosMayoresP50, comparator);
    Collections.sort(this.registrosMayoresP75, comparator);
    
    int height = this.getPreferredSize().height + 20 * (this.registrosMayoresP50.size() + this.registrosMayoresP75.size() + 2);
    int width = this.getPreferredSize().width;
    this.setPreferredSize(new Dimension(width, height));
    this.revalidate();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    //((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (this.mes == -1) {
      return;
    }
    
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
          g.drawString("Discarded year: " + entry.getKey(), 150*count, y);
        } else {
          g.drawString("Year " + entry.getKey() + " : " + DobleHelper.doble2String(entry.getValue()), 150*count, y);
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
        if (this.cdf) {
          y1 = y(this.distribution.cdf(d));
          y2 = y(this.distribution.cdf(d + intervalo));
        } else {
          /*
          y1 = y(this.distribution.cdf(d) - this.distribution.cdf(d - intervalo));
          y2 = y(this.distribution.cdf(d + intervalo) - this.distribution.cdf(d));
          */
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
      
      g.setColor(Color.black);
      g.drawString("Scale: 1 to " + multiplicadorHorizontal, 0, 170);
      int y = 200;
      int count = 0;
      for (Map.Entry<Number, Double> entry : this.precipitacionAnios.entrySet()) {
        if (entry.getValue() == null) {
          g.drawString("Discarded year: " + entry.getKey(), 150*count, y);
        } else {
          g.drawString("Year " + entry.getKey() + " : " + DobleHelper.doble2String(entry.getValue()), 150*count, y);
        }
        count++;
        if (count == 5) {
          y += 20;
          count = 0;
        }
      }
      y += 20;
      g.setColor(Color.cyan.darker());
      Double ultimaPrecip = null;
      
      Session sess = DBSessionFactory.getInstance().getCurrentSession();
      sess.beginTransaction();

      for (RegistroDiario registro : this.registrosMayoresP50) {
        registro = (RegistroDiario) sess.merge(registro);
        String texto = registro.toString();
        texto += " | precip value:" + DobleHelper.doble2String((registro.getPrecipitacion().doubleValue() / 10d));
        if (ultimaPrecip != null) {
          double diferencia = registro.getPrecipitacion().doubleValue() - ultimaPrecip;
          if (diferencia > 0) {
            texto += " | difference with previous record:" + DobleHelper.doble2String(diferencia);
          }
        }
        g.drawString(texto, 0, y);
        ultimaPrecip = registro.getPrecipitacion().doubleValue();
        y += 20;
      }
      g.setColor(Color.red);
      double divisor = p75 - p50;
      for (RegistroDiario registro : this.registrosMayoresP75) {
        registro = (RegistroDiario) sess.merge(registro);
        double D = (registro.getPrecipitacion().doubleValue() - p75) / divisor;
        String texto = registro.toString() + " | (X - p75) / (p75 - p50):" + DobleHelper.doble2String(D);
        texto += " | precip value:" + DobleHelper.doble2String((registro.getPrecipitacion().doubleValue() / 10d));
        if (ultimaPrecip != null) {
          double diferencia = registro.getPrecipitacion().doubleValue() - ultimaPrecip;
          if (diferencia > 0) {
            texto += " | difference with previous record:" + DobleHelper.doble2String(diferencia);
          }
        }
        g.drawString(texto, 0, y);
        ultimaPrecip = registro.getPrecipitacion().doubleValue();
        y += 20;
      }
      
      sess.getTransaction().commit();
    }
  }
  
  public int x(double x) {
    return ((int) Math.floor(x * multiplicadorHorizontal));
  }
  
  public int y(double y) {
    return (-(int) Math.floor(y * 100)) + 120;
  }  

}
