package ar.uba.dcao.dbclima.parse;

public interface ItemCorrectorRegistroCrudo {

  public boolean aplica(RegistroCrudo input);

  public void corregirRegistro(RegistroCrudo input);
}
