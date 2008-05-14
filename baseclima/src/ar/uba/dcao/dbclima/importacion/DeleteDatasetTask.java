package ar.uba.dcao.dbclima.importacion;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ar.uba.dcao.dbclima.concurrencia.AbstractTask;
import ar.uba.dcao.dbclima.concurrencia.TaskResult;

public class DeleteDatasetTask extends AbstractTask {

	private Long idDataset;

	public DeleteDatasetTask(Long idDataset) {
		this.idDataset = idDataset;
	}

	public boolean run(SessionFactory runningFactory) {
		this.setProgressDescription("Deleting DataSet with ID: "
				+ this.idDataset);
		Session sess = runningFactory.getCurrentSession();
		sess.beginTransaction();
		try {
			// sess.createQuery("DELETE FROM ResultadoTestQC rtqc WHERE
			// rtqc.registro.dataset.id = ?").setLong(0, this.idDataset)
			// .executeUpdate(); FIXME: Hibernate crea mal esta query, parece
			// que no soporta una indireccion de 3 niveles

			sess.createQuery("DELETE FROM ResultadoTestQC rtqc WHERE rtqc.registro.id IN (SELECT id FROM RegistroDiario rd where rd.dataset.id = ?)").setLong(0, this.idDataset)
				.executeUpdate();

			sess.createQuery("DELETE FROM Sequia seq WHERE seq.registroComienzo.id IN (SELECT id FROM RegistroDiario rd where rd.dataset.id = ?)").setLong(0, this.idDataset)
				.executeUpdate();
			
			this.setCompletionState(1d / 6d);
			sess.createQuery(
					"DELETE FROM RegistroDiario rd WHERE rd.dataset.id = ?")
					.setLong(0, this.idDataset).executeUpdate();
			this.setCompletionState(2d / 6d);
			sess.createQuery(
					"DELETE FROM Estacion est WHERE est.dataset.id = ?")
					.setLong(0, this.idDataset).executeUpdate();
			this.setCompletionState(3d / 6d);
			sess.createQuery(
					"DELETE FROM RegistroSatelital rs WHERE rs.dataset.id = ?")
					.setLong(0, this.idDataset).executeUpdate();
			this.setCompletionState(4d / 6d);
			sess.createQuery(
					"DELETE FROM PuntoSatelital ps WHERE ps.dataset.id = ?")
					.setLong(0, this.idDataset).executeUpdate();
			this.setCompletionState(5d / 6d);
			sess.createQuery("DELETE FROM Dataset ds WHERE ds.id = ?").setLong(
					0, this.idDataset).executeUpdate();
			sess.getTransaction().commit();
			this.setCompletionState(1);
			this.setComplete(true);
			this.setResult(TaskResult.buildSuccessfulResult("Dataset deleted"));
		} catch (Error e) {
			this.setResult(TaskResult.buildUnsuccessfulResult("Delete failed", e));
			throw e;
		} catch (RuntimeException e) {
			this.setResult(TaskResult.buildUnsuccessfulResult("Delete failed", e));
			throw e;
		}
		return true;
	}

	public void updateGUIWhenCompleteSuccessfully() {
	}
}
