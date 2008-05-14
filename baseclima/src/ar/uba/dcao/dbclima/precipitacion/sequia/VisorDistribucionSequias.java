package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.hibernate.Session;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.GammaDist;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.probdist.PiecewiseLinearEmpiricalDist;
import umontreal.iro.lecuyer.probdist.WeibullDist;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Este es el frame encargado de mostrar las distribuciones de las sequias.
 * Se elige una estacion y muestra aquellas sequias de mayor duracion.
 * Cuando se elige una para un mayor analisis de la misma, se abre
 * el frame VisorDistribucionPrecipitacionEnEstacionesVecinas.
 * @see VisorDistribucionPrecipitacionEnEstacionesVecinas
 * @see DistribucionSequiaPanel
 */
public class VisorDistribucionSequias extends javax.swing.JFrame {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1737403110196320406L;

  protected static final String THRESHOLD_TITLE = "Enter new threshold";

  protected static final String NUMBER_NOT_VALID = "The entered number is not valid";

  protected static final String ERROR_OCCURRED = "An error has occurred";
  
  protected static final String NUMBER_OUTSIDE_RANGE = "The entered number is outside the valid range";

  private Component jScrollPanel;
  
  private DistribucionSequiaPanel jDistribucionPanel;

  private JComboBox jEstacionComboBox;
  
  private JComboBox jDistribucionComboBox;
  
  private JMenuItem analyzeProbabilityDistanceDroughtsMenuItem;

  private JMenuItem analyzeDifferenceDayDroughtsMenuItem;
  
  private Map<String, Class<? extends ContinuousDistribution> > distribuciones;
  
  private double umbralDistanciaSequia;
  
  private int umbralSaltoMinimo;
  
  private List<Estacion> estaciones;
  
  private Long datasetId;
  
  /**
   * Auto-generated main method to display this JFrame
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(
        UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    VisorDistribucionSequias inst = new VisorDistribucionSequias();
    inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    inst.setDatasetId(null);
    inst.setVisible(true);
  }

  public VisorDistribucionSequias() {
    super();
    initGUI();
    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }
  
  public void setDatasetId(Long datasetId) {
    this.datasetId = datasetId;
    this.fillEstacionComboBox();
    this.habilitarAnalizadores();
  }

  private void initGUI() {
    try {
      final int widthInicial = 1000;
      final int heightTopPanel = 50;
      final int heightDistPanel = 600;
      
      this.establecerTitulo();

      jDistribucionPanel = new DistribucionSequiaPanel();
      jDistribucionPanel.setPreferredSize(new Dimension(widthInicial, heightDistPanel));
      jScrollPanel = new JScrollPane(jDistribucionPanel);
      jScrollPanel.setSize(getContentPane().getSize());

      JPanel jTopPanel = new JPanel();
      this.crearComboBoxes(jTopPanel);
      this.crearCheckBoxes(jTopPanel);

      this.inicializarPanel();
      jTopPanel.setPreferredSize(new Dimension(widthInicial, heightTopPanel));
      jTopPanel.setMinimumSize(new Dimension(widthInicial, heightTopPanel));
      jTopPanel.setMaximumSize(new Dimension(widthInicial, heightTopPanel));
      jTopPanel.setSize(getContentPane().getSize());
      getContentPane().add(jTopPanel, BorderLayout.NORTH);
      getContentPane().add(jScrollPanel, BorderLayout.CENTER);

      setSize(widthInicial, heightTopPanel + heightDistPanel);
      this.crearMenues();
      this.habilitarAnalizadores();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void inicializarPanel() {
    this.umbralDistanciaSequia = 10.0;
    this.umbralSaltoMinimo = 1;
    
//    int indexEstacionActual = jEstacionComboBox.getSelectedIndex();
//    Estacion estacionActual = this.estaciones.get(indexEstacionActual);
//    jDistribucionPanel.setEstacion(estacionActual);
    String nombreNuevaDist = jDistribucionComboBox.getSelectedItem().toString();
    Class<? extends ContinuousDistribution> nuevaDist = distribuciones.get(nombreNuevaDist);
    jDistribucionPanel.setIncluirSequiaUnDia(true);
    jDistribucionPanel.setClaseDistribucion(nuevaDist);
    jDistribucionPanel.setUmbralDistanciaSequia(umbralDistanciaSequia);
    jDistribucionPanel.setUmbralSaltoMinimo(umbralSaltoMinimo);
    jDistribucionPanel.setCDF(true);
    //jDistribucionPanel.calcularDistribucion();
  }
  
  private void habilitarAnalizadores() {
    this.analyzeProbabilityDistanceDroughtsMenuItem.setEnabled(jDistribucionPanel.getSequiasMayores75MayorUmbralDistancia().size() != 0);
    this.analyzeDifferenceDayDroughtsMenuItem.setEnabled(jDistribucionPanel.getSequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia().size() != 0);
  }

  private void crearMenues() {
    JMenuBar jMenuBar = new JMenuBar();
    setJMenuBar(jMenuBar);
    
    JMenu jThresholdMenu = new JMenu();
    jMenuBar.add(jThresholdMenu);
    jThresholdMenu.setText("Threshold");
    
    JMenuItem selectProbabilityDistanceThresholdMenuItem = new JMenuItem();
    jThresholdMenu.add(selectProbabilityDistanceThresholdMenuItem);
    selectProbabilityDistanceThresholdMenuItem.setText("Set probability distance threshold...");
    selectProbabilityDistanceThresholdMenuItem.addActionListener(new ActionListener() {
    
      public void actionPerformed(ActionEvent event) {
        boolean umbralOK = false;
        while (!umbralOK) {
          String viejoUmbral = Double.toString(umbralDistanciaSequia);
          Object nuevoUmbral = JOptionPane.showInputDialog(VisorDistribucionSequias.this, 
              THRESHOLD_TITLE + " (greater than 0.0)", "Change probability distance threshold", JOptionPane.INFORMATION_MESSAGE, null, null, viejoUmbral);
          if (nuevoUmbral == null) {
            // se cancelo la operacion
            umbralOK = true;
          } else {
            try {
              double nuevoValor = Double.parseDouble(nuevoUmbral.toString());
              if (nuevoValor > 0.0) {
                umbralDistanciaSequia = nuevoValor;
                umbralOK = true;
                jDistribucionPanel.setUmbralDistanciaSequia(umbralDistanciaSequia);
                jDistribucionPanel.calcularDistribucion();
                habilitarAnalizadores();
                VisorDistribucionSequias.this.repaint();
              } else {
                JOptionPane.showMessageDialog(VisorDistribucionSequias.this,
                    NUMBER_OUTSIDE_RANGE, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
              }

              umbralDistanciaSequia = Double.parseDouble(nuevoUmbral.toString());
              umbralOK = true;
            } catch (NumberFormatException ex) {
              JOptionPane.showMessageDialog(VisorDistribucionSequias.this, 
                  NUMBER_NOT_VALID, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }
    
    });

    JMenuItem selectMinimumJumpThresholdMenuItem = new JMenuItem();
    jThresholdMenu.add(selectMinimumJumpThresholdMenuItem);
    selectMinimumJumpThresholdMenuItem.setText("Set minimum jump threshold...");
    selectMinimumJumpThresholdMenuItem.addActionListener(new ActionListener() {
    
      public void actionPerformed(ActionEvent event) {
        boolean umbralOK = false;
        while (!umbralOK) {
          String viejoUmbral = Integer.toString(umbralSaltoMinimo);
          Object nuevoUmbral = JOptionPane.showInputDialog(VisorDistribucionSequias.this, 
              THRESHOLD_TITLE + " (greater than 0)", "Change minimum jump threshold", JOptionPane.INFORMATION_MESSAGE, null, null, viejoUmbral);
          if (nuevoUmbral == null) {
            // se cancelo la operacion
            umbralOK = true;
          } else {
            try {
              int nuevoValor = Integer.parseInt(nuevoUmbral.toString());
              if (nuevoValor > 0.0) {
                umbralSaltoMinimo = nuevoValor;
                umbralOK = true;
                jDistribucionPanel.setUmbralSaltoMinimo(umbralSaltoMinimo);
                jDistribucionPanel.calcularDistribucion();
                habilitarAnalizadores();
                VisorDistribucionSequias.this.repaint();
              } else {
                JOptionPane.showMessageDialog(VisorDistribucionSequias.this,
                    NUMBER_OUTSIDE_RANGE, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
              }

              umbralSaltoMinimo = Integer.parseInt(nuevoUmbral.toString());
              umbralOK = true;
            } catch (NumberFormatException ex) {
              JOptionPane.showMessageDialog(VisorDistribucionSequias.this, 
                  NUMBER_NOT_VALID, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }
    
    });

    JMenu jAnalyzeMenu = new JMenu();
    jMenuBar.add(jAnalyzeMenu);
    jAnalyzeMenu.setText("Analyze");
    
    analyzeProbabilityDistanceDroughtsMenuItem = new JMenuItem();
    jAnalyzeMenu.add(analyzeProbabilityDistanceDroughtsMenuItem);
    analyzeProbabilityDistanceDroughtsMenuItem.setText("Analyze greater than probability distance droughts...");
    analyzeProbabilityDistanceDroughtsMenuItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        VisorDistribucionPrecipitacion inst = 
          new VisorDistribucionPrecipitacionEnEstacionesVecinas(
            jDistribucionPanel.getEstacion(),
            jDistribucionPanel.getSequiasMayores75MayorUmbralDistancia());
        inst.setVisible(true);
        inst.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      }
    });
    
    analyzeDifferenceDayDroughtsMenuItem = new JMenuItem();
    jAnalyzeMenu.add(analyzeDifferenceDayDroughtsMenuItem);
    analyzeDifferenceDayDroughtsMenuItem.setText("Analyze greater than difference day droughts...");
    analyzeDifferenceDayDroughtsMenuItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        VisorDistribucionPrecipitacion inst = 
          new VisorDistribucionPrecipitacionEnEstacionesVecinas(
              jDistribucionPanel.getEstacion(),
              jDistribucionPanel.getSequiasMayores75MayorUmbralDistanciaMayorUmbralDiferencia());
        inst.setVisible(true);
        inst.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      }
    });

  }

  private void crearCheckBoxes(JPanel jTopPanel) {
    final JCheckBox jIncluyeSequiaUnDiaCheckBox = new JCheckBox("Include one-day droughts");
    jIncluyeSequiaUnDiaCheckBox.setSelected(true);
    jTopPanel.add(jIncluyeSequiaUnDiaCheckBox);
    jIncluyeSequiaUnDiaCheckBox.addActionListener(new ActionListener() {
    
      public void actionPerformed(ActionEvent e) {
        jDistribucionPanel.setIncluirSequiaUnDia(jIncluyeSequiaUnDiaCheckBox.getModel().isSelected());
        jDistribucionPanel.calcularDistribucion();
        habilitarAnalizadores();
        VisorDistribucionSequias.this.repaint();
      }
    
    });

    final JCheckBox jCDFCheckBox = new JCheckBox("Cumulative");
    jCDFCheckBox.setEnabled(false);
    jCDFCheckBox.setVisible(false);
    jCDFCheckBox.setSelected(true);
    
    jTopPanel.add(jCDFCheckBox);
    jCDFCheckBox.addActionListener(new ActionListener() {
    
      public void actionPerformed(ActionEvent e) {
        jDistribucionPanel.setCDF(jCDFCheckBox.getModel().isSelected());
        jDistribucionPanel.calcularDistribucion();
        habilitarAnalizadores();
        VisorDistribucionSequias.this.repaint();
      }
    
    });
  }

  private void crearComboBoxes(JPanel jTopPanel) {
    jEstacionComboBox = new JComboBox();
    jTopPanel.add(jEstacionComboBox);
    jEstacionComboBox.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        int nuevaEstacionIndex = jEstacionComboBox.getSelectedIndex();
        Estacion estacionActual = estaciones.get(nuevaEstacionIndex);
        jDistribucionPanel.setEstacion(estacionActual);
        jDistribucionPanel.calcularDistribucion();
        habilitarAnalizadores();
        VisorDistribucionSequias.this.repaint();
      }

    });
    
    jDistribucionComboBox = new JComboBox();
    jDistribucionComboBox.setEnabled(false);
    jDistribucionComboBox.setVisible(false);

    jTopPanel.add(jDistribucionComboBox);
    this.fillDistribucionComboBox();
    jDistribucionComboBox.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        String nombreNuevaDist = jDistribucionComboBox.getSelectedItem().toString();
        Class<? extends ContinuousDistribution> nuevaDist = distribuciones.get(nombreNuevaDist);
        jDistribucionPanel.setClaseDistribucion(nuevaDist);
        jDistribucionPanel.calcularDistribucion();
        habilitarAnalizadores();
        VisorDistribucionSequias.this.repaint();
      }

    });
  }

  private void establecerTitulo() {
    this.setTitle("Droughts Viewer");
  }
  
  private void fillEstacionComboBox() {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    
    List<Estacion> listaEstaciones;
    if (this.datasetId == null) {
      listaEstaciones = DAOFactory.getEstacionDAO(sess).findAllForBDR();
    } else {
      listaEstaciones = DAOFactory.getEstacionDAO(sess).findAllByDataset(this.datasetId); 
    }
    DefaultComboBoxModel jEstacionComboBoxModel = new DefaultComboBoxModel();
    
    this.estaciones = new ArrayList<Estacion>(listaEstaciones.size());
    for (Estacion estacion : listaEstaciones) {
      jEstacionComboBoxModel.addElement(estacion.getCodigoOMM() + ": " + estacion.getNombre());
      sess.evict(estacion);
      this.estaciones.add(estacion);
    }
    jEstacionComboBox.setModel(jEstacionComboBoxModel);
    sess.getTransaction().commit();
    int indexEstacionActual = jEstacionComboBox.getSelectedIndex();
    Estacion estacionActual = this.estaciones.get(indexEstacionActual);
    jDistribucionPanel.setEstacion(estacionActual);
    jDistribucionPanel.calcularDistribucion();
  }

  private void fillDistribucionComboBox() {
    this.distribuciones = new HashMap<String, Class<? extends ContinuousDistribution> >();
    this.distribuciones.put("Normal", NormalDist.class);
    this.distribuciones.put("Gamma", GammaDist.class);
    this.distribuciones.put("Weibull", WeibullDist.class);
    this.distribuciones.put("Empirical", PiecewiseLinearEmpiricalDist.class);
    this.distribuciones.put("Chi Square", ChiSquareDist.class);
//    this.distribuciones.put("Beta", BetaDist.class);
//    this.distribuciones.put("Logistic", LogisticDist.class);
//    this.distribuciones.put("Pearson 6", Pearson6Dist.class);
//    this.distribuciones.put("Cauchy", CauchyDist.class);
    DefaultComboBoxModel jDistribucionComboBoxModel = new DefaultComboBoxModel();
    for (String nombreDistribucion : this.distribuciones.keySet()) {
      jDistribucionComboBoxModel.addElement(nombreDistribucion);
    }
    jDistribucionComboBoxModel.setSelectedItem("Empirical");
    jDistribucionComboBox.setModel(jDistribucionComboBoxModel);
  }

}
