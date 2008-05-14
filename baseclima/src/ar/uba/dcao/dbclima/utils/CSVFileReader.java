package ar.uba.dcao.dbclima.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVFileReader {
  private InputLineReader fReader;
  
  public CSVFileReader(File csvFile) throws FileNotFoundException {
      fReader = new InputLineReader(new FileReader(csvFile));
  }

  public List<String[]> readCSV() {
    List<String[]> rv = new ArrayList<String[]>();

    while(!this.fReader.eof()) {
      String s = this.fReader.getLine();
      rv.add(s.split(","));
    }

    return rv;
  }
}
