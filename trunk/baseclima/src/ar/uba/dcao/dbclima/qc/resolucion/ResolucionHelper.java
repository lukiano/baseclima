package ar.uba.dcao.dbclima.qc.resolucion;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.RegistroHelper;

public class ResolucionHelper {

  private static DateFormat dateFrmt = new SimpleDateFormat("dd/MM/yyyy");

  private static DecimalFormat frmt = new DecimalFormat("00.0");

  public static void definirResolucionPeriodo(List<List<RegistroDiario>> regs, ProyectorRegistro proy) {

    ResolucionMerger merger = new ResolucionMerger(proy);
    int merges = merger.merge(regs);

    System.out.println(merges + "--> " + regs.size());
    for (List<RegistroDiario> lr : regs) {
      String dFrom = dateFrmt.format(lr.get(0).getFecha());
      String dTill = dateFrmt.format(lr.get(lr.size() - 1).getFecha());

      double[] fd = frecuenciasDecimales(lr, proy);

      ModeloResolucion modelo = FitResolucion.fit(fd, RegistroHelper.regsConValor(lr, proy));

      System.out.print(modelo + "[" + dFrom + " - " + dTill + "] " + dsp(fd) + "\n");
    }
    System.out.println();
  }

  public static List<Integer> digitosDecimales(List<Integer> valores) {
    List<Integer> rv = new ArrayList<Integer>();
    for (Integer v : valores) {
      rv.add(digitoDecimal(v));
    }
    return rv;
  }

  public static int digitoDecimal(int valor) {
    return valor % 10;
  }

  public static Integer definirResolucionRegistro(RegistroDiario rd, ProyectorRegistro proy, int cantVecinos) {
    List<RegistroDiario> regs = new ArrayList<RegistroDiario>();
    regs.add(rd);

    RegistroDiario r = rd;
    int i = 0;

    while (r.getAyer() != null && i++ < cantVecinos) {
      r = r.getAyer();
      regs.add(r);
    }

    r = rd;
    i = 0;
    while (r.getManiana() != null && i++ < cantVecinos) {
      r = r.getManiana();
      regs.add(r);
    }

    return definirResolucionColeccion(regs, proy);
  }

  public static String dsp(double[] frecuencias) {
    String wrnDisplay = "";

    for (int i = 0; i < frecuencias.length; i++) {
      double frec = frecuencias[i];
      wrnDisplay += "|" + i + ": " + frmt.format(frec * 100) + " ";
    }

    return wrnDisplay;
  }

  public static Integer definirResolucionColeccion(Collection<RegistroDiario> regs, ProyectorRegistro proy) {
    Integer minResol = 11;

    for (RegistroDiario rd : regs) {
      Integer val = proy.getValor(rd);
      Integer resol = definirResolucionValor(val);
      if (resol != null) {
        minResol = Math.min(resol, minResol);
      }
    }

    return minResol > 10 ? null : minResol;
  }

  public static int[] acumuladosDecimales(List<RegistroDiario> regs, ProyectorRegistro proyector) {
    int[] counts = new int[10];
    int noNulos = 0;

    for (RegistroDiario rd : regs) {
      Integer val = proyector.getValor(rd);
      if (val != null) {
        noNulos++;
        /* Agrego un valor grande para evitar problemas con los numeros negativos al usar %. */
        int pos = (val + 1000) % 10;
        counts[pos]++;
      }
    }

    return counts;
  }

  public static double[] frecuenciasDecimales(List<RegistroDiario> regs, ProyectorRegistro proyector) {
    int[] counts = new int[10];
    double[] rv = new double[10];
    int noNulos = 0;

    for (RegistroDiario rd : regs) {
      Integer val = proyector.getValor(rd);
      if (val != null) {
        noNulos++;
        /* Agrego un valor grande para evitar problemas con los numeros negativos. */
        int pos = (val + 1000) % 10;
        counts[pos]++;
      }
    }

    for (int i = 0; i < counts.length; i++) {
      rv[i] = counts[i] / (double) noNulos;
    }

    return rv;
  }

  public static Integer definirResolucionValor(Integer valor) {
    if (valor == null) {
      return null;
    }

    int resT = valor.shortValue() % 10;

    if (resT == 0) {
      return 10;
    } else if (resT == 5) {
      return 5;
    } else {
      return 1;
    }
  }
}