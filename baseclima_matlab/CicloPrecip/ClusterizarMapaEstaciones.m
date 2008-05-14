% Made by Juan
function clusterInfo = ClusterizarMapaEstaciones( sMap, mes)
% Toma un SOM de estaciones de argentina para un mes dado, genera un
% cluster y devuelve un arreglo que mapea neuronas a clusters. Usa
% el algoritmo generico ClusterizarMapa()

    clustersManual = [7;5;7;7;7;7;7;7;7;7;7;7];
   %clustersManual = [7;7;7;7;8;7;8;7;8;8;7;8];
   %May al pasar a 6 clusters divide bs as en 2 zonas medio mezcladas
   %Abr al pasar a 8 divide el centro en 2. Neutro
   %Jul al pasar a 8 divide el sur en 2 medio feo
   %Sep al pasar a 8 divide el sur en 2. Positivo

    numOfClusters = clustersManual(mes+1);
    
    clusterInfo = ClusterizarMapa(sMap, numOfClusters);
end