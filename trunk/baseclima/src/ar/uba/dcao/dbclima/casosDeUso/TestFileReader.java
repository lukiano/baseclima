package ar.uba.dcao.dbclima.casosDeUso;

import java.io.FileNotFoundException;
import java.io.FileReader;

import ar.uba.dcao.dbclima.utils.InputLineReader;
import junit.framework.TestCase;

public class TestFileReader extends TestCase {
  public void testReader() {
    InputLineReader reader;
    try {
      reader = new InputLineReader(new FileReader(
          "/Users/jose/Documents/workspace-eclipse/BaseClima/data/dsSMN/baires.01.smn"));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    int i = 0;
    while (!reader.eof()) {
      i++;
      String l = reader.next();
      System.out.println("Linea " + i + ": " + l);
    }

    reader.closeInput();
  }
}
