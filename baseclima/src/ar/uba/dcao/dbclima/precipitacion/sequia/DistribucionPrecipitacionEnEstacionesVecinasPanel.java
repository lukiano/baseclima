package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.util.MathFunction;
import ar.uba.dcao.dbclima.correlation.CalculadorCorrelacionEnRangos;
import ar.uba.dcao.dbclima.correlation.KSTest;
import ar.uba.dcao.dbclima.correlation.SignificanciaCorrelacionHelper;
import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.CorrelacionNormalizadaEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.estacion.EstacionSatelital;
import ar.uba.dcao.dbclima.precipitacion.rango.PromedioPrecipitacionAnualConSatelitesProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.PromedioPrecipitacionAnualProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.sequia.DatosSequiaEnEstacionVecina.ConfidenceType;
import ar.uba.dcao.dbclima.utils.DobleHelper;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Extiende al panel del que hereda agregando el soporte para estaciones vecinas mencionado en
 * VisorDistribucionPrecipitacionEnEstacionesVecinas.
 * @see CorrelacionEstaciones
 * @see CorrelacionNormalizadaEstaciones
 * @see CalculadorCorrelacionEnRangos
 * @see SignificanciaCorrelacionHelper
 * @see EstacionSatelital
 * @see KSTest
 *
 */
@SuppressWarnings("serial")
public class DistribucionPrecipitacionEnEstacionesVecinasPanel extends DistribucionPrecipitacionPanel {

  /**
   * Estructura que guarda los calculos realizados para cada estacion vecina, para que luego
   * sean mostrados en pantalla.
   *
   */
  static class DatosEstacionVecina {

    public Estacion estacion;

    public double distanciaFisica;

    public CorrelacionEstaciones correlacion;

    public double significanciaCorrelacion;

    public DatosEstacionVecina(Estacion estacion, double distanciaFisica, CorrelacionEstaciones correlacion) {
      this.estacion = estacion;
      this.distanciaFisica = distanciaFisica;
      this.correlacion = correlacion;
      this.significanciaCorrelacion = SignificanciaCorrelacionHelper.obtenerSignificancia(correlacion.getCorrelacion(),
          correlacion.getNumRegsUsados());
    }

    @Override
    public String toString() {
      return "[Station:" + this.estacion.getNombre() + " / Distance:" + DobleHelper.doble2String(this.distanciaFisica)
          + " / Correlation:" + DobleHelper.doble2String(this.correlacion.getCorrelacion()) + " / Sample size:"
          + this.correlacion.getNumRegsUsados() + " / Significance of Correlation:"
          + DobleHelper.doble2String(significanciaCorrelacion)
          // + " / Slope:" + DobleHelper.doble2String(this.correlacion.getPendiente())
          // + " / Y-Intercept:" +
          // DobleHelper.doble2String(this.correlacion.getOrdenadaOrigen())
          + "]";
    }

  }

  /**
   * Ordena las estaciones segun el numero de correlacion.
   *
   */
  static class EstacionIDPorCorrelacionComparator implements Comparator<DatosEstacionVecina> {

    public int compare(DatosEstacionVecina est1, DatosEstacionVecina est2) {
      return est1.correlacion.getCorrelacion().compareTo(est2.correlacion.getCorrelacion());
    }

  }

  /**
   * Clase para realizar los calculos con las estaciones vecinas en otro thread
   * para que no moleste al thread de la GUI.
   */
  private final Runnable CALCULAR_ESTACIONES_VECINAS = new Runnable() {

    public void run() {
      Set<DatosEstacionVecina> conjuntoVecinos = calcularCorrelaciones();
      if (conjuntoVecinos != null) {
        conjuntoVecinos.addAll(calcularCorrelacionesSatelitales());
        calcularPrecipitacionEnVecinas(conjuntoVecinos);
      }
      // SwingUtilities.invokeLater(REPINTAR);
      // REPINTAR.run();
    }

  };

  private final Runnable REPINTAR = new Runnable() {
    public void run() {
      Dimension dimension = getPreferredSize();
      dimension.height += 20 * (2 * estacionesVecinasConBuenaCorrelacion.size() + estacionesVecinasConMuchosNull.size() + estacionesVecinasConPocaCorrelacion
          .size());
      setPreferredSize(dimension);
      repaint();
    }
  };

  private Thread backgroundThread;

  private Map<DatosEstacionVecina, DatosSequiaEnEstacionVecina> estacionesVecinasConBuenaCorrelacion;

  private Set<DatosEstacionVecina> estacionesVecinasConMuchosNull;

  private Map<DatosEstacionVecina, DatosSequiaEnEstacionVecina> estacionesVecinasConPocaCorrelacion;

  private double umbralCorrelacion;

  private double umbralDistanciaEstacion;

  private double umbralSoC;

  public Set<DatosEstacionVecina> calcularCorrelaciones() {
    
    List<Estacion> estacionesVecinas = ClasificadorSequiasHelper.dameEstacionesVecinasCercanas(this.estacionBase, this.umbralDistanciaEstacion);
  
    Date comienzo = this.sequia.getComienzo();
    Date fin = FechaHelper.dameFechaSumada(comienzo, this.sequia.getLongitud());
  
    if (Thread.currentThread().isInterrupted()) {
      return null;
    }
  
    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzo, fin);
    proyectorRango.setUmbralNulos(this.umbralNulos);
    proyectorRango.setExcluirAnioCentral(true);
  
    Set<DatosEstacionVecina> resultado = new HashSet<DatosEstacionVecina>();
    for (Estacion estacionVecina : estacionesVecinas) {
      if (Thread.currentThread().isInterrupted()) {
        return null;
      }
      CorrelacionEstaciones correlacion = CalculadorCorrelacionEnRangos.getCorr(this.estacionBase, estacionVecina,
          proyectorRango, true);
      Double distanciaCorrelacion = correlacion.getCorrelacion();
      if (distanciaCorrelacion != null) {
        double distancia = dameDistancia(this.estacionBase, estacionVecina);
        DatosEstacionVecina datosVecina = new DatosEstacionVecina(estacionVecina, distancia, correlacion);
        resultado.add(datosVecina);
      }
    }
  
    return resultado;
  }

  @Override
  public void calcularPrecipitacion() {
    super.calcularPrecipitacion();
    if (this.backgroundThread != null && this.backgroundThread.isAlive()) {
      this.backgroundThread.interrupt();
      try {
        this.backgroundThread.join();
      } catch (InterruptedException ignored) {
      }
    }
    this.limpiarEstructuras();
    this.backgroundThread = new Thread(this.CALCULAR_ESTACIONES_VECINAS, "Neighbor analyzer");
    this.backgroundThread.start();
  }

  public double dameDistancia(Estacion estacionBase, Estacion estacionVecina) {
    int estacionBaseLatitud = estacionBase.getLatitud();
    int estacionBaseLongitud = estacionBase.getLongitud();
    int estacionActualLatitud = estacionVecina.getLatitud();
    int estacionActualLongitud = estacionVecina.getLongitud();
    int diferenciaLatitud = (estacionActualLatitud - estacionBaseLatitud) * (estacionActualLatitud - estacionBaseLatitud);
    int diferenciaLongitud = (estacionActualLongitud - estacionBaseLongitud) * (estacionActualLongitud - estacionBaseLongitud);
    return Math.sqrt(diferenciaLatitud + diferenciaLongitud) / 1000;
  }

  public void mostrarCorrelaciones() {
    Map<Estacion, CorrelacionNormalizadaEstaciones> mapa = new HashMap<Estacion, CorrelacionNormalizadaEstaciones>();
    synchronized (this.estacionesVecinasConMuchosNull) {
      for (DatosEstacionVecina vecina : this.estacionesVecinasConMuchosNull) {
        if (vecina.correlacion instanceof CorrelacionNormalizadaEstaciones) {
          mapa.put(vecina.estacion, (CorrelacionNormalizadaEstaciones) vecina.correlacion);
        }
      }
    }
    synchronized (this.estacionesVecinasConPocaCorrelacion) {
      for (DatosEstacionVecina vecina : this.estacionesVecinasConPocaCorrelacion.keySet()) {
        if (vecina.correlacion instanceof CorrelacionNormalizadaEstaciones) {
          mapa.put(vecina.estacion, (CorrelacionNormalizadaEstaciones) vecina.correlacion);
        }
      }
    }
    synchronized (this.estacionesVecinasConBuenaCorrelacion) {
      for (DatosEstacionVecina estacionVecina : this.estacionesVecinasConBuenaCorrelacion.keySet()) {
        if (estacionVecina.correlacion instanceof CorrelacionNormalizadaEstaciones) {
          mapa.put(estacionVecina.estacion, (CorrelacionNormalizadaEstaciones) estacionVecina.correlacion);
        }
      }
    }
    if (mapa.isEmpty()) {
      JOptionPane.showMessageDialog(this, "No correlations available!", "Unable to show correlations",
          JOptionPane.INFORMATION_MESSAGE);
    } else {
      VisorCorrelaciones visorCorrelaciones = new VisorCorrelaciones();
      visorCorrelaciones.setCorrelaciones(mapa, this.precipitacionAnios);
      visorCorrelaciones.setVisible(true);
      visorCorrelaciones.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      visorCorrelaciones.repaint();
    }
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    int pos = 200 + 20 + this.precipitacionAnios.size() * 20 / 5;
    g.setColor(Color.magenta);
    synchronized (this.estacionesVecinasConBuenaCorrelacion) {
      for (Map.Entry<DatosEstacionVecina, DatosSequiaEnEstacionVecina> entry : this.estacionesVecinasConBuenaCorrelacion
          .entrySet()) {
        DatosSequiaEnEstacionVecina valor = entry.getValue();
        g.drawString("Station: " + entry.getKey(), 0, pos);
        g.drawString(valor.toString(), 0, pos + 20);
        pos += 40;
      }
    }
    g.setColor(Color.magenta.darker().darker());
    synchronized (this.estacionesVecinasConMuchosNull) {
      for (DatosEstacionVecina estacionID : this.estacionesVecinasConMuchosNull) {
        g.drawString("Discarded values (not enough data) of station: " + estacionID, 0, pos);
        pos += 20;
      }
    }
    g.setColor(Color.blue);
    synchronized (this.estacionesVecinasConPocaCorrelacion) {
      for (Map.Entry<DatosEstacionVecina, DatosSequiaEnEstacionVecina> entry : this.estacionesVecinasConPocaCorrelacion
          .entrySet()) {
        DatosSequiaEnEstacionVecina valor = entry.getValue();
  
        g.drawString("Station: " + entry.getKey(), 0, pos);
        g.drawString(valor.toString(), 0, pos + 20);
        pos += 40;
      }
    }
  }

  public void setUmbralDistanciaEstacion(double umbral) {
    this.umbralDistanciaEstacion = umbral;
  }

  public void setUmbralCorrelacion(double umbral) {
    this.umbralCorrelacion = umbral;
  }

  public void setUmbralSoC(double umbral) {
    this.umbralSoC = umbral;
  }

  private Set<DatosEstacionVecina> calcularCorrelacionesSatelitales() {
    Set<DatosEstacionVecina> resultado = new HashSet<DatosEstacionVecina>();
  
    Date comienzo = this.sequia.getComienzo();
    Date fin = FechaHelper.dameFechaSumada(comienzo, sequia.getLongitud());
  
    List<EstacionSatelital> estacionesSatelitales = ClasificadorSequiasHelper
        .dameEstacionesSatelitalesCercanas(this.estacionBase, this.umbralDistanciaEstacion);
  
    PromedioPrecipitacionAnualProyectorRango proyectorRango = new PromedioPrecipitacionAnualConSatelitesProyectorRango(
        comienzo, fin);
    proyectorRango.setUmbralNulos(this.umbralNulos);
    proyectorRango.setExcluirAnioCentral(true);
  
    for (EstacionSatelital estacionSatelital : estacionesSatelitales) {
      CorrelacionEstaciones correlacion = CalculadorCorrelacionEnRangos.getCorr(this.estacionBase, estacionSatelital,
          proyectorRango, true);
      Double distanciaCorrelacion = correlacion.getCorrelacion();
      if (distanciaCorrelacion != null) {
        double distancia = dameDistancia(this.estacionBase, estacionSatelital);
        DatosEstacionVecina estacionVecina = new DatosEstacionVecina(estacionSatelital, distancia, correlacion);
        resultado.add(estacionVecina);
      }
    }
    
    return resultado;
  }

  private void calcularPrecipitacionEnVecinas(Set<DatosEstacionVecina> estacionesVecinas) {
    Date comienzoSequia = this.sequia.getComienzo();
    Date finSequia = FechaHelper.dameFechaSumada(comienzoSequia, this.sequia.getLongitud());
    for (DatosEstacionVecina vecina : estacionesVecinas) {
      if (Thread.currentThread().isInterrupted()) {
        return;
      }

      Double valor = ClasificadorSequiasHelper.damePrecipitacionCaidaEnPeriodoParaEstacion(vecina.estacion,
          comienzoSequia, finSequia, this.umbralNulos);

      if (valor == null) {
        // se ignora la estacion
        this.estacionesVecinasConMuchosNull.add(vecina);
      } else {
        double precipitacionVecina = valor.doubleValue();

        List<Integer> aniosEnComun = ClasificadorSequiasHelper.obtenerAniosEnComun(this.estacionBase, vecina.estacion,
            comienzoSequia, finSequia, this.umbralNulos);

        // se calculan los SPI

        DatosSequiaEnEstacionVecina datosRangoEnEstacionVecina;
        if (aniosEnComun.size() < SPIHelper.CANTIDAD_MINIMA_ANIOS) {
           datosRangoEnEstacionVecina = new DatosSequiaEnEstacionVecina(
               precipitacionVecina,
               null, // sin SPI local acotado 
               null, // sin SPI vecino
               aniosEnComun,
               null, // sin KS test
               null);// sin KS test 
        } else {
          MathFunction spiF = ClasificadorSequiasHelper.calcularSPI(aniosEnComun, comienzoSequia, finSequia, this.estacionBase, this.umbralNulos);
          double spiLocalAcotado = spiF.evaluate(0d);
          Distribution neighborDistribution = ClasificadorSequiasHelper.calcularEmpirica(aniosEnComun, comienzoSequia, finSequia,
              vecina.estacion, this.umbralNulos); 
          double percent = neighborDistribution.cdf(precipitacionVecina);
          Distribution localDistribution = ClasificadorSequiasHelper.calcularEmpirica(aniosEnComun, comienzoSequia, finSequia,
              this.estacionBase, this.umbralNulos); 
          double linearValue = localDistribution.inverseF(percent);
          double spiVecino = spiF.evaluate(linearValue);
          
          double[] muestraBase = ClasificadorSequiasHelper.obtenerMuestra(aniosEnComun, comienzoSequia, finSequia, this.estacionBase, this.umbralNulos);
          ClasificadorSequiasHelper.moverACero(muestraBase);
          double[] muestraVecina = ClasificadorSequiasHelper.obtenerMuestra(aniosEnComun, comienzoSequia, finSequia, vecina.estacion, this.umbralNulos);
          ClasificadorSequiasHelper.moverACero(muestraVecina);
          double kstest = KSTest.ksTest(muestraBase, muestraVecina);
          
          datosRangoEnEstacionVecina = new DatosSequiaEnEstacionVecina(
              precipitacionVecina,
              spiLocalAcotado, 
              spiVecino,
              aniosEnComun,
              ConfidenceType.KS,
              kstest);
        }

        if (vecina.significanciaCorrelacion <= this.umbralSoC
            && vecina.correlacion.getCorrelacion() >= this.umbralCorrelacion) {
          this.estacionesVecinasConBuenaCorrelacion.put(vecina, datosRangoEnEstacionVecina);
        } else {
          this.estacionesVecinasConPocaCorrelacion.put(vecina, datosRangoEnEstacionVecina);
        }
      }

      REPINTAR.run();
    }
  }

  private void limpiarEstructuras() {
    this.estacionesVecinasConBuenaCorrelacion = Collections
        .synchronizedMap(new TreeMap<DatosEstacionVecina, DatosSequiaEnEstacionVecina>(
            new EstacionIDPorCorrelacionComparator()));
    this.estacionesVecinasConMuchosNull = Collections.synchronizedSet(new HashSet<DatosEstacionVecina>());
    this.estacionesVecinasConPocaCorrelacion = Collections
        .synchronizedMap(new HashMap<DatosEstacionVecina, DatosSequiaEnEstacionVecina>());
  }

}
