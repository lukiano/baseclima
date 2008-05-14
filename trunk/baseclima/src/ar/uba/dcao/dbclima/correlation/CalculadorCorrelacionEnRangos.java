package ar.uba.dcao.dbclima.correlation;

import java.util.ArrayList;
import java.util.List;

import ar.uba.dcao.dbclima.data.CorrelacionEstaciones;
import ar.uba.dcao.dbclima.data.CorrelacionNormalizadaEstaciones;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.precipitacion.rango.ProyectorRango;
import ar.uba.dcao.dbclima.precipitacion.rango.Rango;
import ar.uba.dcao.dbclima.precipitacion.sequia.MarcadorSequia;
import ar.uba.dcao.dbclima.utils.FechaHelper;

/**
 * Calcula correlacion entre dos series de variables (una por estacion), pero tomando conjuntos de registros.
 */
public class CalculadorCorrelacionEnRangos {

  /** Minima longitud de una lista para buscar correlacion con SPI. */
  public static final int MIN_LEN_BASE_CORR = MarcadorSequia.UMBRAL_ANIOS_EN_COMUN /*SPIHelper.CANTIDAD_MINIMA_ANIOS*/;
  
  private CalculadorCorrelacionEnRangos() {}

  public static CorrelacionEstaciones getCorr(Estacion e1, Estacion e2, ProyectorRango proyector, boolean normalizada) {
    CorrelacionEstaciones corr;
    List<ValuePair> vals;
    
    if (normalizada) {
      corr = new CorrelacionNormalizadaEstaciones(e1, e2, -1);
      vals = buildSeriesNormalizada(e1, e2, (CorrelacionNormalizadaEstaciones)corr, proyector);
    } else {
      corr = new CorrelacionEstaciones(e1, e2, -1);
      vals = buildSeries(e1, e2, corr, proyector);
    }

    corr.setVariable(proyector.nombre());
    calcularCorrelacion(corr, vals);
    return corr;
  }
  
  private static void calcularCorrelacion(CorrelacionEstaciones correlacionEstaciones, List<ValuePair> valores) {
    if (valores.size() > MIN_LEN_BASE_CORR) {

      double sumXY = 0;
      double sumX = 0;
      double sumY = 0;
      double sumX2 = 0;
      double sumY2 = 0;
      
      /*
      for (ValuePair vp : valores) {
        System.out.print(vp.v1.doubleValue());
        System.out.print('\t');
      }
      System.out.println();
      for (ValuePair vp : valores) {
        System.out.print(vp.v2.doubleValue());
        System.out.print('\t');
      }
      System.out.println();
      System.out.println();
      */
      for (ValuePair vp : valores) {
        double v1n = vp.v1.doubleValue();
        double v2n = vp.v2.doubleValue();

        sumXY += v1n * v2n;
        sumX += v1n;
        sumY += v2n;
        sumX2 += v1n * v1n;
        sumY2 += v2n * v2n;
      }

      /**
       * E: sumatoria<br>
       * E(y) = na + b E(x)<br>
       * E(xy) = aE(x) + b * E(pow(x,2))
       */

      /*
       * Calculo de correlacion
       */
      int n = valores.size();
      double numeradorCorrelacion = n * sumXY - sumX * sumY;
      double denominadorCorrelacionAlCuadrado = (n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY);

      double corr = numeradorCorrelacion / Math.sqrt(denominadorCorrelacionAlCuadrado);
      //corr = Math.round(corr * 1000) / 1000d;
      correlacionEstaciones.setCorrelacion(corr);

      /*
       * Calculo de relacion lineal x minimos cuadrados
       */
      double b = (sumXY - sumX * sumY / n) / (sumX2 - sumX * sumX / n);
      double a = (sumY - b * sumX) / n;

      double desvio = 0;

      for (ValuePair vp : valores) {
        double v2est = vp.v1.doubleValue() * b + a;
        desvio += Math.pow(vp.v2.doubleValue() - v2est, 2);
      }
      desvio = Math.sqrt(desvio / (n - 1));

      /*
      a = Math.round(a * 100) / 100d;
      b = Math.round(b * 100) / 100d;
      desvio = Math.round(desvio * 100) / 100d;
      */

      correlacionEstaciones.setPendiente(b);
      correlacionEstaciones.setOrdenadaOrigen(a);
      correlacionEstaciones.setDesviacionEstimacion(desvio);
/*      
      SimpleRegression simpleRegression = new SimpleRegression();
      Tally tally = new Tally();
      for (ValuePair vp : valores) {
        simpleRegression.addData(vp.v1.doubleValue(), vp.v2.doubleValue());
        tally.add(vp.v1.doubleValue());
      }
      
      System.out.println("slope:" + simpleRegression.getSlope());
      System.out.println("inter:" + simpleRegression.getIntercept());
      System.out.println("corr:" + simpleRegression.getR());
      System.out.println("mse:" + simpleRegression.getMeanSquareError());
      System.out.println("N:" + simpleRegression.getN());
      System.out.println("significance:" + simpleRegression.getSignificance());
      System.out.println("SlopeConfidenceInterval:" + simpleRegression.getSlopeConfidenceInterval());
      System.out.println("sse:" + simpleRegression.getSumSquaredErrors());
      simpleRegression.getSignificance();
      */
      
      correlacionEstaciones.setMes(n);
    } else {
      correlacionEstaciones.setCorrelacion(null);
    }
  }
  
  /**
   * Actualiza la lista de correlaciones por mes (con inicio, fin y
   * cantidad de registros usados) y devuelve una lista de pares de valor (el valor de cada estacion)
   * discriminadas por mes.
   * 
   * @param e1
   * @param e2
   * @param corrs
   * @param proy
   * @return
   */
  private static List<ValuePair> buildSeries(Estacion e1, Estacion e2, CorrelacionEstaciones corr, ProyectorRango proyector) {
    List<ValuePair> rv = new ArrayList<ValuePair>();
    
    int i = 0;
    int j = 0;

    Number vi;
    Number vj;
    
    List<Rango> lista1 = proyector.proyectarRangos(e1);
    List<Rango> lista2 = proyector.proyectarRangos(e2);

    while (i < lista1.size() && j < lista2.size()) {
      Rango r1 = lista1.get(i);
      Rango r2 = lista2.get(j);

      if (r1.comienzo().equals(r2.comienzo())) {
        // Si ambos registros corresponden al mismo dia, se agregan a la
        // lista.
        vi = r1.valor();
        vj = r2.valor();

        if (vi != null && vj != null) {
          rv.add(new ValuePair(vi, vj));
          if (corr.getComienzo() == null || corr.getComienzo().compareTo(r1.comienzo()) == 1) {
            corr.setComienzo(r1.comienzo());
          }
          if (corr.getFin() == null || corr.getFin().compareTo(r1.fin()) == -1) {
            corr.setFin(r1.fin());
          }
          corr.setNumRegsUsados(corr.getNumRegsUsados() + 1);
        }
      }

      if (!r1.comienzo().before(r2.comienzo())) {
        // Si el 1er registro no es anterior al 2do, se avanza la 2da
        // lista.
        j++;
      }

      if (!r2.comienzo().before(r1.comienzo())) {
        // Si el 2do registro no es anterior al 1ero, se avanza la 1era
        // lista.
        i++;
      }
    }
    
    return rv;
  }

  /**
   * Actualiza la lista de correlaciones por mes (con inicio, fin y
   * cantidad de registros usados) y devuelve una lista de pares de valor (el valor de cada estacion)
   * discriminadas por mes.
   * 
   * @param e1
   * @param e2
   * @param corrs
   * @param proy
   * @return
   */
  private static List<ValuePair> buildSeriesNormalizada(Estacion e1, Estacion e2, CorrelacionNormalizadaEstaciones corr, ProyectorRango proyector) {
    List<ValuePair> rv = new ArrayList<ValuePair>();
    
    int i = 0;
    int j = 0;

    Number vi;
    Number vj;
    
    //UniformProyectorRangoDecorator uprd = new UniformProyectorRangoDecorator(proyector);
    
    List<Rango> lista1 = proyector.proyectarRangos(e1);
    List<Rango> lista2 = proyector.proyectarRangos(e2);
    List<Double> valores1 = new ArrayList<Double>();

    List<Double> valores2 = new ArrayList<Double>();

    while (i < lista1.size() && j < lista2.size()) {
      Rango r1 = lista1.get(i);
      Rango r2 = lista2.get(j);

      if (r1.comienzo().equals(r2.comienzo())) {
        // Si ambos registros corresponden al mismo dia, se agregan a la
        // lista.
        vi = r1.valor();
        vj = r2.valor();

        if (vi != null && vj != null) {
          rv.add(new ValuePair(vi, vj));
          if (!valores1.contains(vi.doubleValue())) {
            valores1.add(vi.doubleValue());
          }
          if (!valores2.contains(vj.doubleValue())) {
            valores2.add(vj.doubleValue());
          }
          
          if (corr.getComienzo() == null || corr.getComienzo().compareTo(r1.comienzo()) == 1) {
            corr.setComienzo(r1.comienzo());
          }
          if (corr.getFin() == null || corr.getFin().compareTo(r1.fin()) == -1) {
            corr.setFin(r1.fin());
          }
          corr.setNumRegsUsados(corr.getNumRegsUsados() + 1);
        }
      }

      if (!r1.comienzo().before(r2.comienzo())) {
        // Si el 1er registro no es anterior al 2do, se avanza la 2da
        // lista.
        j++;
      }

      if (!r2.comienzo().before(r1.comienzo())) {
        // Si el 2do registro no es anterior al 1ero, se avanza la 1era
        // lista.
        i++;
      }
    }
    
    java.util.Collections.sort(valores1);
    java.util.Collections.sort(valores2);
    
    corr.setValoresEstacion1(valores1);
    corr.setValoresEstacion2(valores2);

    List<ValuePair> rv2 = new ArrayList<ValuePair>(rv.size());
    
    List<Integer> pares1 = new ArrayList<Integer>(rv.size());
    List<Integer> pares2 = new ArrayList<Integer>(rv.size());
    
    for (ValuePair vp : rv) {
      int ind1 = valores1.indexOf(vp.v1.doubleValue()) + 1;
      int ind2 = valores2.indexOf(vp.v2.doubleValue()) + 1;
      rv2.add(new ValuePair(ind1, ind2));
      pares1.add(ind1);
      pares2.add(ind2);
    }
    
    corr.setParesEstacion1(pares1);
    corr.setParesEstacion2(pares2);
    
    return rv2;
  }
  
  public static List<Integer> obtenerAniosEnComun(Estacion e1, Estacion e2, ProyectorRango proyector) {
    List<Integer> resultado = new ArrayList<Integer>();
    
    int i = 0;
    int j = 0;

    List<Rango> lista1 = proyector.proyectarRangos(e1);
    List<Rango> lista2 = proyector.proyectarRangos(e2);

    while (i < lista1.size() && j < lista2.size()) {
      Rango r1 = lista1.get(i);
      Rango r2 = lista2.get(j);

      if (r1.comienzo().equals(r2.comienzo())) {
        resultado.add(FechaHelper.dameAnio(r1.comienzo()));
      }

      if (!r1.comienzo().before(r2.comienzo())) {
        // Si el 1er registro no es anterior al 2do, se avanza la 2da
        // lista.
        j++;
      }

      if (!r2.comienzo().before(r1.comienzo())) {
        // Si el 2do registro no es anterior al 1ero, se avanza la 1era
        // lista.
        i++;
      }
    }
    
    return resultado;
  }


  protected final static class ValuePair {
    Number v1;

    Number v2;

    public ValuePair(Number v1, Number v2) {
      this.v1 = v1;
      this.v2 = v2;
    }
  }
}
