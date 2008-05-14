package ar.uba.dcao.dbclima.precipitacion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.qc.StationBasedQualityCheck;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Crea un archivo .csv con los resultados de la clasificacion de precipitacion para las estaciones
 * seleccionadas. Asume que la clasificacion ya se realizo y esta persistida en la base de datos.
 * 
 */
public class ReportePrecipitacion extends StationBasedQualityCheck {

  private static class CodigoPrecipitacionExtremaComparator implements Comparator<CodigoPrecipitacionExtrema> {

    public int compare(CodigoPrecipitacionExtrema arg0, CodigoPrecipitacionExtrema arg1) {
      return arg0.toString().compareTo(arg1.toString());
    }

  }

  private static final Map<CodigoPrecipitacionExtrema, Confianza> CODIGO2CONFIANZA = new TreeMap<CodigoPrecipitacionExtrema, Confianza>(
      new CodigoPrecipitacionExtremaComparator());

  static {
    CODIGO2CONFIANZA.put(CodigoPrecipitacionExtrema.POK, Confianza.OK);
    CODIGO2CONFIANZA.put(CodigoPrecipitacionExtrema.P75, Confianza.OK);
    CODIGO2CONFIANZA.put(CodigoPrecipitacionExtrema.PJUMP, Confianza.OK);
    CODIGO2CONFIANZA.put(CodigoPrecipitacionExtrema.P90, Confianza.ERROR);
  }

  private static class DatoPrecipitacion {

    public Estacion estacion;

    public Date fecha;

    @Override
    public String toString() {
      return "Station " + this.estacion.getNombre() + " (OMM: " + this.estacion.getCodigoOMM() + ") Record date: "
          + this.fecha;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((estacion == null) ? 0 : estacion.hashCode());
      result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final DatoPrecipitacion other = (DatoPrecipitacion) obj;
      if (estacion == null) {
        if (other.estacion != null)
          return false;
      } else if (!estacion.equals(other.estacion))
        return false;
      if (fecha == null) {
        if (other.fecha != null)
          return false;
      } else if (!fecha.equals(other.fecha))
        return false;
      return true;
    }

  }
  
  private File fileToWrite;

  private final Map<CodigoPrecipitacionExtrema, List<DatoPrecipitacion>> resultadoCodigo = new TreeMap<CodigoPrecipitacionExtrema, List<DatoPrecipitacion>>(
      new CodigoPrecipitacionExtremaComparator());

  public ReportePrecipitacion(File fileToWrite) {
    this.fileToWrite = fileToWrite;
  }

  @Override
  protected String finalDescription(int totalStations) {
    return Integer.valueOf(totalStations) + " reported";
  }

  @Override
  protected void processStation(Session sess, Estacion station) {
    for (RegistroDiario registro : station.getRegistros()) {
      DatoPrecipitacion datoPrecipitacion = new DatoPrecipitacion();
      datoPrecipitacion.estacion = station;
      datoPrecipitacion.fecha = registro.getFecha();
      String codigo = registro.getCodigoConfianzaPrecip();
      if (codigo == null || codigo.trim().length() == 0) {
        continue;
      } else {
        codigo = codigo.trim();
        sess.evict(registro);

        CodigoPrecipitacionExtrema codigoPrecipitacionExtrema = CodigoPrecipitacionExtrema.valueOf(codigo);
        Confianza confianza = CODIGO2CONFIANZA.get(codigoPrecipitacionExtrema);
        if (confianza == null) {
          System.out.println(codigoPrecipitacionExtrema);
        }

        List<DatoPrecipitacion> registrosActuales = this.resultadoCodigo.get(codigoPrecipitacionExtrema);
        if (registrosActuales == null) {
          registrosActuales = new ArrayList<DatoPrecipitacion>();
          this.resultadoCodigo.put(codigoPrecipitacionExtrema, registrosActuales);
        }
        registrosActuales.add(datoPrecipitacion);
      }
    }
  }

  @Override
  protected String progressDescription(int processedStations, int totalStations) {
    return Integer.valueOf(processedStations) + "station reported.";
  }

  @Override
  protected String startingDescription() {
    return "Reporting stations...";
  }

  private void printReport() throws IOException {
    Set<Integer> meses = new TreeSet<Integer>();
    Set<Integer> anios = new TreeSet<Integer>();

    try {
      for (Map.Entry<CodigoPrecipitacionExtrema, List<DatoPrecipitacion>> entrada : this.resultadoCodigo
          .entrySet()) {
        List<DatoPrecipitacion> registros = entrada.getValue();
        for (DatoPrecipitacion reg : registros) {
          meses.add(FechaHelper.dameMes(reg.fecha));
          anios.add(FechaHelper.dameAnio(reg.fecha));
        }
      }
    } finally {
    }

    PrintWriter printWriter = new PrintWriter(new FileWriter(this.fileToWrite));
    printWriter.print("Label,");
    try {
      for (CodigoPrecipitacionExtrema codigoPrecipitacion : this.resultadoCodigo.keySet()) {
        printWriter.print(codigoPrecipitacion);
        printWriter.print(',');
      }
      printWriter.println();

      printWriter.print("All,");
      for (List<DatoPrecipitacion> registros : this.resultadoCodigo.values()) {
        int size = registros.size();
        printWriter.print(size);
        printWriter.print(',');
      }
      printWriter.println();
      printWriter.println();

      for (Integer mes : meses) {
        printWriter.print(FechaHelper.mes(mes));
        printWriter.print(',');
        for (List<DatoPrecipitacion> registros : this.resultadoCodigo.values()) {
          int size = 0;
          for (DatoPrecipitacion registro : registros) {
            int mesRegistro = FechaHelper.dameMes(registro.fecha);
            if (mesRegistro == mes.intValue()) {
              size++;
            }
          }
          printWriter.print(size);
          printWriter.print(',');
        }
        printWriter.println();
      }
      printWriter.println();

      for (Integer anio : anios) {
        printWriter.print(anio);
        printWriter.print(',');
        for (List<DatoPrecipitacion> registros : this.resultadoCodigo.values()) {
          int size = 0;
          for (DatoPrecipitacion registro : registros) {
            int anioRegistro = FechaHelper.dameAnio(registro.fecha);
            if (anioRegistro == anio.intValue()) {
              size++;
            }
          }
          printWriter.print(size);
          printWriter.print(',');
        }
        printWriter.println();
      }
      printWriter.println();
    } finally {
      printWriter.close();
    }
  }

  public void updateGUIWhenCompleteSuccessfully() {
    try {
      this.printReport();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
