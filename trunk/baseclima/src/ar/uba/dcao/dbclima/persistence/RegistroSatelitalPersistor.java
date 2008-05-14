package ar.uba.dcao.dbclima.persistence;

import java.util.Date;

import ar.uba.dcao.dbclima.data.RegistroSatelital;
import ar.uba.dcao.dbclima.parse.ParseProblemLog;

public class RegistroSatelitalPersistor extends ElementPersistor<RegistroSatelital> {

	private static int MAX_UNCOMMITED_REGS = 10000;

	private long lastFlush = new Date().getTime();

	private long opStart = lastFlush;

	private int tregs = 0;

	public RegistroSatelitalPersistor(ParseProblemLog log) {
		super(MAX_UNCOMMITED_REGS, DBSessionFactory.getInstance());
		//this.val = new PrePersistanceChecker(log);
	}

	@Override
	public void queue(RegistroSatelital r) {
		// Validaciones (no hay)
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