/**
 * 
 */
package ar.uba.dcao.dbclima.precipitacion.sequia;

final class Pair {
  
  public int p1;
  
  public int p2;
  
  public Pair(int p1, int p2) {
    this.p1 = p1;
    this.p2 = p2;
  }
  
  @Override
  public String toString() {
    return "[P1:" + this.p1 + "-P2:" + this.p2 + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + p1;
    result = prime * result + p2;
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
    final Pair other = (Pair) obj;
    if (p1 != other.p1)
      return false;
    if (p2 != other.p2)
      return false;
    return true;
  }

}