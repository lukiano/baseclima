package ar.uba.dcao.dbclima.casosDeUso.reportes;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.utils.FechaHelper;

public class ErroresPorEstacion {

  private static final String[] MESES = { "1. Enero", "2. Feb", "3. Mar", "4. Abr", "5. May", "6. Jun", "7. Jul",
      "8. Ago", "9. Sep", "10. Oct", "11. Nov", "12. Dic", "Todos" };

  private static final int[] PERIODO = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 365};

  public static void main(String[] args) throws IOException {
    ProyectorRegistro proy = ProyectorRegistro.PROY_TMAX;

    FileWriter fw = new FileWriter("RepErrores" + proy.nombreVariable() + ".csv");

    escribirReporte(fw, proy);

    fw.close();
  }

  public static void escribirReporte(FileWriter fw, ProyectorRegistro proy) throws IOException {

    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);

    fw.append("Estacion,Longitud,Latitud,Provincia,Mes,#Dias,#Registros,NeedCheck,Dudosos,Errores\n");

    List<Long> ids = estacionDAO.findAllNamedIDs();
    for (int estN = 0; estN < ids.size(); estN++) {

      Long id = ids.get(estN);
      System.out.println("Procesando estacion " + (estN+1) + "/" + ids.size());
      Estacion e = estacionDAO.findByID(id);

      int[][] confRegs = new int[13][3];
      int[] regsTotal = new int[13];
      for (RegistroDiario r : e.getRegistros()) {
        ConfianzaVariable confianza = getConfianza(proy, r);
        if (confianza != null) {
          int nivelConfianza = confianza.getConfianza();
          int mes = FechaHelper.dameMes0a11(r.getFecha());

          regsTotal[12]++;
          regsTotal[mes]++;

          if (nivelConfianza > 0) {
            confRegs[12][nivelConfianza - 1]++;
            confRegs[mes][nivelConfianza - 1]++;
          }
        }
      }

      for (int i = 0; i < 13; i++) {
        fw.append(e.getNombre() + "," + e.getLongitud() + "," + e.getLatitud() + "," + e.getProvincia() + ",");
        fw.append(MESES[i] + "," + PERIODO[i] + "," + regsTotal[i] + "," + confRegs[i][0] + "," + confRegs[i][1] + "," + confRegs[i][2] + "\n");
      }
      
      sess.clear();
    }
  }

  private static ConfianzaVariable getConfianza(ProyectorRegistro proy, RegistroDiario rd) {
    return proy == ProyectorRegistro.PROY_TMIN ? rd.getConfianzaTempMin() : rd.getConfianzaTempMax();
  }
}
