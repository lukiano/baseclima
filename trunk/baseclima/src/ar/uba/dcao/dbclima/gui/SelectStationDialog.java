package ar.uba.dcao.dbclima.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ar.uba.dcao.dbclima.concurrencia.ListBDRStationsRequest;
import ar.uba.dcao.dbclima.concurrencia.ListStationsByDatasetRequest;
import ar.uba.dcao.dbclima.concurrencia.RunnableWorker;
import ar.uba.dcao.dbclima.data.Estacion;

/**
 * Cuadro de dialogo para elegir las estaciones a ser procesadas cuando 
 * se desea ejecutar un chequeo de calidad o generar un reporte.
 *
 */
@SuppressWarnings("serial")
public class SelectStationDialog extends JDialog {

  private RunnableWorker worker;

  private Long datasetId;

  private boolean allStations = false;

  private List<Estacion> datasetStations;

  private int resultadoOperacion = JOptionPane.CANCEL_OPTION;

  private DefaultTableModel stationsTableModel;

  private JTable stationsTable;
  
  private JButton okButton;

  private ActionListener okButtonListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (checkVariables()) {
        resultadoOperacion = JOptionPane.OK_OPTION;
        dispose();
      }
    }
  };

  private ActionListener cancelButtonListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      resultadoOperacion = JOptionPane.CANCEL_OPTION;
      dispose();
    }
  };

  private ActionListener allStationsCheckBoxListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      allStations = ((JCheckBox)e.getSource()).isSelected();
    }
  };

  /**
   * @param owner
   * @param worker
   * @param datasetId
   *            el dataset para el cual se mostraran las estaciones, o NULL para que se
   *            muestren las estaciones de los dataset de referencia.
   */
  public SelectStationDialog(JFrame owner, RunnableWorker worker, Long datasetId) {
    super(owner, "Select stations to apply test", true);
    this.datasetId = datasetId;
    this.worker = worker;
    this.pack();
    this.initGUI();
    this.validate();
  }

  private void initGUI() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    this.okButton = new JButton("OK");
    this.okButton.addActionListener(this.okButtonListener);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this.cancelButtonListener);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(okButton);
    buttonPanel.add(Box.createHorizontalStrut(15));
    buttonPanel.add(cancelButton);

    String[] columnHeaders = new String[] { "Name", "OMN Code", "Country Code"};
    this.stationsTableModel = new DefaultTableModel(new Object[0][columnHeaders.length], columnHeaders) {

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:
          return String.class;
        case 1:
          return Integer.class;
        case 2:
          return Integer.class;
        default:
          return Object.class;
        }
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }

    };
    this.stationsTable = new JTable(stationsTableModel);

    
    JScrollPane stationsScrollList = new JScrollPane(this.stationsTable);
    this.updateStationsTable();
    JCheckBox allStationsCheckBox = new JCheckBox("Apply to all stations", false);
    allStationsCheckBox.addActionListener(this.allStationsCheckBoxListener);

    this.getContentPane().add(stationsScrollList);
    this.getContentPane().add(allStationsCheckBox);
    this.getContentPane().add(buttonPanel);

    GridBagLayout gridBagLayout = new GridBagLayout();
    this.getContentPane().setLayout(gridBagLayout);
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 10, 0, 10); // top,left,bottom,right
    c.gridx = 0;
    c.insets.top = 20;
    c.insets.bottom = 10;
    c.gridy = 0;
    gridBagLayout.setConstraints(stationsScrollList, c);
    c.insets.top = 0;
    c.gridy = 1;
    gridBagLayout.setConstraints(allStationsCheckBox, c);
    c.gridy = 2;
    gridBagLayout.setConstraints(buttonPanel, c);

    this.setSize(300, 400);
    this.setResizable(false);
    this.pack();
  }

  @SuppressWarnings("unchecked")
  private void updateStationsTable() {
    this.stationsTableModel.setRowCount(0);
    if (this.datasetId == null) {
      ListBDRStationsRequest req = new ListBDRStationsRequest();
      this.datasetStations = (List<Estacion>) this.worker.executeSynchronicRequest(req);
    } else {
      ListStationsByDatasetRequest req = new ListStationsByDatasetRequest(this.datasetId);
      this.datasetStations = (List<Estacion>) this.worker.executeSynchronicRequest(req);
    }
    
    this.stationsTableModel.setRowCount(this.datasetStations.size());
    for (int i = 0; i < this.datasetStations.size(); i++) {
      Estacion estacion = this.datasetStations.get(i);

      this.stationsTableModel.setValueAt(estacion.getNombre(), i, 0);
      this.stationsTableModel.setValueAt(estacion.getCodigoOMM(), i, 1);
      this.stationsTableModel.setValueAt(estacion.getCodigoPais(), i, 2);
    }
    if (this.datasetStations.size() == 0) {
      this.okButton.setEnabled(false);
    }
  }

  public int getResultadoOperacion() {
    return resultadoOperacion;
  }

  private boolean checkVariables() {
    if (this.stationsTable.getSelectedRows().length == 0 && !this.allStations) {
      JOptionPane.showMessageDialog(this, "No stations selected!", "Cannot execute task",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }
  
  public List<Estacion> getSelectedDatasetStations() {
    if (this.allStations) {
        return this.datasetStations;
    } else {
    int[] selectedRows = this.stationsTable.getSelectedRows();
    if (selectedRows.length == 0) {
      return java.util.Collections.emptyList();
    }
    List<Estacion> selectedDatasetStations = new ArrayList<Estacion>(selectedRows.length);
    for (int i = 0; i < selectedRows.length; i++) {
      selectedDatasetStations.add(this.datasetStations.get(selectedRows[i]));
    }
    return selectedDatasetStations; 
    }
  }

}
