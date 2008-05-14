package ar.uba.dcao.dbclima.persistence;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.ResultadoTestQC;
import ar.uba.dcao.dbclima.utils.MultiList;

public abstract class TestResultsFileOut {

  private MultiList<RegistroDiario> registros;

  private String[] testsExportados;

  public TestResultsFileOut(List<Estacion> estaciones, String[] testsExportados) {

    List<List<RegistroDiario>> ll = new ArrayList<List<RegistroDiario>>();

    for (Estacion e : estaciones) {
      ll.add(e.getRegistros());
    }

    this.registros = new MultiList<RegistroDiario>(ll);
    this.testsExportados = testsExportados;
  }

  public void export(String fileName, String separator) {
    FileWriter fw = null;
    try {
      fw = new FileWriter(fileName);
      fw.append("" + this.testsExportados.length + '\n');
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    for (RegistroDiario rd : this.registros) {
      this.writeRegistro(rd, fw, separator);
    }

    try {
      fw.close();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void writeRegistro(RegistroDiario rd, FileWriter fw, String separator) {
    StringBuilder rvtext = new StringBuilder();
    boolean anyHighResult = false;

    double sumTestResults = 0;
    for (String testName : this.testsExportados) {
      ResultadoTestQC rt = rd.getResultadoByID(testName);
      if (rt == null) {
        rvtext.append("0" + separator);
      } else {
        double absVal = Math.abs(rt.getValor());
        sumTestResults += absVal;
        rvtext.append(rt.getValor() + separator);
        if (absVal > 2d) {
          anyHighResult = true;
        }
      }
    }

    if (anyHighResult && sumTestResults > 3) {
      try {
        int length = rvtext.length() - separator.length();

        String label = this.getLabel(rd);

        fw.append(rvtext.substring(0, length));
        fw.append(" " + rd.getFecha() + " " + label + "\n");
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  protected abstract String getLabel(RegistroDiario rd);
}
