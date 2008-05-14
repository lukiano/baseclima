package ar.uba.dcao.dbclima.casosDeUso.browsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.qc.qc1.QC1;

public class TestResultDistributionBrowser implements Browser {

  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    List<String> tests = Arrays.asList(QC1.testsQC1);
    printResEst(getEstacionesTesteadas(sess), tests, sess, 1.4d);

    sess.close();
  }

  @SuppressWarnings("unchecked")
  private static List<Integer> getEstacionesTesteadas(Session sess) {
    List<Integer> cods = sess.createSQLQuery("SELECT distinct est.cod_smn" +
            " FROM Resultado_Test rt, Registro_Diario rd, Estacion est" +
            " WHERE rt.reg_id = rd.reg_id AND rd.est_id = est.est_id")
            .list();

    return cods;
  }

  @SuppressWarnings("unused")
  private static List<Integer> getEstacionesElegidas() {
    final List<Integer> codsEstaciones = new ArrayList<Integer>();
    codsEstaciones.add(34); // Chaco
    codsEstaciones.add(62); // Santiago Del Estero
    codsEstaciones.add(100); // Cordoba
    codsEstaciones.add(163); // Rio Negro
    codsEstaciones.add(166); // Ezeiza
    codsEstaciones.add(175); // PONTON PRACTICOS RECALADA
    codsEstaciones.add(548); // Usuhaia

    return codsEstaciones;
  }

  @SuppressWarnings("unchecked")
  private static void printResEst(List<Integer> codsSMN, List<String> tests, Session sess, Double minVal) {
    System.out.println("Estacion Registros Test Marcados(cant) Marcados(%) 0 .1 .2 .3 .4 .5 .6 .7 .8 .9 1");

    for (String testID : tests) {
      for (Integer codSMN : codsSMN) {
        String qEst = "FROM Estacion e WHERE e.codigoSMN = " + codSMN;
        Estacion e = (Estacion) sess.createQuery(qEst).uniqueResult();

        String qEnumTests = "SELECT r.valor FROM ResultadoTestQC r" +
                " WHERE r.registro.estacion.codigoSMN = " + codSMN +
                " AND r.testID = '" + testID + "' AND abs(r.valor) >= " + minVal;

        List<Double> vals = sess.createQuery(qEnumTests).list();

        String qCountRegs = "SELECT COUNT(*) FROM RegistroDiario r WHERE r.estacion.codigoSMN = " + codSMN;
        Integer regsNum = (Integer) sess.createQuery(qCountRegs).uniqueResult();

        Collections.sort(vals);
        double percMarcados = formatDouble(100 * ((double) vals.size() / regsNum), 2);

        String resultadoEstacion = "'" + e.getNombre() + "' " + regsNum + " " + testID + " " + vals.size() + " " + percMarcados + "% ";

        List<Integer> listPositions = new ArrayList<Integer>();
        // Percentil "0" (se descuentan los 2 mayores outliers)
        listPositions.add(2);

        // Percentiles 10 a 90
        for (int i = 1; i < 10; i++) {
          int listPos = (int) Math.round(vals.size() * i / 10d);
          listPositions.add(listPos);
        }

        // Percentil "100" (se descuentan los 2 mayores outliers)
        listPositions.add(vals.size() - 3);

        if (vals.size() > 80) {
          for (int i = 0; i <= 10; i++) {
            int listPos = (int) Math.round(vals.size() * i / 10d);
            listPos = Math.min(listPos, vals.size() - 1);
            resultadoEstacion += formatDouble(vals.get(listPos), 2) + " ";
          }
        }

        System.out.println(resultadoEstacion);
      }

      System.out.println("");
    }
  }

  private static double formatDouble(double d, int digits) {
    double pow = Math.pow(10, digits);
    return Math.round(d * pow) / pow;
  }
}
