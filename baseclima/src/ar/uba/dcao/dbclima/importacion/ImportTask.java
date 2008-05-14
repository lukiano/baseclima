package ar.uba.dcao.dbclima.importacion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.AbstractTask;
import ar.uba.dcao.dbclima.concurrencia.TaskResult;
import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.PersistentObject;
import ar.uba.dcao.dbclima.utils.InputLineReader;

/**
 * Tarea que importa informacion desde archivos de entrada. Las instancias particulares de
 * {@link PersistentObjectParser} asociadas definen que tipo de datos se podra importar.
 */
public class ImportTask extends AbstractTask {

  private static final int UNCOMMITED_OBJS = 5000;

  private final File[] files;

  private Dataset dataset;

  private int cantLineasImportadas;

  private int cantEntidadesPersistidas;

  private PersistentObjectParser parser;

  private Queue<PersistentObject> uncommited = new LinkedList<PersistentObject>();

  private int numberOfExceptionsAllowed;

  private List<ParseException> exceptions = new ArrayList<ParseException>();

  public ImportTask(PersistentObjectParser parser, File[] files, String usuario, String fuente, Date fecha, int numberOfExceptionsAllowed,
      boolean referente) {
    this.parser = parser;
    this.files = files;
    this.numberOfExceptionsAllowed = numberOfExceptionsAllowed;
    this.dataset = new Dataset(usuario, fuente, fecha);
    this.dataset.setReferente(referente);
    this.uncommited.add(this.dataset);
    this.setProgressDescription("Pre-procesando archivos de entrada");
  }

  public Dataset getDataset() {
    return dataset;
  }

  public boolean run(SessionFactory runningFactory) {
    Session sess = runningFactory.getCurrentSession();
    sess.beginTransaction();

    this.cantLineasImportadas = 0;
    int cantFiles = this.files.length;

    for (File f : this.files) {
      InputLineReader reader;
      try {
        reader = new InputLineReader(new FileReader(f));
      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException(e);
      }

      while (!reader.eof()) {
        reader.getLine();
        this.cantLineasImportadas++;
      }
      reader.closeInput();
    }

    for (int i = 0; i < cantFiles; i++) {
      String importDesc = "Importando desde archivo de entrada (" + (i + 1) + "/" + cantFiles + "). ";
      File f = this.files[i];
      InputLineReader reader;
      try {
        reader = new InputLineReader(new FileReader(f));
      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException(e);
      }

      importDataFromFile(runningFactory, reader, importDesc);
      if (this.exceptions.size() > this.numberOfExceptionsAllowed) {
        String errDesc = "Importacion termino con errores. Mostrando el primer error: "
            + this.exceptions.get(0).getMessage();
        this.setResult(TaskResult.buildUnsuccessfulResult(errDesc, this.exceptions.get(0)));
        return false;
      }
      reader.closeInput();
    }

    String resDesc = "Importacion finalizada. " + this.cantLineasImportadas + " registros importados.";
    this.setResult(TaskResult.buildSuccessfulResult(resDesc));
    this.setComplete(true);

    return true;
  }

  private void importDataFromFile(SessionFactory runningFactory, InputLineReader reader, String titlePrefix) {
    int importados = 0;

    Session sess = runningFactory.getCurrentSession();
    this.parser.init(this.dataset, sess);

    while (!reader.eof()) {
      importados++;
      String linea = reader.getLine();
      try {
        parser.parseRegistro(linea, this.dataset, this.uncommited);
      } catch (ParseException e) {
        e.setLineNumber(importados);
        e.setLine(linea);
        this.exceptions.add(e);
      }

      if (this.exceptions.size() > this.numberOfExceptionsAllowed) {
        this.undo(runningFactory);
        return;
      }

      this.setProgressDescription(titlePrefix + importados + " lineas.");
      this.setCompletionState(importados / (double) this.cantLineasImportadas);

      if (this.uncommited.size() / UNCOMMITED_OBJS > 0) {
        this.parser.prepareForCommit();

        this.clearCache(runningFactory);
      }
    }

    this.parser.prepareForCommit();
    this.clearCache(runningFactory);
    this.setProgressDescription(titlePrefix + importados + " registros");
    this.setCompletionState(1d);
    this.close(runningFactory);
  }

  private void close(SessionFactory runningFactory) {
    Session session = runningFactory.getCurrentSession();
    if (session.getTransaction() == null || !session.getTransaction().isActive()) {
      session.getTransaction().commit();
    }
  }

  private void undo(SessionFactory runningFactory) {
    this.setCompletionState(0d);
    this.setComplete(true);
    if (this.cantEntidadesPersistidas > 0) {
      new DeleteDatasetTask(this.dataset.getId()).run(runningFactory);
    }
  }

  private void clearCache(SessionFactory runningFactory) {
    Session session = runningFactory.getCurrentSession();
    if (session.getTransaction() == null || !session.getTransaction().isActive()) {
      session.beginTransaction();
    }

    int cantEntidadesAPersistir = this.uncommited.size();
    for (PersistentObject pe : this.uncommited) {
      session.save(pe);
    }

    this.cantEntidadesPersistidas += cantEntidadesAPersistir;

    session.getTransaction().commit();
    this.uncommited.clear();
    
    session = runningFactory.getCurrentSession();
    session.beginTransaction();
  }

  public void updateGUIWhenCompleteSuccessfully() {
  }
}
