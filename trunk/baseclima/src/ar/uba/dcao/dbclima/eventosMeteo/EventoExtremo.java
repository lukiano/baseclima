package ar.uba.dcao.dbclima.eventosMeteo;

import java.util.Calendar;
import java.util.Date;

public class EventoExtremo {

  private static final Calendar CAL = Calendar.getInstance();

  private final Date fecha;

  private final int registrosUltraEvento;

  private final int diasUltraExtremo;

  private final boolean isPrimerEvento;

  public EventoExtremo(Date fecha, int registrosUltraEvento, int diasUltraExtremo, boolean esPrimerEvento) {
    this.fecha = fecha;
    this.registrosUltraEvento = registrosUltraEvento;
    this.diasUltraExtremo = diasUltraExtremo;
    this.isPrimerEvento = esPrimerEvento;
  }

  public Date getFecha() {
    return fecha;
  }

  public int getDiaDelAnio() {
    CAL.setTime(this.fecha);
    return CAL.get(Calendar.DAY_OF_YEAR);
  }

  public int getRegistrosUltraEvento() {
    return registrosUltraEvento;
  }

  public int getDiasUltraEvento() {
    return diasUltraExtremo;
  }

  public boolean isPrimerEvento() {
    return isPrimerEvento;
  }

  public double getSoporte() {
    return this.registrosUltraEvento / (double) this.diasUltraExtremo;
  }
}
