package ar.uba.dcao.dbclima.casosDeUso.reportes;

import java.awt.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.clasificacion.ClasificadorEstacion;
import ar.uba.dcao.dbclima.clasificacion.ClasificadorMinMax;
import ar.uba.dcao.dbclima.clasificacion.GraficoClasificacion;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.utils.CollectionUtils;

public class RepClasificacion {

  private static final int HEIGHT = 600;

  private static final int WIDTH = 800;

  private static final DecimalFormat frm = new DecimalFormat("0.00");

  private static final int MAPS_SIDE_SIZE = 5;

  public static void main(String[] args) {
    try {
      FileWriter fw1 = new FileWriter("infoN.csv");
      FileWriter fw2 = new FileWriter("scatter.csv");
      reporte(1L, 3, fw1, fw2);
      fw1.close();
      fw2.close();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void reporte(Long estID, int mes, FileWriter repNeuronas, FileWriter repScatter) {
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);

    Estacion e = estacionDAO.findByID(estID);

    ClasificadorEstacion clasificador = new ClasificadorMinMax(e, mes);
    Map<RegistroDiario, Point> clasif = clasificador.clasificar();

    Map<Point, List<RegistroDiario>> neurs = invert(clasif);

    /* Organizacion de datos para el grafico de neuronas y estaciones. */
    Collection<Point> neuronasCol = neurs.keySet();
    Collection<Point> regsCol = clasif.values();

    graficar(neurs);

    String[][] vals = new String[MAPS_SIDE_SIZE][MAPS_SIDE_SIZE];
    for (Map.Entry<Point, List<RegistroDiario>> neurona : neurs.entrySet()) {
      String val = procesarNeurona(neurona.getValue(), repScatter);
      Point p = neurona.getKey();
      vals[p.x][p.y] = val;
    }

    try {
      for (int i = 0; i < MAPS_SIDE_SIZE; i++) {
        for (int j = 0; j < MAPS_SIDE_SIZE; j++) {
          repNeuronas.append(vals[i][j] + ",");
        }
        repNeuronas.append("\n");
      }

    } catch (IOException e1) {
      throw new IllegalStateException(e1);
    }
  }

  private static void graficar(Map<Point, List<RegistroDiario>> neuronas) {
    JComponent graf = new GraficoClasificacion(neuronas, WIDTH, HEIGHT);
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(WIDTH, HEIGHT);
    frame.getContentPane().add(graf);
    frame.setVisible(true);
  }

  private static String procesarNeurona(List<RegistroDiario> valsNeurona, FileWriter repScatter) {
    List<Double> tmins = new ArrayList<Double>();
    List<Double> tmaxs = new ArrayList<Double>();
    for (RegistroDiario rd : valsNeurona) {
      Double tmin = ProyectorRegistro.PROY_TMIN.getValor(rd).doubleValue();
      tmins.add(tmin);
      Double tmax = ProyectorRegistro.PROY_TMAX.getValor(rd).doubleValue();
      tmaxs.add(tmax);
      try {
        repScatter.append("v," + tmin + "," + tmax + "\n");
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }

    double avgTn = CollectionUtils.avg(tmins);
    double stdvTn = CollectionUtils.stdv(tmins, avgTn);
    double avgTx = CollectionUtils.avg(tmaxs);
    double stdvTx = CollectionUtils.stdv(tmaxs, avgTx);

    try {
      repScatter.append("n," + avgTn + "," + avgTx + "\n");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    return String.valueOf("(" + frm.format(avgTn) + ";" + frm.format(avgTx) + ") / (" + frm.format(stdvTn) + ";"
        + frm.format(stdvTx) + ") * " + valsNeurona.size());
  }

  private static Map<Point, List<RegistroDiario>> invert(Map<RegistroDiario, Point> regs) {
    Map<Point, List<RegistroDiario>> rv = new HashMap<Point, List<RegistroDiario>>();
    for (Map.Entry<RegistroDiario, Point> e : regs.entrySet()) {
      List<RegistroDiario> l = rv.get(e.getValue());
      if (l == null) {
        l = new ArrayList<RegistroDiario>();
        rv.put(e.getValue(), l);
      }
      l.add(e.getKey());
    }

    return rv;
  }
}
