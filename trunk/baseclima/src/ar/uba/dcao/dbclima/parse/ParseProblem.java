package ar.uba.dcao.dbclima.parse;

public class ParseProblem {

  private String registro;

  private String campo;
  
  private String valor;
  
  private String origen;

  private String accion;

  public ParseProblem(String registro, String campo, String valor) {
    super();
    this.registro = registro;
    this.campo = campo;
    this.valor = valor;
  }

  public String getAccion() {
    return accion;
  }

  public void setAccion(String accion) {
    this.accion = accion;
  }

  public String getCampo() {
    return campo;
  }

  public String getOrigen() {
    return origen;
  }

  public void setOrigen(String origen) {
    this.origen = origen;
  }

  public String getRegistro() {
    return registro;
  }

  public String getValor() {
    return valor;
  }

  public String getDescripcion() {
    return "El valor '" + this.valor + "' no es valido para el campo " + this.campo + " (" + this.registro + ")";
  }
}