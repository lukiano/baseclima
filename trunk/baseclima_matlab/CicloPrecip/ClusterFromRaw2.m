function ClusterFromRaw2( dataMatrix, labelsEstacion, mes )
% Se clasifican los vectores de cada estacion para el mes indicado, se
% clusteriza el mapa resultante y se grafica el cluster de cada estacion en
% el mapa.

[sD, sMap, positions] = CrearMapaMensual( dataMatrix, labelsEstacion, mes, 40 );

figure;
subplot(2, 5, 1);
clusterInfo = ClusterizarMapa( sMap, 'showGraph' );

for i = 4:12

    clusterInfo = ClusterizarMapa( sMap, i );

    [bmus, labels] = RecuperarNeuronaDato( sMap, sD );

    clusterInfo.bmus = bmus;
    clusterInfo.labels = labels;
    clusterInfo.positions = positions;

    subplot(2,5,i-2);

    gridInfo = struct();
    gridInfo.map.res = 0.5;
    gridInfo.map.minLat = -55.5;
    gridInfo.map.maxLat = -21.5;
    gridInfo.map.minLon = 286;
    gridInfo.map.maxLon = 307;

    MostrarClusters( clusterInfo, gridInfo );
end

