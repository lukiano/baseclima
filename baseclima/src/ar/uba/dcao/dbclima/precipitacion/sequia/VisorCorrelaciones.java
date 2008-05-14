package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import ar.uba.dcao.dbclima.data.CorrelacionNormalizadaEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;

/**
 * Muestra las correlaciones calculadas entre dos estaciones vecinas. Usado para obtener un mayor
 * analisis sobre aquellas estaciones vecinas que no dan buena correlacion.
 * @see CorrelacionesPanel
 *
 */
@SuppressWarnings("serial")
public class VisorCorrelaciones extends JFrame {
  
  public VisorCorrelaciones() {
    this.setTitle("Correlations");
  }
  
  private Map<Estacion, CorrelacionNormalizadaEstaciones> mapa;

  public void setCorrelaciones(Map<Estacion, CorrelacionNormalizadaEstaciones> mapa, Map<Number, Double> precipitacionAnios) {
    this.mapa = mapa;
    JTabbedPane tabbedPane = new JTabbedPane();
    for (Map.Entry<Estacion, CorrelacionNormalizadaEstaciones> entrada : this.mapa.entrySet()) {
      CorrelacionesPanel panel = new CorrelacionesPanel();
      panel.setCorrelacion(entrada.getValue());
      panel.setPrecipitacionAnios(precipitacionAnios);
      JScrollPane scrollPane = new JScrollPane(panel);
      tabbedPane.add(entrada.getKey().toString(), scrollPane);
    }
    this.setSize(tabbedPane.getPreferredSize());
    this.add(tabbedPane);
  }

}
