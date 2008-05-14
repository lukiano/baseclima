package ar.uba.dcao.dbclima.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import ar.uba.dcao.dbclima.concurrencia.ListUsersRequest;
import ar.uba.dcao.dbclima.concurrencia.RunnableWorker;

/**
 * Cuadro de dialogo para importar un nuevo Dataset, ya sea como prueba o para referencia.
 *
 */
@SuppressWarnings("serial")
public class CreateDatasetDialog extends JDialog {
  
  private RunnableWorker worker;
  
  private String usuario;
  
  private String fuente;
  
  private File[] listaArchivos = new File[0];
  
  private int resultadoOperacion = JOptionPane.CANCEL_OPTION;
  
  private DefaultListModel fileListModel = new DefaultListModel();
  
  private DefaultComboBoxModel userNameComboBoxModel = new DefaultComboBoxModel();
  
  private ActionListener filesButtonListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      JFileChooser fc = new JFileChooser();
      fc.setMultiSelectionEnabled(true);
      if (fc.showOpenDialog(CreateDatasetDialog.this) == JFileChooser.APPROVE_OPTION) {
        listaArchivos = fc.getSelectedFiles();
        updateFileList();
      }
    }
  };

  private ActionListener importButtonListener = new ActionListener() {
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

  private ActionListener userNameComboBoxListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      usuario = (String)userNameComboBoxModel.getSelectedItem();
    }
  };

  private DocumentListener sourceTextFieldListener = new DocumentListener() {
  
    public void removeUpdate(DocumentEvent e) {
      try {
        fuente = e.getDocument().getText(0, e.getDocument().getLength());
      } catch (BadLocationException ignored) {
      }
      
    }
  
    public void insertUpdate(DocumentEvent e) {
      try {
        fuente = e.getDocument().getText(0, e.getDocument().getLength());
      } catch (BadLocationException ignored) {
      }
      
    }
  
    public void changedUpdate(DocumentEvent e) {
      try {
        fuente = e.getDocument().getText(0, e.getDocument().getLength());
      } catch (BadLocationException ignored) {
      }
    }
  
  };
  
  private final class FilesListKeyListener implements KeyListener {
    private final JList filesList;

    private FilesListKeyListener(JList filesList) {
      this.filesList = filesList;
    }

    public void keyTyped(KeyEvent ignored) {}

    public void keyReleased(KeyEvent ignored) {}

    public void keyPressed(KeyEvent kEvent) {
      if (kEvent.getKeyCode() == KeyEvent.VK_DELETE && filesList.getSelectedIndices() != null && filesList.getSelectedIndices().length > 0) {
        int[] selectedIndexes = filesList.getSelectedIndices().clone();
        java.util.Arrays.sort(selectedIndexes);
        File[] nuevaListaArchivos = new File[listaArchivos.length - selectedIndexes.length];
        int j = 0;
        for (int i = 0; i < listaArchivos.length; i++) {
          if (java.util.Arrays.binarySearch(selectedIndexes, i) < 0) {
            nuevaListaArchivos[j] = listaArchivos[i];
            j++;
          }
        }
        listaArchivos = nuevaListaArchivos;
        updateFileList();
      }
    }
  }
  
  public CreateDatasetDialog(JFrame owner, RunnableWorker worker, boolean reference) {
    super(owner, "Import new " + (reference?"reference":"test") + " Dataset", true);
    this.worker = worker;
    this.pack();
    this.initGUI();
    this.validate();
  }
  
  private void initGUI() {
    JLabel userNameLabel = new JLabel("Enter User name:");
    JComboBox userNameComboBox = new JComboBox(userNameComboBoxModel);
    userNameComboBox.setEditable(true);
    userNameComboBox.addActionListener(userNameComboBoxListener);
    this.updateUserNameComboBox();
    JLabel sourceLabel = new JLabel("Data source description:");
    
    JTextField sourceTextField = new JTextField();
    if (this.fuente != null) {
      sourceTextField.setText(this.fuente);
    }
    sourceTextField.getDocument().addDocumentListener(sourceTextFieldListener);
    
    JLabel filesLabel = new JLabel("Files to process:");
    final JList filesList = new JList(fileListModel);
    JScrollPane filesScrollList = new JScrollPane(filesList);
    filesList.addKeyListener(new FilesListKeyListener(filesList));
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

    JButton selectFilesButton = new JButton("Select files...");
    selectFilesButton.addActionListener(filesButtonListener);
    JButton importButton = new JButton("Proceed");
    importButton.addActionListener(importButtonListener);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(cancelButtonListener);
    
    buttonPanel.add(selectFilesButton);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(importButton);
    buttonPanel.add(Box.createHorizontalStrut(15));
    buttonPanel.add(cancelButton);
    
    this.getContentPane().add(userNameLabel);
    this.getContentPane().add(userNameComboBox);
    this.getContentPane().add(sourceLabel);
    this.getContentPane().add(sourceTextField);
    this.getContentPane().add(filesLabel);
    this.getContentPane().add(filesScrollList);
    this.getContentPane().add(buttonPanel);
    
    GridBagLayout gridBagLayout = new GridBagLayout();
    this.getContentPane().setLayout(gridBagLayout);
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0,10,0,10); //top,left,bottom,right
    c.gridx = 0;
    c.gridy = 0;
    gridBagLayout.setConstraints(userNameLabel, c);
    c.insets.bottom = 10;
    c.gridy = 1;
    gridBagLayout.setConstraints(userNameComboBox, c);
    c.insets.bottom = 0;
    c.gridy = 2;
    gridBagLayout.setConstraints(sourceLabel, c);
    c.insets.bottom = 10;
    c.gridy = 3;
    gridBagLayout.setConstraints(sourceTextField, c);
    c.insets.bottom = 0;
    c.gridy = 4;
    gridBagLayout.setConstraints(filesLabel, c);
    c.insets.bottom = 10;
    c.gridy = 5;
    gridBagLayout.setConstraints(filesScrollList, c);
    c.gridy = 6;
    gridBagLayout.setConstraints(buttonPanel, c);

    this.setSize(300,400);
    this.setResizable(false);
    this.pack();
    this.setDragDrop(filesList);
  }

  @SuppressWarnings("unchecked")
  private void updateUserNameComboBox() {
    this.userNameComboBoxModel.removeAllElements();
    ListUsersRequest req = new ListUsersRequest();
    final List<String> users = (List<String>) this.worker.executeSynchronicRequest(req);
    for (String userName : users) {
      this.userNameComboBoxModel.addElement(userName);
    }
  }

  private void updateFileList() {
    this.fileListModel.clear();
    for (File file : this.listaArchivos) {
        this.fileListModel.addElement(file);
    }
    //this.repaint();
  }
  
  public String getUsuario() {
    return usuario;
  }

  public String getFuente() {
    return fuente;
  }

  public File[] getListaArchivos() {
    return listaArchivos;
  }

  public int getResultadoOperacion() {
    return resultadoOperacion;
  }
  
  private boolean checkVariables() {
    if (this.usuario == null || this.usuario.length() == 0) {
      JOptionPane.showMessageDialog(this, "User Name field is empty!", "Cannot import dataset", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (this.fuente == null || this.fuente.length() == 0) {
      JOptionPane.showMessageDialog(this, "Source description field is empty!", "Cannot import dataset", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (this.listaArchivos == null || this.listaArchivos.length == 0) {
      JOptionPane.showMessageDialog(this, "File list is empty!", "Cannot import dataset", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  private void setDragDrop(JComponent component) {
    TransferHandler transferHandler = new TransferHandler("file") {
      
      @Override
      public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        for (DataFlavor flavor : transferFlavors) {
          if (DataFlavor.javaFileListFlavor.equals(flavor)) {
            return true;
          }
        }
        return false;
      }
      
      @SuppressWarnings("unchecked")
      @Override
      public boolean importData(JComponent comp, Transferable t) {
        try {
          List<File> filelist = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
          
          for (File file : filelist) {
            if (!file.isFile()) {
              return false; // solo se aceptan archivos
            }
          }
          
          Set<File> conjuntoArchivos = new HashSet<File>(filelist);
          
          for (File file : listaArchivos) {
            conjuntoArchivos.add(file);
          }
          
          listaArchivos = conjuntoArchivos.toArray(new File[conjuntoArchivos.size()]);
          updateFileList();
          return true;
        } catch (UnsupportedFlavorException e) {
          return false;
        } catch (IOException e) {
          return false;
        }
      }
      
    };
    component.setTransferHandler(transferHandler);
  }

}
