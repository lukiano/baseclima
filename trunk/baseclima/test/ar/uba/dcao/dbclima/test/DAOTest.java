package ar.uba.dcao.dbclima.test;

import java.util.List;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;

public class DAOTest extends ConnectedTestCase {

  public void testEstacionDAO() {
    List<Estacion> e = DAOFactory.getEstacionDAO(this.getSession()).findNotQC1d(true);
    System.out.println(e.size());
  }
}
