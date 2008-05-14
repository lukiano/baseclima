package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
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

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Frame encargado de mostrar la distribucion empirica de precipitaciones para una sequia
 * calculando la precipitacion caida en el mismo periodo pero en el resto de los 
 * anios disponibles en la estacion.
 * @see DistribucionPrecipitacionPanel
 */
public class VisorDistribucionPrecipitacion extends javax.swing.JFrame {

  public static final double UMBRAL_DISTANCIA_FISICA = 1.5;

  public static final double UMBRAL_NULOS = 0.1;

  /**
   * 
   */
  private static final long serialVersionUID = 1090341612447240753L;

  protected static final String THRESHOLD_TITLE = "Enter new threshold";

  protected static final String NUMBER_OUTSIDE_RANGE = "The entered number is outside the valid range";

  protected static final String NUMBER_NOT_VALID = "The entered number is not valid";

  protected static final String ERROR_OCCURRED = "An error has occurred";

  protected DistribucionPrecipitacionPanel jDistribucionPanel;

  private JComboBox jEstacionComboBox;

  private JComboBox jSequiaComboBox;

  private Estacion estacionActual;

  private double umbralDistancia;

  private double umbralNulos;
  
  private List<Estacion> estaciones;
  
  private List<Sequia> sequias;

  /**
   * Auto-generated main method to display this JFrame
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignorada) {}
    
    VisorDistribucionPrecipitacion inst = new VisorDistribucionPrecipitacion();
    inst.setVisible(true);
    inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public VisorDistribucionPrecipitacion() {
    this(null, null);
  }

  public VisorDistribucionPrecipitacion(Estacion estacion, List<Sequia> sequias) {
    super();
    initGUI(estacion, sequias);
    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }

  private void initGUI(Estacion estacion, List<Sequia> sequias) {
    try {
      final int widthInicial = 1000;
      final int heightTopPanel = 50;
      final int heightDistPanel = 600;
      this.establecerTitulo();

      jDistribucionPanel = this.nuevoPanel();
      
      JScrollPane jScrollPanel = new JScrollPane(jDistribucionPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
          JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      jScrollPanel.setSize(getContentPane().getSize());

      JPanel jTopPanel = new JPanel();
      this.crearComboBoxes(estacion, sequias, jTopPanel);

      jTopPanel.setPreferredSize(new Dimension(widthInicial, heightTopPanel));
      jTopPanel.setMinimumSize(new Dimension(widthInicial, heightTopPanel));
      jTopPanel.setMaximumSize(new Dimension(widthInicial, heightTopPanel));
      jTopPanel.setSize(getContentPane().getSize());
      
      getContentPane().add(jTopPanel, BorderLayout.NORTH);
      getContentPane().add(jScrollPanel, BorderLayout.CENTER);

      setSize(widthInicial, heightTopPanel + heightDistPanel);
      this.crearMenues();

      this.inicializarPanel(widthInicial, heightDistPanel);

    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }

  protected DistribucionPrecipitacionPanel nuevoPanel() {
    return new DistribucionPrecipitacionPanel();
  }

  protected void inicializarPanel(final int widthInicial, final int heightDistPanel) {
    this.umbralDistancia = UMBRAL_DISTANCIA_FISICA;
    this.umbralNulos = UMBRAL_NULOS;
    jDistribucionPanel.setPreferredSize(new Dimension(widthInicial, heightDistPanel));
    jDistribucionPanel.setUmbralDistancia(umbralDistancia);
    jDistribucionPanel.setUmbralNulos(umbralNulos);
    jDistribucionPanel.calcularPrecipitacion();
    jDistribucionPanel.calcularDistribucion();
  }

  protected void establecerTitulo() {
    this.setTitle("Precipitation Viewer");
  }

  protected void crearMenues() {
    JMenuBar jMenuBar = new JMenuBar();
    setJMenuBar(jMenuBar);
    
    JMenu jUmbralMenu = new JMenu();
    jMenuBar.add(jUmbralMenu);
    jUmbralMenu.setText("Thresholds");
    this.llenarUmbralMenu(jUmbralMenu);
  }

  protected void llenarUmbralMenu(JMenu jUmbralMenu) {
    JMenuItem umbralDistanciaMenuItem = new JMenuItem();
    jUmbralMenu.add(umbralDistanciaMenuItem);
    umbralDistanciaMenuItem.setText("Set probability distance threshold...");
    umbralDistanciaMenuItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent event) {
        boolean umbralOK = false;
        while (!umbralOK) {
          String viejoUmbral = Double.toString(umbralDistancia);
          Object nuevoUmbral = JOptionPane.showInputDialog(VisorDistribucionPrecipitacion.this,
              THRESHOLD_TITLE + " (greater than 0.0)", "Change probability distance threshold", JOptionPane.INFORMATION_MESSAGE, null, null,
              viejoUmbral);
          if (nuevoUmbral == null) {
            // se cancelo la operacion
            umbralOK = true;
          } else {
            try {
              double nuevoValor = Double.parseDouble(nuevoUmbral.toString());
              if (nuevoValor > 0.0) {
                umbralDistancia = nuevoValor;
                umbralOK = true;
                jDistribucionPanel.setUmbralDistancia(umbralDistancia);
                VisorDistribucionPrecipitacion.this.repaint();
              } else {
                JOptionPane.showMessageDialog(VisorDistribucionPrecipitacion.this,
                    NUMBER_OUTSIDE_RANGE, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
              }
            } catch (NumberFormatException ex) {
              JOptionPane.showMessageDialog(VisorDistribucionPrecipitacion.this,
                  NUMBER_NOT_VALID, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }

    });

    JMenuItem umbralNulosMenuItem = new JMenuItem();
    jUmbralMenu.add(umbralNulosMenuItem);
    umbralNulosMenuItem.setText("Set null values threshold...");
    umbralNulosMenuItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent event) {
        boolean umbralOK = false;
        while (!umbralOK) {
          String viejoUmbral = Double.toString(umbralNulos);
          Object nuevoUmbral = JOptionPane.showInputDialog(VisorDistribucionPrecipitacion.this,
              THRESHOLD_TITLE + " (between 0.0 and 1.0)", "Change null values threshold", JOptionPane.INFORMATION_MESSAGE, null, null,
              viejoUmbral);
          if (nuevoUmbral == null) {
            // se cancelo la operacion
            umbralOK = true;
          } else {
            try {
              double nuevoValor = Double.parseDouble(nuevoUmbral.toString());
              if (nuevoValor >= -1.0 && nuevoValor <= 1.0) {
                umbralNulos = nuevoValor;
                umbralOK = true;
                jDistribucionPanel.setUmbralNulos(umbralNulos);
                jDistribucionPanel.calcularPrecipitacion();
                jDistribucionPanel.calcularDistribucion();
                VisorDistribucionPrecipitacion.this.repaint();
              } else {
                JOptionPane.showMessageDialog(VisorDistribucionPrecipitacion.this,
                    NUMBER_OUTSIDE_RANGE, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
              }
            } catch (NumberFormatException ex) {
              JOptionPane.showMessageDialog(VisorDistribucionPrecipitacion.this,
                  NUMBER_NOT_VALID, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }

    });
  }

  private void crearComboBoxes(Estacion estacion, List<Sequia> sequiasALlenar, JPanel jTopPanel) {
    if (sequiasALlenar == null) {
      jEstacionComboBox = new JComboBox();
      jTopPanel.add(jEstacionComboBox);
      this.fillEstacionComboBox();
      jEstacionComboBox.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          int nuevaEstacionIndex = jEstacionComboBox.getSelectedIndex();
          estacionActual = estaciones.get(nuevaEstacionIndex);
          jDistribucionPanel.setEstacion(estacionActual);
          fillSequiaComboBox();
          jDistribucionPanel.calcularPrecipitacion();
          jDistribucionPanel.calcularDistribucion();
          VisorDistribucionPrecipitacion.this.repaint();
        }

      });
    } else {
      jDistribucionPanel.setEstacion(estacion);
    }
    
    jSequiaComboBox = new JComboBox();
    jTopPanel.add(jSequiaComboBox);
    if (sequiasALlenar == null) {
      this.fillSequiaComboBox();
    } else {
      this.fillSequiaComboBox(sequiasALlenar);
    }
    jSequiaComboBox.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        int nuevaSequiaIndex = jSequiaComboBox.getSelectedIndex();
        Sequia sequia = sequias.get(nuevaSequiaIndex);
        jDistribucionPanel.setSequia(sequia);
        jDistribucionPanel.calcularPrecipitacion();
        jDistribucionPanel.calcularDistribucion();
        VisorDistribucionPrecipitacion.this.repaint();
      }

    });
    
  }

  private void fillEstacionComboBox() {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    List<Estacion> listaEstacion = DAOFactory.getEstacionDAO(sess).findAll();
    DefaultComboBoxModel jEstacionComboBoxModel = new DefaultComboBoxModel();
    this.estaciones = new ArrayList<Estacion>(listaEstacion.size());
    for (Estacion estacion : listaEstacion) {
      jEstacionComboBoxModel.addElement(estacion.getCodigoSMN() + ": " + estacion.getNombre());
      this.estaciones.add(estacion);
    }
    jEstacionComboBox.setModel(jEstacionComboBoxModel);
    sess.getTransaction().commit();
    int indexEstacionActual = jEstacionComboBox.getSelectedIndex();
    this.estacionActual = this.estaciones.get(indexEstacionActual);
    jDistribucionPanel.setEstacion(this.estacionActual);
  }

  private void fillSequiaComboBox(List<Sequia> sequias) {
    DefaultComboBoxModel jSequiaComboBoxModel = new DefaultComboBoxModel();
    this.sequias = new ArrayList<Sequia>(sequias);
    for (Sequia sequia : sequias) {
      String nombreSequia = sequia.getComienzo() + " - days:" + sequia.getLongitud();
      jSequiaComboBoxModel.addElement(nombreSequia);
    }
    jSequiaComboBox.setModel(jSequiaComboBoxModel);
    jSequiaComboBox.setSelectedIndex(0);
    
    int nuevaSequiaIndex = jSequiaComboBox.getSelectedIndex();
    Sequia sequia = sequias.get(nuevaSequiaIndex);
    jDistribucionPanel.setSequia(sequia);
  }
  
  private void fillSequiaComboBox() {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    List<Sequia> resultados = DAOFactory.getSequiaDAO(sess).findAllByStation(this.estacionActual);

    DefaultComboBoxModel jSequiaComboBoxModel = new DefaultComboBoxModel();
    this.sequias = new ArrayList<Sequia>(resultados.size()); 
    for (Sequia sequiaOriginal : resultados) {
      sess.evict(sequiaOriginal);
      this.sequias.add(sequiaOriginal);
    }
    sess.getTransaction().commit();
    
    jSequiaComboBox.setModel(jSequiaComboBoxModel);
    jSequiaComboBox.setSelectedIndex(0);

    int nuevaSequiaIndex = jSequiaComboBox.getSelectedIndex();
    Sequia sequia = sequias.get(nuevaSequiaIndex);
    jDistribucionPanel.setSequia(sequia);
  }

}
