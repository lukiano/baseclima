/**
 * 
 */
package ar.uba.dcao.dbclima.precipitacion.sequia;

import java.util.ArrayList;

/**
 * Enumeracion y composicion de los distintos codigos que puede tener una sequia luego de ser clasificada.
 *
 */
public final class CodigoSequia {
  
  public enum Codigo {
    LP75,// less than 75 percentile in the drought distribution 
    SLD, // short length drought
    LLD, // long length drought
    NO,  // no value
    MD,  // many droughts in different years

    SPI_EW, // extremely wet
    SPI_VW, // very wet
    SPI_MW, // moderately wet
    SPI_NN, // near normal
    SPI_MD, // moderately dry
    SPI_SD, // severely dry
    SPI_ED, // extremely dry

    OV,   // original value
    
    CORR, // neighbor with good correlation 
    KS,   // neighbor with KS value
    
    MIN,  // neighbor value is the minimum of its distribution
    P1,   // neighbor value is in the 10% of its distribution
    P2,   // neighbor value is in the 20% of its distribution
    P3,   // neighbor value is greater than 20% of its distribution
    
    NORM, // neighbor obtained from comparing with the questioned drought
    ALTERN, // neighbor obtained from comparing with an alternative drought
    SATEL // neighbor obtained from a satellital point.

  };
  
  private ArrayList<Codigo> codigos;
  
  public CodigoSequia(Codigo... codigos) {
    this.codigos = new ArrayList<Codigo>(codigos.length);
    for (Codigo codigo : codigos) {
      this.codigos.add(codigo);
    }
  }

//  public CodigoSequia(List<Codigo> codigos) {
//    this.codigos = new ArrayList<Codigo>(codigos);
//  }

  protected CodigoSequia(ArrayList<Codigo> codigos) {
    this.codigos = codigos;
  }

  public void add(Codigo codigo) {
    this.codigos.add(codigo);
  }

  public void add(CodigoSequia codigo) {
    this.codigos.addAll(codigo.codigos);
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Codigo codigo : this.codigos) {
      builder.append(codigo);
      builder.append('/');
    }
    if (builder.length() > 0) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((codigos == null) ? 0 : codigos.hashCode());
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
    final CodigoSequia other = (CodigoSequia) obj;
    if (codigos == null) {
      if (other.codigos != null)
        return false;
    } else if (!codigos.equals(other.codigos))
      return false;
    return true;
  }

  public static CodigoSequia parse(String string) {
    string = string.trim();
    if (string.length() == 0) {
      return new CodigoSequia();
    }
    ArrayList<CodigoSequia.Codigo> codigos = new ArrayList<Codigo>();
    while (string.length() > 0) {
      int slash = string.indexOf('/');
      if (slash == -1) {
        CodigoSequia.Codigo codigo = CodigoSequia.Codigo.valueOf(string);
        codigos.add(codigo);
        string = "";
      } else {
        CodigoSequia.Codigo codigo = CodigoSequia.Codigo.valueOf(string.substring(0, slash));
        codigos.add(codigo);
        if (slash == string.length()) {
          string = "";
        } else {
          string = string.substring(slash + 1);
        }
      }
    }
    return new CodigoSequia(codigos);
  }

}