package ar.uba.dcao.dbclima.qc;

import ar.uba.dcao.dbclima.data.ProyectorRegistro;
import ar.uba.dcao.dbclima.qc.qc1.DIPTest;
import ar.uba.dcao.dbclima.qc.qc1.DistributionBasedTest;
import ar.uba.dcao.dbclima.qc.qc1.OutlierTest;
import ar.uba.dcao.dbclima.qc.qc1.SequenceHomogeneityTest;
import ar.uba.dcao.dbclima.qc.qc1.StepCheckTest;
import ar.uba.dcao.dbclima.qc.qc1.TempRangeTest;

public final class ListValidatorFactory {

  public static final String TMAX_POSTFIX = "_Tx";

  public static final String TMIN_POSTFIX = "_Tn";

  public static final String TRNG_POSTFIX = "_Tr";

  private static final double MAX_DIST = DistributionBasedTest.MAX_DISTANCE_DISTRIB_DEFAULT;

  private static OutlierTest minTempOutlierValidator = new OutlierTest(true, true, MAX_DIST, MAX_DIST) {
    public ProyectorRegistro getProyector() {
      return ProyectorRegistro.PROY_TMIN;
    }
    public String getNombreTest() {
      return OutlierTest.TEST_PREFIX + TMIN_POSTFIX;
    }
  };

  private static OutlierTest maxTempOutlierValidator = new OutlierTest(true, true, MAX_DIST, MAX_DIST) {
    public ProyectorRegistro getProyector() {
      return ProyectorRegistro.PROY_TMAX;
    }
    public String getNombreTest() {
      return OutlierTest.TEST_PREFIX + TMAX_POSTFIX;
    }
  };

  private static ListValidator tempRangeOutlierValidator = new TempRangeTest(true, true, MAX_DIST, MAX_DIST);

  private static StepCheckTest minTempStepValidator = new StepCheckTest(MAX_DIST) {
    public ProyectorRegistro getProyector() {
      return ProyectorRegistro.PROY_TMIN;
    }
    public String getNombreTest() {
      return StepCheckTest.TEST_PREFIX + TMIN_POSTFIX;
    }
  };

  private static StepCheckTest maxTempStepValidator = new StepCheckTest(MAX_DIST) {
    public ProyectorRegistro getProyector() {
      return ProyectorRegistro.PROY_TMAX;
    }
    public String getNombreTest() {
      return StepCheckTest.TEST_PREFIX + TMAX_POSTFIX;
    }
  };

  private static SequenceHomogeneityTest minTempHomogeneityCheck = new SequenceHomogeneityTest() {
    public ProyectorRegistro getProyector() {
      return ProyectorRegistro.PROY_TMIN;
    }
    public String getNombreTest() {
      return SequenceHomogeneityTest.TEST_PREFIX + TMIN_POSTFIX;
    }
  };

  private static SequenceHomogeneityTest maxTempHomogeneityCheck = new SequenceHomogeneityTest() {
    public ProyectorRegistro getProyector() {
      return ProyectorRegistro.PROY_TMAX;
    }
    public String getNombreTest() {
      return SequenceHomogeneityTest.TEST_PREFIX + TMAX_POSTFIX;
    }
  };

  private static DIPTest minTempDIPTest = new DIPTest(MAX_DIST) {
    public ProyectorRegistro getProyector() {
      return ProyectorRegistro.PROY_TMIN;
    }
    public String getNombreTest() {
      return DIPTest.TEST_PREFIX + TMIN_POSTFIX;
    }
  };

  private static DIPTest maxTempDIPTest = new DIPTest(MAX_DIST) {
    public ProyectorRegistro getProyector() {
      return ProyectorRegistro.PROY_TMAX;
    }
    public String getNombreTest() {
      return DIPTest.TEST_PREFIX + TMAX_POSTFIX;
    }
  };

  private ListValidatorFactory() {
  }

  public static OutlierTest getMaxTempOutlierValidator() {
    return maxTempOutlierValidator;
  }

  public static OutlierTest getMinTempOutlierValidator() {
    return minTempOutlierValidator;
  }

  public static ListValidator getTempRangeOutlierValidator() {
    return tempRangeOutlierValidator;
  }

  public static StepCheckTest getMaxTempStepValidator() {
    return maxTempStepValidator;
  }

  public static StepCheckTest getMinTempStepValidator() {
    return minTempStepValidator;
  }

  public static SequenceHomogeneityTest getMaxTempHomogeneityCheck() {
    return maxTempHomogeneityCheck;
  }

  public static SequenceHomogeneityTest getMinTempHomogeneityCheck() {
    return minTempHomogeneityCheck;
  }

  public static DIPTest getMaxTempDIPTest() {
    return maxTempDIPTest;
  }

  public static DIPTest getMinTempDIPTest() {
    return minTempDIPTest;
  }
}