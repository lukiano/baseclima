package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hibernate.Session;

import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.data.Sequia;
import ar.uba.dcao.dbclima.precipitacion.Confianza;
import ar.uba.dcao.dbclima.qc.StationBasedQualityCheck;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Crea un archivo .csv con los resultados de la clasificacion de sequias para las estaciones
 * seleccionadas.
 * Asume que la clasificacion ya se realizo y esta persistida en la base de datos.
 *
 */
public class ReporteSPI extends StationBasedQualityCheck {
  
  private static class CodigoSequiaComparator implements Comparator<CodigoSequia> {

    public int compare(CodigoSequia arg0, CodigoSequia arg1) {
      return arg0.toString().compareTo(arg1.toString());
    }
    
  }

  private static final Map<CodigoSequia, Confianza> CODIGO2CONFIANZA = new TreeMap<CodigoSequia, Confianza>(new CodigoSequiaComparator());

  static {
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.LP75), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SLD), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.MD), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.LLD), Confianza.ERROR);
    
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.NO), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.SATEL), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.MIN, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.MIN, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.P1, CodigoSequia.Codigo.NORM), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.P1, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.P2, CodigoSequia.Codigo.NORM), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.P2, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.P3, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.NO, CodigoSequia.Codigo.P3, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);

    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.NO), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.SATEL), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.ERROR);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.ERROR);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.ERROR);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.ERROR);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);

    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.NO), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.SATEL), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.ERROR);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.ERROR);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.ERROR);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);

    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.NO), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.OV, CodigoSequia.Codigo.SATEL), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.ERROR);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.NORM), Confianza.ERROR);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.CORR, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_ED, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_SD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.OK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_NN, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_MW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_VW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.NORM), Confianza.DOUBTFUL);
    CODIGO2CONFIANZA.put(new CodigoSequia(CodigoSequia.Codigo.SPI_MD, CodigoSequia.Codigo.SPI_EW, CodigoSequia.Codigo.KS, CodigoSequia.Codigo.ALTERN), Confianza.NEED_CHECK);

  }
  
  private static class DatoSequia {
    
    public Estacion estacion;
    
    public Date fechaComienzo;
    
    public Integer duracion;

    @Override
    public String toString() {
      return "Station " + this.estacion.getNombre() + " (OMM: " + this.estacion.getCodigoOMM() + ") Drought Start: " + this.fechaComienzo + " - length: " + this.duracion; 
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((duracion == null) ? 0 : duracion.hashCode());
      result = prime * result + ((fechaComienzo == null) ? 0 : fechaComienzo.hashCode());
      result = prime * result + ((estacion == null) ? 0 : estacion.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final DatoSequia other = (DatoSequia) obj;
      if (duracion == null) {
        if (other.duracion != null)
          return false;
      } else if (!duracion.equals(other.duracion))
        return false;
      if (fechaComienzo == null) {
        if (other.fechaComienzo != null)
          return false;
      } else if (!fechaComienzo.equals(other.fechaComienzo))
        return false;
      if (estacion == null) {
        if (other.estacion != null)
          return false;
      } else if (!estacion.equals(other.estacion))
        return false;
      return true;
    }
    
  }
  
  private final Map<CodigoSequia, List<DatoSequia>> resultadoCodigo = 
    new TreeMap<CodigoSequia, List<DatoSequia>>(new CodigoSequiaComparator());
  
  private boolean acumularLaCantidadDiasDeSequia;
  
  private File fileToWrite;

  public ReporteSPI(File fileToWrite) {
    this(fileToWrite, false);
  }
  
  public ReporteSPI(File fileToWrite, boolean acumularLaCantidadDiasDeSequia) {
    this.acumularLaCantidadDiasDeSequia = acumularLaCantidadDiasDeSequia;
    this.fileToWrite = fileToWrite;
  }

  @Override
  protected String finalDescription(int totalStations) {
    return Integer.valueOf(totalStations) + " reported";
  }

  @Override
  protected void processStation(Session sess, Estacion station) {
    for (Sequia sequia : station.getSequias()) {
      RegistroDiario registro = sequia.getRegistroComienzo();
      DatoSequia datoSequia = new DatoSequia();
      datoSequia.estacion = station;      datoSequia.fechaComienzo = sequia.getComienzo();
      datoSequia.duracion = sequia.getLongitud();
      String codigo = registro.getCodigoConfianzaDrought();
      if (codigo == null) {
        System.err.println("Drought Code is null / " + registro);
      } else {
        codigo = codigo.trim();
        sess.evict(registro);
        
        if (codigo.equals(CodigoSequia.Codigo.LP75.toString())) {
          continue;
        }
        
        CodigoSequia codigoSequia = CodigoSequia.parse(codigo);
        Confianza confianza = CODIGO2CONFIANZA.get(codigoSequia);
        if (confianza == null) {
          System.out.println(codigoSequia);
        }
        
        List<DatoSequia> registrosActuales = this.resultadoCodigo.get(codigoSequia);
        if (registrosActuales == null) {
          registrosActuales = new ArrayList<DatoSequia>();
          this.resultadoCodigo.put(codigoSequia, registrosActuales);
        }
        registrosActuales.add(datoSequia);
      }
    }
  }

  @Override
  protected String progressDescription(int processedStations, int totalStations) {
    return Integer.valueOf(processedStations) + "station reported.";
  }

  @Override
  protected String startingDescription() {
    return "Reporting stations...";
  }
  
  private void printReport() throws IOException {
    int[] meses = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    Set<Integer> anios = new TreeSet<Integer>();
    
//    PrintWriter printWriter = new PrintWriter(new FileWriter("report/DetailedDroughtReport.txt"));
    try {
      for (Map.Entry<CodigoSequia, List<DatoSequia>> entrada : this.resultadoCodigo.entrySet()) {
//        CodigoSequia codigoLocal = entrada.getKey();
        List<DatoSequia> registros = entrada.getValue();
//        printWriter.println(codigoLocal + ": " + registros.size() + " records.");
        for (DatoSequia reg : registros) {
//          printWriter.println(reg);
          anios.add(FechaHelper.dameAnio(reg.fechaComienzo));
        }
//        printWriter.println();
      }
    } finally {
//      printWriter.close();
    }
    
    PrintWriter printWriter = new PrintWriter(new FileWriter(this.fileToWrite));
    printWriter.print("Label,");
    try {
      for (CodigoSequia codigoSequia : this.resultadoCodigo.keySet()) {
        printWriter.print(codigoSequia);
        printWriter.print(',');
      }
      printWriter.println();

      printWriter.print("All,");
      for (List<DatoSequia> registros : this.resultadoCodigo.values()) {
        int size = 0;
        if (this.acumularLaCantidadDiasDeSequia) {
          for (DatoSequia registro : registros) {
            size += registro.duracion;
          }
        } else {
          size = registros.size();
        }
        printWriter.print(size);
        printWriter.print(',');
      }
      printWriter.println();
      printWriter.println();
      
      for (int mes : meses) {
        printWriter.print(FechaHelper.mes(mes));
        printWriter.print(',');
        for (List<DatoSequia> registros : this.resultadoCodigo.values()) {
          int size = 0;
          for (DatoSequia registro : registros) {
            int mesRegistro = FechaHelper.dameMes(registro.fechaComienzo);
            if (mesRegistro == mes) {
              if (this.acumularLaCantidadDiasDeSequia) {
                size += registro.duracion;
              } else {
                size++;
              }
            }
          }
          printWriter.print(size);
          printWriter.print(',');
        }
        printWriter.println();
      }
      printWriter.println();

      for (int anio : anios) {
        printWriter.print(anio);
        printWriter.print(',');
        for (List<DatoSequia> registros : this.resultadoCodigo.values()) {
          int size = 0;
          for (DatoSequia registro : registros) {
            int anioRegistro = FechaHelper.dameAnio(registro.fechaComienzo);
            if (anioRegistro == anio) {
              if (this.acumularLaCantidadDiasDeSequia) {
                size += registro.duracion;
              } else {
                size++;
              }
            }
          }
          printWriter.print(size);
          printWriter.print(',');
        }
        printWriter.println();
      }
      printWriter.println();
      
      //-----
      
      
      printWriter.print("Label,");
      for (Confianza confianza : Confianza.values()) {
        printWriter.print(confianza);
        printWriter.print(',');
      }
      printWriter.println();

      {
        Map<Confianza, Integer> cantidadConfianza = new TreeMap<Confianza, Integer>();
        for (Map.Entry<CodigoSequia, List<DatoSequia>> entrada : this.resultadoCodigo.entrySet()) {
          CodigoSequia codigoLocal = entrada.getKey();
          List<DatoSequia> registros = entrada.getValue();
          int size = registros.size();
          Confianza confianza = CODIGO2CONFIANZA.get(codigoLocal);
          if (confianza == null) {
            System.out.println(codigoLocal);
          }
          Integer valor = cantidadConfianza.get(confianza);
          if (valor == null) {
            cantidadConfianza.put(confianza, size);
          } else {
            cantidadConfianza.put(confianza, valor + size);
          }
        }
        printWriter.print("All,");
        for (Integer valor : cantidadConfianza.values()) {
          printWriter.print(valor);
          printWriter.print(',');
        }
        printWriter.println();
      }

      for (int mes : meses) {
        printWriter.print(FechaHelper.mes(mes));
        printWriter.print(',');
        Map<Confianza, Integer> cantidadConfianza = new TreeMap<Confianza, Integer>();
        for (Map.Entry<CodigoSequia, List<DatoSequia>> entrada : this.resultadoCodigo.entrySet()) {
          CodigoSequia codigoLocal = entrada.getKey();
          List<DatoSequia> registros = entrada.getValue();
          int size = 0;
          for (DatoSequia registro : registros) {
            int mesRegistro = FechaHelper.dameMes(registro.fechaComienzo);
            if (mesRegistro == mes) {
              if (this.acumularLaCantidadDiasDeSequia) {
                size += registro.duracion;
              } else {
                size++;
              }
            }
          }
          Confianza confianza = CODIGO2CONFIANZA.get(codigoLocal);
          Integer valor = cantidadConfianza.get(confianza);
          if (valor == null) {
            cantidadConfianza.put(confianza, size);
          } else {
            cantidadConfianza.put(confianza, valor + size);
          }
        }
        for (Integer valor : cantidadConfianza.values()) {
          printWriter.print(valor);
          printWriter.print(',');
        }
        printWriter.println();
      }
      printWriter.println();

      for (int anio : anios) {
        printWriter.print(anio);
        printWriter.print(',');
        Map<Confianza, Integer> cantidadConfianza = new TreeMap<Confianza, Integer>();
        for (Map.Entry<CodigoSequia, List<DatoSequia>> entrada : this.resultadoCodigo.entrySet()) {
          CodigoSequia codigoLocal = entrada.getKey();
          List<DatoSequia> registros = entrada.getValue();
          int size = 0;
          for (DatoSequia registro : registros) {
            int anioRegistro = FechaHelper.dameAnio(registro.fechaComienzo);
            if (anioRegistro == anio) {
              if (this.acumularLaCantidadDiasDeSequia) {
                size += registro.duracion;
              } else {
                size++;
              }
            }
          }
          Confianza confianza = CODIGO2CONFIANZA.get(codigoLocal);
          Integer valor = cantidadConfianza.get(confianza);
          if (valor == null) {
            cantidadConfianza.put(confianza, size);
          } else {
            cantidadConfianza.put(confianza, valor + size);
          }
        }
        for (Integer valor : cantidadConfianza.values()) {
          printWriter.print(valor);
          printWriter.print(',');
        }
        printWriter.println();
      }
      printWriter.println();

    } finally {
      printWriter.close();
    }
    
  }

  public void updateGUIWhenCompleteSuccessfully() {
    try {
      this.printReport();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
