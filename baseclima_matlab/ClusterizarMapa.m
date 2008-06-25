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
        %figure;
        %bar(4:20, wardDif(4:20));
        %figure;
        %bar(4:20, ward(4:20));
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
    ones = find(bits == 1) + length(sCl.color) - length(clusterMap) + 1;
    twos = find(bits == 2) + length(sCl.color) - length(clusterMap) + 1;
    dendo(ones) = 1;
    dendo(twos) = 1;
    figure; %show classification tree
    sCl.color(:, :) = 0;
    sCl.color(ones, :) = jet(numOfClusters);
    sCl.color(twos, :) = 0.5;
    som_clplot(sCl, 'dendrogram', dendo);

end

function r = reverse(vec)

    for i = 1:length(vec)
        r(i) = vec(length(vec) - i + 1);
    end
end

function [clusterMap, bits] = buildClusterMap(sCl, numOfClusters)
    % Se genera el mapeo base de neurona -> cluster. Inicialmente cada
    % neurona ES su propio cluster.
    clusterMap = sCl.base;

    % Para cada par de neuronas que se agrupan, se actualiza el mapeo
    % informando el nuevo cluster de las neuronas del par. Al salir del
    % ciclo, cada neurona tiene asignado su cluster final.

    bits = zeros(length(sCl.tree), 1);
    counter = 0;
    baselength = length(sCl.base);
    index = length(sCl.tree);
    while index >= 1 && counter < numOfClusters;
        bit_indices = find(bits > 0);
        left = find(sCl.tree(bit_indices, 1) == index + baselength);
        right = find(sCl.tree(bit_indices, 2) == index + baselength);
        total = [left right];
        ok = 1;
        if (bits(bit_indices(total)) == 2)
            grandleft = find(sCl.tree(:, 1) == bit_indices(total) + baselength);
            grandright = find(sCl.tree(:, 2) == bit_indices(total) + baselength);
            grandtotal = [grandleft grandright];
            if length(grandtotal) == 1
                if bits(grandtotal) == 3
                    bits(bit_indices(total)) = 3;
                else
                    ok = 0;
                end
            else
                bits(bit_indices(total)) = 3;
            end
        elseif (bits(bit_indices(total)) == 1)
            bits(bit_indices(total)) = 2;
            counter = counter - length(total);
        end
        if (ok == 1)
            bits(index) = 1;
            counter = counter + 1;
        end
            index = index - 1;
    end
    bits(find(bits == 3)) = 2;
    twos = find(bits == 2);
    twos_that_should_be_one = bits(sCl.tree(twos, 1) - baselength) == 0 | bits(sCl.tree(twos, 2) - baselength) == 0;
    twos_that_should_be_one_indices = twos(find(twos_that_should_be_one == 1));
    bits(twos_that_should_be_one_indices) = 1;
    leftIndices = sCl.tree(twos_that_should_be_one_indices, 1) - baselength;
    bits = clearBitsRecursively(bits, leftIndices, sCl, baselength);
    rightIndices = sCl.tree(twos_that_should_be_one_indices, 2) - baselength;
    bits = clearBitsRecursively(bits, rightIndices, sCl, baselength);
    
    for i = 1:length(sCl.tree)
        if bits(i) ~= 2
            boolValue = (clusterMap == sCl.tree(i,1) | clusterMap == sCl.tree(i,2));
            clusterMap = clusterMap - boolValue.*clusterMap + boolValue.*(length(clusterMap) + i);
        end
    end
    
%    for i = 1:length(sCl.tree)
%        if bits(i) ~= 2
%            for j = 1:length(clusterMap)
%                if (clusterMap(j) == sCl.tree(i,1) || clusterMap(j) == sCl.tree(i,2))
%                    clusterMap(j) = length(clusterMap) + i;
%                end
%            end
%        end
%    end
    
    
%     for i = 1:length(sCl.tree) - numOfClusters + 1
%         for j = 1:length(clusterMap)
%             if (clusterMap(j) == sCl.tree(i,1) || clusterMap(j) == sCl.tree(i,2))
%                 clusterMap(j) = length(clusterMap) + i;
%             end
%         end
%     end

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
    
%     for currNewIndex = numOfClusters:-1:1
%         for i = 1:length(clusterMap)
%             if clusterMap(i) > length(clusterMap)
%                 clusterMap = replace(clusterMap,clusterMap(i),currNewIndex);
%                 break;
%             end
%         end
%     end
end

function bits = clearBitsRecursively(bits, indices, sCl, baselength)
    if sum(bits(indices)) > 0
       for i = 1:length(indices)
           leftIndices = sCl.tree(indices(i), 1) - baselength;
           bits = clearBitsRecursively(bits, leftIndices, sCl, baselength);
           rightIndices = sCl.tree(indices(i), 2) - baselength;
           bits = clearBitsRecursively(bits, rightIndices, sCl, baselength);           
       end
       bits(indices) = 0;
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