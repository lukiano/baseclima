package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

/**
 * Este frame se encarga de mostrar las sequias tomando en cuenta registros
 * faltantes dentro de las mismas. Se calculan directamente a partir de los registros
 * de las estaciones.
 * @see DistribucionONOsPanel
 * @see ActualizarInfoSecuenciasSequiasONO
 */
public class VisorDistribucionONOs extends javax.swing.JFrame {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1737403110196320406L;

  private Component jScrollPanel;
  
  private DistribucionONOsPanel jDistribucionPanel;

  /**
   * Auto-generated main method to display this JFrame
   */
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(
        UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    VisorDistribucionONOs inst = new VisorDistribucionONOs();
    inst.setVisible(true);
    inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public VisorDistribucionONOs() {
    super();
    initGUI();
  }

  private void initGUI() {
    try {
      final int widthInicial = 1000;
      final int heightDistPanel = 600;
      
      this.establecerTitulo();

      jDistribucionPanel = new DistribucionONOsPanel();
      jDistribucionPanel.setPreferredSize(new Dimension(widthInicial, heightDistPanel));
      jScrollPanel = new JScrollPane(jDistribucionPanel);
      jScrollPanel.setSize(getContentPane().getSize());

      this.inicializarPanel();
      getContentPane().add(jScrollPanel, BorderLayout.CENTER);

      setSize(widthInicial, heightDistPanel);
      this.crearMenues();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void inicializarPanel() {
    jDistribucionPanel.calcular();
  }
  
  private void crearMenues() {
    JMenuBar jMenuBar = new JMenuBar();
    setJMenuBar(jMenuBar);
    
    JMenu jFileMenu = new JMenu();
    jMenuBar.add(jFileMenu);
    jFileMenu.setText("File");
    
    JMenuItem exitMenuItem = new JMenuItem();
    jFileMenu.add(exitMenuItem);
    exitMenuItem.setText("Exit");
    exitMenuItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        dispose();
        System.exit(0);
      }

    });
    
  }

  private void establecerTitulo() {
    this.setTitle("Drought & Missing records Viewer");
  }
  
}
