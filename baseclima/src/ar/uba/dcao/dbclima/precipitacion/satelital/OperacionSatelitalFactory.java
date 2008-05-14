package ar.uba.dcao.dbclima.precipitacion.satelital;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class OperacionSatelitalFactory {
  
  private static OperacionSatelital instance;
  
  private OperacionSatelitalFactory() {}
  
  public synchronized static OperacionSatelital getInstance() {
    if (instance == null) {
      InputStream propFile = ClassLoader.getSystemResourceAsStream("netcdf_satellital.ini");
      if (propFile == null) {
        
        // Properties file does not exist, use Hibernate database instead of NetCDF
        instance = new HibernateOperacionSatelital();
        
      } else {
        Properties properties = new Properties();        
        try {
          try {
            properties.load(propFile);
          } finally {
            propFile.close();
          }
        } catch (IOException e) {
          throw new RuntimeException("Unable to load 'netcdf_satellital.ini'", e);
        }
        try {
          instance = new NetCDFOperacionSatelital(properties);
        } catch (IllegalArgumentException e) {
          throw new RuntimeException("Unable to load 'netcdf_satellital.ini'", e);
        }
      }
    }
    return instance;
  }

}
