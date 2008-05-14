package ar.uba.dcao.dbclima.qc;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import ar.uba.dcao.dbclima.data.ConfianzaVariable;

public class ConfVarFactory {

  /* CATEGORIAS CONFIANZA. */

  public static final String CVNOK = "VNOK";

  public static final String COK = "OK";

  /* Variables internas. */

  private static final String BUNDLE_NAME = "ar.uba.dcao.dbclima.qc.CategoriasConfianza";

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  private static Map<String, ConfianzaVariable> confianzas = new HashMap<String, ConfianzaVariable>();

  /* Constructor oculto por tratarse de una clase factory. */
  private ConfVarFactory() {
  }

  public static ConfianzaVariable get(String codigo) {
    ConfianzaVariable rv;

    if (codigo == null) {
      rv = null;
    } else if (confianzas.containsKey(codigo)) {
      rv = confianzas.get(codigo);

    } else {
      String rep = getConfianzaRep(codigo);
      if (rep == null) {
        throw new IllegalArgumentException("Codigo de confianza invalido: " + codigo);
      }

      String[] arrayRep = rep.split("/");
      if (arrayRep.length != 3) {
        throw new IllegalStateException("Representacion interna invalida para el codigo " + codigo);
      }

      byte lvl = Byte.valueOf(arrayRep[0]);
      byte prio = Byte.valueOf(arrayRep[1]);
      String desc = arrayRep[2];

      rv = new ConfianzaVariable(lvl, desc, codigo, prio);

      confianzas.put(codigo, rv);
    }

    return rv;
  }

  private static String getConfianzaRep(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
