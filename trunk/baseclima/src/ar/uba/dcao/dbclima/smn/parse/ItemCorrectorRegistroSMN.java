package ar.uba.dcao.dbclima.smn.parse;

import ar.uba.dcao.dbclima.parse.ItemCorrectorRegistroCrudo;
import ar.uba.dcao.dbclima.parse.RegistroCrudo;

public class ItemCorrectorRegistroSMN implements ItemCorrectorRegistroCrudo {

  public static final char CARACT_COMODIN = '*';

  public static final char CARACT_NO_REMPLAZO = '_';

  private String filtro;

  private String cambio;

  public ItemCorrectorRegistroSMN(String filtro, String cambio) {
    this.validar(filtro, cambio);
    this.filtro = filtro;
    this.cambio = cambio;
  }

  private void validar(String filtro, String cambio)
      throws IllegalArgumentException {
    if (filtro.length() != cambio.length()) {
      throw new IllegalArgumentException(
          "El filtro y el patron de cambio del corrector deben ser de misma long.");
    }
  }

  public void corregirRegistro(RegistroCrudo registroSMN) {
    if (this.aplica(registroSMN)) {
      StringBuilder str = new StringBuilder();

      for (int i = 0; i < this.filtro.length(); i++) {
        char ci = this.cambio.charAt(i);
        if (ci == CARACT_NO_REMPLAZO) {
          str.append((registroSMN).getRegistro().charAt(i));
        } else {
          str.append(ci);
        }
      }

      registroSMN.setRegistro(str.toString());
    }
  }

  public boolean aplica(RegistroCrudo inputSMN) {

    if (inputSMN.getRegistro().length() != this.filtro.length()) {
      return false;
    }

    for (int i = 0; i < this.filtro.length(); i++) {
      char ci = this.filtro.charAt(i);
      if (ci != CARACT_COMODIN
          && ci != inputSMN.getRegistro().charAt(i)) {
        return false;
      }
    }

    return true;
  }

}
