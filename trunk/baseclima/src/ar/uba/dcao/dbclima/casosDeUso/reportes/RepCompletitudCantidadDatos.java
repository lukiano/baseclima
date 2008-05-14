package ar.uba.dcao.dbclima.casosDeUso.reportes;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.correlation.CatalogoCorrelacion;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.FiltroRegistro;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.qc.VecindadEstacion;

public class RepCompletitudCantidadDatos {

  private static final int ANIO_FIN = 2005;

  private static final int ANIO_INICIO = 1959;

  private static Calendar cal = Calendar.getInstance();

  public static void main(String[] args) {
    relevarCompletitud(DBSessionFactory.getInstance().getCurrentSession());
  }

  private static void relevarCompletitud(Session sess) {
    sess.beginTransaction();
    int[] regsPosibles = new int[ANIO_FIN - ANIO_INICIO + 1];
    int[] regsTmin = new int[ANIO_FIN - ANIO_INICIO + 1];
    int[] regsTmax = new int[ANIO_FIN - ANIO_INICIO + 1];

    /* Cantidad de vecindades por anio, segun cantidad de vecinos. */
    int[][] vecinosTmin = new int[3][ANIO_FIN - ANIO_INICIO + 1];
    int[][] vecinosTmax = new int[3][ANIO_FIN - ANIO_INICIO + 1];

    /* Estaciones x anio completas al 1%, 10% y 90%. */
    int[] estQueFiguranXAnioTn = new int[ANIO_FIN - ANIO_INICIO + 1];
    int[] estXAnioTn = new int[ANIO_FIN - ANIO_INICIO + 1];
    int[] estComplXAnioTn = new int[ANIO_FIN - ANIO_INICIO + 1];
    int[] estXAnioTx = new int[ANIO_FIN - ANIO_INICIO + 1];
    int[] estQueFiguranXAnioTx = new int[ANIO_FIN - ANIO_INICIO + 1];
    int[] estComplXAnioTx = new int[ANIO_FIN - ANIO_INICIO + 1];

    EstacionDAO dao = DAOFactory.getEstacionDAO(sess);
    List<Long> estIDs = dao.findAllIDs();

    CatalogoCorrelacion catCorrMin = new CatalogoCorrelacion(ProyectorRegistro.PROY_TMIN.nombreVariable());
    CatalogoCorrelacion catCorrMax = new CatalogoCorrelacion(ProyectorRegistro.PROY_TMAX.nombreVariable());

    List<CorrelacionEstaciones> csMin;
    List<CorrelacionEstaciones> csMax;

    for (int i = 0; i < estIDs.size(); i++) {
      System.out.println("Procesando estacion " + (i + 1) + "/" + estIDs.size());
      Estacion e = dao.findByID(estIDs.get(i));

      csMin = catCorrMin.getCorrelaciones(e, sess, 0.85, 30, 14);
      csMax = catCorrMax.getCorrelaciones(e, sess, 0.85, 30, 14);

      obtenerDiasPosibles(e, regsPosibles);
      obtenerDiasConValor(sess, e, regsTmin, csMin, vecinosTmin, ProyectorRegistro.PROY_TMIN,
          VecindadEstacion.filtroVecinoTn, estQueFiguranXAnioTn, estXAnioTn, estComplXAnioTn);
      obtenerDiasConValor(sess, e, regsTmax, csMax, vecinosTmax, ProyectorRegistro.PROY_TMAX,
          VecindadEstacion.filtroVecinoTx, estQueFiguranXAnioTx, estXAnioTx, estComplXAnioTx);
      sess.clear();
    }

    System.out.print("Año,Datos posibles,Datos Tmin,Tmin Con 0 vec,Tmin con 1 vec,Tmin con 2 vec,");
    System.out.print("Datos Tmax,Tmax Con 0 vec,Tmax con 1 vec,Tmax con 2 vec,");
    System.out
        .print("Est con 1% regs Tn,Est completas 10% Tn,Est completas 90% Tn,Est con 1% regs Tx,Est completas 10% Tx,Est completas 90% Tx");
    System.out.println();

    for (int i = ANIO_INICIO; i <= ANIO_FIN; i++) {
      int ind = i - ANIO_INICIO;
      System.out.print(i + "," + regsPosibles[ind] + "," + regsTmin[ind]);
      System.out.print("," + vecinosTmin[0][ind] + "," + vecinosTmin[1][ind] + "," + vecinosTmin[2][ind]);

      System.out.print("," + regsTmax[ind]);
      System.out.print("," + vecinosTmax[0][ind] + "," + vecinosTmax[1][ind] + "," + vecinosTmax[2][ind]);

      System.out.print("," + estQueFiguranXAnioTn[ind] + "," + estXAnioTn[ind] + "," + estComplXAnioTn[ind] + ","
          + estQueFiguranXAnioTx[ind] + "," + estXAnioTx[ind] + "," + estComplXAnioTx[ind]);
      System.out.println();
    }
  }

  private static void obtenerDiasConValor(Session sess, Estacion e, int[] regsReales, List<CorrelacionEstaciones> cs,
      int[][] vecinos, ProyectorRegistro proy, FiltroRegistro filtro, int[] estQAparecenXAnio, int[] estXAnio,
      int[] estComplXAnio) {
    VecindadEstacion v = new VecindadEstacion(e, filtro, cs);

    int[] regsEstAnio = new int[ANIO_FIN - ANIO_INICIO + 1];

    for (RegistroDiario rd : e.getRegistros()) {
      cal.setTime(rd.getFecha());
      if (proy.getValor(rd) != null && cal.get(Calendar.YEAR) <= ANIO_FIN) {
        /* Cantidad valores */
        int indAnio = cal.get(Calendar.YEAR) - ANIO_INICIO;
        regsReales[indAnio]++;
        regsEstAnio[indAnio]++;

        /* cantidad vecinos. */
         int cantVec = v.getRegistrosVecinos(rd.getFecha()).size();
         cantVec = Math.min(cantVec, 2);
         vecinos[cantVec][indAnio]++;
      }
    }

    for (int i = 0; i < regsEstAnio.length; i++) {
      if (regsEstAnio[i] > 3) {
        estQAparecenXAnio[i]++;
      }
      if (regsEstAnio[i] > 36) {
        estXAnio[i]++;
      }
      if (regsEstAnio[i] > 365d * 0.9) {
        estComplXAnio[i]++;
      }
    }
  }

  private static void obtenerDiasPosibles(Estacion e, int[] regsPosibles) {
    cal.setTime(e.getFechaInicio());
    int anioInicial = cal.get(Calendar.YEAR);
    cal.setTime(e.getFechaFin());
    int anioFin = cal.get(Calendar.YEAR);

    for (int i = ANIO_INICIO; i <= ANIO_FIN; i++) {
      int diaInicial = 0;
      int diaFin = 364;

      if (anioInicial == i) {
        cal.setTime(e.getFechaInicio());
        diaInicial = cal.get(Calendar.DAY_OF_YEAR) - 1;
      } else if (anioInicial > i) {
        continue;
      }

      if (anioFin == i) {
        cal.setTime(e.getFechaFin());
        diaFin = cal.get(Calendar.DAY_OF_YEAR) - 1;
      } else if (anioFin < i) {
        continue;
      }

      int bisiesto = (i % 4 == 0 && diaFin > 59 && diaInicial < 58) ? 1 : 0;
      regsPosibles[i - ANIO_INICIO] += diaFin - diaInicial + 1 + bisiesto;
    }
  }
}
