function clusterInfo = ClusterizarMapa( sMap, clusters)
% Toma un SOM, genera un cluster y devuelve un arreglo que mapea neuronas a
% clusters. La variable clusters puede ser 'auto', o un valor numerico
    % Primero se obtiene la informacion de clustering.
    sCl = som_cllinkage(sMap, 'linkage', 'ward');


    %maxClusters = sMap.topol.msize(1) * sMap.topol.msize(2);

    if strcmp(clusters,'auto')
        FIT_BEST_REGION_START = 4;
        FIT_BEST_REGION_END = 11;

        ward = flipud(sCl.tree(:,3));
        ward = ward';
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
        ward = flipud(sCl.tree(:,3));
        ward = ward';
        ward1 = (circshift(ward,[0 -1]));
        wardDif = (ward - ward1) ./ ward1;
        figure;
        bar(4:20, wardDif(4:20));
        figure;
        bar(4:20, ward(4:20));
        numOfClusters = 6;
    end

    % Number of clusters is fixed.
    if (isnumeric(clusters))
        numOfClusters = clusters;
    end
    [clusterMap, bits] = buildClusterMap(sCl, numOfClusters);

    clusterInfo = struct();
    clusterInfo.numOfClusters = numOfClusters;
    clusterInfo.clusterMap = clusterMap;
    
    dendo = zeros(2*length(sMap.codebook) - 1, 1);
    ones = find(bits == 1);% + length(sCl.color) - length(clusterMap) + 1;
    twos = find(bits == 2);% + length(sCl.color) - length(clusterMap) + 1;
    dendo(ones) = 1;
    dendo(twos) = 1;
    figure; %show classification tree
    sCl.color(:, :) = 0;
    sCl.color(ones, :) = jet(numOfClusters);
    sCl.color(twos, :) = 0.5;
    som_clplot(sCl, 'dendrogram', dendo);
end

function [clusterMap, bits] = buildClusterMap(sCl, numOfClusters)
    % Se genera el mapeo base de neurona -> cluster. Inicialmente cada
    % neurona ES su propio cluster.
    clusterMap = sCl.base;

    % Para cada par de neuronas que se agrupan, se actualiza el mapeo
    % informando el nuevo cluster de las neuronas del par. Al salir del
    % ciclo, cada neurona tiene asignado su cluster final.

    baselength = length(sCl.base);
    treelength = length(sCl.tree);
    bits = zeros(treelength + baselength, 1);
    %index = length(sCl.tree);
    % primero marcamos la raiz, empezamos por ahi
    bits(treelength + baselength) = 1;
    marked_leaf_indices = find(bits == 1);
    while length(marked_leaf_indices) < numOfClusters;
        weightless_node = 0;
        current_weight = baselength + treelength;
        for i = 1:length(marked_leaf_indices)
            node = marked_leaf_indices(i);
            if node > baselength
                %El nodo no es hoja, me fijo en sus hijos
                weight = calculateChildrenWeight(sCl.tree, node - baselength, treelength + baselength);
                if (weight < current_weight)
                    weightless_node = node;
                    current_weight = weight;
                end
            end
        end
        if weightless_node > 0
            % Ahora tengo el nodo con menos peso
            % Marco a sus hijos con 1
            leftChild = sCl.tree(weightless_node - baselength, 1);
            bits(leftChild) = 1;
            rightChild = sCl.tree(weightless_node - baselength, 2);
            bits(rightChild) = 1;
            % el que estaba marcado con 1 ahora tendra 2
            bits(weightless_node) = 2;
        else
            % no hay mas arbol para recorrer
            numOfClusters = length(marked_leaf_indices);
        end
        marked_leaf_indices = find(bits == 1);
    end
    %bits = bits(baselength:treelength + baselength);
    for i = 1:treelength
        if bits(i + baselength) ~= 2
            boolValue = (clusterMap == sCl.tree(i,1) | clusterMap == sCl.tree(i,2));
            clusterMap = clusterMap - boolValue.*clusterMap + boolValue.*(length(clusterMap) + i);
        end
    end

    % Renombro los clusters para que queden libres los valores
    % 1:length(cluster)
    clusterMap = clusterMap + length(clusterMap);

    % Aprovechandome de que los valores 1:length(cluster) estan libres y
    % que numClusters <= length(clusters)
    % cambio los clusters por los valores 1:numClusters.
    sorted = sort(unique(clusterMap));
    for i = 1:length(sorted)
        clusterMap(find(clusterMap == sorted(i))) = i;
    end
end

function weight = calculateChildrenWeight(tree, node, treesize)
    leftChild = tree(node, 1);
    rightChild = tree(node, 2);
    weight = treesize - min([leftChild, rightChild]);
end

