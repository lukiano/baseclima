package ar.uba.dcao.dbclima.precipitacion;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
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

import org.hibernate.Query;
import org.hibernate.Session;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.GammaDist;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.probdist.PiecewiseLinearEmpiricalDist;
import umontreal.iro.lecuyer.probdist.WeibullDist;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.precipitacion.rango.AcumPrecipitacionAnualProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.Rango;

/**
 * Muestra una ventana con las distribuciones de precipitacion enfocadas en una estacion y mes particulares.
 * Se pueden elegir varias distribuciones (empirica, gamma, weibull, chi-square, normal).
 * Se puede elegir si se muestra la funcion cumulativa o la real.
 * @see DistribucionPrecipitacionDelMesPanel
 */
public class VisorDistribucionPrecipitacionDelMes extends javax.swing.JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1090341612447240753L;

  protected static final String THRESHOLD_TITLE = "Enter new threshold";

  protected static final String NUMBER_OUTSIDE_RANGE = "The entered number is outside the valid range";

  protected static final String NUMBER_NOT_VALID = "The entered number is not valid";

  protected static final String ERROR_OCCURRED = "An error has occurred";

  protected DistribucionPrecipitacionDelMesPanel jDistribucionPanel;

  private JComboBox jEstacionComboBox;

  private JComboBox jMesComboBox;

  private JComboBox jDistribucionComboBox;

  private Long idEstacionActual;

  private Map<String, Class<? extends ContinuousDistribution>> distribuciones;

  private double umbralDistancia;

  private List<Long> estaciones;
  
  private List<Integer> meses;
  
  private Long datasetId;
  
  private static final Map<Integer, String> MESES;
  static {
    MESES = new HashMap<Integer, String>();
    MESES.put(1,"January");
    MESES.put(2,"February");
    MESES.put(3,"March");
    MESES.put(4,"April");
    MESES.put(5,"May");
    MESES.put(6,"June");
    MESES.put(7,"July");
    MESES.put(8,"August");
    MESES.put(9,"September");
    MESES.put(10,"October");
    MESES.put(11,"November");
    MESES.put(12,"December");

  }

  /**
   * Auto-generated main method to display this JFrame
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ignorada) {}
    
    VisorDistribucionPrecipitacionDelMes inst = new VisorDistribucionPrecipitacionDelMes();
    inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    inst.setDatasetId(null);
    inst.setVisible(true);
  }

  public VisorDistribucionPrecipitacionDelMes() {
    initGUI();
    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }

  private void initGUI() {
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
      this.crearComboBoxes(jTopPanel);
      this.crearCheckBoxes(jTopPanel);

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

  protected DistribucionPrecipitacionDelMesPanel nuevoPanel() {
    return new DistribucionPrecipitacionDelMesPanel();
  }

  protected void inicializarPanel(final int widthInicial, final int heightDistPanel) {
    this.umbralDistancia = 2.0;
    jDistribucionPanel.setPreferredSize(new Dimension(widthInicial, heightDistPanel));
    jDistribucionPanel.setClaseDistribucion(PiecewiseLinearEmpiricalDist.class);
    jDistribucionPanel.setCDF(true);
    jDistribucionPanel.setUmbralDistancia(umbralDistancia);
    
    this.fillEstacionComboBox();
    this.fillMesComboBox();

    jDistribucionPanel.calcularPrecipitacion();
    jDistribucionPanel.calcularDistribucion();
  }

  protected void establecerTitulo() {
    this.setTitle("Precipitation Viewer");
  }

  private void crearMenues() {
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
          Object nuevoUmbral = JOptionPane.showInputDialog(VisorDistribucionPrecipitacionDelMes.this,
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
                VisorDistribucionPrecipitacionDelMes.this.repaint();
              } else {
                JOptionPane.showMessageDialog(VisorDistribucionPrecipitacionDelMes.this,
                    NUMBER_OUTSIDE_RANGE, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
              }
            } catch (NumberFormatException ex) {
              JOptionPane.showMessageDialog(VisorDistribucionPrecipitacionDelMes.this,
                  NUMBER_NOT_VALID, ERROR_OCCURRED, JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }

    });

  }

  private void crearCheckBoxes(JPanel jTopPanel) {
    final JCheckBox jCDFCheckBox = new JCheckBox("Cumulative");
    jCDFCheckBox.setVisible(false);
    jCDFCheckBox.setSelected(true);
    jCDFCheckBox.setEnabled(false);
    jTopPanel.add(jCDFCheckBox);
    jCDFCheckBox.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        jDistribucionPanel.setCDF(jCDFCheckBox.getModel().isSelected());
        jDistribucionPanel.calcularDistribucion();
        VisorDistribucionPrecipitacionDelMes.this.repaint();
      }

    });
  }

  private void crearComboBoxes(JPanel jTopPanel) {
    jEstacionComboBox = new JComboBox();
    jTopPanel.add(jEstacionComboBox);
    jEstacionComboBox.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        int nuevaEstacionIndex = jEstacionComboBox.getSelectedIndex();
        idEstacionActual = estaciones.get(nuevaEstacionIndex);
        jDistribucionPanel.setEstacion(idEstacionActual);
        fillMesComboBox();
        jDistribucionPanel.calcularPrecipitacion();
        jDistribucionPanel.calcularDistribucion();
        VisorDistribucionPrecipitacionDelMes.this.repaint();
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
        VisorDistribucionPrecipitacionDelMes.this.repaint();
      }

    });

    jMesComboBox = new JComboBox();
    jTopPanel.add(jMesComboBox);
    jMesComboBox.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        int nuevoMesIndex = jMesComboBox.getSelectedIndex();
        jDistribucionPanel.setMes(meses.get(nuevoMesIndex));
        jDistribucionPanel.calcularPrecipitacion();
        jDistribucionPanel.calcularDistribucion();
        VisorDistribucionPrecipitacionDelMes.this.repaint();
      }

    });

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
    this.estaciones = new ArrayList<Long>(listaEstaciones.size());
    for (Estacion estacion : listaEstaciones) {
      jEstacionComboBoxModel.addElement(estacion.getCodigoSMN() + ": " + estacion.getNombre());
      this.estaciones.add(estacion.getId());
    }
    jEstacionComboBox.setModel(jEstacionComboBoxModel);
    sess.getTransaction().commit();
    int estacionActual = jEstacionComboBox.getSelectedIndex();
    this.idEstacionActual = this.estaciones.get(estacionActual);
    jDistribucionPanel.setEstacion(this.idEstacionActual);
  }

  public void setDatasetId(Long datasetId) {
    this.datasetId = datasetId;
    this.fillEstacionComboBox();
    this.fillMesComboBox();
  }

  @SuppressWarnings("unchecked")
  public boolean hayPrecipitacionesMayoresACumulDelMes(int mes) {

    int finDia = DistribucionPrecipitacionDelMesPanel.dameFinDia(mes);
    
    AcumPrecipitacionAnualProyectorRango proyectorRango = new AcumPrecipitacionAnualProyectorRango(1, mes, finDia, mes);

    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    Estacion estacion =  DAOFactory.getEstacionDAO(sess).findByID(this.idEstacionActual);
    sess.getTransaction().commit();
    proyectorRango.setIncluir0mm(false);
    List<Rango> rangos = proyectorRango.proyectarRangos(estacion);

    List<Double> valores = new ArrayList<Double>(); 
    for (Rango rango : rangos) {
      if (rango.valor() != null) {
        valores.add(rango.valor().doubleValue());
      }
    }
    Collections.sort(valores);
    if (valores.size() == 0) {
      return false;
    }
    double p50;
    if (valores.size() % 2 == 0) {
      p50 = (valores.get(valores.size() / 2) + valores.get((valores.size() / 2) - 1)) / 2;
    } else {
      p50 = valores.get((valores.size() / 2));
    }

    int percentil50 = (int)Math.floor(p50 * 10); // multiplicado por 10 porque en la base estan en decimas de milimetro
    sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    Query query = sess.createQuery("FROM RegistroDiario WHERE estacion.id = ? AND month(fecha) = ? AND precipitacion >= ?").
      setLong(0, this.idEstacionActual).setInteger(1, mes);
    List<RegistroDiario> registros = query.setInteger(2, percentil50).list();
    sess.getTransaction().commit();
    return (registros != null && !registros.isEmpty());

  }


  private void fillMesComboBox() {
    DefaultComboBoxModel jSequiaComboBoxModel = new DefaultComboBoxModel();
    this.meses = new ArrayList<Integer>(12);
    for (int i = 1; i <= 12; i++) {
      if (this.hayPrecipitacionesMayoresACumulDelMes(i)) {
        this.meses.add(i);
      }
    }
    if (this.meses.size() > 0) {
      for (Integer mes : this.meses) {
        jSequiaComboBoxModel.addElement(MESES.get(mes));
      }
      jMesComboBox.setModel(jSequiaComboBoxModel);
      jMesComboBox.setSelectedIndex(0);
      jMesComboBox.setVisible(true);

      int nuevoMesIndex = jMesComboBox.getSelectedIndex();
      jDistribucionPanel.setMes(this.meses.get(nuevoMesIndex));
      /*jDistribucionPanel.calcularPrecipitacion();
      jDistribucionPanel.calcularDistribucion();*/
    } else {
      jMesComboBox.setVisible(false);
      jDistribucionPanel.setMes(-1);
    }
  }

  private void fillDistribucionComboBox() {
    this.distribuciones = new HashMap<String, Class<? extends ContinuousDistribution> >();
    this.distribuciones.put("Normal", NormalDist.class);
    this.distribuciones.put("Gamma", GammaDist.class);
    this.distribuciones.put("Weibull", WeibullDist.class);
    this.distribuciones.put("Empirical", PiecewiseLinearEmpiricalDist.class);
    this.distribuciones.put("Chi Square", ChiSquareDist.class);
    // this.distribuciones.put("Beta", BetaDist.class);
    // this.distribuciones.put("Logistic", LogisticDist.class);
    // this.distribuciones.put("Pearson 6", Pearson6Dist.class);
    // this.distribuciones.put("Cauchy", CauchyDist.class);
    DefaultComboBoxModel jDistribucionComboBoxModel = new DefaultComboBoxModel();
    for (String nombreDistribucion : this.distribuciones.keySet()) {
      jDistribucionComboBoxModel.addElement(nombreDistribucion);
    }
    jDistribucionComboBoxModel.setSelectedItem("Empirical");
    jDistribucionComboBox.setModel(jDistribucionComboBoxModel);
  }

}
