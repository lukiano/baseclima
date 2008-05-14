package ar.uba.dcao.dbclima.data;

/**
 * Los proyectores de registros saben obtener un valor de un registro.
 */
public interface ProyectorRegistro {

  Integer getValor(RegistroDiario rd);

  String nombreVariable();

  ProyectorRegistro PROY_TMIN = new ProyectorRegistro() {
    public Integer getValor(RegistroDiario rd) {
      return rd.getTempMin() == null ? null : rd.getTempMin().intValue();
    }

    public String nombreVariable() {
      return "Tn";
    }
  };

  ProyectorRegistro PROY_TMAX = new ProyectorRegistro() {
    public Integer getValor(RegistroDiario rd) {
      return rd.getTempMax() == null ? null : rd.getTempMax().intValue();
    }

    public String nombreVariable() {
      return "Tx";
    }
  };

  ProyectorRegistro PROY_TRANGE = new ProyectorRegistro() {
    public Integer getValor(RegistroDiario rd) {
      return (rd.getTempMax() == null || rd.getTempMin() == null) ? null : ((int) rd.getTempMax() - rd.getTempMin());
    }

    public String nombreVariable() {
      return "Tr";
    }
  };
}
