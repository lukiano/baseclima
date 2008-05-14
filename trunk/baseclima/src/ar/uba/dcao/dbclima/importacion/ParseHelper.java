package ar.uba.dcao.dbclima.importacion;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Clase con metodos de ayuda utiles para la importacion y analisis de los archivos de texto de entrada.
 *
 */
public class ParseHelper {

  private static final BigDecimal bTen = BigDecimal.TEN;
  
  private static final BigDecimal bHundred = BigDecimal.valueOf(100);

  private static final Object TEMP_FALTANTE = "0-  ";

  private static final Object TEMP_EXT_FALTANTE = "    ";

  public static Short parseTemp(String asciiReg, int from, String nombreLogico) throws ParseException {
    String tStr = asciiReg.substring(from, from + 4);

    if (tStr.equals(TEMP_FALTANTE) || tStr.equals(TEMP_EXT_FALTANTE)) {
      return null;
    }

    if (tStr.charAt(0) == '&') {
      tStr = tStr.replace('&', '-');
    }

    try {
      return Short.parseShort(tStr);
    } catch (NumberFormatException e) {
      throw new ParseException(e);
    }
  }

  public static Short parseTenthShort(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante) throws ParseException {
    BigDecimal rvD = ParseHelper.parseDecimal(asciiReg, from, to, nombreLogico, repDatoFaltante);
    return (rvD == null) ? null : rvD.multiply(bTen).shortValue();
  }

  public static Integer parseTenthInteger(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante) throws ParseException {
    BigDecimal rvD = ParseHelper.parseDecimal(asciiReg, from, to, nombreLogico, repDatoFaltante);
    return (rvD == null) ? null : rvD.multiply(bTen).intValue();
  }

  public static Short parseHundredthShort(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante) throws ParseException {
    BigDecimal rvD = ParseHelper.parseDecimal(asciiReg, from, to, nombreLogico, repDatoFaltante);
    return (rvD == null) ? null : rvD.multiply(bHundred).shortValue();
  }

  public static Integer parseHundredthInteger(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante) throws ParseException {
    BigDecimal rvD = ParseHelper.parseDecimal(asciiReg, from, to, nombreLogico, repDatoFaltante);
    return (rvD == null) ? null : rvD.multiply(bHundred).intValue();
  }

  public static BigDecimal parseDecimal(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante) throws ParseException {
    String tStr = asciiReg.substring(from, to).trim();
    BigDecimal rv = null;
    
    if (tStr.length() != 0 && (repDatoFaltante == null || !repDatoFaltante.contains(tStr))) {
      try {
        Double dVal = Double.parseDouble(tStr);
        rv = BigDecimal.valueOf(dVal);
      } catch (NumberFormatException e) {
        throw new ParseException(e);
      }
    }

    return rv;
  }

  public static Boolean parseBoolean(String asciiReg, int from, String nombreLogico, Set<String> repDatoFaltante) throws ParseException {
    Short val = parseShort(asciiReg, from, from + 1, nombreLogico, repDatoFaltante);
    Boolean rv = null;
    if (val != null) {
      if (val.equals((short) 1)) {
        rv = true;
      } else if (val.equals((short) 0)) {
        rv = false;
      } else {
        throw new ParseException("Can't convert value to boolean.");
      }
    }

    return rv;
  }

  public static Short parseShort(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante) throws ParseException {
    String tStr = asciiReg.substring(from, to).trim();
    Short rv = null;
    if (tStr.length() != 0 && (repDatoFaltante == null || !repDatoFaltante.contains(tStr))) {
      try {
        rv = Short.parseShort(tStr);
      } catch (NumberFormatException e) {
        throw new ParseException(e);
      }
    }

    return rv;
  }

  public static Integer parseInteger(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante) throws ParseException {
    String tStr = asciiReg.substring(from, to).trim();
    Integer rv = null;
    if (tStr.length() != 0 && (repDatoFaltante == null || !repDatoFaltante.contains(tStr))) {
      try {
        rv = Integer.parseInt(tStr);
      } catch (NumberFormatException e) {
        throw new ParseException(e);
      }
    }

    return rv;
  }

  public static Date parseDate(String asciiReg, int des, int has, SimpleDateFormat dateFormatter, String nombreLogico) throws ParseException {
    String substring = asciiReg.substring(des, has);
    try {
      return dateFormatter.parse(substring);
    } catch (java.text.ParseException e) {
      throw new ParseException(e);
    }
  }

}
