package ar.uba.dcao.dbclima.qc;

import java.util.List;

import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.data.RegistroDiario;

public interface ListValidator {

  void validate(List<RegistroDiario> regs);

  String getNombreTest();

  ProyectorRegistro getProyector();
}
