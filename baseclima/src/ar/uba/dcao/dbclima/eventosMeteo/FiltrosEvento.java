package ar.uba.dcao.dbclima.eventosMeteo;

public final class FiltrosEvento {

  public static FiltroEvento buildFiltroPorTope(final int tope, final boolean nullEsEvento,
      final boolean topeEsSuperior) {
    return new FiltroEvento() {

      public boolean isEvento(Integer val) {
        int sentido = topeEsSuperior ? 1 : -1;
        return (val != null || nullEsEvento) && (val == null || (tope - val) * sentido >= 0);
      }
    };
  }
}
