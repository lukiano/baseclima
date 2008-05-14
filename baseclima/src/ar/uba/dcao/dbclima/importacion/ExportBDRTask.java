package ar.uba.dcao.dbclima.importacion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.AbstractTask;
import ar.uba.dcao.dbclima.concurrencia.TaskResult;
import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.dao.EstacionDAO;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Tarea que exporta los contenidos de los datasets de referencia en un archivo de texto compatible con la importacion 
 * (en alguna otra base de datos).
 *
 */
public class ExportBDRTask extends AbstractTask {

  private static SimpleDateFormat frmt = (SimpleDateFormat) SimpleDateFormat.getInstance();

  private final File fileToWrite;

  public ExportBDRTask(File fileToWrite) {
    this.fileToWrite = fileToWrite;
    frmt.applyPattern("dd/MM/yyyy");
  }

  public void doExport(SessionFactory runningFactory) throws IOException {
    List<Estacion> estaciones = this.obtenerEstacionesBDR(runningFactory);
    PrintWriter out = new PrintWriter(new FileWriter(this.fileToWrite));
    try {
      for (int i = 0; i < estaciones.size(); i++) {
        Estacion estacion = estaciones.get(i);
        this.setProgressDescription("Exporting reference station (" + (i+1) + '/' + estaciones.size() + ')');
        Session session = runningFactory.getCurrentSession();
        try {
          session.beginTransaction();
          estacion = (Estacion) session.merge(estacion);
          this.doExport(estacion, out);
        } finally {
          session.close();
        }
        this.setCompletionState((double)i / (double) estaciones.size());
      }
    } finally {
      out.close();
    }
  }

  private List<Estacion> obtenerEstacionesBDR(SessionFactory runningFactory) {
    Session session = runningFactory.getCurrentSession();
    try {
      session.beginTransaction();
      EstacionDAO estacionDAO = DAOFactory.getEstacionDAO(session);
      List<Estacion> estaciones = estacionDAO.findAllForBDR();
      return estaciones;
    } finally {
      session.close();
    }
  }
  
  public void doExport(Estacion e, PrintWriter out) {
    CharSequence estDesc = this.getDescFromEstacion(e);
    for (RegistroDiario rd : e.getRegistros()) {
      out.print(estDesc);
      out.print(',');
      out.println(getDescFromRegistro(rd));
    }
  }

  private CharSequence getDescFromEstacion(Estacion e) {
    StringBuilder stringBuilder = new StringBuilder();
    if (e.getCodigoPais() != null) {
      stringBuilder.append(e.getCodigoPais());  
    }
    stringBuilder.append(',');
    if (e.getCodigoOMM() != null) {
      String codigoOMM = e.getCodigoOMM().toString();
      if (codigoOMM.length() > 3) {
        codigoOMM = codigoOMM.substring(codigoOMM.length() - 3);
      }
      stringBuilder.append(codigoOMM);
    }
    stringBuilder.append(',');
    if (e.getCodigoSMN() != null) {
      stringBuilder.append(e.getCodigoSMN());  
    }
    stringBuilder.append(',');
    if (e.getProvincia() != null) {
      stringBuilder.append(e.getProvincia());  
    }
    stringBuilder.append(',');
    if (e.getNombre() != null) {
      stringBuilder.append(e.getNombre());  
    }
    stringBuilder.append(',');
    stringBuilder.append(e.getLatitud());
    stringBuilder.append(',');
    stringBuilder.append(e.getLongitud());
    stringBuilder.append(',');
    stringBuilder.append(e.getAltura());
    return stringBuilder;
  }

  private CharSequence getDescFromRegistro(RegistroDiario rd) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(frmt.format(rd.getFecha()));
    stringBuilder.append(',');
    if (rd.getTempMin() != null) {
      stringBuilder.append(rd.getTempMin());
    }
    stringBuilder.append(',');
    if (rd.getTempMax() != null) {
      stringBuilder.append(rd.getTempMax());
    }
    stringBuilder.append(',');
    if (rd.getPrecipitacion() != null) {
      stringBuilder.append(rd.getPrecipitacion());
    }
    return stringBuilder;
  }

  public boolean run(SessionFactory runningFactory) {
    this.setProgressDescription("Exporting reference datasets");
    this.setCompletionState(0);
    this.setComplete(false);
    try {
      this.doExport(runningFactory);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    this.setCompletionState(1);
    this.setProgressDescription("Reference datasets successfully exported");
    this.setComplete(true);
    this.setResult(TaskResult.buildSuccessfulResult(this.getProgressDescription()));
    return true;
  }

  public void updateGUIWhenCompleteSuccessfully() {
  }
}
