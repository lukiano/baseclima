package ar.uba.dcao.dbclima.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorrectorRegistroCrudo {

  private List<ItemCorrectorRegistroCrudo> correctores;

  public CorrectorRegistroCrudo(ItemCorrectorRegistroCrudo... correctores) {
    this.correctores = Arrays.asList(correctores);
  }

  public CorrectorRegistroCrudo(List<ItemCorrectorRegistroCrudo> correctores) {
    this.correctores = new ArrayList<ItemCorrectorRegistroCrudo>(correctores);
  }

  public void corregirRegistro(RegistroCrudo input) {
    for (ItemCorrectorRegistroCrudo i : this.correctores) {
      i.corregirRegistro(input);
    }
  }
}
