package ar.uba.dcao.dbclima.qc.qc1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.qc.ListValidator;
import ar.uba.dcao.dbclima.utils.CollectionUtils;

public class TempRangeTest implements ListValidator {

  public static final double MAX_DISTANCE_DISTRIB_DEFAULT = 1.5;

  public static final double PERCENTIL_ALTO_REFERENCIA = 0.75d;

  public static final double PERCENTIL_BAJO_REFERENCIA = 0.25d;

  private boolean checkMin;

  private boolean checkMax;

  private double pLow;

  private double p50;

  private double pHigh;

  private Double maxDistValidaPos;

  private Double maxDistValidaNeg;

  private int registrosMarcados;

  private double pZero;

  public TempRangeTest(boolean checkMin, boolean checkMax, Double maxDistValidaPos, Double maxDistValidaNeg) {
    this.checkMin = checkMin;
    this.checkMax = checkMax;
    this.maxDistValidaPos = maxDistValidaPos;
    this.maxDistValidaNeg = maxDistValidaNeg;
  }

  private double desviacionDato(double valor) {
    double rv = 0;

    if (valor > this.pHigh) {
      rv = (valor - this.pHigh) / (this.pHigh - this.p50);
    } else if (valor < this.pLow) {
      rv = (valor - this.pLow) / (this.p50 - this.pLow);
    }

    return rv;
  }

  public final void validate(List<RegistroDiario> regs) {

    if (regs.size() > 0) {
      List<ValorRegistro> vrs = this.valoresRegistros(regs);
      List<Double> valsOrd = new ArrayList<Double>();

      for (ValorRegistro rv : vrs) {
        valsOrd.add(rv.v);
      }

      Collections.sort(valsOrd);

      int size = valsOrd.size();

      this.pLow = CollectionUtils.percentilOrderedList(valsOrd, PERCENTIL_BAJO_REFERENCIA);
      this.p50 = valsOrd.get(size / 2);
      this.pHigh = CollectionUtils.percentilOrderedList(valsOrd, PERCENTIL_ALTO_REFERENCIA);

      this.pZero = desviacionDato(0);

      for (ValorRegistro vr : vrs) {
        double dm = desviacionDato(vr.v);

        if ((this.checkMax && dm > this.maxDistValidaPos) || (this.checkMin && dm * (-1d) > this.maxDistValidaNeg)) {
          QC1.marcarRegistro(vr.r, this.getNombreTest(), dm);
          this.registrosMarcados++;
        }

        if (dm / this.pZero > 0.5) {
          QC1.marcarRegistro(vr.r, this.getNombreTestBajaAmplitud(), dm / this.pZero);
        }
      }
    }
  }

  private List<ValorRegistro> valoresRegistros(List<RegistroDiario> registros) {
    List<ValorRegistro> rv = new ArrayList<ValorRegistro>();

    for (RegistroDiario r : registros) {
      Integer vi = getProyector().getValor(r);

      if (vi != null) {
        rv.add(new ValorRegistro(r, vi.doubleValue()));
      }
    }

    return rv;
  }

  protected static class ValorRegistro {
    public ValorRegistro(RegistroDiario r, Double v) {
      this.r = r;
      this.v = v;
    }

    public RegistroDiario r;

    public Double v;
  }

  public String getNombreTestBajaAmplitud() {
    return "OUTL_Tl";
  }

  public String getNombreTest() {
    return "OUTL_Tr";
  }

  public ProyectorRegistro getProyector() {
    return ProyectorRegistro.PROY_TRANGE;
  }
}
