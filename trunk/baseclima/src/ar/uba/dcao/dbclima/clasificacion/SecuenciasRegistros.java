package ar.uba.dcao.dbclima.clasificacion;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ar.uba.dcao.dbclima.data.RegistroDiario;

/**
 * Dado un mapeo de RegistoDiario -> Serializable, una secuencia de registros consecutivos
 * esta definida por la secuencia de serializables que se obtienen de estos.
 */
public class SecuenciasRegistros {

  private static final int PATH_LENGTH = 2;

  private final Map<RegistroDiario, Point> mapeo;

  private int pathNumber;

  private Map<String, Integer> paths;

  public SecuenciasRegistros(Map<RegistroDiario, Point> mapeo) {
    this.mapeo = mapeo;
  }

  public void init() {
    this.pathNumber = 0;
    this.paths = new HashMap<String, Integer>();
    for (RegistroDiario rd : mapeo.keySet()) {
      String pattern = getPattern(rd, PATH_LENGTH);

      if (pattern != null) {
        Integer patternCount = this.paths.get(pattern);

        if (patternCount == null) {
          this.paths.put(pattern, 1);
        } else {
          this.paths.put(pattern, patternCount + 1);
        }

        this.pathNumber++;
      }
    }
  }

  private String getPattern(RegistroDiario rd, int length) {
    String rv = "";

    for (int i = 0; i < length; i++) {
      Serializable value = this.mapeo.get(rd);
      if (rd == null || value == null) {
        rv = null;
        break;
      }

      rv += value + ",";
      rd = rd.getManiana();
    }

    return rv == null ? rv : rv.substring(0, rv.length() - 1);
  }

  public Map<String, Integer> getPaths() {
    return paths;
  }

  public int getPathNumber() {
    return pathNumber;
  }
}
