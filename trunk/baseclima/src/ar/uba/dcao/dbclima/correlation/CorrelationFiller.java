package ar.uba.dcao.dbclima.correlation;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

public class CorrelationFiller {

  private static final int MILIS_POR_DIA = 3600 * 24000;

  private static final int BLOCK_SIZE = 13;

  private static final double MAX_DIST = 4500;

  private ProyectorRegistro proyector;

  private CatalogoCorrelacion catalogo;

  public CorrelationFiller(ProyectorRegistro proyector) {
    this.catalogo = new CatalogoCorrelacion(proyector.nombreVariable());
    this.proyector = proyector;
  }

  @SuppressWarnings("unchecked")
  public void fillDB() {

    boolean iterate = true;
    while (iterate) {
      Session sess = DBSessionFactory.getInstance().getCurrentSession();
      sess.beginTransaction();

      List<Estacion> ests = sess.createQuery("FROM Estacion e WHERE e.longitud != NULL AND e.latitud != NULL").list();

      this.catalogo.initialize(sess);

      boolean iterationMeaningfull = searchCorr(ests);
      iterate = iterate && iterationMeaningfull;

      this.catalogo.saveTo(sess);

      sess.getTransaction().commit();
    }
  }

  /**
   * Selecciona el primer par no cotejado de grupos de estaciones y los
   * coteja. Los grupos de estaciones se definen con tamanio BLOCK_SIZE.
   * Cotejar dos grupos le llamo a comparar todos los pares de elementos,
   * En este caso, buscar la correlacion entre ellos.
   * 
   * @param ests
   *          Lista de estaciones a correlacionar
   * @return
   */
  private boolean searchCorr(List<Estacion> ests) {
    boolean doIterate = true;

    int count = 0;
    for (int iBlock = 0; iBlock < ests.size() && doIterate; iBlock += BLOCK_SIZE) {
      for (int jBlock = iBlock; jBlock < ests.size() && doIterate; jBlock += BLOCK_SIZE) {
        count++;

        List<Estacion> le1 = ests.subList(iBlock, Math.min(iBlock + BLOCK_SIZE, ests.size()));
        List<Estacion> le2 = ests.subList(jBlock, Math.min(jBlock + BLOCK_SIZE, ests.size()));

        doIterate = !this.listCorrFinder(le1, le2);

        if (!doIterate) {
          /* INFO DEBUG/AVANCE */
          int numBloques = (int) Math.ceil((double) ests.size() / BLOCK_SIZE);
          int numSectores = numBloques * (numBloques + 1) / 2;
          System.out.println("Sector " + count + "/" + numSectores + " escaneado");
        }
      }
    }

    /*
     * El metodo finaliza tras el primer bloque de estaciones para el cual
     * se busco correlacion. Al volver se indica si tiene sentido seguir
     * analizando bloques.
     */
    return !doIterate;
  }

  private boolean listCorrFinder(List<Estacion> le1, List<Estacion> le2) {
    boolean anyNewResult = false;
    for (int i = 0; i < le1.size(); i++) {
      for (int j = 0; j < le2.size(); j++) {
        Estacion e1 = le1.get(i);
        Estacion e2 = le2.get(j);

        double dist = Math.pow((e1.getLatitud() - e2.getLatitud()), 2) + Math.pow((e1.getLongitud() - e2.getLongitud()), 2);
        dist = Math.sqrt(dist);

        /*
         * Se evaluan las precondiciones 'instantaneas' de la evaluacion de
         * precondicion
         */
        if (dist < MAX_DIST && e1.getId() < e2.getId()) {
          /* Si se cumplen, se evaluan las precondiciones mas caras. */
          List<CorrelacionEstaciones> correlations = this.catalogo.getCorrelaciones(e1, e2);
          Date e1i = e1.getFechaInicio();
          Date e2i = e2.getFechaInicio();
          Date e1f = e1.getFechaFin();
          Date e2f = e2.getFechaFin();

          Date startOverlap = e1i.after(e2i) ? e1i : e2i;
          Date endOverlap = e1f.before(e2f) ? e1f : e2f;

          long daysOverlap = (endOverlap.getTime() - startOverlap.getTime()) / MILIS_POR_DIA;

          if (correlations.size() == 0 && daysOverlap >= CalculadorCorrelacion.MIN_LEN_BASE_CORR) {
            anyNewResult = true;

            List<CorrelacionEstaciones> corrs = CalculadorCorrelacion.getCorr(e1, e2, this.proyector);
            this.catalogo.addCorrelaciones(corrs);
          }
        }
      }
    }

    return anyNewResult;
  }
}