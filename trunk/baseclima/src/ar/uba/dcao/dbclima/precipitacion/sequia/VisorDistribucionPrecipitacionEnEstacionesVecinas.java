package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.precipitacion.sequia.DistribucionPrecipitacionPanel;
import ar.uba.dcao.dbclima.precipitacion.sequia.VisorDistribucionPrecipitacion;


/**
 * Extiende al frame anterior mostrando ademas las precipitaciones en el momento de la
 * sequia para las estaciones vecinas (con una distancia euclideana menor a un numero determinado
 * expresado en grados). Tambien muestra, para cada estacion vecina, su correlacion o test KS con la
 * estacion base.
 * Las estaciones vecinas incluyen a los puntos satelitales si estos se encuentran disponibles.
 * @see DistribucionPrecipitacionEnEstacionesVecinasPanel
 */
public class VisorDistribucionPrecipitacionEnEstacionesVecinas extends VisorDistribucionPrecipitacion {

  /**
   * 
   */
  private static final long serialVersionUID = -3360765737472717111L;

  private double umbralCorrelacion;
  
  private double umbralSoC;

  private double umbralDistanciaEstacion;

  /**
   * Auto-generated main method to display this JFrame
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    VisorDistribucionPrecipitacionEnEstacionesVecinas inst = new VisorDistribucionPrecipitacionEnEstacionesVecinas();
    inst.setVisible(true);
    inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public VisorDistribucionPrecipitacionEnEstacionesVecinas() {
    super();
  }

  public VisorDistribucionPrecipitacionEnEstacionesVecinas(Estacion estacion, List<Sequia> sequias) {
    super(estacion, sequias);
  }

  @Override
  protected DistribucionPrecipitacionPanel nuevoPanel() {
    return new DistribucionPrecipitacionEnEstacionesVecinasPanel();
  }

  @Override
  protected void establecerTitulo() {
    this.setTitle("Precipitation Viewer with neighbor stations (normalized)");
  }

  @Override
  protected void inicializarPanel(final int widthInicial, final int heightDistPanel) {
    this.umbralDistanciaEstacion = 2.0;
    this.umbralSoC = 0.01;
    this.umbralCorrelacion = 0.8;
    ((DistribucionPrecipitacionEnEstacionesVecinasPanel)jDistribucionPanel).setUmbralDistanciaEstacion(umbralDistanciaEstacion);
    ((DistribucionPrecipitacionEnEstacionesVecinasPanel)jDistribucionPanel).setUmbralCorrelacion(umbralCorrelacion);
    ((DistribucionPrecipitacionEnEstacionesVecinasPanel)jDistribucionPanel).setUmbralSoC(umbralSoC);
    super.inicializarPanel(widthInicial, heightDistPanel);
  }
  
  @Override
  protected void llenarUmbralMenu(JMenu jUmbralMenu) {
    super.llenarUmbralMenu(jUmbralMenu);
    
    JMenuItem umbralDistanciaEstacionMenuItem = new JMenuItem();
    jUmbralMenu.add(umbralDistanciaEstacionMenuItem);
    umbralDistanciaEstacionMenuItem.setText("Set stations distance threshold...");
    umbralDistanciaEstacionMenuItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent event) {
        boolean umbralOK = false;
        while (!umbralOK) {
          String viejoUmbral = Double.toString(umbralDistanciaEstacion);
          Object nuevoUmbral = JOptionPane.showInputDialog(VisorDistribucionPrecipitacionEnEstacionesVecinas.this,
              THRESHOLD_TITLE + " (greater than 0.0)", "Change stations distance threshold", JOptionPane.INFORMATION_MESSAGE, null, null,
              viejoUmbral);
          if (nuevoUmbral == null) {
            // se cancelo la operacion
            umbralOK = true;
          } else {
            try {
              double nuevoValor = Double.parseDouble(nuevoUmbral.toString());
              if (nuevoValor > 0.0) {
                umbralDistanciaEstacion = nuevoValor;
                umbralOK = true;
                ((DistribucionPrecipitacionEnEstacionesVecinasPanel)jDistribucionPanel).setUmbralDistanciaEstacion(umbralDistanciaEstacion);
                jDistribucionPanel.calcularPrecipitacion();
                jDistribucionPanel.calcularDistribucion();
                VisorDistribucionPrecipitacionEnEstacionesVecinas.this.repaint();
              } else {
                JOptionPane.showMessageDialog(VisorDistribucionPrecipitacionEnEstacionesVecinas.this,
                    NUMBER_OUTSIDE_RANGE, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
              }
            } catch (NumberFormatException ex) {
              JOptionPane.showMessageDialog(VisorDistribucionPrecipitacionEnEstacionesVecinas.this,
                  NUMBER_NOT_VALID, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }

    });

    JMenuItem umbralCorrelacionMenuItem = new JMenuItem();
    jUmbralMenu.add(umbralCorrelacionMenuItem);
    umbralCorrelacionMenuItem.setText("Set correlation threshold...");
    umbralCorrelacionMenuItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent event) {
        boolean umbralOK = false;
        while (!umbralOK) {
          String viejoUmbral = Double.toString(umbralCorrelacion);
          Object nuevoUmbral = JOptionPane.showInputDialog(VisorDistribucionPrecipitacionEnEstacionesVecinas.this,
              THRESHOLD_TITLE + " (between -1.0 and 1.0)", "Change correlation threshold", JOptionPane.INFORMATION_MESSAGE, null, null,
              viejoUmbral);
          if (nuevoUmbral == null) {
            // se cancelo la operacion
            umbralOK = true;
          } else {
            try {
              double nuevoValor = Double.parseDouble(nuevoUmbral.toString());
              if (nuevoValor >= -1.0 && nuevoValor <= 1.0) {
                umbralCorrelacion = nuevoValor;
                umbralOK = true;
                ((DistribucionPrecipitacionEnEstacionesVecinasPanel)jDistribucionPanel).setUmbralCorrelacion(umbralCorrelacion);
                jDistribucionPanel.calcularPrecipitacion();
                jDistribucionPanel.calcularDistribucion();
                VisorDistribucionPrecipitacionEnEstacionesVecinas.this.repaint();
              } else {
                JOptionPane.showMessageDialog(VisorDistribucionPrecipitacionEnEstacionesVecinas.this,
                    NUMBER_OUTSIDE_RANGE, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
              }
            } catch (NumberFormatException ex) {
              JOptionPane.showMessageDialog(VisorDistribucionPrecipitacionEnEstacionesVecinas.this,
                  NUMBER_NOT_VALID, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }

    });

    JMenuItem umbralSoCMenuItem = new JMenuItem();
    jUmbralMenu.add(umbralSoCMenuItem);
    umbralSoCMenuItem.setText("Set Soc threshold...");
    umbralSoCMenuItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent event) {
        boolean umbralOK = false;
        while (!umbralOK) {
          Double viejoUmbral = Double.valueOf(umbralSoC);
          Object nuevoUmbral = JOptionPane.showInputDialog(
              VisorDistribucionPrecipitacionEnEstacionesVecinas.this,
              THRESHOLD_TITLE, 
              "Change significance of correlation threshold", 
              JOptionPane.INFORMATION_MESSAGE, 
              null, 
              new Double[] {0.05, 0.025, 0.01, 0.005, 0.0005, 0.999},
              viejoUmbral);
          
          if (nuevoUmbral == null) {
            // se cancelo la operacion
            umbralOK = true;
          } else {
            try {
              double nuevoValor = ((Double)nuevoUmbral).doubleValue();
              umbralSoC = nuevoValor;
              umbralOK = true;
              ((DistribucionPrecipitacionEnEstacionesVecinasPanel)jDistribucionPanel).setUmbralSoC(umbralSoC);
              jDistribucionPanel.calcularPrecipitacion();
              jDistribucionPanel.calcularDistribucion();
              VisorDistribucionPrecipitacionEnEstacionesVecinas.this.repaint();
            } catch (NumberFormatException ex) {
              JOptionPane.showMessageDialog(VisorDistribucionPrecipitacionEnEstacionesVecinas.this,
                  NUMBER_NOT_VALID, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }

    });

  }
  
  @Override
  protected void crearMenues() {
    super.crearMenues();
    JMenu correlacionesMenu = new JMenu();
    correlacionesMenu.setText("Correlations");
    
    JMenuItem mostrarCorrelacionesMenuItem = new JMenuItem();
    mostrarCorrelacionesMenuItem.setText("Show correlations...");
    mostrarCorrelacionesMenuItem.addActionListener(new ActionListener() {
    
      public void actionPerformed(ActionEvent arg0) {
        ((DistribucionPrecipitacionEnEstacionesVecinasPanel)jDistribucionPanel).mostrarCorrelaciones();
      }
    
    });
    correlacionesMenu.add(mostrarCorrelacionesMenuItem);
    this.getJMenuBar().add(correlacionesMenu);
  }

}
