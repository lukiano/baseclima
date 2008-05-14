package ar.uba.dcao.dbclima.qc.resolucion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModeloResolucion {

  private static final DecimalFormat frmt = new DecimalFormat("0.00");

  public static final double MAX_PROP_AJUSTE = 1.5d;

  private static final double MAX_DESV_MODELO = 0.5d;

  private static final int MIN_SIZE_MUESTRA_VALIDA = 80;

  private static final Comparator<AjusteModelo> cmprFit = new Comparator<AjusteModelo>() {
    public int compare(AjusteModelo o1, AjusteModelo o2) {
      return (int) Math.signum(o1.fit - o2.fit);
    }
  };

  /** Nombres de los modelos que ajustan bien a la distribucion. */
  private final List<String> relevantes = new ArrayList<String>();

  /** Detalle de como ajusta la distribucion a los distintos modelos. */
  private Map<String, AjusteModelo> modelos = new HashMap<String, AjusteModelo>();

  private String toString;

  private final int sizeMuestra;

  public ModeloResolucion(Collection<AjusteModelo> results, int sizeMuestra) {
    this.sizeMuestra = sizeMuestra;

    List<AjusteModelo> mdlsOrd = new ArrayList<AjusteModelo>(results);
    Collections.sort(mdlsOrd, cmprFit);

    double base = mdlsOrd.get(0).fit;

    for (AjusteModelo a : mdlsOrd) {
      this.modelos.put(a.modelo, a);

      if (a.fit <= base * MAX_PROP_AJUSTE && a.fit < MAX_DESV_MODELO) {
        this.relevantes.add(a.modelo);
      }
    }
  }

  @Override
  public String toString() {
    if (this.toString == null) {
      StringBuilder rvB = new StringBuilder();

      for (String nomModelo : this.relevantes) {
        AjusteModelo a = this.modelos.get(nomModelo);
        rvB.append(nomModelo + "(" + frmt.format(a.fit) + ") / ");
      }

      this.toString = (rvB.length() > 0) ? rvB.substring(0, rvB.length() - 3) : rvB.toString();

      if (this.sizeMuestra < MIN_SIZE_MUESTRA_VALIDA) {
        this.toString = "*(" + this.sizeMuestra + ") " + this.toString;
      }
    }

    return this.toString;
  }

  public boolean similar(ModeloResolucion otroModelo) {
    Set<String> modelosNomb = new HashSet<String>(this.relevantes);
    modelosNomb.addAll(otroModelo.relevantes);

    boolean rv = true;
    for (String mod : modelosNomb) {
      AjusteModelo a1 = this.modelos.get(mod);
      AjusteModelo a2 = otroModelo.modelos.get(mod);
      rv = (a1 != null && a2 != null && Math.abs(a1.fit - a2.fit) < 0.1);
    }

    return rv;
  }

  public static class AjusteModelo {

    public double fit;

    public String modelo;

    public AjusteModelo(double fit, String modelo) {
      this.fit = fit;
      this.modelo = modelo;
    }

    @Override
    public int hashCode() {
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + ((modelo == null) ? 0 : modelo.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final AjusteModelo other = (AjusteModelo) obj;
      if (modelo == null) {
        if (other.modelo != null)
          return false;
      } else if (!modelo.equals(other.modelo))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return modelo + " (" + frmt.format(fit) + ")";
    }
  }
}
