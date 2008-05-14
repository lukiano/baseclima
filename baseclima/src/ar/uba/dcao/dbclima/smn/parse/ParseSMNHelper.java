package ar.uba.dcao.dbclima.smn.parse;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import ar.uba.dcao.dbclima.parse.ParseProblem;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;

public class ParseSMNHelper {

  private static final BigDecimal bTen = BigDecimal.TEN;
  
  private static final BigDecimal bHundred = BigDecimal.valueOf(100);

  private static final Object TEMP_FALTANTE = "0-  ";

  private static final Object TEMP_EXT_FALTANTE = "    ";

  public static Short parseTemp(String asciiReg, int from, String nombreLogico, ParseProblemLog log) {
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
      log.logException(new ParseProblem(asciiReg, nombreLogico, tStr));
      return null;
    }
  }

  public static Short parseTenthShort(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante, ParseProblemLog log) {
    BigDecimal rvD = ParseSMNHelper.parseDecimal(asciiReg, from, to, nombreLogico, repDatoFaltante, log);
    return (rvD == null) ? null : rvD.multiply(bTen).shortValue();
  }

  public static Integer parseTenthInteger(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante, ParseProblemLog log) {
    BigDecimal rvD = ParseSMNHelper.parseDecimal(asciiReg, from, to, nombreLogico, repDatoFaltante, log);
    return (rvD == null) ? null : rvD.multiply(bTen).intValue();
  }

  public static Short parseHundredthShort(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante, ParseProblemLog log) {
    BigDecimal rvD = ParseSMNHelper.parseDecimal(asciiReg, from, to, nombreLogico, repDatoFaltante, log);
    return (rvD == null) ? null : rvD.multiply(bHundred).shortValue();
  }

  public static Integer parseHundredthInteger(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante, ParseProblemLog log) {
    BigDecimal rvD = ParseSMNHelper.parseDecimal(asciiReg, from, to, nombreLogico, repDatoFaltante, log);
    return (rvD == null) ? null : rvD.multiply(bHundred).intValue();
  }

  public static BigDecimal parseDecimal(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante, ParseProblemLog log) {
    String tStr = asciiReg.substring(from, to).trim();
    BigDecimal rv = null;
    
    if (tStr.length() != 0 && (repDatoFaltante == null || !repDatoFaltante.contains(tStr))) {
      try {
        Double dVal = Double.parseDouble(tStr);
        rv = BigDecimal.valueOf(dVal);
      } catch (NumberFormatException e) {
        log.logException(new ParseProblem(asciiReg, nombreLogico, tStr));
      }
    }

    return rv;
  }

  public static Boolean parseBoolean(String asciiReg, int from, String nombreLogico, Set<String> repDatoFaltante, ParseProblemLog log) {
    Short val = parseShort(asciiReg, from, from + 1, nombreLogico, repDatoFaltante, log);
    Boolean rv = null;
    if (val != null) {
      if (val.equals((short) 1)) {
        rv = true;
      } else if (val.equals((short) 0)) {
        rv = false;
      } else {
        log.logException(new ParseProblem(asciiReg, nombreLogico, asciiReg.substring(from, from+1)));
      }
    }

    return rv;
  }

  public static Short parseShort(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante, ParseProblemLog log) {
    String tStr = asciiReg.substring(from, to).trim();
    Short rv = null;
    if (tStr.length() != 0 && (repDatoFaltante == null || !repDatoFaltante.contains(tStr))) {
      try {
        rv = Short.parseShort(tStr);
      } catch (NumberFormatException e) {
        // inf.regValue(nombreLogico, rv);
        log.logException(new ParseProblem(asciiReg, nombreLogico, tStr));
      }
    }

    // inf.regValue(nombreLogico, rv);
    return rv;
  }

  public static Integer parseInteger(String asciiReg, int from, int to, String nombreLogico, Set<String> repDatoFaltante, ParseProblemLog log) {
    String tStr = asciiReg.substring(from, to).trim();
    Integer rv = null;
    if (tStr.length() != 0 && (repDatoFaltante == null || !repDatoFaltante.contains(tStr))) {
      try {
        rv = Integer.parseInt(tStr);
      } catch (NumberFormatException e) {
        // inf.regValue(nombreLogico, rv);
        log.logException(new ParseProblem(asciiReg, nombreLogico, tStr));
      }
    }

    // inf.regValue(nombreLogico, rv);
    return rv;
  }

  public static Date parseDate(String asciiReg, int des, int has, SimpleDateFormat dateFormatter, String nombreLogico, ParseProblemLog log) {
    String substring = asciiReg.substring(des, has);
    try {
      return dateFormatter.parse(substring);
    } catch (ParseException e) {
      log.logException(new ParseProblem(asciiReg, nombreLogico, substring));
      return null;
    }
  }

}
