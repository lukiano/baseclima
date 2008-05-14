package ar.uba.dcao.dbclima.casosDeUso.batchs;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.casosDeUso.browsers.Browser;
import ar.uba.dcao.dbclima.correlation.CatalogoCorrelacion;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.RegistroHelper;
import ar.uba.dcao.dbclima.data.ResultadoTestQC;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.qc.ResultadoVecindad;
import ar.uba.dcao.dbclima.qc.ResultadoVecindad.EstimacionVecino;
import ar.uba.dcao.dbclima.qc.qc1.AsignadorConfianza;
import ar.uba.dcao.dbclima.qc.qc1.DIPTest;
import ar.uba.dcao.dbclima.qc.qc1.OutlierTest;
import ar.uba.dcao.dbclima.qc.qc1.TestVecindad;
import ar.uba.dcao.dbclima.utils.TimeDisplayUtil;

public class ConfianzaUseCase implements Browser {

  private static DateFormat dfrmt = new SimpleDateFormat("MM/dd/yyyy");

  private static DecimalFormat frmt = new DecimalFormat("0.00");

  private static final int NUM_VECINOS = 10;

  private final ProyectorRegistro proy;

  /*
   * FIXME: Uso esto para visualizar algunas categorias especiales. Habria que borrarlo o
   * formalizarlo.
   */
  private static Set<String> catsDeInteres = new HashSet<String>();

  // static {
  // catsDeInteres.add("DAVEI0");
  // catsDeInteres.add("ECV1");
  // }

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("**** ASIGNACION DE CONFIANZA Y REPORTES ****");

    int actionKnown = 0;
    boolean calcularTx = false;
    boolean calcularTn = false;
    boolean recalcular = false;

    do {
      System.out.println("Variable de interes? (M)inima/ma(X)ima/(A)mbas/");
      String line = in.readLine();

      if (line.equals("M")) {
        actionKnown = 1;
        calcularTn = true;
      } else if (line.equals("X")) {
        actionKnown = 1;
        calcularTx = true;
      } else if (line.equals("A")) {
        actionKnown = 1;
        calcularTn = true;
        calcularTx = true;
      } else if (line.equals("N")) {
        actionKnown = 1;
      }
    } while (actionKnown == 0);

    do {
      System.out.println("Recalcular o solo reporte? (R)ecalcular/(S)olo reporte");
      String line = in.readLine();

      if (line.equals("R")) {
        actionKnown = 2;
        recalcular = true;
      } else if (line.equals("S")) {
        actionKnown = 2;
      }
    } while (actionKnown == 1);

    /* Se borran las confianzas viejas. */
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    if (calcularTn && recalcular) {
      sess.createQuery("UPDATE RegistroDiario r SET r.codigoConfianzaTempMin = null").executeUpdate();
    }
    if (calcularTx && recalcular) {
      sess.createQuery("UPDATE RegistroDiario r SET r.codigoConfianzaTempMax = null").executeUpdate();
    }
    sess.close();

    /* Asignacion de confianza. */
    if (calcularTn) {
      asignarConfianza(ProyectorRegistro.PROY_TMIN, recalcular);
    }
    if (calcularTx) {
      asignarConfianza(ProyectorRegistro.PROY_TMAX, recalcular);
    }

    DBSessionFactory.getInstance().close();
  }

  private static void asignarConfianza(ProyectorRegistro proy, boolean recalcular) throws IOException {
    Session session = DBSessionFactory.getInstance().getCurrentSession();
    session.beginTransaction();

    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(session);
    List<Long> ids = estacionDAO.findAllIDs();
    // List<Integer> ids = Collections.singletonList(30);
    session.clear();

    /* Variables para el reporte. */
    ConfianzaUseCase confianzaUseCase = new ConfianzaUseCase(proy);
    FileWriter fw = new FileWriter("Reporte_" + proy.nombreVariable() + ".csv");
    fw.append("Estacion,Posicion,Fecha,SecuMin,SecuMax,Outl,OutlRng,DIP,");
    fw.append("Confianza,CodigoConf,ConfianzaRange,Prediccion,DesvPrediccion,AngCobertura,#Vecinos,");

    for (int i = 1; i <= 10; i++) {
      fw.append("Est" + i + ",Ang#" + i + ",Pred#" + i + ",");
    }
    fw.append("\n");

    /* Variables para el informe de avance. */
    long start = new Date().getTime();
    int estI = 1;
    System.out.println("Asignando confianza a variable " + proy.nombreVariable());

    AsignadorConfianza asignadorConf = new AsignadorConfianza(proy);
    AsignadorConfianza asignadorConfTr = new AsignadorConfianza(ProyectorRegistro.PROY_TRANGE);

    /* Iteracion en las estaciones. */
    for (Long id : ids) {
      session = DBSessionFactory.getInstance().getCurrentSession();
      session.beginTransaction();
      estacionDAO = DAOFactory.getEstacionDAO(session);

      Estacion e = estacionDAO.findByID(id);
      if (recalcular) {
        /* Asignacion de confianza. */
        asignadorConf.asignarConfianzaL1(e, session);
        asignadorConfTr.asignarConfianzaL1(e, session);
      }

      /* Reporte. */
      confianzaUseCase.reportarEstacion(session, e, fw);

      session.getTransaction().commit();

      /* Informe avance. */
      if (estI % 1 == 0) {
        String elapsed = TimeDisplayUtil.displayLapse(new Date().getTime() - start);
        System.out.println("Estacion " + estI + "/" + ids.size() + " procesada. Tiempo parcial: " + elapsed);
      }

      estI++;
    }

    fw.close();
    System.out.println("\n\n");
  }

  public ConfianzaUseCase(ProyectorRegistro proy) {
    this.proy = proy;
  }

  public void reportarEstacion(Session sess, Estacion e, FileWriter fw) throws IOException {
    CatalogoCorrelacion catCorr = new CatalogoCorrelacion(proy.nombreVariable());
    List<CorrelacionEstaciones> cs = catCorr.getCorrelaciones(e, sess, 0.85, 30, NUM_VECINOS);
    TestVecindad testVcnd = new TestVecindad(sess, e, cs, proy, false);

    for (RegistroDiario rd : e.getRegistros()) {
      ConfianzaVariable confianza = this.getConfianzaRelevante(rd);
      ConfianzaVariable confianzaRange = rd.getConfianzaTempRange();

      ResultadoTestQC resOutl = rd.getResultadoByID(OutlierTest.TEST_PREFIX + "_" + this.proy.nombreVariable());
      double valOutl = resOutl != null ? Math.abs(resOutl.getValor()) : 0;

      boolean tempAReportar = confianza != null
          && (confianza.getConfianza() > 0 || valOutl > 6.5 || catsDeInteres.contains(confianza.getCodigo()));
      boolean tempRangeAReportar = confianzaRange != null
          && (confianzaRange.getConfianza() > 0 || catsDeInteres.contains(confianzaRange.getCodigo()));

      if (tempAReportar || tempRangeAReportar) {
        String rep = reportarRegistro(rd, testVcnd, proy);
        fw.append(rep + "\n");
      }
    }
  }

  public String reportarRegistro(RegistroDiario rd, TestVecindad testVcnd, ProyectorRegistro proy) {
    StringBuilder rv = new StringBuilder();

    Estacion est = rd.getEstacion();

    /* Datos de la estacion. */
    rv.append(est.getNombre() + "/" + est.getId() + ",");
    rv.append((est.getLatitud() / -100d) + " x " + (est.getLongitud() / -100d) + " x " + est.getAltura() + ",");

    /* Datos del registro. */
    rv.append(dfrmt.format(rd.getFecha()) + "," + RegistroHelper.getDescrCorta(rd, ProyectorRegistro.PROY_TMIN) + ","
        + RegistroHelper.getDescrCorta(rd, ProyectorRegistro.PROY_TMAX) + ",");

    /* Datos de los tests de DIP y Outl. */
    ResultadoTestQC routl = rd.getResultadoByID(OutlierTest.TEST_PREFIX + "_" + proy.nombreVariable());
    ResultadoTestQC routlTr = rd.getResultadoByID("OUTL_Tr");
    ResultadoTestQC rdip = rd.getResultadoByID(DIPTest.TEST_PREFIX + "_" + proy.nombreVariable());

    String outl = (routl == null) ? "" : frmt.format(routl.getValor());
    String outlTr = (routlTr == null) ? "" : frmt.format(routlTr.getValor());
    String dip = (rdip == null) ? "" : frmt.format(rdip.getValor());

    rv.append(outl + "," + outlTr + "," + dip + ",");

    /* Datos de confianza. */
    ConfianzaVariable confianza = this.getConfianzaRelevante(rd);
    ConfianzaVariable confianzaRange = rd.getConfianzaTempRange();

    String confRangeStr = confianzaRange == null ? "" : confianzaRange.getCodigo();
    rv.append(confianza.getConfianza() + "," + confianza.getCodigo() + "," + confRangeStr + ",");

    ResultadoVecindad resVcnd = (testVcnd == null) ? null : testVcnd.cotejarVecindad(rd);

    /* Datos del test de Vecindad. */
    if (resVcnd != null) {
      rv.append(frmt.format(resVcnd.getPrediccion()) + "," + frmt.format(resVcnd.getDesviacionEstimacion()) + ",");
      rv.append(frmt.format(resVcnd.getAnguloCubierto()) + "," + resVcnd.getCantidadVecinos() + ",");

      List<EstimacionVecino> estimaciones = resVcnd.getEstimaciones();
      for (int i = 0; i < estimaciones.size(); i++) {
        EstimacionVecino rVec = estimaciones.get(i);
        String nEst = rVec.getRd().getEstacion().getNombre() + "/" + rVec.getRd().getEstacion().getId();
        rv.append(nEst + "," + frmt.format(rVec.getAnguloEstacion()) + "," + frmt.format(rVec.getPrediccion()) + ",");
      }

      /* Subcategorias de interes para los casos de vecindad relevante pero sin dip. */

    }

    return rv.toString();
  }

  private ConfianzaVariable getConfianzaRelevante(RegistroDiario rd) {
    return (this.proy == ProyectorRegistro.PROY_TMIN) ? rd.getConfianzaTempMin() : rd.getConfianzaTempMax();
  }
}