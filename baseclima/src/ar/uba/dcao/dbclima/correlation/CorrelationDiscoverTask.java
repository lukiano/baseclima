package ar.uba.dcao.dbclima.correlation;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.AbstractTask;
import ar.uba.dcao.dbclima.concurrencia.TaskResult;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Dataset;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;

/**
 * 
 */
public class CorrelationDiscoverTask extends AbstractTask {

  private static final int MILIS_POR_DIA = 3600 * 24000;

  private static final int BLOCK_SIZE = 13;

  private static final double MAX_DIST = 450;

  private final ProyectorRegistro[] proyectores;

  private final CatalogoCorrelacion[] catalogos;

  private final Dataset ds;

  public CorrelationDiscoverTask(Dataset ds, ProyectorRegistro... proyectores) {
    this.ds = ds;
    this.setProgressDescription("Buscando pares de estaciones correlacionadas.");
    this.proyectores = proyectores;
    this.catalogos = new CatalogoCorrelacion[proyectores.length];
    for (int i = 0; i < proyectores.length; i++) {
      ProyectorRegistro proyector = proyectores[i];
      this.catalogos[i] = new CatalogoCorrelacion(proyector.nombreVariable());
    }
  }

  /**
   * Itera en los pares de grupos de estaciones y los coteja. Los grupos de estaciones se
   * definen con tamanio BLOCK_SIZE. Cotejar dos grupos se le llama a comparar todos los
   * pares de elementos, En este caso, buscar la correlacion entre ellos.
   * 
   * @param ests
   *            Lista de estaciones a correlacionar
   * @return
   */
  @SuppressWarnings("unchecked")
  public boolean run(SessionFactory sessFactory) {
    String descPref = "Buscando pares de estaciones correlacionadas.";
    this.setProgressDescription(descPref);
    Session sess = sessFactory.getCurrentSession();
    sess.beginTransaction();

    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);

    List<Long> idsDs = estacionDAO.findForCorrelationSearchInDataset(ds);
    List<Long> idsAg = estacionDAO.findForCorrelationSearchAgainstDataset(ds);

    for (CatalogoCorrelacion cat : this.catalogos) {
      cat.initialize(sess);
    }

    int iSector = 0;
    int numBloquesAg = (int) Math.ceil((double) idsAg.size() / BLOCK_SIZE);

    int numSectores = idsDs.size() * numBloquesAg;

    for (Long idEstacionDS : idsDs) {

      for (int iBlock = 0; iBlock < idsAg.size(); iBlock += BLOCK_SIZE) {
        iSector++;
        this.setProgressDescription(descPref + " Sector " + iSector + "/" + numSectores);

        sess = sessFactory.getCurrentSession();
        sess.beginTransaction();

        Estacion e = DAOFactory.getEstacionDAO(sess).findByID(idEstacionDS);
        List<Long> ids1 = idsAg.subList(iBlock, Math.min(iBlock + BLOCK_SIZE, idsAg.size()));

        String q = "FROM Estacion WHERE id IN (:_ids)";
        List<Estacion> le = sess.createQuery(q).setParameterList("_ids", ids1).list();

        this.listCorrFinder(e, le);

        for (CatalogoCorrelacion cat : this.catalogos) {
          cat.saveTo(sess);
        }

        sess.getTransaction().commit();
        this.setCompletionState(iSector / (double) numSectores);
      }
    }

    this.setCompletionState(1);
    this.setProgressDescription("Tarea completa. Correlaciones relevadas.");
    this.setComplete(true);
    this.setResult(TaskResult.buildSuccessfulResult(this.getProgressDescription()));
    return true;
  }

  private void listCorrFinder(Estacion e1, List<Estacion> le) {
    for (int j = 0; j < le.size(); j++) {
      Estacion e2 = le.get(j);

      double dist = Math.pow((e1.getLatitud() - e2.getLatitud()), 2)
          + Math.pow((e1.getLongitud() - e2.getLongitud()), 2);
      dist = Math.sqrt(dist);

      /*
       * Se evaluan las precondiciones 'instantaneas' de la evaluacion de precondicion
       */
      if (dist < MAX_DIST && e1.getId() != e2.getId()) {
        /* Si se cumplen, se evaluan las precondiciones mas caras. */
        Date e1i = e1.getFechaInicio();
        Date e2i = e2.getFechaInicio();
        Date e1f = e1.getFechaFin();
        Date e2f = e2.getFechaFin();

        Date startOverlap = e1i.after(e2i) ? e1i : e2i;
        Date endOverlap = e1f.before(e2f) ? e1f : e2f;

        long daysOverlap = (endOverlap.getTime() - startOverlap.getTime()) / MILIS_POR_DIA;

        if (daysOverlap >= CalculadorCorrelacion.MIN_LEN_BASE_CORR) {
          for (int iP = 0; iP < this.proyectores.length; iP++) {
            ProyectorRegistro proy = this.proyectores[iP];
            CatalogoCorrelacion cat = this.catalogos[iP];
            List<CorrelacionEstaciones> corrsNuevas = CalculadorCorrelacion.getCorr(e1, e2, proy);
            this.addCorrelacionesNuevas(corrsNuevas, cat);
          }
        }
      }
    }
  }

  private void addCorrelacionesNuevas(List<CorrelacionEstaciones> correlacionesNuevas, CatalogoCorrelacion catalogo) {
    for (CorrelacionEstaciones corr : correlacionesNuevas) {
      List<CorrelacionEstaciones> corrs = catalogo.getCorrelaciones(corr.getE1(), corr.getE2());
      if (!corrs.contains(corr)) {
        catalogo.addCorrelacion(corr);
      }
    }
  }

  public void updateGUIWhenCompleteSuccessfully() {
  }
}