package ar.uba.dcao.dbclima.casosDeUso;

import java.awt.Point;
import java.util.Map;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.clasificacion.ClasificadorEstacion;
import ar.uba.dcao.dbclima.clasificacion.ClasificadorMinMax;
import ar.uba.dcao.dbclima.clasificacion.SecuenciasRegistros;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

public class SeqClasUseCase {
  public static void main(String[] args) {
//    SequenceClasificator sq = new SequenceClasificator(2, 2, 2);
//    double[][] dataSet = { {2, 0}, {0, 8}, {2, 0}, {2, 5214} };
//    double[][] clasify = sq.train(dataSet, 3000);
//
//    for (double[] d : clasify) {
//      System.out.println(Arrays.toString(d));
//    }

    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(sess);

    Estacion e = estacionDAO.findByID(1L);

    ClasificadorEstacion clasificador = new ClasificadorMinMax(e, 3);
    Map<RegistroDiario, Point> clasif = clasificador.clasificar();

    SecuenciasRegistros sr = new SecuenciasRegistros(clasif);
    sr.init();
    Map<String, Integer> paths = sr.getPaths();

    System.out.print(sr.getPathNumber() + " secuencias");
    System.out.print(" clasificados en " + paths.size() + " categorias.\n\n");

    for (Map.Entry<String, Integer> path : paths.entrySet()) {
      if (path.getValue() > 2) {
        System.out.println(path.getKey() + " con " + path.getValue() + " entradas.");
      }
    }
  }
}
