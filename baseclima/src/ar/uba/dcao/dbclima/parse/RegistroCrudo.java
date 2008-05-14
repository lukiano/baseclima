package ar.uba.dcao.dbclima.parse;


public class RegistroCrudo {

  private String registro;

  public RegistroCrudo(String registro) {
    this.registro = registro;
  }

  public String getRegistro() {
    return registro;
  }

  public void setRegistro(String registro) {
    this.registro = registro;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.registro;
  }
  
}
