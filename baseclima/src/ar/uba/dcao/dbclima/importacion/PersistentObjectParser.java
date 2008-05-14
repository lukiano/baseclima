package ar.uba.dcao.dbclima.importacion;

import java.util.Queue;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.PersistentObject;

/**
 * Parser for {@link PersistentObject}s which reads data from a String (line in an input
 * file) and puts its resulting objects in a store queue.
 */
public interface PersistentObjectParser {

  /**
   * Read input string, and add parsed object to the store queue.
   * 
   * @param input
   *            string recognizable by the parser, can contain zero or plus
   *            {@link PersistentObject}s
   * @param dataset
   *            dataset being imported from input.
   * @param storeQueue
   *            queue on which the parser must put parsed storable objects.
   */
  void parseRegistro(String input, Dataset dataset, Queue<PersistentObject> storeQueue) throws ParseException;

  /**
   * Binds the parser to the session. Might load data or whatever in order to synchronize
   * state. If the parser NEEDS to bind it can throw an exception when used unbinded.
   * 
   * @param ds
   *            Dataset been imported
   * @param sess
   *            session to bind to.
   */
  void init(Dataset ds, Session sess);

  void prepareForCommit();
}
