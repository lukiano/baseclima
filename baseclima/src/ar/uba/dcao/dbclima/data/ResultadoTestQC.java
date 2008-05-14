package ar.uba.dcao.dbclima.data;


/**
 * Resultados de testsQC sobre RegistrosDiarios. Cada resultado identifica
 * el tipo de test aplicado y el valor del test, codificado por un numero.
 */
public class ResultadoTestQC extends PersistentObject {

  private String testID;

  private double valor;

  private RegistroDiario registro;

  public double getValor() {
    return valor;
  }

  public void setValor(double resultado) {
    this.valor = resultado;
  }

  public String getTestID() {
    return testID;
  }

  public void setTestID(String testID) {
    this.testID = testID;
  }

  public RegistroDiario getRegistro() {
    return registro;
  }
  
  public void setRegistro(RegistroDiario registro) {
    this.registro = registro;
  }
}
