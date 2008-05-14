%Made by Luciano, so you know whom to address for errors.

%month 0: annual mean; 1 - 12: actual month
%difftype -1: "1975-2000 (model)" - "1975-2000 (obs)"
% n >= 1000 : "12-n+12 (model)" - "1975-2000 (model)"
% 1 >= n > 1000: "n degree (model)" - "1975-2000 (model)"
%worldMap 0: build only South-American Map; 1: build full World Map.
%msize: neuron map size (two dimensional)
function [sD, sMap, sC, neuron_models, BmusTwoDims] = som_binary_data(scen, month, difftype, worldMap, msize)
    if worldMap == 1
        mapname = 'world';
    else
        mapname = 'sa';
    end
    
    if difftype < 0
        modeltype = '20th-_obs';
    elseif difftype >= 1000
        modeltype = ['21th-Year' num2str(difftype) '-_20th'];
    else
        modeltype = ['21th-Degree' num2str(difftype) 'deg-_20th'];
    end

    load([scen '_binary_data_' num2str(month) '_' mapname '_' modeltype '.mat'], 'pr_big_data', 'tas_big_data', 'gridpoints', 'dims', 'worldMap', 'pr_big_binary_data', 'tas_big_binary_data');

    %generate map
    %sD = som_data_struct([pr_big_data, tas_big_data]);

    filecmap='precip_7903_meansc.mat'; % use this file to load the map grid
    load(filecmap, 'xgrid','ygrid');
    x = xgrid;
    x = x-1.25;
    y = ygrid;
    y = y-1.25;
    
    
%     pr25 = NaN(length(gridpoints),1);
%     pr75 = NaN(length(gridpoints),1);
%     tas25 = NaN(length(gridpoints),1);
%     tas75 = NaN(length(gridpoints),1);
%     
%     for i = 1:length(gridpoints)
%         %[dim1, dim2] = index2grid(gridpoints(i), dims);
%         slice = pr_big_data(gridpoints(i), :);
%         [avg, sigma] = normfit(slice);
%         sort_slice = sort(slice);
%         pr25(i) = sum(sort_slice < norminv(0.25, avg, sigma));
%         pr75(i) = sum(sort_slice > norminv(0.75, avg, sigma));
%         
%         slice = tas_big_data(gridpoints(i), :);
%         [avg, sigma] = normfit(slice);
%         sort_slice = sort(slice);
%         tas25(i) = sum(sort_slice < norminv(0.25, avg, sigma));
%         tas75(i) = sum(sort_slice > norminv(0.75, avg, sigma));
%     end
%     display_neurons_in_world_map(worldMap, 7, x, y, dims, gridpoints, pr25);
%     display_neurons_in_world_map(worldMap, 7, x, y, dims, gridpoints, pr75);
%     display_neurons_in_world_map(worldMap, 7, x, y, dims, gridpoints, tas25);
%     display_neurons_in_world_map(worldMap, 7, x, y, dims, gridpoints, tas75);
%     return;
    
%     lillie10pr = NaN(length(gridpoints),1);
%     lillie5pr = NaN(length(gridpoints),1);
%     lillie10tas = NaN(length(gridpoints),1);
%     lillie5tas = NaN(length(gridpoints),1);
%     for i = 1:length(gridpoints)
%         lillie10pr(gridpoints(i)) = lillietest(pr_big_data(gridpoints(i), :), 0.1);
%         lillie5pr(gridpoints(i)) = lillietest(pr_big_data(gridpoints(i), :), 0.05);
%         lillie10tas(gridpoints(i)) = lillietest(tas_big_data(gridpoints(i), :), 0.1);
%         lillie5tas(gridpoints(i)) = lillietest(tas_big_data(gridpoints(i), :), 0.05);
%     end
%     display_neurons_in_world_map(worldMap, 2, x, y, dims, gridpoints, lillie10pr);
%     display_neurons_in_world_map(worldMap, 2, x, y, dims, gridpoints, lillie5pr);
%     display_neurons_in_world_map(worldMap, 2, x, y, dims, gridpoints, lillie10tas);
%     display_neurons_in_world_map(worldMap, 2, x, y, dims, gridpoints, lillie5tas);
%     return;

%     display_neurons_in_world_map(worldMap, 20, x, y, dims, gridpoints, pr_big_data);
%     display_neurons_in_world_map(worldMap, 20, x, y, dims, gridpoints, tas_big_data);
%     return;
    
    big_binary_data = tas_big_binary_data;

    %big_binary_data(find(big_binary_data == 0)) = 2;
    big_binary_data(find(big_binary_data == 1)) = 0;
    big_binary_data(find(big_binary_data == -1)) = 1;
    %big_binary_data(find(big_binary_data == 2)) = 1;
    
    models_length = size(big_binary_data, 2);
    
    sD = som_data_struct(big_binary_data);
    
    if isempty(msize)
        sMap = som_make(sD);        
    else
        sMap = som_make(sD, 'msize', msize);
    end

    %figure; %show neuron map
    %som_show(sMap);
    
    %generate automatic classification
    
    sC = som_cllinkage(sMap,'ward');
 
    %clusterInfo = ClusterizarMapa(sMap, sD, NaN, 'fitBest');
    clusterInfo = ClusterizarMapa(sMap, sD, 6, 'bars');
    clusterMap = clusterInfo.clusterMap;
    numOfClusters = clusterInfo.numOfClusters;
    
%    figure; %show classification tree
%    som_clplot(sC);
    
    models_activated = cell(0);
    total = size(big_binary_data, 1);
    models_activated(1) = {[0, 100, total]};
    for i = 1:models_length
        model_slice = squeeze(big_binary_data(:,i));
        value = sum(model_slice);
        models_activated(i+1) = {[i, round(value*100/total), value]};
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
    
    display_neurons_in_world_map(worldMap, numColors, x, y, dims, gridpoints, ShowingMUS);

    %Find out which model has a significance in each neuron.
    neuron_models = cell(0);
    for i = 1:numOfClusters %total_neurons
        points = find(Cmus == i);
        totalPointsSize = length(points);
        if totalPointsSize == 0
            %no grid points belong to this neuron
            neuron_models(i) = {[]};
        else
            
            models_activated = cell(0);
            total = totalPointsSize;
            models_activated(1) = {[0, 100, total]};
            for j = 1:models_length
                model_slice = squeeze(big_binary_data(:,j));
                summ = 0;
                for k = 1:length(points)
                    if model_slice(points(k)) == 1
                        summ = summ + 1;
                    end
                end
                models_activated(j+1) = {[j, round(summ*100/total), summ]};
            end
            models_activated = mysort(models_activated, 2);
            neuron_models(i) = {models_activated};
            
            
%             totalPositivePointsActivated = zeros(models_length);
%             totalNegativePointsActivated = zeros(models_length);
%             for j = 1:length(points)
%                 for k = 1:models_length
%                     if big_binary_data(points(j), k) == 1
%                         totalPositivePointsActivated(k) = totalPositivePointsActivated(k) + 1;
%                     elseif big_binary_data(points(j), k) == -1
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
%             neuron_models(i) = {models};
        end
    end
    
    %display on console

    %celldisp(neuron_models);
    
    for i = 1:length(neuron_models)
        neuron_models{i} = cellToMatrix(neuron_models{i});
    end
    %display on map
    %display_neurons_data_differences_in_world_map(tas_big_data, worldMap, total_neurons, x, y, dims, gridpoints, Bmus, neuron_models);
    
end

function display_neurons_in_world_map(worldMap, total_neurons, x, y, dims, gridpoints, Bmus)
    if (worldMap == 1)
        load coast_world;
    else
        load coast_sa;
    end

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
    
    BmusTwoDims=circshift(BmusTwoDims,[0 72]);
    
    cmin = 1;
    cmax = total_neurons + 1;
    ncol = total_neurons; %one color for each neuron.

%     for i = 1:total_neurons
%         %figure; %a4l;
%         if (worldMap == 1)
%             map_global; 
%         else
%             map_sa;
%         end
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
    if (worldMap == 1)
        map_globe; 
    else
        map_sa;
    end
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
    if (worldMap == 1)
        plotm(latW, lonW, 'k');
    else
        plotm(latsa, lonsa, 'k');
    end
    hold on;
    drawnow;
end

function display_neurons_data_differences_in_world_map(big_data, worldMap, total_neurons, x, y, dims, gridpoints, Bmus, neuron_models)
    if (worldMap == 1)
        load coast_world;
    else
        load coast_sa;
    end

    x=circshift(x,[72 1]);
    x(1:72)=x(1:72)-360;
    x = x - 0.5;
    [plon,plat] = meshgrid(x,y);
    for i = 1:length(neuron_models)
        neuron = neuron_models(i);
        neuron_values = Bmus;
        neuron_values(find(Bmus ~= i)) = NaN;
        for models = neuron
            models = models{1};
            for j = 1:length(models)
                figure; %a4l;
                if (worldMap == 1)
                    map_globe; 
                else
                    map_sa;
                end
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
                data = circshift(data, [0 72]);
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