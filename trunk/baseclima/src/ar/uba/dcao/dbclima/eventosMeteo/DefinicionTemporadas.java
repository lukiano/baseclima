package ar.uba.dcao.dbclima.eventosMeteo;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class DefinicionTemporadas {

  private static final Calendar CAL = Calendar.getInstance();

  private static final int DIAS_ANIO = 366;

  private final int primerDiaTemporada;

  private final int ultimoDiaTemporada;

  private final Comparator<Integer> comparadorDias;

  private final Comparator<Double> comparadorDiasDouble;

  public DefinicionTemporadas(int primerDiaTemporada, int ultimoDiaTemporada) {
    this.primerDiaTemporada = primerDiaTemporada;
    this.ultimoDiaTemporada = ultimoDiaTemporada;

    this.comparadorDias = new Comparator<Integer>() {
      public int compare(Integer o1, Integer o2) {
        int diaO1 = DefinicionTemporadas.this.largoRango(DefinicionTemporadas.this.primerDiaTemporada, o1
            .intValue());
        int diaO2 = DefinicionTemporadas.this.largoRango(DefinicionTemporadas.this.primerDiaTemporada, o2
            .intValue());

        return diaO1 - diaO2;
      }
    };

    this.comparadorDiasDouble = new Comparator<Double>() {
      public int compare(Double o1, Double o2) {
        return DefinicionTemporadas.this.comparadorDias.compare(o1.intValue(), o2.intValue());
      }
    };
  }

  public int getPrimerDiaTemporada() {
    return primerDiaTemporada;
  }

  public int getUltimoDiaTemporada() {
    return ultimoDiaTemporada;
  }

  public int getDuracionTemporadas() {
    return this.ultimoDiaTemporada - this.primerDiaTemporada + 1;
  }

  public Integer getTemporada(Date fecha) {
    CAL.setTime(fecha);
    int diaDelAnio = CAL.get(Calendar.DAY_OF_YEAR);

    Integer rv;
    if (primerDiaTemporada < ultimoDiaTemporada && primerDiaTemporada <= diaDelAnio
        && diaDelAnio <= ultimoDiaTemporada) {
      rv = CAL.get(Calendar.YEAR);
    } else if (primerDiaTemporada > ultimoDiaTemporada && primerDiaTemporada <= diaDelAnio) {
      rv = CAL.get(Calendar.YEAR);
    } else if (primerDiaTemporada > ultimoDiaTemporada && diaDelAnio <= ultimoDiaTemporada) {
      rv = CAL.get(Calendar.YEAR) - 1;
    } else {
      rv = null;
    }

    return rv;
  }

  public int diaTemporada(Date fecha) {
    CAL.setTime(fecha);
    return largoRango(this.primerDiaTemporada, CAL.get(Calendar.DAY_OF_YEAR));
  }

  public Comparator<Integer> getComparadorDia() {
    return this.comparadorDias;
  }

  public Comparator<Double> getComparadorDiaAsDouble() {
    return this.comparadorDiasDouble;
  }

  private int largoRango(int diaAnioDesde, int diaAnioHasta) {
    int rv;

    if (diaAnioDesde > diaAnioHasta) {
      rv = diaAnioHasta + DIAS_ANIO - diaAnioDesde;
    } else {
      rv = diaAnioHasta - diaAnioDesde;
    }

    return rv;
  }
}
