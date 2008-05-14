package ar.uba.dcao.dbclima.utils;

import java.text.DecimalFormat;

public class TimeDisplayUtil {

  private static final DecimalFormat frmt = new DecimalFormat("0.0");

  public static String displayLapse(long milis) {

    int hrs = (int) (milis / 3600000);
    milis -= hrs * 3600000;

    int mins = (int) (milis / 60000);
    milis -= mins * 60000;

    String secs = frmt.format(milis / 1000d) + "s.";

    String rv = (hrs > 0 ? hrs + "hs. " : "") + (hrs > 0 || mins > 0 ? mins + "mins. " : "") + secs;

    return rv;
  }
}
