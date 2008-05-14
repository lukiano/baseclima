package ar.uba.dcao.dbclima.persistence;

import java.io.FileWriter;
import java.io.IOException;

import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.parse.InputClimaReader;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;
import ar.uba.dcao.dbclima.parse.RegistroClimaticoParser;
import ar.uba.dcao.dbclima.parse.RegistroCrudo;

public class InputClimaReaderPersistor {

  public static void persistInputData(RegistroClimaticoParser parser, InputClimaReader reader, ParseProblemLog log) {
    RegistroDiarioPersistor pers = new RegistroDiarioPersistor(log);

    while (reader.quedanRegistros()) {
      try {
        // Obtencion del registro como string desde el archivo
        RegistroCrudo reg = reader.proximoRegistro();

        // Se obtiene el parser que le corresponde al registro
        RegistroDiario r = parser.parse(reg, log);
        if (r != null) {
          pers.queue(r);
        }
      } catch (IllegalStateException e) {
        break;
      }
    }

    pers.commit();

    try {
      log.printToStream(new FileWriter("data/out.txt"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
}