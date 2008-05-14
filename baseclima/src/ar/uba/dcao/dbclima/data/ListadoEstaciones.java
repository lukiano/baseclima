package ar.uba.dcao.dbclima.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.utils.CSVFileReader;

public class ListadoEstaciones {

  public static final String LISTADO_ESTACIONES_FILENAME = "data/EstacionesSMN.csv";

  private final File listadoEstaciones;

  private List<Estacion> estaciones;

  private List<Estacion> estacionesFromReg;

  private Session session;

  private HashMap<Integer, Estacion> estacionesHash;

  public ListadoEstaciones() throws FileNotFoundException {
    this(new File(LISTADO_ESTACIONES_FILENAME));
  }

  public ListadoEstaciones(File listadoEstaciones) throws FileNotFoundException {
    if (!listadoEstaciones.exists()) {
      throw new FileNotFoundException("No se encontro el archivo indicado");
    }
    this.listadoEstaciones = listadoEstaciones;
    this.buildEstaciones();
  }

  @SuppressWarnings("unchecked")
  private void buildEstaciones() {
    this.session = DBSessionFactory.getInstance().getCurrentSession();
    this.session.beginTransaction();

    this.estaciones = new ArrayList<Estacion>();
    this.estaciones.addAll(this.session.createQuery("FROM Estacion").list());
    this.estacionesHash = new HashMap<Integer, Estacion>();

    for (Estacion e : this.estaciones) {
      this.estacionesHash.put(e.getCodigoSMN(), e);
    }

    List<Estacion> estacionesHC = this.getEstacionesListadas();
    for (Estacion e : estacionesHC) {
      this.ensureExists(e);
    }
  }

  public List<Estacion> getEstacionesListadas() {
    if (this.estacionesFromReg == null) {
      this.estacionesFromReg = new ArrayList<Estacion>();

      List<String[]> estacionesEnc = null;
      try {
        estacionesEnc = new CSVFileReader(this.listadoEstaciones).readCSV();
        for (String[] estEnc : estacionesEnc) {
          Estacion e = this.parseEstacion(estEnc);
          if (e != null) {
            this.estacionesFromReg.add(e);
          }
        }
      } catch (FileNotFoundException e) {
        System.out.println("Problemas leyendo el archivo " + listadoEstaciones.getName()
            + ". No se podran actualizar algunos datos de las estaciones.");
      }
    }

    return this.estacionesFromReg;
  }

  private Estacion parseEstacion(String[] estEnc) {
    if (estEnc.length != 9) {
      return null;
    }

    Estacion rv = new Estacion();
    try {

      Float latF = Float.valueOf(estEnc[1]);
      Integer lat = Math.round(latF * 100);

      Float lonF = Float.valueOf(estEnc[2]);
      Integer lon = Math.round(lonF * 100);

      /*
       * Conversion de latitud y longitud a sistema decimal. XXX: Falta probar que
       * funcione.
       */
      float latFraccion = (lat % 100) / 0.6f;
      lat += latFraccion - (lat % 100);

      float lonFraccion = (lon % 100) / 0.6f;
      lon += lonFraccion - (lon % 100);

      rv.setNombre(estEnc[0].trim());
      rv.setLatitud(lat);
      rv.setLongitud(lon);
      rv.setAltura(Integer.valueOf(estEnc[3]));
      rv.setProvincia(estEnc[4].trim());
      rv.setCodigoOMM(Integer.valueOf(estEnc[5]));
      rv.setCodigoSMN(Integer.valueOf(estEnc[6]));

    } catch (NumberFormatException e) {
      return null;
    }

    return rv;
  }

  private void ensureExists(Estacion e) {
    Estacion et = this.estacionesHash.get(e.getCodigoSMN());

    if (et == null) {
      this.estaciones.add(e);
      this.estacionesHash.put(e.getCodigoSMN(), e);
    }
  }

  public Estacion getByCodigoSMN(Integer codigo) {
    Estacion e = this.estacionesHash.get(codigo);
    if (e == null) {
      e = new Estacion();
      e.setCodigoSMN(codigo);
      this.estaciones.add(e);
      this.estacionesHash.put(codigo, e);
    }

    return e;
  }
}
