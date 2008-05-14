package ar.uba.dcao.dbclima.concurrencia;

import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Tool used for performing thread-safe inter-thread communication.
 */
public class RunnableWorker implements Runnable {

  private Thread t;

  private SessionFactory sessionFactory = DBSessionFactory.getInstance();

  private Request currentRequest;

  private volatile boolean exitPending = false;

  /**
   * @param name
   *            A logical name for the mailbox
   * @param listener
   *            Reference to object that receives Message when dequeued
   */
  public RunnableWorker() {
    this.t = new Thread(this);
    this.t.setName("Worker Thread");
    this.t.start();
  }

  /**
   * Called by the client thread to do some work in the worker thread.
   * 
   * @param task
   *            Task to execute in WorkerThread
   */
  public void executeRequest(Request request) throws AlreadyExecutingException {
    if (this.currentRequest != null) {
      throw new AlreadyExecutingException("Already executing another request.");
    }
    this.currentRequest = request;

    synchronized (this) {
      this.notifyAll();
    }
  }

  public Object executeSynchronicRequest(Query query) {
    this.executeRequest(query);

    while (!query.isComplete() && t.isAlive()) {
      synchronized (this) {
        try {
          this.wait(500);
        } catch (InterruptedException ie) {
          System.out.println("GUI thread " + Thread.currentThread().getName() + " interrupted. Awaking.");
        }
      }
    }

    return query.getResult();
  }

  public void run() {
    synchronized (this) {
      while (!this.exitPending) {
        if (this.currentRequest == null) {
          try {
            this.wait(500);
          } catch (InterruptedException ie) {
            System.out.println("Thread " + Thread.currentThread().getName() + " interrupted. Awaking.");
          }
        } else {
          try {
            this.currentRequest.run(this.sessionFactory);
          } catch (Error e) {
            e.printStackTrace();
          } catch (RuntimeException e) {
            e.printStackTrace();
          } finally {
            this.currentRequest = null;
          }
        }
      }
    }

    DBSessionFactory.close();
    System.out.println("Worker thread terminating...");
  }

  public boolean isWorking() {
    return this.currentRequest != null;
  }

  /**
   * request current task (if any) and worker thread to quit job.
   */
  public boolean terminate() {
    this.exitPending = true;
    if (this.currentRequest != null) {
      //TODO: Handle active request.
    }

    return this.currentRequest != null;
  }
}
