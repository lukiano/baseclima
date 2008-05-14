package ar.uba.dcao.dbclima.smn.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataValueInf {
  private Map<String, DVDetail> map = new HashMap<String, DVDetail>();

  public void regValue(String nombreLogico, Short val) {
    DVDetail d = map.get(nombreLogico);
    if (d == null) {
      d = new DVDetail();
      map.put(nombreLogico, d);
    }

    Integer i = d.values.get(val);
    if (i == null) {
      i = 0;
    }
    i++;
    d.values.put(val, i);

    if (val != null) {
      d.notNullValues++;
    }

    d.nValues++;
  }

  public void print() {
    for (Map.Entry<String, DVDetail> e : this.map.entrySet()) {
      System.out.println(e.getKey() + " completo con prop. " + e.getValue().notNullValues + "/" + e.getValue().nValues
          + ". Distribucion: <" + getDistribucionRep(e.getValue().values) + ">");
    }
    this.map.clear();
  }

  public static class DVDetail {
    public int notNullValues;

    public int nValues;

    public Map<Short, Integer> values = new HashMap<Short, Integer>();
  }

  private String getDistribucionRep(Map<Short, Integer> map) {
    List<Short> vals = new ArrayList<Short>();
    long suma = 0;
    long cantidad = 0;
    for (Map.Entry<Short, Integer> e : map.entrySet()) {
      if (e.getKey() != null) {
        suma += e.getKey() * e.getValue();
        cantidad += e.getValue();
        for (int i = 0; i < e.getValue(); i++) {
          vals.add(e.getKey());
        }
      }
    }

    Collections.sort(vals);

    double d = (suma / (double) cantidad);
    d = Math.round(d * 100) / 100d;
    String rv = " Media: " + d + ". Mediana: " + vals.get(vals.size() / 2) + " Cuartiles: "
        + vals.get(Math.round(vals.size() * 0.25f)) + " / " + vals.get(Math.round(vals.size() * 0.75f)) + " Extremos: "
        + vals.get(0) + " " + vals.get(vals.size() - 1);

    return rv;
  }
}
