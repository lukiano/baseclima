package ar.uba.dcao.dbclima.correlation;

/**
 * Clase de ayuda que provee un metodo para obtener la significacia de una correlacion,
 * dado el valor obtenido de correlacion (entre -1 y 1) y el numero de muestras que se utilizaron.
 *
 */
public class SignificanciaCorrelacionHelper {
  
  private SignificanciaCorrelacionHelper() {}
  
  // sacado del JavaScript de http://faculty.vassar.edu/lowry/ch4apx.html
  public static double obtenerSignificancia(double correlacion, int numeroMuestras) {
    int df = numeroMuestras - 2; // grados de libertad
    double t = correlacion * Math.sqrt(df / (1 - correlacion * correlacion));
    return buzz(t, df);
  }
  
  private static double buzz(double t, int n) {
    t = Math.abs(t);
    double rt = t / Math.sqrt(n);
    double fk = Math.atan(rt);
    if(n == 1) { 
      return 1 - 2 * fk / Math.PI; 
    }
    double ek = Math.sin(fk); 
    double dk = Math.cos(fk);
    if((n % 2) == 1) { 
      return 1 - 2 * (fk + ek * dk * zip(dk * dk, 2, n - 3)) / Math.PI; 
    } else { 
      return 1 - ek * zip(dk * dk, 1, n - 3); 
    }
  }
  
  private static double zip(double q, int i, int j) {
    double temp = 1.0;
    double res = temp;
    for (int k = i; k <= j; k+= 2) {
      temp = temp * q * k / (k + 1); 
      res += temp; 
    }
    return res;
  }


}
