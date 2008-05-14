package ar.uba.dcao.dbclima.parse;


public interface InputClimaReader {

  boolean quedanRegistros();

  RegistroCrudo proximoRegistro();
  
  void close();
}
