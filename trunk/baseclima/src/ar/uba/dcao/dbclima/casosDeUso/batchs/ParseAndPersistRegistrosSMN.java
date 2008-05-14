package ar.uba.dcao.dbclima.casosDeUso.batchs;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.casosDeUso.CorrectorDefault;
import ar.uba.dcao.dbclima.parse.InputClimaReader;
import ar.uba.dcao.dbclima.parse.MultiInputClimaReader;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;
import ar.uba.dcao.dbclima.persistence.InputClimaReaderPersistor;
import ar.uba.dcao.dbclima.smn.SMNReader;
import ar.uba.dcao.dbclima.smn.parse.RegistroSMNParser;
import ar.uba.dcao.dbclima.utils.InputDataFileCatalog;

public class ParseAndPersistRegistrosSMN {

  public static void main(String[] args) {
    ParseProblemLog log = new ParseProblemLog();

    String path;
    if (args.length == 0 || args[0].length() == 0) {
      path = "data/dsSMN/";
    } else {
      path = args[0];
    }
    List<File> files = InputDataFileCatalog.getInputFiles(path, "smn");
    List<InputClimaReader> readers = new ArrayList<InputClimaReader>();

    try {
      for (int j = 0; j < files.size(); j++) {
        FileReader fileReader = new FileReader(files.get(j));
        readers.add(new SMNReader(fileReader));
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    RegistroSMNParser parser = new RegistroSMNParser(false, true, false);
    parser.setCorrector(CorrectorDefault.getInstance());

    MultiInputClimaReader reader = new MultiInputClimaReader(readers);
    InputClimaReaderPersistor.persistInputData(parser, reader, log);
    
    reader.close();
  }
}
