package ar.uba.dcao.dbclima.persistence;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.parse.ParseProblem;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;

public class PrePersistanceChecker {

  private Map<String, Date> fechas = new HashMap<String, Date>();

  private ParseProblemLog log;

  public PrePersistanceChecker(ParseProblemLog log) {
    this.log = log;
  }

  public void validarProximo(RegistroDiario rd) {
    String id = rd.getEstacion().getId().toString();

    Date ud = fechas.get(id);

    if (ud != null && rd.getFecha() != null && rd.getFecha().before(ud)) {
      this.log.logException(new ParseProblem("Se encontro " + rd.getFecha() + " despues de " + ud + " para la estacion " + rd.getEstacion().getId(), "Fecha", "Secuencialidad registros"));
    } else {
      fechas.put(id, rd.getFecha());
    }

    Short tempMax = rd.getTempMax();
    Short tempMin = rd.getTempMin();

    // Chequeos temperatura
    if (tempMax != null && tempMin != null && tempMax < tempMin) {
      log.logException(new ParseProblem(rd.toString(), "Amplitud termica", String.valueOf(tempMax - tempMin)));
    }

    // Chequeos precipitacion
    //Integer precip = rd.getPrecipitacion();
  }
}
