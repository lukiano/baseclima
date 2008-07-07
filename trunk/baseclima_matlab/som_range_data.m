%Made by Luciano, so you know whom to address for errors.

%month 0: annual mean; 1 - 12: actual month
%difftype -1: "1975-2000 (model)" - "1975-2000 (obs)"
% n >= 1000 : "12-n+12 (model)" - "1975-2000 (model)"
% 1 >= n > 1000: "n degree (model)" - "1975-2000 (model)"
%masks: cell array with string with mask names. Ex: {'southamerica','africa'}
%msize: neuron map size (two dimensional)
%numOfClusters: number of clusters; 'auto'
function [sD, sMap, cluster_models, BmusTwoDims] = som_range_data(scen, cvar, month, difftype, masks, msize, numOfClusters)
    
    if difftype < 0
        modeltype = '20th-_obs';
    elseif difftype >= 1000
        modeltype = ['21th-Year' num2str(difftype) '-_20th'];
    else
        modeltype = ['21th-Degree' num2str(difftype) 'deg-_20th'];
    end

    load([scen '_' cvar '_range_data_' num2str(month) '_' modeltype '.mat'], 'big_data', 'gridpoints', 'dims', 'big_range_data');
    
    %generate map

    filecmap='precip_7903_meansc.mat'; % use this file to load the map grid
    load(filecmap, 'xgrid','ygrid');
    x = xgrid;
    x = x-1.25;
    y = ygrid;
    y = y-1.25;
    
    indices = setMasks(masks, gridpoints, dims);
    gridpoints(indices) = [];
    big_range_data(indices,:) = [];
    
    models_length = size(big_range_data, 2);

    %normalize
    mean_data = mean(big_range_data, 2);
    std_data = std(big_range_data, 0, 2);
    num_models = size(big_range_data, 2);
    mean_data = repmat(mean_data, 1, num_models);
    std_data = repmat(std_data, 1, num_models);
    big_range_data = big_range_data - mean_data;
    big_range_data = big_range_data ./ std_data;
    %end normalize
    
    sD = som_data_struct(big_range_data);
    
    if isempty(msize)
        sMap = som_make(sD);        
    else
        sMap = som_make(sD, 'msize', msize);
    end

    %figure; %show neuron map
    %som_show(sMap);
    
    %generate automatic classification
    
    clusterInfo = ClusterizarMapa(sMap, numOfClusters);
    %return;
    clusterMap = clusterInfo.clusterMap;
    numOfClusters = clusterInfo.numOfClusters;

%    sC = som_cllinkage(sMap,'ward');
%    figure; %show classification tree
%    som_clplot(sC);
    
    models_activated = cell(0);
    total = size(big_range_data, 1);
    models_activated(1) = {[0, 100, mean(1:num_models)]};
    for i = 1:models_length
        model_slice = squeeze(big_range_data(:,i));
        value = mean(model_slice);
        models_activated(i+1) = {[i, 0, value]};
    end
    models_activated = mysort(models_activated, 2);
    
    cellToMatrix(models_activated)
    %celldisp(models_activated);

    [Bmus, Qerrors] = som_bmus(sMap, sD);
    % 'Bmus' knows which neuron owns each grid point.
    
    Cmus = Bmus;
    for i = 1:length(Bmus)
        Cmus(i) = clusterMap(Bmus(i));
    end
    
    BmusTwoDims = NaN(length(y), length(x));
    for i = 1:length(gridpoints)
        [dim1, dim2] = index2grid(gridpoints(i), dims);
        BmusTwoDims(dim1, dim2) = Cmus(i);
    end
    
    ShowingMUS = Cmus; %Bmus
    numColors = numOfClusters; %size(sMap.codebook, 1)
    
    display_neurons_in_world_map(numColors, x, y, dims, gridpoints, ShowingMUS);

    %Find out which model has a significance in each neuron.
    cluster_models = cell(0);
    for i = 1:numOfClusters %total_neurons
        points = find(Cmus == i);
        totalPointsSize = length(points);
        if totalPointsSize == 0
            %no grid points belong to this neuron
            cluster_models(i) = {[]};
        else
            
            models_activated = cell(0);
            total = totalPointsSize;
            models_activated(1) = {[0, 100, mean(1:num_models)]};
            for j = 1:models_length
                model_slice = squeeze(big_range_data(:,j));
                summ = mean(model_slice(points));
                %summ = 0;
                %for k = 1:length(points)
                %    if model_slice(points(k)) == 1
                %        summ = summ + 1;
                %    end
                %end
                models_activated(j+1) = {[j, 0, summ]};
            end
            models_activated = mysort(models_activated, 2);
            cluster_models(i) = {models_activated};
            
            
%             totalPositivePointsActivated = zeros(models_length);
%             totalNegativePointsActivated = zeros(models_length);
%             for j = 1:length(points)
%                 for k = 1:models_length
%                     if big_range_data(points(j), k) == 1
%                         totalPositivePointsActivated(k) = totalPositivePointsActivated(k) + 1;
%                     elseif big_range_data(points(j), k) == -1
%                         totalNegativePointsActivated(k) = totalNegativePointsActivated(k) + 1;
%                     end
%                 end
%             end
%             avg = 6; %floor(25 * models_length / 100) / models_length;
%             
%             
%             %TODO: probar de leer para los valores negativos y comparar con
%             %-3/14 (o -4/14) y ver que modelos quedan. Luego hacer lo mismo
%             %para los valores positivos.
%             
%             % probar con Scatterplot para ver relacion entre precip y temp.
%             % Probar con 3 dimensiones: grilla x modelo x [t p]
%             % Probar con [x y temp precip] donde cada muestra es un punto
%             % de grilla de un modelo, o sea que la cantidad de muestras
%             % serian 72x144x14 (menos los NaN).
%             
%             models = [];
%             for k = 1:models_length
%                 [phatPos, pciPos] = binofit(totalPositivePointsActivated(k), totalPointsSize);
%                 %[phatNeg, pciNeg] = binofit(totalNegativePointsActivated(k), totalPointsSize);
%                 %if (phatPos > avg && pciPos(1) > avg) || (phatNeg > avg && pciNeg(1) > avg)
%                 if (phatPos > avg && pciPos(1) > avg)
%                     models = [models, k];
%                 end
%             end
%             cluster_models(i) = {models};
        end
    end
    
    %display on console

    %celldisp(cluster_models);
    
    for i = 1:length(cluster_models)
        cluster_models{i} = cellToMatrix(cluster_models{i});
    end
    %display on map
    %display_neurons_data_differences_in_world_map(tas_big_data, total_neurons, x, y, dims, gridpoints, Bmus, cluster_models);
    
    cluster_masks = bmusToMasks(BmusTwoDims);
    
    fileout = [scen '_' cvar '_' num2str(numOfClusters) 'cluster_data_' num2str(month) '_' modeltype '.mat'];
    save(fileout,'sD', 'sMap', 'cluster_models', 'cluster_masks');
end

function cluster_masks = bmusToMasks(BmusTwoDims)
    clusters = unique(BmusTwoDims); 
    clusters(find(isnan(clusters))) = []; % remove NaNs
    cluster_masks = zeros([length(clusters) size(BmusTwoDims)]);
    for i = 1:length(clusters)
        cluster_masks(i, find(BmusTwoDims == clusters(i))) = 1;
    end
end

function display_neurons_in_world_map(total_neurons, x, y, dims, gridpoints, Bmus)
    load coast_world;

    % Begin world map drawing...
    x=circshift(x,[72 1]);
    x(1:72)=x(1:72)-360;
    x = x - 0.5;
    [plon,plat] = meshgrid(x,y);
    
    BmusTwoDims = NaN(length(y), length(x));
    
    for i = 1:length(gridpoints)
       [dim1, dim2] = index2grid(gridpoints(i), dims);
        BmusTwoDims(dim1, dim2) = Bmus(i);
    end
    
    BmusTwoDims=circshift(BmusTwoDims, [0 72]);
    
    cmin = 1;
    cmax = total_neurons + cmin;
    ncol = total_neurons; %one color for each neuron.

%     for i = 1:total_neurons
%         %figure; %a4l;
%             map_global; 
%         tightmap;
%         title ( ['neuron ' num2str(i)] );
%         data_i = BmusTwoDims;
%         data_i(find(BmusTwoDims ~= i)) = NaN;
%         pcolorm(plat, plon, data_i);
%         caxis([cmin, cmax]);
%         cmap=colormap(jet(ncol)); % set N. of colors.
%         colormap(cmap);
%         colorbar('horizon');
%         %shading interp;
%         hold on;
%         % Re-Draw the map
%         if (worldMap == 1)
%             plotm(latW, lonW, 'k');
%         else
%             plotm(latsa, lonsa, 'k');
%         end
%         hold on;
%         drawnow;
%     end
    figure; %a4l;
    map_globe;
    tightmap;
    title ('all neurons');
    pcolorm(plat, plon, BmusTwoDims);
    caxis([cmin, cmax]);
    cmap=colormap(jet(ncol)); % set N. of colors.
    colormap(cmap);
    colorbar('horizon');
    %shading interp;
    hold on;
    % Re-Draw the map
    plotm(latW, lonW, 'k');
    hold on;
    drawnow;
end

function display_neurons_data_differences_in_world_map(big_data, total_neurons, x, y, dims, gridpoints, Bmus, cluster_models)
    load coast_world;

    [plon,plat] = meshgrid(x,y);
    for i = 1:length(cluster_models)
        neuron = cluster_models(i);
        neuron_values = Bmus;
        neuron_values(find(Bmus ~= i)) = NaN;
        for models = neuron
            models = models{1};
            for j = 1:length(models)
                figure; %a4l;
                map_global; 
                tightmap;
                
                data = NaN(length(y), length(x));
                for k = 1:length(gridpoints)
                    if ~isnan(neuron_values(k))
                        [dim1, dim2] = index2grid(gridpoints(k), dims);
                        data(dim1, dim2) = big_data(gridpoints(k), models(j));
                    end
                end
                cmin = squeeze(min(min(data)));
                cmax = squeeze(max(max(data)));
                ncol = 20;
                title ([ 'neuron ' num2str(i) ' - model ' num2str(models(j)) ]);
                pcolorm(plat, plon, data);
                caxis([cmin, cmax+1]);
                cmap=colormap(jet(ncol)); % set N. of colors.
                colormap(cmap);
                colorbar('horizon');
                %shading interp;
                hold on;
                % Re-Draw the map
                if (worldMap == 1)
                    plotm(latW, lonW, 'k');
                else
                    plotm(latsa, lonsa, 'k');
                end
                hold on;
                drawnow;
            end
        end
    end
end

function m = dobleCellToMatrix(cell_with_cell)
    m = [];
    for i = 1:length(cell_with_cell)
        z = zeros(size(m,1),1);
        m = [m, z, cellToMatrix(cell_with_cell{i})];
    end
end

function m = cellToMatrix(cell_with_array)
    m = [];
    for i = 1:length(cell_with_array)
        m(i,:) = cell_with_array{i};
    end
end

function cell_with_array = mysort(cell_with_array, array_column)
    if length(cell_with_array) > 1
        for i = fliplr(2:length(cell_with_array))
            for j = 2:i
                if cell_with_array{j}(array_column) > cell_with_array{j-1}(array_column)
                    tmp = cell_with_array{j};
                    cell_with_array{j} = cell_with_array{j-1};
                    cell_with_array{j-1} = tmp;
                end
            end
        end
    end
end

function indices = setMasks(masks, gridpoints, dims)
    load('land_masks.mat');
    if isempty(masks)
        %empty mask
        finalmask = ones(dims(1), dims(2));
    elseif length(masks) == 1 && strcmp(masks{1}, 'land') == 1
        finalmask = land_mask;
    else
        finalmask = zeros(dims(1), dims(2));
        for i = 1:length(masks)
            if strcmp(masks{i}, 'southamerica') == 1
                finalmask = finalmask | southamerica_mask;
            elseif strcmp(masks{i}, 'northamerica') == 1
                finalmask = finalmask | northamerica_mask;
            elseif strcmp(masks{i}, 'europe') == 1
                finalmask = finalmask | europe_mask;
            elseif strcmp(masks{i}, 'siberia') == 1
                finalmask = finalmask | siberia_mask;
            elseif strcmp(masks{i}, 'india') == 1
                finalmask = finalmask | india_mask;
            elseif strcmp(masks{i}, 'australia') == 1
                finalmask = finalmask | australia_mask;
            elseif strcmp(masks{i}, 'africa') == 1
                finalmask = finalmask | africa_mask;
            elseif strcmp(masks{i}, 'north') == 1
                finalmask = finalmask | north_mask;
            elseif strcmp(masks{i}, 'south') == 1
                finalmask = finalmask | south_mask;
            end
        end
        if containsMask(masks, 'land')
            mask = mask & land_mask;
        elseif containsMask(masks, 'ocean')
            mask = mask & ocean_mask;
        end
    end
    indices = [];
    for i = 1:length(gridpoints)
        [lat, lon] = index2grid(gridpoints(i), dims);
        if finalmask(lat, lon) == 0
            indices = cat(1, indices, i);
        end
    end
end

function ret = containsMask(masks, masknameString) 
    ret = sum(strcmp(masks, masknameString)) > 0;
end