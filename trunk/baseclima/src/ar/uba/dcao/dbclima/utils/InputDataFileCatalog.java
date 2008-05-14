package ar.uba.dcao.dbclima.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InputDataFileCatalog {

  public static final String dataDirPath = "data/dsSMN2/";
  private static final String SMN_EXT = "smn";

  public static List<File> getSMNInputFiles() {
    return InputDataFileCatalog.getInputFiles(dataDirPath, SMN_EXT);
  }

  public static List<File> getInputFiles(String dirPath, String ext) {
    File dir = new File(dirPath);
    List<File> rv = new ArrayList<File>();
    ext = "." + ext;

    for (String fn : dir.list()) {
      if (fn.endsWith(ext)) {
        rv.add(new File(dirPath + fn));
      }
    }

    return rv;
  }
}