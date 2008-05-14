package ar.uba.dcao.dbclima.qc.qc1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.qc.ListValidator;
import ar.uba.dcao.dbclima.utils.CollectionUtils;

public abstract class DistributionBasedTest implements ListValidator {

  public static final double MAX_DISTANCE_DISTRIB_DEFAULT = 1.5;

  public static final double PERCENTIL_ALTO_REFERENCIA = 0.75;

  public static final double PERCENTIL_BAJO_REFERENCIA = 0.25;

  private boolean checkMin;

  private boolean checkMax;

  private double pLow;

  private double p50;

  private double pHigh;

  private Double maxDistValidaPos;

  private Double maxDistValidaNeg;

  private int registrosMarcados;

  public DistributionBasedTest(boolean checkMin, boolean checkMax, Double maxDistValidaPos, Double maxDistValidaNeg) {
    this.checkMin = checkMin;
    this.checkMax = checkMax;
    this.maxDistValidaPos = maxDistValidaPos;
    this.maxDistValidaNeg = maxDistValidaNeg;
  }

  protected double desviacionDato(ValorRegistro vr) {
    double dato = vr.v;
    double rv = 0;

    if (dato > this.pHigh) {
      rv = (dato - this.pHigh) / (this.pHigh - this.p50);
    } else if (dato < this.pLow) {
      rv = (dato - this.pLow) / (this.p50 - this.pLow);
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

      for (ValorRegistro vr : vrs) {
        double dm = desviacionDato(vr);

        if ((this.checkMax && dm > this.maxDistValidaPos) || (this.checkMin && dm * (-1d) > this.maxDistValidaNeg)) {
          QC1.marcarRegistro(vr.r, this.getNombreTest(), dm);
          this.registrosMarcados++;
        }
      }
    }
  }

  protected abstract List<ValorRegistro> valoresRegistros(List<RegistroDiario> registros);

  protected static class ValorRegistro {
    public ValorRegistro(RegistroDiario r, Double v) {
      this.r = r;
      this.v = v;
    }

    public RegistroDiario r;

    public Double v;
  }
}
