% Made by Juan
function clusterInfo = ClusterizarMapa( sMap, clusters)
% Toma un SOM, genera un cluster y devuelve un arreglo que mapea neuronas a
% clusters. La variable clusters puede ser 'auto', o un valor numerico

    % Primero se obtiene la informacion de clustering.
    sCl = som_cllinkage(sMap, 'linkage', 'ward');

    maxClusters = sMap.topol.msize(1) * sMap.topol.msize(2);

    if strcmp(clusters,'auto')
        FIT_BEST_REGION_START = 4;
        FIT_BEST_REGION_END = 11;

        ward = reverse(sCl.tree(:,3));
        ward1 = (circshift(ward,[0 -1]));
        wardDif = (ward - ward1) ./ ward1;

        numOfClusters = FIT_BEST_REGION_START;
        for i = FIT_BEST_REGION_START + 1:FIT_BEST_REGION_END
            if (power(i,0.7) * wardDif(i) > power(numOfClusters,0.7) * wardDif(numOfClusters))
                numOfClusters = i;
            end
        end

        disp('Number of clusters: ');
        disp(numOfClusters);
    end

    if strcmp(clusters,'showGraph')
        ward = reverse(sCl.tree(:,3));
        ward1 = (circshift(ward,[0 -1]));
        wardDif = (ward - ward1) ./ ward1;

        bar(4:12, wardDif(4:12));
        numOfClusters = 1;
    end

    % Number of clusters is fixed.
    if (isnumeric(clusters))
        numOfClusters = clusters;
    end

    clusterMap = buildClusterMap(sCl, numOfClusters);

    clusterInfo = struct();
    clusterInfo.numOfClusters = numOfClusters;
    clusterInfo.clusterMap = clusterMap;
end

function r = reverse(vec)

    for i = 1:length(vec)
        r(i) = vec(length(vec) - i + 1);
    end
end

function clusterMap = buildClusterMap(sCl, numOfClusters)
    % Se genera el mapeo base de neurona -> cluster. Inicialmente cada
    % neurona ES su propio cluster.
    clusterMap = sCl.base;

    % Para cada par de neuronas que se agrupan, se actualiza el mapeo
    % informando el nuevo cluster de las neuronas del par. Al salir del
    % ciclo, cada neurona tiene asignado su cluster final.
    for i = 1:length(sCl.tree) - numOfClusters + 1
        for j = 1:length(clusterMap)
            if (clusterMap(j) == sCl.tree(i,1) || clusterMap(j) == sCl.tree(i,2))
                clusterMap(j) = length(clusterMap) + i;
            end
        end
    end

    % Renombro los clusters para que queden libres los valores
    % 1:length(cluster)
    clusterMap = clusterMap + length(clusterMap);

    % Aprovechandome de que los valores 1:length(cluster) estan libres y
    % que numClusters <= length(clusters)
    % cambio los clusters por los valores 1:numClusters.
    for currNewIndex = numOfClusters:-1:1
        for i = 1:length(clusterMap)
            if clusterMap(i) > length(clusterMap)
                clusterMap = replace(clusterMap,clusterMap(i),currNewIndex);
                break;
            end
        end
    end
end

function rv = replace(vector,oldVal,newVal)
    rv = vector;
    for i = 1:length(rv)
        if (rv(i) == oldVal)
            rv(i) = newVal;
        end
    end
end