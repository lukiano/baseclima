package ar.uba.dcao.dbclima.utils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

/**
 * InputLineReader original
 * @deprecated tiene bugs. Se esta clase se estaba usando en vez de BufferedReader por alguna razon en particular, avisar y corregir.
 *
 */
@Deprecated
public class InputLineReader2 {

  private static final int BUFF_SIZE = 10000;

  private static final Set<Character> LINE_RETURN = new HashSet<Character>();
  
  static {
    LINE_RETURN.add('\n');
    LINE_RETURN.add('\r');
  }

  private Reader reader = null;

  private char[] buffer = new char[BUFF_SIZE];

  private int iBuff = BUFF_SIZE - 1;

  private int buffEndPos = -1;

  public InputLineReader2(Reader reader) {
    this.reader = reader;
    this.move();
  }

  private char readCurr() {
    return this.buffer[this.iBuff];
  }

  private void move() {
    if (this.iBuff == BUFF_SIZE - 1) {
      try {
        int read = this.reader.read(this.buffer, 0, BUFF_SIZE);
        if (read != BUFF_SIZE) { this.buffEndPos = read; }
      } catch (IOException e) {
        throw new IllegalArgumentException(
            "No se puede leer del archivo indicado", e);
      }
      this.iBuff = 0;
    } else {
      this.iBuff++;
    }
  }

  public String getLine() {
    StringBuilder rv = new StringBuilder();
    Character read = null;
    while (!this.eof()) {
      read = this.readCurr();
      if (LINE_RETURN.contains(read)) { break; }
      rv.append(read);
      this.move();
    }

    while (!this.eof() && LINE_RETURN.contains(read)) {
      this.move();
      read = this.readCurr();
    }

    return rv.toString();
  }

  public boolean eof() {
    return this.buffEndPos == iBuff;
  }

  public String next() {
    return this.getLine();
  }

  public void closeInput() {
    try {
      this.reader.close();
    } catch (IOException e) {
      throw new IllegalArgumentException("No se puede cerrar el archivo.");
    }
  }
}
