package ar.uba.dcao.dbclima.utils;

import java.text.DecimalFormat;

public final class DobleHelper {

  /**
   * Clase estatica con metodos para la visualizacion de valores de punto flotante con doble precision.
   */
  private DobleHelper() {  }

  public static String doble2String(double d) {
    if (Double.isNaN(d)) {
      return "NaN";
    }
    DecimalFormat myFormatter = new DecimalFormat("0.0000");
    return myFormatter.format(d); // redondea automaticamente
    /*
     * String string = String.valueOf(d); int dot = string.indexOf('.'); if (dot > 0 &&
     * dot < string.length() - 3) { string = string.substring(0, dot + 3); } return
     * string;
     */

  }

  public static String doble2String(Double d) {
    if (d.isNaN()) {
      return "NaN";
    }
    DecimalFormat myFormatter = new DecimalFormat("0.0000");
    return myFormatter.format(d); // redondea automaticamente
    /*
     * String string = d.toString(); int dot = string.indexOf('.'); if (dot > 0 && dot <
     * string.length() - 3) { string = string.substring(0, dot + 3); } return string;
     */
  }

}
