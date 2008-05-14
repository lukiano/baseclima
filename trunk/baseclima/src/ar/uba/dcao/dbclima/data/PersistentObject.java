package ar.uba.dcao.dbclima.data;

/**
 * Clase base de aquellas que representan a los objetos persistibles en Hibernate (y la base de datos detras).
 *
 */
public class PersistentObject {
  
  private Long id;

  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    final PersistentObject other = (PersistentObject) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}
