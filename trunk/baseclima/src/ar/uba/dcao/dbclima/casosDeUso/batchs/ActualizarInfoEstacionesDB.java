package ar.uba.dcao.dbclima.casosDeUso.batchs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.ListadoEstaciones;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;
import ar.uba.dcao.dbclima.utils.InputDataFileCatalog;
import ar.uba.dcao.dbclima.utils.InputLineReader;

/**
 * Proceso que actualiza las estaciones que existan en la base de
 * datos. Para esto se usan dos fuentes adicionales de informacion.
 * Los archivos .smn2 que se encuentren en el directorio /data/dsSMN2/
 * y el archivo /data/EstacionesSMN.csv (si es que existe).
 */
public class ActualizarInfoEstacionesDB {
	@SuppressWarnings("unchecked")
	public static void actualizarInfoEstacionesDB(String listEstsFname) {
		Session sess = DBSessionFactory.getInstance().getCurrentSession();
		sess.beginTransaction();

		File listadoFile = new File(listEstsFname);
		if (!listadoFile.exists()) {
			System.out
					.println("No se podran actualizar las estaciones a partir del listado "
							+ listEstsFname + " (no se encontro el archivo)");
		}

		List<Estacion> estacionesBase = sess.createQuery("FROM Estacion").list();

		if (listadoFile.exists()) {
			updateFromListado(estacionesBase, sess, listadoFile);
		}

		actualizarCoordenadas(estacionesBase);
		sess.getTransaction().commit();
	}

	private static void updateFromListado(List<Estacion> estacionesBase, Session sess,
			File listadoFile) {
		ListadoEstaciones listadoEstaciones;
		try {
			listadoEstaciones = new ListadoEstaciones(listadoFile);
		} catch (FileNotFoundException e1) {
			throw new IllegalStateException(e1);
		}

		List<Estacion> estacionesListado = listadoEstaciones
				.getEstacionesListadas();

		if (estacionesListado != null) {
			for (Estacion e : estacionesBase) {
				Estacion eb = buscarEquiv(e.getCodigoSMN(), estacionesListado);
				if (eb != null) {
					e.setAltura(eb.getAltura());
					e.setLatitud(eb.getLatitud());
					e.setLongitud(eb.getLongitud());
					e.setNombre(eb.getNombre());
					e.setProvincia(eb.getProvincia());

					e.setCodigoOMM(eb.getCodigoOMM());
				}

				actualizarPeriodoActividad(sess, e);
				sess.update(e);
			}
		}
	}

	private static void actualizarCoordenadas(List<Estacion> listEsts) {
		String path = "data/dsSMN2/";
		List<File> files = InputDataFileCatalog.getInputFiles(path, "smn2");

		System.out.println("Actualizando coordenadas");

		Map<Integer, Estacion> ests = new HashMap<Integer, Estacion>();
		for (int j = 0; j < files.size(); j++) {
			System.out.println("Leyendo archivo " + (j + 1) + " de "
					+ files.size());

			InputLineReader reader = null;
			try {
				reader = new InputLineReader(new FileReader(files.get(j)));
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

			while (!reader.eof()) {
				String line = reader.getLine();
				try {
					Integer codInt = Integer.valueOf(line.substring(0, 4)
							.trim());

					if (!ests.containsKey(codInt)) {
						Double latitud = Double.valueOf(line.substring(15, 20)
								.trim());
						Double longitud = Double.valueOf(line.substring(26, 31)
								.trim());
						Integer altura = Integer.valueOf(line.substring(32, 36)
								.trim());
						Estacion e = new Estacion();
						e.setAltura(altura);
						e.setLatitud((int) Math.round(latitud * 100));
						e.setLongitud((int) Math.round(longitud * 100));

						ests.put(codInt, e);
					}

				} catch (Exception e) {
					System.out.println("No se pudo parsear el registro '"
							+ line + "'. Causa: " + e.getMessage());
				}
			}
		}

		for (Estacion edb : listEsts) {
			if (ests.containsKey(edb.getCodigoSMN()) && edb.getAltura() == null) {
				Estacion efile = ests.get(edb.getCodigoSMN());
				edb.setAltura(efile.getAltura());
				edb.setLatitud(efile.getLatitud());
				edb.setLongitud(efile.getLongitud());
			}
		}
	}

	private static void actualizarPeriodoActividad(Session sess, Estacion e) {
		Date fin = (Date) sess.createQuery(
				"SELECT MAX(r.fecha) FROM RegistroDiario r WHERE r.estacion.id = "
						+ e.getId()).uniqueResult();
		Date inicio = (Date) sess.createQuery(
				"SELECT MIN(r.fecha) FROM RegistroDiario r WHERE r.estacion.id = "
						+ e.getId()).uniqueResult();

		e.setFechaFin(fin);
		e.setFechaInicio(inicio);
	}

	private static Estacion buscarEquiv(int codigoSMN, List<Estacion> estaciones) {
		for (Estacion e : estaciones) {
			if (e.getCodigoSMN().equals(codigoSMN)) {
				return e;
			}
		}
		return null;
	}
}