package ar.uba.dcao.dbclima.gui.viz;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.gui.ManagementConsole;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

public class VisualizacionMapa extends JFrame {

  private static final int WIDTH = 800;

  private static final int HEIGHT = 800;

  private BaseLayer baseLayer;

  private Class<? extends RepresentacionEstacion> representador;

  private WindowListener windowListener = new WindowAdapter() {
    @Override
    public void windowClosing(WindowEvent e) {
      VisualizacionMapa.this.dispose();
    }
  };

  public VisualizacionMapa(Class<? extends RepresentacionEstacion> representador) {
    this.representador = representador;
    this.setSize(WIDTH, HEIGHT);
    this.baseLayer = new BaseLayer(this.getBounds());
    this.getContentPane().add(baseLayer);
    this.addWindowListener(this.windowListener);
  }

  public void init(Long dsID) {
    try {
      this.baseLayer.init(loadRepresentadores(dsID));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private List<RepresentacionEstacion> loadRepresentadores(Long dsID) throws Exception {
    /* Get estaciones. */
    Session session = DBSessionFactory.getInstance().getCurrentSession();
    session.beginTransaction();
    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(session);

    List<Long> ids;

    if (dsID == null) {
      ids = estacionDAO.findAllIDsForBDR();
    } else {
      ids = estacionDAO.findAllIDsByDatasetID(dsID);
    }

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
}
