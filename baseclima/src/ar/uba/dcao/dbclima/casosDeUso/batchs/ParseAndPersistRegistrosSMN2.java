package ar.uba.dcao.dbclima.casosDeUso.batchs;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.ListadoEstaciones;
import ar.uba.dcao.dbclima.parse.InputClimaReader;
import ar.uba.dcao.dbclima.parse.MultiInputClimaReader;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;
import ar.uba.dcao.dbclima.persistence.InputClimaReaderPersistor;
import ar.uba.dcao.dbclima.smn.SMNReader;
import ar.uba.dcao.dbclima.smn.parse.RegistroSMN2Parser;
import ar.uba.dcao.dbclima.utils.InputDataFileCatalog;

public class ParseAndPersistRegistrosSMN2 {

	public static void main(String[] args) {
		String path;
		if (args.length == 0 || args[0].length() == 0) {
			path = "data/dsSMN2/";
		} else {
			path = args[0];
		}

		ParseAndPersistRegistrosSMN2.doImport(path);

		System.out.println("Actualizando datos de las estaciones...");
		ActualizarInfoEstacionesDB
				.actualizarInfoEstacionesDB(ListadoEstaciones.LISTADO_ESTACIONES_FILENAME);
		System.out.println("Actualizacion de estaciones finalizada.");

		System.out.println("Actualizando datos de los registros...");
		ActualizarInfoRegistrosDB.actualizarInfoRegistrosDB();
		System.out.println("Actualizacion de registros finalizada.");
	}

	private static void doImport(String path) {
		ParseProblemLog log = new ParseProblemLog();

		List<File> files = InputDataFileCatalog.getInputFiles(path, "smn2");
		List<InputClimaReader> readers = new ArrayList<InputClimaReader>();

		try {
			for (int j = 0; j < files.size(); j++) {
				FileReader fileReader = new FileReader(files.get(j));
				readers.add(new SMNReader(fileReader));
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		System.out.println("Importando " + files.size()
				+ " archivos con datos...");

		RegistroSMN2Parser parser = new RegistroSMN2Parser();
		MultiInputClimaReader reader = new MultiInputClimaReader(readers);
		InputClimaReaderPersistor.persistInputData(parser, reader, log);
		reader.close();

		System.out.println("Importacion finalizada.");
	}
}
