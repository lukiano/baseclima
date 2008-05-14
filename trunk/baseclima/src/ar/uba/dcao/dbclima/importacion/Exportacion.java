package ar.uba.dcao.dbclima.importacion;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Clase que exporta la base de datos de referencia existente en el modelo original como un archivo
 * de texto compatible con el modelo nuevo. Ya no deberia ser usada.
 * 
 * @deprecated
 */
@Deprecated
public class Exportacion {

  private static SimpleDateFormat frmt = (SimpleDateFormat) SimpleDateFormat.getInstance();

  private static final Integer ID_ARGENTINA = 87;

  private final String filename;

  private final List<Long> idEstaciones;

  private static final Date FECHA_INI = new Date(75, 0, 1);

  private static final Date FECHA_FIN = new Date(105, 0, 1);

  public static void main(String[] args) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();

    List<Long> ids = getIDsEstaciones(sess);
    new Exportacion("regs.txt", ids).doExport(sess);
  }

  private static List<Long> getIDsEstaciones(Session sess) {
    // return DAOFactory.getEstacionDAO(sess).findAllIDs();

    List<Long> rv = new ArrayList<Long>();
    List<Estacion> ests = DAOFactory.getEstacionDAO(sess).findAll();

    int minLat = 3450;
    int maxLat = 3750;

    int minLon = 5750;
    int maxLon = 6100;

    for (Estacion e : ests) {
      if (e.getLatitud() <= maxLat && e.getLatitud() >= minLat && e.getLongitud() >= minLon
          && e.getLongitud() <= maxLon) {
        rv.add(e.getId());
      }
    }

    System.out.println(rv.size() + " estaciones seleccionadas");
    return rv;
  }

  public Exportacion(String filename, List<Long> idEstaciones) {
    this.filename = filename;
    this.idEstaciones = idEstaciones;
    frmt.applyPattern("dd/MM/yyyy");
  }

  public void doExport(Session sess) {
    FileWriter out = null;
    try {
      out = new FileWriter(this.filename);
    } catch (IOException e1) {
      throw new IllegalStateException(e1);
    }

    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);
    for (Long id : idEstaciones) {
      Estacion e = estacionDAO.findByID(id);
      String estDesc = getDescFromEstacion(e);

      for (RegistroDiario rd : e.getRegistros()) {
        if (rd.getFecha().after(FECHA_INI) && rd.getFecha().before(FECHA_FIN)) {
          String desc = estDesc + "," + getDescFromRegistro(rd) + "\n";
          try {
            out.write(desc);
          } catch (IOException e1) {
            throw new IllegalStateException(e1);
          }
        }
      }
      sess.clear();
    }
    try {
      out.close();
    } catch (IOException e1) {
      throw new IllegalStateException(e1);
    }
  }

  private String getDescFromEstacion(Estacion e) {
    String rv = ID_ARGENTINA + ",";

    String codigoOMM = e.getCodigoOMM() == null ? "" : e.getCodigoOMM().toString();
    if (codigoOMM.length() > 3) {
      codigoOMM = codigoOMM.substring(codigoOMM.length() - 3);
      rv += codigoOMM;
    }
    rv += "," + e.getCodigoSMN() + "," + e.getProvincia() + "," + e.getNombre() + ",";
    rv += (e.getLatitud() * -10) + "," + (e.getLongitud() * -10) + "," + e.getAltura();

    return rv;
  }

  private String getDescFromRegistro(RegistroDiario rd) {
    String fecha = frmt.format(rd.getFecha());
    String tn = rd.getTempMin() == null ? "" : "" + (rd.getTempMin() * 10);
    String tx = rd.getTempMax() == null ? "" : "" + (rd.getTempMax() * 10);
    String prec = rd.getPrecipitacion() == null ? "" : "" + (rd.getPrecipitacion() * 10);

    return fecha + "," + tn + "," + tx + "," + prec;
  }
}
