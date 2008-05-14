package ar.uba.dcao.dbclima.casosDeUso.browsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

public class RepValoresFaltantes implements Browser {

  private static final int DIAS_TOTAL_DISPLAY = 15;

  private static ProyectorRegistro bothNull = new ProyectorRegistro() {
    public Integer getValor(RegistroDiario rd) {
      return rd.getTempMax() == null && rd.getTempMin() == null ? null : 0;
    }

    public String nombreVariable() {
      return "NX";
    }
  };

  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    EstacionDAO edao = DAOFactory.getEstacionDAO(sess);
    List<ProyectorRegistro> proyectores = new ArrayList<ProyectorRegistro>();
    proyectores.add(ProyectorRegistro.PROY_TMIN);
    proyectores.add(ProyectorRegistro.PROY_TMAX);
    proyectores.add(RepValoresFaltantes.bothNull);

    valoresFaltantes(proyectores, edao.findAllIDs());

    sess.close();
    DBSessionFactory.getInstance().close();
  }

  private static void valoresFaltantes(List<ProyectorRegistro> proyectores, List<Long> estaciones) {
    int[][] faltantesXLongRachaTotal = new int[proyectores.size()][DIAS_TOTAL_DISPLAY];
    int[] faltantesXProyTotal = new int[proyectores.size()];
    int[] cantDiasXProyTotal = new int[proyectores.size()];

    for (Long id : estaciones) {
      int[][] faltantesXLongRacha = new int[proyectores.size()][DIAS_TOTAL_DISPLAY];
      int[] faltantesXProy = new int[proyectores.size()];
      int[] cantDiasXProy = new int[proyectores.size()];

      Session sess = DBSessionFactory.getInstance().getCurrentSession();
      sess.clear();

      EstacionDAO edao = DAOFactory.getEstacionDAO(sess);
      Estacion e = edao.findByID(id);

      for (int i = 0; i < proyectores.size(); i++) {

        ProyectorRegistro proy = proyectores.get(i);
        Date lastIterDate = null;

        Date firstDay = null;
        Date lastDay = null;

        for (RegistroDiario rd : e.getRegistros()) {
          if (proy.getValor(rd) != null) {
            /* Registro con valor no nulo. */
            if (firstDay == null) {
              /* Primer registro con valor no nulo. */
              firstDay = rd.getFecha();
            }
            lastDay = rd.getFecha();

            if (lastIterDate != null) {
              int diff = (int) ((rd.getFecha().getTime() - lastIterDate.getTime()) / (1000 * 3600 * 24));
              diff--;
              if (diff > 0) {
                /* En este registro termino una secuencia de valores nulos. */
                int indice = Math.min(DIAS_TOTAL_DISPLAY, diff);
                faltantesXLongRacha[i][indice - 1]++;
                faltantesXLongRachaTotal[i][indice - 1]++;
                faltantesXProy[i] += diff;
                faltantesXProyTotal[i] += diff;
              }
            }
            lastIterDate = rd.getFecha();
          }
        }
        //FIXME: lastDay y firstDay podrian ser null...
        cantDiasXProy[i] = (int) ((lastDay.getTime() - firstDay.getTime()) / (1000 * 3600 * 24));
        cantDiasXProyTotal[i] += cantDiasXProy[i];
      }

//      System.out.print("\n" + e.getId() + "/" + e.getNombre() + "," + e.getProvincia() + ","
//          + e.getRegistros().size() + ",");
//      for (int i = 0; i < proyectores.size(); i++) {
//        double faltantes = Math.round(faltantesXProy[i] * 1000d / (double) cantDiasXProy[i]);
//        System.out.print(faltantes / 10d + ",");
//      }
       System.out.print("\nEstacion " + e.getId() + "/" + e.getNombre() + ", " + e.getProvincia() + " - ");
      System.out.println(e.getRegistros().size() + " registros");
      for (int i = 0; i < proyectores.size(); i++) {
        System.out.print(proyectores.get(i).nombreVariable() + ": " + Arrays.toString(faltantesXLongRacha[i]));
        double faltantes = Math.round(faltantesXProy[i] * 10000d / cantDiasXProy[i]);
        System.out.println(" Faltantes: " + faltantes / 100d + "% (" + cantDiasXProy[i] + ")");
      }
    }

    System.out.println("\nTotal");
    for (int i = 0; i < proyectores.size(); i++) {
      System.out.print(proyectores.get(i).nombreVariable() + ": " + Arrays.toString(faltantesXLongRachaTotal[i]));
      double faltantes = Math.round(faltantesXProyTotal[i] * 10000d / cantDiasXProyTotal[i]);
      System.out.println(" Faltantes: " + faltantes / 100d + "% (" + cantDiasXProyTotal[i] + ")");
    }
  }
}
