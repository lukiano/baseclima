function MostrarClusters( clusterInfo, gridInfo )
% Muestra el cluster asignado a cada estacion luego de los procesos de
% agrupamiento por SOM y cluster-linkage.
% 'estLabels' es un vector con los labels de cada estacion.
% 'estPos' es una matriz de 'lat', 'lon', conservando el orden de estLabels
% 'bmus' es el mapeo de estaciones en neuronas, conservando el orden de
% estLabels.
% 'clusters' es el mapeo de neuronas en clusters.

%Se grafica el mapa de la region

    MIN_LAT = gridInfo.map.minLat;
    MAX_LAT = gridInfo.map.maxLat;
    MIN_LON = gridInfo.map.minLon;
    MAX_LON = gridInfo.map.maxLon;
    GRID_RES = gridInfo.map.res;

    colormap('jet');
    colors = [[0 0 1];  [0 1 0]; [0 1 1]; [1 0 0];
        [1 0 1]; [0.2 0.2 0.2]; [0.5 0.5 0.5]; [0 0.5 0.7];
        [0.7 0.5 0]; [1 0.5 0.5]];

    axesm('MapProjection','eqdcylin', 'MapLatLimit',[MIN_LAT MAX_LAT], 'Maplonlimit',[MIN_LON MAX_LON], 'grid','on', 'MeridianLabel', 'on', 'ParallelLabel', 'on');
    load coast_argentina; 
    plotm(latar,lonar,'k');

    % Se grafican los poligonos de los clusters
    %displayPolygons(gridInfo, colors);

    % Se grafican la grilla regular
    %displayGrid(gridInfo, colors);

    % Se grafican las estaciones
    displayEstaciones(clusterInfo, colors);

    tightmap;
end

function displayGrid(gridInfo, colors)
    GRID_RES = gridInfo.map.res;

    for i = 1:size(gridInfo.clusters, 2)
        infl = gridInfo.clusters(i).influence;
        color = colors(i,:) * 0.7;

        for j = 1:size(infl,2)
            x = infl(1,j);
            y = infl(2,j);

            sqrX = [x, x, x+GRID_RES, x+GRID_RES, x];
            sqrY = [y, y+GRID_RES, y+GRID_RES, y, y];

            %fillm(sqrX, sqrY, color);
            fillm(sqrX, sqrY, 'FaceColor', color, 'EdgeColor', 'none', 'FaceAlpha', 0.5);
        end
    end
end

function displayPolygons(gridInfo, colors)
    for i = 1:size(gridInfo.clusters, 2)
        polygon = gridInfo.clusters(i).polygon;
        if (length(polygon) > 0)
            x = polygon(:,1);
            y = polygon(:,2);

            color = colors(i,:) * 0.7;
            fillm(x, y, 'FaceColor', color, 'EdgeColor', 'none');
            %fillm(x, y, color);
        end
    end
end

function displayEstaciones(clusterInfo, colors)
    for i = 1:size(clusterInfo.positions,1)
        Lat(i) = clusterInfo.positions(i,1);
        Lon(i) = clusterInfo.positions(i,2);

        color = colors(clusterInfo.clusterMap(clusterInfo.bmus(i)), :);
        color = color + (1 - color) * 0.3;
        Color(i,:) = color;
    end

    scatterm(Lat,Lon,25,Color, 'filled', 'k');
end
