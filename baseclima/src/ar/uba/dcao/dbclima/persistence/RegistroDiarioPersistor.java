package ar.uba.dcao.dbclima.persistence;

import java.io.FileNotFoundException;
import java.util.Date;

import ar.uba.dcao.dbclima.data.ListadoEstaciones;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;

public class RegistroDiarioPersistor extends ElementPersistor<RegistroDiario> {

	private static int MAX_UNCOMMITED_REGS = 10000;

	private ListadoEstaciones estacionesArg;

	private PrePersistanceChecker val;

	private long lastFlush = new Date().getTime();

	private long opStart = lastFlush;

	private int tregs = 0;

	public RegistroDiarioPersistor(ParseProblemLog log) {
		super(MAX_UNCOMMITED_REGS, DBSessionFactory.getInstance());
		try {
			this.estacionesArg = new ListadoEstaciones();
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}

		this.val = new PrePersistanceChecker(log);
	}

	@Override
	public void queue(RegistroDiario r) {
		// Validaciones
		val.validarProximo(r);

		// Se completa la estacion
		if (r.getEstacion().getId() != null) {
			r.setEstacion(estacionesArg.getByCodigoSMN(r.getEstacion().getCodigoSMN()));
		}

		super.queue(r);
	}

	public void commit() {
		this.tregs += this.uncommited.size();
		this.printFlushInfo(this.uncommited.size());

		super.commit();
	}

	private void printFlushInfo(int regNum) {
		// Se persiste el registro
		System.gc();
		double elapsed = ((new Date().getTime() - this.lastFlush) / 1000d);
		double tElapsed = ((new Date().getTime() - this.opStart) / 1000d);

		long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
		long usedMem = Runtime.getRuntime().totalMemory() / (1024 * 1024)
				- freeMem;

		System.out.println(regNum + "/" + this.tregs + " regs en " + elapsed
				+ "s./" + tElapsed + "s. (" + freeMem + "MB/" + usedMem
				+ "MB libres/usados)");

		this.lastFlush = new Date().getTime();
	}
}