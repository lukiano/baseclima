package ar.uba.dcao.dbclima.parse;

import ar.uba.dcao.dbclima.data.RegistroDiario;


public interface RegistroClimaticoParser {

  RegistroDiario parse(RegistroCrudo rc, ParseProblemLog log);
}
