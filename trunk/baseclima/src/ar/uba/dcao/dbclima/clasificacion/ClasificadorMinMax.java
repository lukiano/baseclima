package ar.uba.dcao.dbclima.clasificacion;

import java.util.Calendar;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;

public class ClasificadorMinMax extends ClasificadorEstacion {

  private CicloEstacional cicloMin;

  private CicloEstacional cicloMax;

  private Calendar cal = Calendar.getInstance();

  private int mes;

  public ClasificadorMinMax(Estacion e, int mes) {
    super(e);
    this.mes = mes;
    this.cicloMin = new CicloEstacional(e, ProyectorRegistro.PROY_TMIN);
    this.cicloMax = new CicloEstacional(e, ProyectorRegistro.PROY_TMAX);
  }

  @Override
  protected double[] getVectorCaracts(RegistroDiario rd) {
    double[] rv = { cicloMin.getValorNormalizado(rd), cicloMax.getValorNormalizado(rd) };
    return rv;
  }

  @Override
  protected int getNumeroCaracts() {
    return 2;
  }

  @Override
  protected boolean registroAplica(RegistroDiario rd) {
    cal.setTime(rd.getFecha());
    return cal.get(Calendar.MONTH) == mes && rd.getTempMax() != null && rd.getTempMin() != null;
  }
}
