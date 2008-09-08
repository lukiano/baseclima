%Made by Luciano, so you know whom to address for errors.

%month 0: annual mean; 1 - 12: actual month
%difftype:
% n >= 1000 : "12-n+12 (model)" - "around year20 year(model)"
% 1 >= n > 1000: "n degree (model)" - "around year20 year (model)"
%masks: cell array with string with mask names. Ex: {'southamerica','africa'}
%msize: neuron map size (two dimensional)
%numOfClusters: number of clusters; 'auto'
function [sD, sMap, cluster_models, BmusTwoDims] = som_range_data(scen, cvar, month, difftype, year20, masks, msize, numOfClusters)
    
    if difftype >= 1000
        modeltype = ['Year' num2str(difftype) '-Year' num2str(year20)];
    else
        modeltype = ['Degree' num2str(difftype) '-_Year' num2str(year20)];
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
    
    %normalize
    big_range_data = normalizeData(big_range_data);
    
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
    clusterMap = clusterInfo.clusterMap;
    number_of_clusters = clusterInfo.numOfClusters;
    number_of_neurons = size(sMap.codebook, 1);
    number_of_models = size(big_range_data, 2);

    points = 1:size(big_range_data, 1);
    models_activated = fillCluster(points, number_of_models, big_range_data)
    
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
    
    display_neurons_in_SA_map(number_of_clusters, x, y, dims, gridpoints, Cmus);
    %display_neurons_in_SA_map(number_of_neurons, x, y, dims, gridpoints, Bmus);

    cluster_models = getClusterModels(number_of_clusters, Cmus, big_range_data, number_of_models);
   
    %display on console
    cluster_models
    
    %neuron_models = getClusterModels(number_of_neurons, Bmus, big_range_data, number_of_models);

    %display on console
    %neuron_models

    cluster_masks = bmusToMasks(BmusTwoDims);
    
    fileout = [scen '_' cvar '_' num2str(number_of_clusters) 'cluster_data_' num2str(month) '_' modeltype '.mat'];
    save(fileout,'sD', 'sMap', 'cluster_models', 'cluster_masks');
end

function cluster_models = getClusterModels(number_of_clusters, Cmus, big_range_data, num_models)
    %Find out which model has a significance in each or cluster
    cluster_models = NaN(num_models + 1, 3, number_of_clusters);
    for i = 1:number_of_clusters
        points = find(Cmus == i);
        cluster_models(:,:,i) = fillCluster(points, num_models, big_range_data);
    end
end

function m = fillCluster(points, num_models, big_range_data)
    totalPointsSize = length(points);
    if totalPointsSize == 0
        %no grid points belong to this cluster
        m = NaN(num_models + 1, 3);
        m(1,:) = [0, 100, mean(1:num_models)];
    else
        models_activated = NaN(num_models + 1, 3);
        models_activated(1,:) = [0, 100, mean(1:num_models)];
        for j = 1:num_models
            model_slice = squeeze(big_range_data(:,j));
            summ = mean(model_slice(points));
            models_activated(j+1, :) = [j, 0, summ];
        end
        models_activated = mysort(models_activated, 3);
        m = models_activated;
    end
end

function cluster_masks = bmusToMasks(BmusTwoDims)
    clusters = unique(BmusTwoDims); 
    clusters(find(isnan(clusters))) = []; % remove NaNs
    cluster_masks = zeros([length(clusters) size(BmusTwoDims)]);
    for i = 1:length(clusters)
        cluster_masks(i, find(BmusTwoDims == clusters(i))) = 1;
    end
end

function big_range_data = normalizeData(big_range_data)
    mean_data = mean(big_range_data, 2);
    std_data = std(big_range_data, 0, 2);
    num_models = size(big_range_data, 2);
    
    mean_data = repmat(mean_data, 1, num_models);
    std_data = repmat(std_data, 1, num_models);
    big_range_data = big_range_data - mean_data;
    big_range_data = big_range_data ./ std_data;
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
        BmusTwoDims(dim1, dim2) = Bmus(i) - 1;
    end
    
    BmusTwoDims=circshift(BmusTwoDims, [0 72]);
    
    cmin = 0;
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

function display_neurons_in_SA_map(total_neurons, x, y, dims, gridpoints, Bmus)
    load coast_sa;

    % Begin southamerica map drawing...
    x=circshift(x,[72 1]);
    x(1:72)=x(1:72)-360;
    x = x - 0.5;
    [plon,plat] = meshgrid(x,y);
    
    BmusTwoDims = NaN(length(y), length(x));
    
    for i = 1:length(gridpoints)
       [dim1, dim2] = index2grid(gridpoints(i), dims);
        BmusTwoDims(dim1, dim2) = Bmus(i) - 1;
    end
    
    BmusTwoDims=circshift(BmusTwoDims, [0 72]);
    
    cmin = 0;
    cmax = total_neurons + cmin;
    ncol = total_neurons; %one color for each neuron.

    figure; %a4l;
    map_sa;
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
    plotm(latsa, lonsa, 'k');
    hold on;
    drawnow;
end

function matrix = mysort(matrix, column)
    matrix = sortrows(matrix, -column);
end

function matrix = mysort2(matrix, column)
    if size(matrix, 1) > 1
        for i = fliplr(2:size(matrix, 1))
            for j = 2:i
                if matrix(j, column) > matrix(j-1, column)
                    tmp = matrix(j, :);
                    matrix(j, :) = matrix(j-1, :);
                    matrix(j-1, :) = tmp;
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
            finalmask = finalmask & land_mask;
        elseif containsMask(masks, 'ocean')
            finalmask = finalmask & ocean_mask;
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