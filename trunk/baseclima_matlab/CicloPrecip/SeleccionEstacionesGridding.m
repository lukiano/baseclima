function ests = SeleccionEstacionesGridding(mes, datamatrix, clusters)
   x = -30;
   y = -60;

   ests = getEstacionesMasCercanas(x, y, mes, datamatrix, 2);

   % Encontrar estaciones en estos clusters
   ests = getTodasEstacionesClusters(ests, clusters(mes+1));

   %Filtrar por radio?
   ests = filtrarEstaciones(x, y, ests, mes, datamatrix);
end

function estsClusters = getTodasEstacionesClusters(ests, clustersMes)
    j = 1;
    for i = 1:size(ests, 1)
        clusters(j) = getClusterEstacion(ests(i), clustersMes);
        j = j + 1;
    end

    coinc2(1:size(clustersMes.clusters, 2)) = 0;
    for i = 1:size(clusters, 2)
        coinc2 = coinc2 | (clustersMes.clusters == clusters(i));
    end
    estsClusters = clustersMes.ids(find(coinc2));
end

function clus = getClusterEstacion(id, clustersMes)
    for i = 1:size(clustersMes.ids, 2)
        if (clustersMes.ids(i) == id)
            clus = clustersMes.clusters(i);
            break;
        end
    end
end

function pos = getPosicionEstacion(id, mes, datamatrix)
    for i = 1:size(datamatrix, 1)
        if (datamatrix(i, 1) == id && datamatrix(i, 4) == mes)
            pos = i;
            break;
        end
    end
end

function ests = getEstacionesMasCercanas(x, y, mes, datamatrix, cant)
    j = 1;
    for i = 1:size(datamatrix,1)
        if (datamatrix(i,4) == mes)
            % Guardo el ID de la estacion
            dist(j,1) = datamatrix(i,1);
            
            %Guardo la dist a la estacion
            dist(j,2) = sqrt(power(datamatrix(i,2) - x, 2) + power(datamatrix(i,3) - y, 2));
            j = j + 1;
        end
    end
    
    dist = sortrows(dist,2);
    ests = dist(1:cant,1);
end

function estacionesFiltradas = filtrarEstaciones(x, y, estaciones, mes, datamatrix)
    MAX_DIST_EST = 5;

    j = 1;
    for i = 1:size(estaciones, 2)
        pos = getPosicionEstacion(estaciones(i), mes, datamatrix);
        dist = sqrt(power(datamatrix(pos,2) - x, 2) + power(datamatrix(pos,3) - y, 2));
        
        if (dist <= MAX_DIST_EST)
            estacionesFiltradas(j) = estaciones(i);
            j = j + 1;
        end
    end
end