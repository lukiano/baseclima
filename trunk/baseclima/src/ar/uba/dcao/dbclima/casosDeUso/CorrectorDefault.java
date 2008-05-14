package ar.uba.dcao.dbclima.casosDeUso;

import ar.uba.dcao.dbclima.parse.CorrectorRegistroCrudo;

public class CorrectorDefault {

  public static CorrectorRegistroCrudo getInstance() {

    //String mensual9a8filtro = "9*******************************************************************************";
    //String mensual9a8correc = "8_______________________________________________________________________________";
    
    //ItemCorrectorRegistroCrudo item = new ItemCorrectorRegistroSMN(mensual9a8filtro, mensual9a8correc);
    //return new CorrectorRegistroCrudo(item);
    return new CorrectorRegistroCrudo();
  }
}
