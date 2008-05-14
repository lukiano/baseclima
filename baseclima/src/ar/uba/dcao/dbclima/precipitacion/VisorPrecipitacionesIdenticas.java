package ar.uba.dcao.dbclima.precipitacion;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import ar.uba.dcao.dbclima.dao.DAOFactory;
import ar.uba.dcao.dbclima.data.Estacion;
import ar.uba.dcao.dbclima.data.RegistroDiario;
import ar.uba.dcao.dbclima.persistence.DBSessionFactory;

/**
 * Este programa muestra en consola a aquellos registros diarios que tienen el mismo valor de precipitacion que en el registro
 * de su estacion mas cercana, en el mismo dia. Esto suele ser sospechoso (uno es verdadero y el otro fue copiado).
 *
 */
public class VisorPrecipitacionesIdenticas {
  
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    
    Session sess = DBSessionFactory.getInstance().getCurrentSession();
    sess.beginTransaction();
    Query vecinasQuery = sess.createQuery("FROM Estacion WHERE id != ? ORDER BY (latitud - ?) * (latitud - ?) + (longitud - ?) * (longitud - ?)");
    Query registrosQuery = sess.createQuery("FROM RegistroDiario WHERE estacion = ? AND precipitacion > 0");
    Query registroVecinoQuery = sess.createQuery("FROM RegistroDiario WHERE estacion = ? AND fecha = ? AND precipitacion > 0");
    List<Estacion> listaEstacion = DAOFactory.getEstacionDAO(sess).findAll();
    
    for (Estacion estacion : listaEstacion) { // realizo el procedimiento para cada estacion
      
      
      System.out.println("Processing station: " + estacion.getCodigoSMN() + " - " + estacion.getNombre());
      
      // obtengo las estaciones vecinas ordenadas segun la distancia y excluyendo la local
      List<Estacion> estacionesVecinas = vecinasQuery.setLong(0, estacion.getId())
        .setInteger(1, estacion.getLatitud()).setInteger(2, estacion.getLatitud())
        .setInteger(3, estacion.getLongitud()).setInteger(4, estacion.getLongitud()).list();
      
      if (estacionesVecinas != null && !estacionesVecinas.isEmpty()) {
        // se encontro al menos una vecina
        Estacion estacionVecina = estacionesVecinas.get(0); // la primera es la mas cercana

        System.out.println("Neighbor station: " + estacionVecina.getCodigoSMN() + " - " + estacionVecina.getNombre());
        
        // obtengo los registros de la estacion local
        List<RegistroDiario> registros = registrosQuery.setParameter(0, estacion).list();
        
        for (RegistroDiario registro : registros) {
          // para cada registro de la estacion local, me fijo si existe un registro de la misma fecha en la estacion vecina mas cercana
          RegistroDiario registroVecino = (RegistroDiario) registroVecinoQuery.setParameter(0, estacionVecina).setParameter(1, registro.getFecha()).uniqueResult();
          if (registroVecino != null) {
            // comparo las precipitaciones
            if (registro.getPrecipitacion() == registroVecino.getPrecipitacion()) {
              // son iguales las precipitaciones, entonces es una medida sospechosa
              System.out.println("Register in doubt:" + registro.getFecha() 
                  + " - value:" + registro.getPrecipitacion());
            }
          }
        }
        System.out.println();
      }
      sess.clear();
    }
    sess.getTransaction().commit();
  }

}
