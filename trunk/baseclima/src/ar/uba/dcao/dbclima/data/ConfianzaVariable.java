package ar.uba.dcao.dbclima.data;

/**
 * Confianza asignada a una variable de un registro. Se indica la variable, la confianza
 * que se le asigno (a menor valor mas confiable es) y la descripcion de la situacion.
 */
public class ConfianzaVariable {

  public static final byte ERRONEO = 3;

  public static final byte POCO_CONFIABLE = 2;

  public static final byte LIMITROFE = 1;

  public static final byte CONFIABLE = 0;

  /** Codigo alfanumerico con que se designa este problema. */
  private final String codigo;

  /** Descripcion del problema. */
  private final String descripcion;

  /** Confianza asociada a este problema. */
  private final byte confianza;

  /**
   * Puede haber mas de un problema para un valor de confianza. Cuando una variable de
   * registro tiene asociados mas de uno de estos se elige el de mayor prioridad.
   */
  private final byte prioridad;

  public static ConfianzaVariable masRelevante(ConfianzaVariable c1, ConfianzaVariable c2) {
    ConfianzaVariable rv;
    if (c1 == null || c2 == null) {
      rv = (c1 == null) ? c2 : c1;
    } else if (c1.getConfianza() != c2.getConfianza()) {
      rv = (c1.getConfianza() > c2.getConfianza()) ? c1 : c2;
    } else {
      rv = (c1.getPrioridad() >= c2.getPrioridad()) ? c1 : c2;
    }

    return rv;
  }

  public ConfianzaVariable(byte confianza, String razon, String codigo, byte prioridad) {
    this.confianza = confianza;
    this.descripcion = razon;
    this.codigo = codigo;
    this.prioridad = prioridad;
  }

  public byte getConfianza() {
    return confianza;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public String getCodigo() {
    return codigo;
  }

  public byte getPrioridad() {
    return prioridad;
  }
}