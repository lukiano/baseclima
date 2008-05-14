package ar.uba.dcao.dbclima.eventosMeteo;

import ar.uba.dcao.dbclima.data.ConfianzaVariable;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;

public final class ProyectoresConfiables {

  public static ProyectorRegistro buildProyectorTMinConfiable(final byte maxDesconf) {
    return new ProyectorRegistro() {
      public Integer getValor(RegistroDiario rd) {
        ConfianzaVariable confianza = rd.getConfianzaTempMin();
        Integer rv;
        if (confianza != null && confianza.getConfianza() > maxDesconf) {
          rv = null;
        } else {
          rv = ProyectorRegistro.PROY_TMIN.getValor(rd);
        }

        return rv;
      }

      public String nombreVariable() {
        return ProyectorRegistro.PROY_TMIN.nombreVariable();
      }
    };
  }

  public static ProyectorRegistro buildProyectorTMaxConfiable(final byte maxDesconf) {
    return new ProyectorRegistro() {
      public Integer getValor(RegistroDiario rd) {
        ConfianzaVariable confianza = rd.getConfianzaTempMax();
        Integer rv;
        if (confianza != null && confianza.getConfianza() > maxDesconf) {
          rv = null;
        } else {
          rv = ProyectorRegistro.PROY_TMAX.getValor(rd);
        }

        return rv;
      }

      public String nombreVariable() {
        return ProyectorRegistro.PROY_TMAX.nombreVariable();
      }
    };
  }
}
