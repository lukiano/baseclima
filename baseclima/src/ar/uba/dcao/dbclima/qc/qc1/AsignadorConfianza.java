package ar.uba.dcao.dbclima.qc.qc1;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.correlation.CatalogoCorrelacion;
import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.ResultadoTestQC;
import ar.uba.dcao.dbclima.qc.ConfVarFactory;
import ar.uba.dcao.dbclima.qc.ResultadoVecindad;

public class AsignadorConfianza {

  private static final int[][] nivelVecindad = { { 0, 0, 1, 1, 2, 3, 3, 4 }, { 0, 0, 1, 1, 2, 3, 4, 4 },
      { 0, 0, 1, 2, 3, 4, 4, 4 }, { 0, 1, 2, 2, 3, 4, 4, 4 } };

  private ProyectorRegistro proyector;

  public AsignadorConfianza(ProyectorRegistro proyector) {
    this.proyector = proyector;
  }

  public void asignarConfianzaL1(Estacion estacion, Session sess) {

    CatalogoCorrelacion catCorr;
    List<CorrelacionEstaciones> cs;

    catCorr = new CatalogoCorrelacion(this.proyector.nombreVariable());
    cs = catCorr.getCorrelaciones(estacion, sess, 0.85, 300, 11);

    TestVecindad testVcnd = new TestVecindad(sess, estacion, cs, this.proyector, false);

    double[] avgRange = null;
    if (this.proyector == ProyectorRegistro.PROY_TRANGE) {
      avgRange = getAvgRange(estacion);
    }

    for (RegistroDiario rd : estacion.getRegistros()) {
      if (this.proyector.getValor(rd) != null) {
        ConfianzaVariable confianza = asignarConfianza(rd, this.proyector, testVcnd, avgRange);
        if (this.proyector == ProyectorRegistro.PROY_TMIN) {
          rd.setCodigoConfianzaTempMin(confianza.getCodigo());
        } else if (this.proyector == ProyectorRegistro.PROY_TMAX) {
          rd.setCodigoConfianzaTempMax(confianza.getCodigo());
        } else if (this.proyector == ProyectorRegistro.PROY_TRANGE) {
          rd.setCodigoConfianzaTempRange(confianza.getCodigo());
        }

        sess.saveOrUpdate(rd);
      }
    }
  }

  private static ConfianzaVariable asignarConfianzaRange(double[] avgRangePorMes, RegistroDiario rd) {
    ConfianzaVariable rv;
    ResultadoTestQC testOutl = rd.getResultadoByID("OUTL_Tr");
    double outlVal = testOutl == null ? 0 : testOutl.getValor();
    ResultadoTestQC testOutlLimit = rd.getResultadoByID("OUTL_Tl");
    double outlValL = testOutlLimit == null ? 0 : testOutlLimit.getValor();

    if (outlValL >= 1) {
      rv = ConfVarFactory.get("TRNE");
    } else if (outlValL > 0.92) {
      rv = ConfVarFactory.get("TRXS");
    } else if (outlValL > 0.88) {
      rv = ConfVarFactory.get("TRS");
    } else if (outlVal > 8) {
      rv = ConfVarFactory.get("TRXL");
    } else if (outlVal > 5) {
      rv = ConfVarFactory.get("TRL");
    } else {
      rv = ConfVarFactory.get(ConfVarFactory.COK);
    }

    return rv;
  }

  private double[] getAvgRange(Estacion estacion) {
    double[] sumRgs = new double[12];
    int[] numRegs = new int[12];
    for (RegistroDiario rd : estacion.getRegistros()) {
      Integer rng = ProyectorRegistro.PROY_TRANGE.getValor(rd);
      if (rng != null) {
        int mes = getMonthRegistro(rd);
        sumRgs[mes] += rng;
        numRegs[mes]++;
      }
    }

    double[] rv = new double[12];
    for (int i = 0; i < 12; i++) {
      rv[i] = sumRgs[i] / numRegs[i];
    }

    return rv;
  }

  public static ConfianzaVariable asignarConfianza(RegistroDiario rd, ProyectorRegistro proy,
      TestVecindad testVecindad, double[] avgRange) {
    ConfianzaVariable rv = proy == ProyectorRegistro.PROY_TRANGE ? asignarConfianzaRange(avgRange, rd)
        : confianzaSegunTests(rd, testVecindad, proy);
    return rv;
  }

  private static ConfianzaVariable confianzaSegunTests(RegistroDiario rd, TestVecindad testVcnd, ProyectorRegistro proy) {
    ResultadoVecindad resVcnd = testVcnd.cotejarVecindad(rd);

    String postfijo = "_" + proy.nombreVariable();
    ResultadoTestQC resHomg = rd.getResultadoByID(SequenceHomogeneityTest.TEST_PREFIX + postfijo);
    double hmgn = resHomg == null ? 0 : resHomg.getValor();

    ConfianzaVariable rvQC11 = ConfVarFactory.get(ConfVarFactory.COK);
    ConfianzaVariable rvQC12 = rvQC11;

    /*
     * Decision de Nivel de confianza QC11
     */
    String tipoVec = tipoVecindad(rd, testVcnd, proy);
    String nivelDIP = tipoDIP(rd, proy);
    char nivelVec = nivelVecindad(resVcnd, rd, proy);

    String catConf = "";
    if (nivelVec == 'N') {
      catConf = nivelDIP + "VN";

    } else if (nivelDIP.equals("DN")) {
      catConf = "DNV" + nivelVec;

    } else if (nivelDIP.equals("EC")) {
      catConf = "ECV" + nivelVec;

    } else {
      catConf = nivelDIP + tipoVec + nivelVec;
    }

    rvQC11 = ConfVarFactory.get(catConf);
    /* Termina decision de Nivel de confianza QC11. */

    /* Comienza decision de Nivel de confianza QC12. */
    if (hmgn >= 2) {
      rvQC12 = ConfVarFactory.get("SLV" + nivelVec);
    } else if (hmgn > 1) {
      rvQC12 = ConfVarFactory.get("SCV" + nivelVec);
    }
    /* Termina decision de Nivel de confianza QC12 */

    return ConfianzaVariable.masRelevante(rvQC11, rvQC12);
  }

  private static String tipoVecindad(RegistroDiario rd, TestVecindad vcnd, ProyectorRegistro proy) {

    /* -- Inicializacion de variables. -- */
    ResultadoVecindad resVcnd = vcnd.cotejarVecindad(rd);

    String tipoVec;

    if (resVcnd == null) {
      tipoVec = "VN";

    } else {

      ResultadoVecindad rAy = rd.getAyer() == null ? null : vcnd.cotejarVecindad(rd.getAyer());
      ResultadoVecindad rMa = rd.getManiana() == null ? null : vcnd.cotejarVecindad(rd.getManiana());

      Integer diff1 = (rd.getAyer() == null || proy.getValor(rd.getAyer()) == null) ? null : proy.getValor(rd)
          - proy.getValor(rd.getAyer());
      Double diff1V = (rAy == null) ? null : resVcnd.getPrediccion() - rAy.getPrediccion();
      Double diff2V = (rMa == null) ? null : resVcnd.getPrediccion() - rMa.getPrediccion();

      if (diff1V == null || diff2V == null) {
        tipoVec = "VEN";
      } else if (diff1V.doubleValue() * diff2V.doubleValue() <= 0) {
        tipoVec = "VEC";
      } else if (diff1 != 0 && Math.signum(diff1) == Math.signum(diff1V)) {
        tipoVec = "VEI";
      } else {
        tipoVec = "VED";
      }
    }

    return tipoVec;
  }

  /**
   * Indica en que categoria de DIP se encuentra valor asociado a proy en el registro.
   * Esta categoria depende exclusivamente del resultado del test DIP sobre el valor en
   * cuestion.<br>
   * <ul>
   * <li>DX: DIP eXtra alto.
   * <li>DA: DIP Alto.
   * <li>DM: DIP Moderado.
   * <li>EB: Extremo Bajo.
   * <li>EC: Extremo Cero.
   * <li>EN: Extremidad no calculable.
   * </ul>
   */
  private static String tipoDIP(RegistroDiario rd, ProyectorRegistro proy) {
    String postfijo = "_" + proy.nombreVariable();
    ResultadoTestQC resDipt = rd.getResultadoByID(DIPTest.TEST_PREFIX + postfijo);
    ResultadoTestQC resOutl = rd.getResultadoByID(OutlierTest.TEST_PREFIX + postfijo);
    double dipt = resDipt == null ? 0 : resDipt.getValor();
    double outl = resOutl == null ? 0 : resOutl.getValor();

    double absDIP = Math.abs(dipt);
    double absOutl = Math.abs(outl);

    Integer tHoy = proy.getValor(rd);
    Integer tAyer = rd.getAyer() == null ? null : proy.getValor(rd.getAyer());
    Integer tMan = rd.getManiana() == null ? null : proy.getValor(rd.getManiana());

    String rv;
    if (absDIP > 5.5) {
      rv = "DX";
    } else if (absDIP > 3.5 || (absDIP > 3 && absOutl > 1.5 && dipt * outl >= 0)) {
      rv = "DA";
    } else if (absDIP > 2.5) {
      rv = "DM";
    } else if (tAyer == null || tMan == null) {
      rv = "DN";
    } else if ((tHoy - tAyer) * (tHoy - tMan) > 0) {
      rv = "EB";
    } else {
      rv = "EC";
    }

    return rv;
  }

  /**
   * Nivel de desviacion de la prediccion de los vecinos con respecto al valor del dia.
   * Una prediccion muy deviada indica posibles problemas en el registro.
   * 
   * <b>Posibles niveles: </b> N para vecinidad nula o no-util. 0-4 para vecindades
   * utiles, segun diferencia con el valor de la estacion.
   */
  private static char nivelVecindad(ResultadoVecindad resV, RegistroDiario rd, ProyectorRegistro proy) {
    char rv;

    if (resV == null) {
      /* No hay vecindad, el codigo apropiado es 'N'. */
      rv = 'N';

    } else {

      /* Variables a usar durante la decision de nivel de vecindad. */
      Double desv = resV.getDesviacionEstimacion();
      int cantVec = resV.getCantidadVecinos();
      boolean consenso = resV.consenso() > 0.899d;

      Integer valAnt = rd.getAyer() == null ? null : proy.getValor(rd.getAyer());
      Integer valSig = rd.getManiana() == null ? null : proy.getValor(rd.getManiana());
      int val = proy.getValor(rd);

      Double extr; 
      if (valAnt == null || valSig == null) {
        extr = null;
      } else {
        extr = (double)((val - valAnt) * (val - valSig));
        extr = Math.max(0, extr);
        extr = Math.abs(extr) * Math.signum(val - valAnt);
      }

      boolean dipCoherente = extr == null || Math.signum(extr) * Math.signum(desv) >= 0;
      boolean consYcoh = dipCoherente && consenso;

      /*
       * La vecindad no predice algo mucho mas desviado que el valor mismo (o cuenta con
       * varios vecinos).
       */
      int iAng = (int) Math.floor(resV.getAnguloCubierto() / 90);
      int iDesv = indiceDesviacion(consYcoh, Math.abs(desv));
      int rvI = nivelVecindad[iAng][iDesv];
      rv = Character.forDigit(rvI, 10);

      if (rvI > 0) {
        /*
         * Para vecindades chicas ( < 3 vecinos), solo consideramos las predicciones que
         * no parezcan erroneas. Para esto me fijo q la prediccion no sea mas incoherente
         * que el valor de la estacion mismo.
         */
        if (cantVec < 3 && valAnt != null && valSig != null) {
          double valExp = (valAnt + valSig) / 2d;
          double distPred = Math.abs(valExp - resV.getPrediccion());
          double distVal = Math.abs(valExp - val);

          if (distPred >= distVal * 1.5d) {
            /* La vecindad indica problemas, pero este parece ser propio de la vecindad. */
            rv = 'N';
          }
        }

      }
    }

    return rv;
  }

  /**
   * Fila de la matriz nivelVecindad en la que se encuentra el valor de desviacion
   * indicado.
   */
  private static int indiceDesviacion(boolean hayCons, double desv) {
    int rv;
    if (desv > 3 && hayCons) {
      rv = 7;
    } else if (desv > 2.5 && hayCons) {
      rv = 6;
    } else if (desv > 2 && hayCons) {
      rv = 5;
    } else if (desv > 1.5 && hayCons) {
      rv = 4;
    } else if (desv > 1.25 && hayCons) {
      rv = 3;
    } else if (desv > 1 && hayCons) {
      rv = 2;
    } else if (desv >= 1) {
      rv = 1;
    } else {
      rv = 0;
    }
    return rv;
  }

  private static int getMonthRegistro(RegistroDiario rd) {
    Calendar.getInstance().setTime(rd.getFecha());
    return Calendar.getInstance().get(Calendar.MONTH);
  }
}