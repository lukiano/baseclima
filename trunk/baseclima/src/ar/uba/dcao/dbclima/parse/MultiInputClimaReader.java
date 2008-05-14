package ar.uba.dcao.dbclima.parse;

import java.util.List;

public class MultiInputClimaReader implements InputClimaReader {

  private List<InputClimaReader> readers;

  private int currReader = 0;

  public MultiInputClimaReader(List<InputClimaReader> readers) {
    this.readers = readers;
  }

  public RegistroCrudo proximoRegistro() {
    if (!this.readers.get(this.currReader).quedanRegistros()) {
      this.currReader++;
    }
    return this.readers.get(this.currReader).proximoRegistro();
  }

  public boolean quedanRegistros() {
    return (this.currReader < this.readers.size() - 1)
      || (this.currReader == this.readers.size() - 1 && this.readers.get(this.currReader).quedanRegistros());
  }

  public void close() {
    for (InputClimaReader reader : this.readers) {
      reader.close();
    }
  }
}
