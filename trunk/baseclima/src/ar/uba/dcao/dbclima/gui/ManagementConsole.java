package ar.uba.dcao.dbclima.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import ar.uba.dcao.dbclima.concurrencia.IsReferenceDatasetRequest;
import ar.uba.dcao.dbclima.concurrencia.ListDatasetsRequest;
import ar.uba.dcao.dbclima.concurrencia.MultiTask;
import ar.uba.dcao.dbclima.concurrencia.RunnableGUIUpdater;
import ar.uba.dcao.dbclima.concurrencia.RunnableWorker;
import ar.uba.dcao.dbclima.concurrencia.Task;
import ar.uba.dcao.dbclima.correlation.CorrelationDiscoverTask;
import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.gui.report.ReporteConfTempTask;
import ar.uba.dcao.dbclima.gui.viz.RepresentacionErroresXAnio;
import ar.uba.dcao.dbclima.gui.viz.VisualizacionMapa;
import ar.uba.dcao.dbclima.importacion.BCRegistroDiarioParser;
import ar.uba.dcao.dbclima.importacion.ConsistTask;
import ar.uba.dcao.dbclima.importacion.DeleteDatasetTask;
import ar.uba.dcao.dbclima.importacion.ExportBDRTask;
import ar.uba.dcao.dbclima.importacion.ImportTask;
import ar.uba.dcao.dbclima.importacion.RegistroSatelitalParser;
import ar.uba.dcao.dbclima.importacion.SatellitalConsistTask;
import ar.uba.dcao.dbclima.precipitacion.ClasificadorPrecipitacion;
import ar.uba.dcao.dbclima.precipitacion.ReportePrecipitacion;
import ar.uba.dcao.dbclima.precipitacion.VisorDistribucionPrecipitacionDelMes;
import ar.uba.dcao.dbclima.precipitacion.rango.ClearCacheTask;
import ar.uba.dcao.dbclima.precipitacion.sequia.ActualizarInfoSecuenciasSequias;
import ar.uba.dcao.dbclima.precipitacion.sequia.ClasificadorSequiasSPI;
import ar.uba.dcao.dbclima.precipitacion.sequia.ReporteSPI;
import ar.uba.dcao.dbclima.precipitacion.sequia.VisorDistribucionSequias;
import ar.uba.dcao.dbclima.qc.QualityCheck;
import ar.uba.dcao.dbclima.qc.TemperatureQualityCheckFactory;

/**
 * Ventana principal de la interfaz de usuario del programa. Aqui estan todos los menues y listas de
 * datasets para operar el sistema.
 */
@SuppressWarnings("serial")
public class ManagementConsole extends JFrame {

  private volatile JProgressBar progressBar;

  private volatile JLabel taskLabel;

  private JButton cancelButton;

  private static final int ERRORES_TOLERADOS = 0;

  private static final DateFormat DATE_FRMT = new SimpleDateFormat("MM/dd/yyyy HH:mm");

  private Long[] dataSetIDs;

  private JTable dataSetTable;

  public final RunnableWorker worker = new RunnableWorker();

  public static void main(String[] args) {
    System.setProperty("com.apple.macos.useScreenMenuBar", "true");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
    }
    new ManagementConsole().setVisible(true);
  }

  private WindowListener windowListener = new WindowAdapter() {
    @Override
    public void windowClosing(WindowEvent e) {
      ManagementConsole.this.doOnClose();
    }
  };

  public ManagementConsole() {
    super("Management Console");
    this.setIconImage(this.createImageIcon("icon.png", "icon").getImage());
    this.initGUI();
    this.pack();
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(this.windowListener);
    this.validate();

    Thread thread = new Thread() {

      public void run() {
        if (worker.isWorking()) {
          worker.terminate();
        }
      }

    };

    Runtime.getRuntime().addShutdownHook(thread);

  }

  /** Returns an ImageIcon, or null if the path was invalid. */
  protected ImageIcon createImageIcon(String path, String description) {
    java.net.URL imgURL = getClass().getClassLoader().getResource(path);
    if (imgURL != null) {
      return new ImageIcon(imgURL, description);
    } else {
      System.err.println("Couldn't find file: " + path);
      return null;
    }
  }

  private void initGUI() {
    try {
      JPanel datasetPanel = new JPanel();
      this.buildTable();
      datasetPanel.add(new JScrollPane(this.dataSetTable));
      getContentPane().add(datasetPanel, BorderLayout.CENTER);

      JPanel statusBar = new JPanel();
      statusBar.setLayout(new BorderLayout(10, 0));
      this.cancelButton = new JButton("Cancel Task");
      this.cancelButton.setEnabled(false);
      statusBar.add(this.cancelButton, BorderLayout.LINE_START);

      this.taskLabel = new JLabel("", JLabel.RIGHT);
      statusBar.add(taskLabel, BorderLayout.CENTER);

      progressBar = new JProgressBar();
      progressBar.setStringPainted(true);
      progressBar.setMaximum(100);
      progressBar.setValue(0);

      statusBar.add(progressBar, BorderLayout.LINE_END);

      getContentPane().add(statusBar, BorderLayout.PAGE_END);

      this.cancelButton.setVisible(false);
      this.taskLabel.setVisible(true);
      this.progressBar.setVisible(true);

      this.createMenuBar();
      this.setSize(800, 500);
      this.setPreferredSize(this.getSize());
      this.setResizable(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void createMenuBar() {
    JMenuBar jMenuBar = new JMenuBar();
    this.setJMenuBar(jMenuBar);

    JMenu importExportMenu = this.createImportExportMenu();
    jMenuBar.add(importExportMenu);

    JMenu qcMenu = this.createQCMenu();
    jMenuBar.add(qcMenu);

    JMenu reportMenu = this.createReportMenu();
    jMenuBar.add(reportMenu);
  }

  private JMenu createReportMenu() {
    JMenu reportMenu = new JMenu();
    reportMenu.setText("Reports");

    JMenu referenceReportMenu = this.createReferenceReportMenu();
    reportMenu.add(referenceReportMenu);

    JMenu testingReportMenu = this.createTestingReportMenu();
    reportMenu.add(testingReportMenu);
    return reportMenu;
  }

  private JMenu createTestingReportMenu() {
    JMenu testingReportMenu = new JMenu();
    testingReportMenu.setText("Testing Reports");

    JMenuItem mapaErrorXAnioVI = new JMenuItem("Show temp. error map (per year)");
    mapaErrorXAnioVI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Long dataSetID = getSelectedTestingDataset();
        if (dataSetID != null) {
          VisualizacionMapa inst = new VisualizacionMapa(RepresentacionErroresXAnio.class);
          inst.init(dataSetID);
          inst.setVisible(true);
        }
      }
    });
    testingReportMenu.add(mapaErrorXAnioVI);

    JMenuItem reporteTemp = new JMenuItem("Build temp. problem report");
    reporteTemp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Long dataSetID = getSelectedTestingDataset();
        if (dataSetID != null) {
          ManagementConsole.this.executeTask(new ReporteConfTempTask("reporteTemp.csv", dataSetID));
        }
      }
    });

    testingReportMenu.add(reporteTemp);

    JMenuItem testingPrecipitationViewerItem = new JMenuItem("Show monthly precipitation viewer");
    testingReportMenu.add(testingPrecipitationViewerItem);
    testingPrecipitationViewerItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Long dataSetID = getSelectedTestingDataset();
        if (dataSetID == null) {
          return;
        }
        VisorDistribucionPrecipitacionDelMes inst = new VisorDistribucionPrecipitacionDelMes();
        inst.setDatasetId(dataSetID);
        inst.setVisible(true);

      }
    });
    JMenuItem testingDroughtViewerItem = new JMenuItem("Show drought viewer");
    testingDroughtViewerItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Long dataSetID = getSelectedTestingDataset();
        if (dataSetID == null) {
          return;
        }
        VisorDistribucionSequias inst = new VisorDistribucionSequias();
        inst.setDatasetId(dataSetID);
        inst.setVisible(true);

      }
    });
    testingReportMenu.add(testingDroughtViewerItem);
    return testingReportMenu;
  }

  private JMenu createReferenceReportMenu() {
    JMenu referenceReportMenu = new JMenu();
    referenceReportMenu.setText("Reference Reports");

    JMenuItem mapaErrorXAnioVI = new JMenuItem("Show temp. error map (per year)");
    mapaErrorXAnioVI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VisualizacionMapa inst = new VisualizacionMapa(RepresentacionErroresXAnio.class);
        inst.init(null);
        inst.setVisible(true);
      }
    });
    referenceReportMenu.add(mapaErrorXAnioVI);

    JMenuItem reporteTemp = new JMenuItem("Build temp. problem report");
    reporteTemp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ManagementConsole.this.executeTask(new ReporteConfTempTask("reporteTemp.csv", null));
      }
    });

    referenceReportMenu.add(reporteTemp);

    JMenuItem referencePrecipitationViewerItem = new JMenuItem("Show monthly precipitation viewer");
    referencePrecipitationViewerItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VisorDistribucionPrecipitacionDelMes inst = new VisorDistribucionPrecipitacionDelMes();
        inst.setVisible(true);
      }
    });
    referenceReportMenu.add(referencePrecipitationViewerItem);

    JMenuItem referenceDroughtViewerItem = new JMenuItem("Show drought viewer");
    referenceDroughtViewerItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VisorDistribucionSequias inst = new VisorDistribucionSequias();
        inst.setDatasetId(null);
        inst.setVisible(true);
      }
    });
    referenceReportMenu.add(referenceDroughtViewerItem);
    JMenuItem referenceDroughtReportItem = new JMenuItem("Build drought report");
    referenceDroughtReportItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File fileToWrite = fileToWrite();
        if (fileToWrite != null) {
          selectStationDialog(new ReporteSPI(fileToWrite), true);
        }
      }
    });
    referenceReportMenu.add(referenceDroughtReportItem);
    JMenuItem referenceDroughtReportWithDaysItem = new JMenuItem("Build drought report (counting drought days)");
    referenceDroughtReportWithDaysItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File fileToWrite = fileToWrite();
        if (fileToWrite != null) {
          selectStationDialog(new ReporteSPI(fileToWrite, true), true);
        }
      }
    });
    referenceReportMenu.add(referenceDroughtReportWithDaysItem);
    JMenuItem referencePrecipitationReportItem = new JMenuItem("Build extreme precipitation report");
    referencePrecipitationReportItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File fileToWrite = fileToWrite();
        if (fileToWrite != null) {
          selectStationDialog(new ReportePrecipitacion(fileToWrite), true);
        }
      }
    });
    referenceReportMenu.add(referencePrecipitationReportItem);
    return referenceReportMenu;
  }

  private JMenu createQCMenu() {
    JMenu qcMenu = new JMenu();
    qcMenu.setText("Quality checks");

    JMenu referenceQCMenu = this.createReferenceQCMenu();
    qcMenu.add(referenceQCMenu);

    JMenu testingQCMenu = this.createTestingQCMenu();
    qcMenu.add(testingQCMenu);

    return qcMenu;
  }

  private JMenu createTestingQCMenu() {
    JMenu testingQCMenu = new JMenu();
    testingQCMenu.setText("Testing QCs");

    JMenuItem qc2Item = new JMenuItem("Run temperature QC on selected dataset");
    testingQCMenu.add(qc2Item);
    qc2Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Long dataSetID = ManagementConsole.this.getSelectedTestingDataset();
        if (dataSetID == null) {
          return;
        }

        Task qc = TemperatureQualityCheckFactory.buildQCTaskForDataset(dataSetID);
        ManagementConsole.this.executeTask(qc);
      }
    });

    JMenuItem qcPrecipSelItem = new JMenuItem("Run precipitation QC on selected dataset");
    testingQCMenu.add(qcPrecipSelItem);
    qcPrecipSelItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectStationDialog(new ClasificadorPrecipitacion(), false);
      }
    });
    JMenuItem qcDroughtSelItem = new JMenuItem("Run drought QC on selected dataset");
    testingQCMenu.add(qcDroughtSelItem);
    qcDroughtSelItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectStationDialog(new ClasificadorSequiasSPI(), false);
      }
    });
    return testingQCMenu;
  }

  private JMenu createReferenceQCMenu() {
    JMenu referenceQCMenu = new JMenu();
    referenceQCMenu.setText("Reference QCs");

    JMenuItem qc1Item = new JMenuItem("Run temperature QC on reference datasets");
    referenceQCMenu.add(qc1Item);
    qc1Item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Task qc = TemperatureQualityCheckFactory.buildQCTaskForDataset(null);
        ManagementConsole.this.executeTask(qc);
      }
    });

    JMenuItem qcPrecipRefItem = new JMenuItem("Run precipitation QC on reference datasets");
    referenceQCMenu.add(qcPrecipRefItem);
    qcPrecipRefItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectStationDialog(new ClasificadorPrecipitacion(), true);
      }
    });
    JMenuItem qcDroughtRefItem = new JMenuItem("Run drought QC on reference datasets");
    referenceQCMenu.add(qcDroughtRefItem);
    qcDroughtRefItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectStationDialog(new ClasificadorSequiasSPI(), true);
      }
    });
    return referenceQCMenu;
  }

  private JMenu createImportExportMenu() {
    JMenu importExportMenu = new JMenu();
    importExportMenu.setText("Import/Export");

    this.addImportItems(importExportMenu);
    importExportMenu.addSeparator();

    JMenuItem exportDatasetItem = new JMenuItem("Export reference datasets");
    exportDatasetItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeExportDatasetDialog();
      }

    });
    importExportMenu.add(exportDatasetItem);
    JMenuItem deleteDatasetItem = new JMenuItem("Delete selected dataset");
    deleteDatasetItem.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Long dataSetID = getSelectedDataset();
        if (dataSetID == null) {
          return;
        }
        Task deleteTask = new DeleteDatasetTask(dataSetID);
        Task task = new MultiTask(deleteTask, new ClearCacheTask()) {
            @Override
            public void updateGUIWhenCompleteSuccessfully() {
              ManagementConsole.this.updateDatasetList();
            }
        };
        ManagementConsole.this.executeTask(task);
      }

    });
    importExportMenu.add(deleteDatasetItem);
    return importExportMenu;
  }

  private void addImportItems(JMenu importExportMenu) {
    JMenu importMenu = new JMenu("Import");
    JMenuItem newReferenceDatasetItem = new JMenuItem("Import new reference dataset");
    newReferenceDatasetItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeImportDatasetDialog(true);
      }
    });
    importMenu.add(newReferenceDatasetItem);

    JMenuItem newTestDatasetItem = new JMenuItem("Import new testing dataset");
    newTestDatasetItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeImportDatasetDialog(false);
      }
    });
    importMenu.add(newTestDatasetItem);

    JMenuItem newSatellitalDatasetItem = new JMenuItem("Import new satellital dataset");
    newSatellitalDatasetItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        executeImportSatellitalDatasetDialog();
      }
    });
    importMenu.add(newSatellitalDatasetItem);

    importExportMenu.add(importMenu);
  }

  public void executeTask(Task task) {
    new RunnableGUIUpdater(this, task);
  }

  public JProgressBar getProgressBar() {
    return progressBar;
  }

  public JLabel getTaskLabel() {
    return taskLabel;
  }

  public JButton getCancelButton() {
    return cancelButton;
  }

  @Override
  public void dispose() {
    boolean closeAnyway = true;
    if (this.worker.isWorking()) {
      String wrn = "The application will quit when the current task is finished. Do you want to close the window anyway?";
      closeAnyway = JOptionPane.showConfirmDialog(this, wrn, "Closing GUI...", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    if (closeAnyway) {
      this.worker.terminate();
      super.dispose();
      System.out.println("GUI Thread terminating");
    }
  }

  private void doOnClose() {
    this.dispose();
    // boolean closeAnyway = true;
    // if (this.worker.isWorking()) {
    // String wrn = "The application will quit when the current task is finished. Do you
    // want to close the window anyway?";
    // closeAnyway = JOptionPane.showConfirmDialog(this, wrn, "Closing GUI...",
    // JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    // }
    //
    // if (closeAnyway) {
    // this.worker.terminate();
    // this.dispose();
    // System.out.println("GUI Thread terminating");
    // }
  }

  private void executeExportDatasetDialog() {
    File fileToWrite = this.fileToWrite();

    if (fileToWrite != null) {
      Task task = new ExportBDRTask(fileToWrite);
      this.executeTask(task);
    }
  }

  private File fileToWrite() {
    File fileToWrite = null;
    boolean finished = false;

    while (!finished) {
      JFileChooser fc;
      if (fileToWrite == null) {
        fc = new JFileChooser();
      } else {
        fc = new JFileChooser(fileToWrite.getParentFile());
      }
      if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        fileToWrite = fc.getSelectedFile();
        if (fileToWrite.exists()) {
          int result = JOptionPane.showConfirmDialog(ManagementConsole.this,
              "Do you want to overwrite the existing file?", "File already exists",
              JOptionPane.YES_NO_CANCEL_OPTION);
          if (result == JOptionPane.YES_OPTION) {
            finished = true;
          } else if (result == JOptionPane.CANCEL_OPTION) {
            fileToWrite = null;
            finished = true;
          }
        } else {
          finished = true;
        }
      } else {
        fileToWrite = null;
        finished = true;
      }
    }
    return fileToWrite;
  }

  private void executeImportDatasetDialog(boolean reference) {
    CreateDatasetDialog dlog = new CreateDatasetDialog(ManagementConsole.this, ManagementConsole.this.worker,
        reference);
    dlog.setVisible(true);
    if (dlog.getResultadoOperacion() == JOptionPane.OK_OPTION) {
      ImportTask importTask = new ImportTask(new BCRegistroDiarioParser(), dlog.getListaArchivos(), dlog
          .getUsuario(), dlog.getFuente(), new Date(), ERRORES_TOLERADOS, reference);
      ConsistTask consistencia = new ConsistTask();
      if (!importTask.getDataset().isReferente()) {
        // si queda en null, se procesan todas las estaciones de BDR, si se asigna un
        // dataset, se procesan solo las del dataset
        consistencia.setDatasetToProcess(importTask.getDataset());
      }
      CorrelationDiscoverTask corr = new CorrelationDiscoverTask(importTask.getDataset(),
          ProyectorRegistro.PROY_TMIN, ProyectorRegistro.PROY_TMAX);
      ActualizarInfoSecuenciasSequias sequiaTask = new ActualizarInfoSecuenciasSequias(false);
      if (!importTask.getDataset().isReferente()) {
        // si queda en null, se procesan todas las estaciones de BDR, si se asigna un
        // dataset, se procesan solo las del dataset
        sequiaTask.setDatasetToProcess(importTask.getDataset());
      }
      ClearCacheTask clearCacheTask = new ClearCacheTask();
      Task importConsistTask = new MultiTask(importTask, consistencia, corr, sequiaTask, clearCacheTask) {
        @Override
        public void updateGUIWhenCompleteSuccessfully() {
          ManagementConsole.this.updateDatasetList();
        }
      };
      this.executeTask(importConsistTask);
    }
  }

  private void executeImportSatellitalDatasetDialog() {
    CreateSatellitalDatasetDialog dlog = new CreateSatellitalDatasetDialog(ManagementConsole.this,
        ManagementConsole.this.worker);
    dlog.setVisible(true);
    if (dlog.getResultadoOperacion() == JOptionPane.OK_OPTION) {
      ImportTask importTask = new ImportTask(new RegistroSatelitalParser(), dlog.getListaArchivos(), dlog
          .getUsuario(), "Satellital Points", new Date(), ERRORES_TOLERADOS, false);

      SatellitalConsistTask consistencia = new SatellitalConsistTask();
      consistencia.setDataset(importTask.getDataset());

      Task importConsistTask = new MultiTask(importTask, consistencia) {
        @Override
        public void updateGUIWhenCompleteSuccessfully() {
          ManagementConsole.this.updateDatasetList();
        }
      };
      this.executeTask(importConsistTask);
    }
  }

  private Long getSelectedDataset() {
    int selRow = ManagementConsole.this.dataSetTable.getSelectedRow();
    if (selRow == -1) {
      // ninguna columna seleccionada
      JOptionPane.showMessageDialog(ManagementConsole.this, "No dataset selected.", "Cannot execute task",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }
    long dataSetID = ManagementConsole.this.dataSetIDs[selRow];
    return dataSetID;
  }

  private Long getSelectedTestingDataset() {
    int selRow = this.dataSetTable.getSelectedRow();
    if (selRow == -1) {
      // ninguna columna seleccionada
      JOptionPane
          .showMessageDialog(this, "No dataset selected.", "Cannot execute task", JOptionPane.ERROR_MESSAGE);
      return null;
    }
    long dataSetID = this.dataSetIDs[selRow];

    Boolean isReference = (Boolean) this.worker.executeSynchronicRequest(new IsReferenceDatasetRequest(dataSetID));

    if (isReference) {
      JOptionPane.showMessageDialog(this, "Sorry, this task is not to be executed on a reference dataset.",
          "Cannot execute task", JOptionPane.ERROR_MESSAGE);
      return null;
    }

    return dataSetID;
  }

  private void selectStationDialog(QualityCheck qualityCheck, boolean useReferenceDatasets) {
    SelectStationDialog dlog;

    if (useReferenceDatasets) {
      dlog = new SelectStationDialog(this, this.worker, null);
    } else {
      Long dataSetID = this.getSelectedTestingDataset();
      if (dataSetID == null) {
        return;
      }
      dlog = new SelectStationDialog(this, this.worker, dataSetID);
    }

    dlog.setVisible(true);
    if (dlog.getResultadoOperacion() == JOptionPane.OK_OPTION) {
      qualityCheck.setStationsToProcess(dlog.getSelectedDatasetStations());
      this.executeTask(qualityCheck);
    }
  }

  private void buildTable() {
    this.dataSetTable = new JTable();

    String[] columnHeaders = new String[] { "Author Name", "Source description", "Creation Time", "Reference" };

    TableModel dataModel = new DefaultTableModel(new Object[0][columnHeaders.length], columnHeaders) {

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
          return String.class;
        case 1:
          return String.class;
        case 2:
          return String.class;
        case 3:
          return Boolean.class;
        default:
          return Object.class;
        }
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }

    };

    this.dataSetTable.setModel(dataModel);
    this.dataSetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // dataSetTable.setFillsViewportHeight(true);
    this.updateDatasetList();
  }

  @SuppressWarnings("unchecked")
  private void updateDatasetList() {
    ListDatasetsRequest req = new ListDatasetsRequest();
    final List<Dataset> dss = (List<Dataset>) this.worker.executeSynchronicRequest(req);
    DefaultTableModel dataModel = (DefaultTableModel) this.dataSetTable.getModel();

    dataModel.setRowCount(dss.size());
    this.dataSetIDs = new Long[dss.size()];
    for (int i = 0; i < dss.size(); i++) {
      Dataset ds = dss.get(i);
      Date fechaCreacion = ds.getFechaCreacion();

      dataModel.setValueAt(ds.getUsuario(), i, 0);
      dataModel.setValueAt(ds.getFuente(), i, 1);
      dataModel.setValueAt(DATE_FRMT.format(fechaCreacion), i, 2);
      dataModel.setValueAt(ds.isReferente(), i, 3);

      this.dataSetIDs[i] = ds.getId();

    }
  }

}