package ar.uba.dcao.dbclima.casosDeUso.batchs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Batch que actualiza algunas propiedades de los RegistrosDiarios
 * existentes en la base de datos. En particular, se actualizan las
 * propiedades 'ayer' y 'maniana' de los registros.
 */
public class ActualizarInfoRegistrosDB {

	private static final long MILLIS_DIA = 24000 * 3600;

	@SuppressWarnings("unchecked")
	public static void actualizarInfoRegistrosDB() {
		Session sess = DBSessionFactory.getInstance().getCurrentSession();
		sess.beginTransaction();
		List<Estacion> ests = sess.createQuery("FROM Estacion").list();

		List<Long> idsEsts = new ArrayList<Long>();
		for (int i = 0; i < ests.size(); i++) {
			Estacion es = ests.get(i);
			idsEsts.add(es.getId());
		}

		sess.getTransaction().rollback();

		int i = 0;
		long start = new Date().getTime();

		for (Long idEst : idsEsts) {
			actualizarInfoRegistrosDB(idEst);

			/* Debug, Info estado */
			long now = new Date().getTime();
			long elapsed = (now - start);
			double timeLeft = (elapsed * (idsEsts.size() / (double) ++i) - elapsed) / 60000d;

			DecimalFormat decimalFormat = new DecimalFormat("0.0");

			System.out.println(i + "/" + idsEsts.size()
					+ " estaciones procesadas." + " Tiempo restante estimado: "
					+ decimalFormat.format(timeLeft) + "mins.");
			/* Debug, Info estado */
		}
	}

	@SuppressWarnings("unchecked")
	private static void actualizarInfoRegistrosDB(Long estId) {
		Session sess = DBSessionFactory.getInstance().getCurrentSession();
		sess.beginTransaction();
		Estacion est = (Estacion) sess.createQuery(
				"FROM Estacion WHERE id = " + estId).uniqueResult();

		List<RegistroDiario> registros = est.getRegistros();

		for (int i = 1; i < registros.size(); i++) {
			RegistroDiario hoy = registros.get(i);
			RegistroDiario ayer = registros.get(i - 1);
			if (hoy.getFecha().getTime() - ayer.getFecha().getTime() == MILLIS_DIA) {
				hoy.setAyer(ayer);
				ayer.setManiana(hoy);
			}
		}

		sess.getTransaction().commit();
	}
}
