package ar.uba.dcao.dbclima.qc;

import ar.uba.dcao.dbclima.concurrencia.MultiTask;
import ar.uba.dcao.dbclima.concurrencia.Task;

public class TemperatureQualityCheckFactory extends MultiTask {

  public static Task buildQCTaskForDataset(Long dsID) {
    QC1Task qc1 = new QC1Task(dsID);
    QC2Task qc2 = new QC2Task(dsID);

    return new MultiTask(qc1, qc2);
  }
}
