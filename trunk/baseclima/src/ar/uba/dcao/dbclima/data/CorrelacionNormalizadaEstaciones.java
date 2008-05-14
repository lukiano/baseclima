package ar.uba.dcao.dbclima.data;

import java.util.List;

/**
 * Esta clase tambien se utiliza para indicar la correlacion entre dos estaciones,
 * pero dicha correlacion antes es normalizada, para evitar desvios entre los valores asociados.
 * Tambien se pueden obtener tanto los indices de los valores normalizados, como los valores originales.
 *
 */
public class CorrelacionNormalizadaEstaciones extends CorrelacionEstaciones {

  private List<Double> valoresEstacion1, valoresEstacion2;
  
  private List<Integer> paresEstacion1, paresEstacion2;

  public CorrelacionNormalizadaEstaciones() {
    super();
  }

  public CorrelacionNormalizadaEstaciones(Estacion e1, Estacion e2, int mes) {
    super(e1, e2, mes);
  }

  public double predecirEstacion(Estacion estacionAPredecir, double valorEstacionVecina) {
    
    if (estacionAPredecir.equals(this.getE1())) {
      
      // comienzo agregado 1
      double valorEstacionVecinaNormalizada;
      int mayor = -1;
      for (int i = 0; i < this.valoresEstacion1.size(); i++) {
        Double valorMapaVecino = this.valoresEstacion1.get(i);
        if (valorMapaVecino >  valorEstacionVecina) {
          mayor = i;
          break;
        }
      }
      if (mayor == 0) { // menor que el primer elemento
         double valorMayor = this.valoresEstacion1.get(mayor);
         double valorMenor = 0;
         double interpolacion = (valorEstacionVecina - valorMenor) / (valorMayor - valorMenor);
         valorEstacionVecinaNormalizada = interpolacion + mayor;
      } else if (mayor == -1) { // mayor que el ultimo elemento
        valorEstacionVecinaNormalizada = 0.5 + mayor; // una cota superior elegida de manera arbitraria
      } else {
        double valorMayor = this.valoresEstacion1.get(mayor);
        double valorMenor = this.valoresEstacion1.get(mayor - 1);
        double interpolacion = (valorEstacionVecina - valorMenor) / (valorMayor - valorMenor);
        valorEstacionVecinaNormalizada = interpolacion + mayor;
      }
      // fin agregado 1

      double rv = (valorEstacionVecinaNormalizada - this.getOrdenadaOrigen()) / this.getPendiente();
      
      // comienzo agregado 2
      double rvDesnormalizada;
      int indice = (int)Math.floor(rv);
      if (indice == 0) {
        double valorMayor = this.valoresEstacion2.get(indice);
        double valorMenor = 0;
        double porcentaje = rv - indice;
        rvDesnormalizada = porcentaje * (valorMayor - valorMenor) + valorMenor;
      } else if (indice >= this.valoresEstacion2.size()) {
        double valorMenor = this.valoresEstacion2.get(this.valoresEstacion2.size() - 1);
        double valorMayor = valorMenor * 2;
        double porcentaje = rv - (this.valoresEstacion2.size() - 1);
        rvDesnormalizada = porcentaje * (valorMayor - valorMenor) + valorMenor;
      } else {
        double valorMayor = this.valoresEstacion2.get(indice);
        double valorMenor = this.valoresEstacion2.get(indice - 1);
        double porcentaje = rv - indice;
        rvDesnormalizada = porcentaje * (valorMayor - valorMenor) + valorMenor;
      }
      // fin agregado 2
      return rvDesnormalizada;

    } else if (estacionAPredecir.equals(this.getE2())) {
      // comienzo agregado 1
      double valorEstacionVecinaNormalizada;
      int mayor = -1;
      for (int i = 0; i < this.valoresEstacion2.size(); i++) {
        Double valorMapaVecino = this.valoresEstacion2.get(i);
        if (valorMapaVecino >  valorEstacionVecina) {
          mayor = i;
          break;
        }
      }
      if (mayor == 0) { // menor que el primer elemento
         double valorMayor = this.valoresEstacion2.get(mayor);
         double valorMenor = 0;
         double interpolacion = (valorEstacionVecina - valorMenor) / (valorMayor - valorMenor);
         valorEstacionVecinaNormalizada = interpolacion + mayor;
      } else if (mayor == -1) { // mayor que el ultimo elemento
        valorEstacionVecinaNormalizada = 0.5 + mayor; // una cota superior elegida de manera arbitraria
      } else {
        double valorMayor = this.valoresEstacion2.get(mayor);
        double valorMenor = this.valoresEstacion2.get(mayor - 1);
        double interpolacion = (valorEstacionVecina - valorMenor) / (valorMayor - valorMenor);
        valorEstacionVecinaNormalizada = interpolacion + mayor;
      }
      // fin agregado 1

      double rv = valorEstacionVecinaNormalizada * this.getPendiente() + this.getOrdenadaOrigen();

      // comienzo agregado 2
      double rvDesnormalizada;
      int indice = (int)Math.floor(rv);
      if (indice < 0) {
        indice = 0;
      }
      if (indice == 0) {
        double valorMayor = this.valoresEstacion1.get(indice);
        double valorMenor = 0;
        double porcentaje = rv - indice;
        rvDesnormalizada = porcentaje * (valorMayor - valorMenor) + valorMenor;
      } else if (indice >= this.valoresEstacion1.size()) {
        double valorMenor = this.valoresEstacion1.get(this.valoresEstacion1.size() - 1);
        double valorMayor = valorMenor * 2;
        double porcentaje = rv - (this.valoresEstacion1.size() - 1);
        rvDesnormalizada = porcentaje * (valorMayor - valorMenor) + valorMenor;
      } else {
        double valorMayor = this.valoresEstacion1.get(indice);
        double valorMenor = this.valoresEstacion1.get(indice - 1);
        double porcentaje = rv - indice;
        rvDesnormalizada = porcentaje * (valorMayor - valorMenor) + valorMenor;
      }
      // fin agregado 2
      return rvDesnormalizada;

    } else {
      throw new IllegalArgumentException("La estacion no esta en la correlacion");
    }

  }

  public List<Double> getValoresEstacion1() {
    return valoresEstacion1;
  }

  public void setValoresEstacion1(List<Double> valoresEstacion1) {
    this.valoresEstacion1 = valoresEstacion1;
  }

  public List<Double> getValoresEstacion2() {
    return valoresEstacion2;
  }

  public void setValoresEstacion2(List<Double> valoresEstacion2) {
    this.valoresEstacion2 = valoresEstacion2;
  }

  public List<Integer> getParesEstacion1() {
    return paresEstacion1;
  }

  public void setParesEstacion1(List<Integer> paresEstacion1) {
    this.paresEstacion1 = paresEstacion1;
  }

  public List<Integer> getParesEstacion2() {
    return paresEstacion2;
  }

  public void setParesEstacion2(List<Integer> paresEstacion2) {
    this.paresEstacion2 = paresEstacion2;
  }

}
