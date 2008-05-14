package ar.uba.dcao.dbclima.importacion;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.qc.StationBasedQualityCheck;

public class ConsistTask extends StationBasedQualityCheck {

  private static final long MILLIS_DIA = 24000 * 3600;

  public ConsistTask() {
  }

  @Override
  protected String startingDescription() {
    return "Consisting databases...";
  }
  
  @Override
  protected String progressDescription(int processedStations, int totalStations) {
    return "Consisting station " + processedStations + "/" + totalStations; //TODO consisting o consistencing? 
  }
  
  @Override
  protected String finalDescription(int totalStations) {
    return "Database consisted, " + totalStations + " processed stations.";
  }

  @Override
  protected void processStation(Session sess, Estacion e) {
    Date inicio = (Date) sess.createQuery("SELECT MIN(fecha) FROM RegistroDiario WHERE estacion = ?").setParameter(0, e).uniqueResult();
    Date fin = (Date) sess.createQuery("SELECT MAX(fecha) FROM RegistroDiario WHERE estacion = ?").setParameter(0, e).uniqueResult();

    e.setFechaInicio(inicio);
    e.setFechaFin(fin);

    List<RegistroDiario> registros = e.getRegistros();
    for (int i = 1; i < registros.size(); i++) {
      RegistroDiario hoy = registros.get(i);
      RegistroDiario ayer = registros.get(i - 1);
      if (hoy.getFecha().getTime() - ayer.getFecha().getTime() == MILLIS_DIA) {
        hoy.setAyer(ayer);
        ayer.setManiana(hoy);
      }
    }
  }

  public void updateGUIWhenCompleteSuccessfully() {
  }
  
}
