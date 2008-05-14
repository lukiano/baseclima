package ar.uba.dcao.dbclima.graficos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.hibernate.classic.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

public class Graficador {

  private static final String CACHE_PREFIX = "REP_";

  private static final int START_YEAR = 50;

  private static final int END_YEAR = 109;

  private JFrame frame;

  private MapaGrafico mapaGrafico;

  private Class<? extends RepresentacionEstacion> representador;

  public static void main(String[] args) {
    new Graficador(RepCantidadEventosActual.class, 800, 850).render();
    //new Graficador(RepEvExtremosActual.class, 800, 850).render();
  }

  public Graficador(Class<? extends RepresentacionEstacion> representador, int width, int height) {
    this.representador = representador;

    this.frame = new JFrame();
    this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.frame.setSize(width, height);
    this.mapaGrafico = new MapaGrafico(frame.getBounds());
    frame.getContentPane().add(mapaGrafico);
    try {
      this.mapaGrafico.init(loadRepresentadores());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public void render() {
    frame.setVisible(true);
  }

  private List<RepresentacionEstacion> loadRepresentadores() throws Exception {
    String fileName = "resources/" + CACHE_PREFIX + this.representador.getSimpleName() + ".ser";
    List<RepresentacionEstacion> representaciones = deserialize(fileName);

    if (representaciones == null) {
      representaciones = loadFromDB(START_YEAR, END_YEAR);
      serialize(fileName, representaciones);
    }

    return representaciones;
  }

  private List<RepresentacionEstacion> loadFromDB(int startY, int endY) throws Exception {
    /* Get estaciones. */
    Session session = DBSessionFactory.getInstance().getCurrentSession();
    session.beginTransaction();
    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(session);
    List<Long> ids = estacionDAO.findAllIDsByActivityDate(startY, endY);

    List<RepresentacionEstacion> rv = new ArrayList<RepresentacionEstacion>();
    for (int i = 0; i < ids.size(); i++) {
      Estacion estacion = estacionDAO.findByID(ids.get(i));
      RepresentacionEstacion representador = this.representador.newInstance();
      representador.init(estacion);
      rv.add(representador);
      session.clear();
    }

    session.close();
    return rv;
  }

  @SuppressWarnings("unchecked")
  private List<RepresentacionEstacion> deserialize(String filename) throws Exception {
    List<RepresentacionEstacion> rv = null;
    File serialFile = new File(filename);

    if (serialFile.exists()) {
      ObjectInputStream deser = new ObjectInputStream(new FileInputStream(serialFile));
      rv = (List<RepresentacionEstacion>) deser.readObject();
      deser.close();
    }

    return rv;
  }

  private void serialize(String filename, List<RepresentacionEstacion> representaciones) throws Exception {
    ObjectOutputStream ser = new ObjectOutputStream(new FileOutputStream(filename));
    ser.writeObject(representaciones);
    ser.close();
  }
}
