package ar.uba.dcao.dbclima.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class InputLineReader {
  
  private BufferedReader bufferedReader;

  public InputLineReader(Reader reader) {
    this.bufferedReader = new BufferedReader(reader);
  }
  
  public String getLine() {
    try {
      return this.bufferedReader.readLine();
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to read file", e);
    }
  }
  
  public boolean eof() {
    try {
      return !this.bufferedReader.ready();
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to read file", e);
    }
  }

  public String next() {
    return this.getLine();
  }

  public void closeInput() {
    try {
      this.bufferedReader.close();
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to close file.", e);
    }
  }

}
