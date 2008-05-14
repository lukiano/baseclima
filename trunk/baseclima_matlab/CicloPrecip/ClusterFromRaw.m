%Made by Juan
function clustersPorMes = ClusterFromRaw( dataMatrix, labelsEstacion, mesInicial, mesFinal, graficar )
% Se clasifican los vectores de cada estacion para el mes indicado, se
% clusteriza el mapa resultante y se grafica el cluster de cada estacion en
% el mapa.

    clustersPorMes = struct();

    if (graficar)
        figure;
    end

    for i = mesInicial:mesFinal
        [sD, sMap, positions, ids] = CrearMapaMensual( dataMatrix, labelsEstacion, i, 40 );

        clusterInfo = ClusterizarMapaEstaciones( sMap, i );

        [bmus, labels] = RecuperarNeuronaDato( sMap, sD );

        clusterInfo.bmus = bmus;
        clusterInfo.labels = labels;
        clusterInfo.positions = positions;

        if (graficar && mesInicial ~= mesFinal)
            subplot(2,6,i+1);
        end

        gridInfo = GrillaVacia( );
%        BasinsTests(gridInfo);
        
        %gridInfo = CrearGrilla(clusterInfo);

        if (graficar)
            MostrarClusters( clusterInfo, gridInfo );
        end
        
        clustersPorMes(i+1).ids = ids;
        clustersPorMes(i+1).clusters = clusterInfo.clusterMap(clusterInfo.bmus);
    end
end

function gridInfo = GrillaVacia()
    %Test Code
    gridInfo = struct();
    gridInfo.map = struct();
    gridInfo.map.res = 0.5;
    gridInfo.map.minLat = -55.5;
    gridInfo.map.maxLat = -21.5;
    gridInfo.map.minLon = 286;
    gridInfo.map.maxLon = 307;
    %%%%%%%%%%%%%
end