%Made by Luciano, so you know whom to address for errors.

%scen: 'sresa2' or 'sresa1b'
%cvar_x: 'tas', 'pr' or 'sic'
%cvar_y: 'tas', 'pr' or 'sic'
%year21: average centered year of 21th century
%year20: average centered year of 20th century
%type_x:
%1) absolute 21th century values
%2) difference between 21th century and 20th century values
%3) absolute 20th century values
%4) difference between 20th century and observed values (same period)
%type_y: same as type_x for y axis
%month 0: annual mean; 1 - 12: actual month
%regional_masks: cell array with string with mask names. Ex: {'southamerica','africa'}
%cluster_filename: name of file created by som_range_data to be used for cluster masks
%cluster_number_mask: number of the cluster to be used as a mask
function comparison_plot(scen, cvar_x, cvar_y, year21, year20, type_x, type_y, month, regional_masks, cluster_filename, cluster_number_mask)
    dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
    %dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
    if (dirString == 0)
        % no directory was chosen, exit program
        return;
    end
    [big_data_x, modelnames_x] = create_big_data(scen, cvar_x, dirString, month, year21, year20, type_x);
    [big_data_y, modelnames_y] = create_big_data(scen, cvar_y, dirString, month, year21, year20, type_y);
    if length(modelnames_x) ~= length(modelnames_y)
        'Cvar X has not the same models as Cvar Y'
        return;
    end
    modelnames = cell(length(modelnames_x), 1);
    for i = 1:length(modelnames_x)
        if strcmp(modelnames_x{1}, modelnames_y{1}) == 0
            'Cvar X has not the same models as Cvar Y'
            return;
        end    
        modelnames{i} = regexprep(modelnames_x{i},'\_','\\_');
    end
    
    mask = getMasks(regional_masks, cluster_filename, cluster_number_mask);

    sum_array_x = sum_data_norm(big_data_x, mask);
    sum_array_y = sum_data_norm(big_data_y, mask);
    
    figure;
    hold on
    half = floor(length(modelnames_x) / 2);
    quarter = floor(length(modelnames_x) / 4);
    for i = 1:quarter
        scatter(sum_array_x(i), sum_array_y(i));
    end
    for i = quarter+1:half
        scatter(sum_array_x(i), sum_array_y(i), 'filled');
    end
    for i = half+1:half+quarter
        scatter(sum_array_x(i), sum_array_y(i),'*');
    end
    for i = half+quarter+1:length(modelnames_x)
        scatter(sum_array_x(i), sum_array_y(i),'+');
    end
    
    legend(modelnames,'location','EastOutside');
    if type_x == 1
        xlabel([cvar_x '(diff with avg. 20th century values)']);
    elseif type_x == 2
        xlabel([cvar_x '(avg. 20th century values)']);
    else
        xlabel(cvar_x);
    end
    if type_y == 1
        ylabel([cvar_y '(diff with avg. 20th century values)']);
    elseif type_y == 2
        ylabel([cvar_y '(avg. 20th century values)']);
    else
        ylabel(cvar_y);
    end
    titulo = [scen ' - year:' num2str(year)];
    if ~isempty(regional_masks)
        titulo = [titulo ' - Reg. Masks:'];
        for i = 1:length(regional_masks)
            titulo = [titulo regional_masks{i}];
        end
    end
    if ~isempty(cluster_filename)
        titulo = [titulo ' - Cluster Mask:' num2str(cluster_number_mask)];
    end
    title(titulo);
    grid on;
    hold off;
    drawnow;
end

function sum_array = sum_data(big_data, mask)
    sum_array = zeros(size(big_data, 3), 1);
    for i = 1:size(big_data, 3)
        data = squeeze(big_data(:, :, i));
        data(find(isnan(data))) = 0;
        data = data .* mask;
        sum_array(i) = sum(sum(data, 2), 1);
    end
end

function sum_array = sum_data_norm(big_data, mask)
    sum_array = zeros(size(big_data, 3), 1);
    load('weightMatrix.mat', 'weight_matrix');
    norm = sum(sum(weight_matrix .* mask, 2), 1);
    for i = 1:size(big_data, 3)
        data = squeeze(big_data(:, :, i));
        data(find(isnan(data))) = 0;
        data = data .* mask;
        data = data .* weight_matrix;
        sum_array(i) = sum(sum(data, 2), 1) / norm;
    end
end

function sum_array = mean_data(big_data, mask)
    sum_array = zeros(size(big_data, 3), 1);
    for i = 1:size(big_data, 3)
        data = squeeze(big_data(:, :, i));
        data(find(isnan(data))) = 0;
        data = data .* mask;
        sum_array(i) = mean(mean(data, 2), 1);
    end
end

function mask = getMasks(regional_masks, cluster_filename, cluster_number_mask)
    load('land_masks.mat');
    if isempty(regional_masks)
        %transparent mask
        mask = ones(size(land_mask));
    elseif length(regional_masks) == 1 && strcmp(regional_masks{1}, 'land') == 1
        mask = land_mask;
    else
        mask = zeros(size(land_mask));
        for i = 1:length(regional_masks)
            if strcmp(regional_masks{i}, 'southamerica') == 1
                mask = mask | southamerica_mask;
            elseif strcmp(regional_masks{i}, 'northamerica') == 1
                mask = mask | northamerica_mask;
            elseif strcmp(regional_masks{i}, 'europe') == 1
                mask = mask | europe_mask;
            elseif strcmp(regional_masks{i}, 'siberia') == 1
                mask = mask | siberia_mask;
            elseif strcmp(regional_masks{i}, 'india') == 1
                mask = mask | india_mask;
            elseif strcmp(regional_masks{i}, 'australia') == 1
                mask = mask | australia_mask;
            elseif strcmp(regional_masks{i}, 'africa') == 1
                mask = mask | africa_mask;
            elseif strcmp(regional_masks{i}, 'north') == 1
                mask = mask | north_mask;
            elseif strcmp(regional_masks{i}, 'south') == 1
                mask = mask | south_mask;
            end
        end
        if containsMask(regional_masks, 'land')
            mask = mask & land_mask;
        elseif containsMask(regional_masks, 'ocean')
            mask = mask & ocean_mask;
        end
    end
    
    if ~isempty(cluster_filename)
        load(cluster_filename, 'cluster_masks');
        if cluster_number_mask > 0 && cluster_number_mask <= size(cluster_masks, 1)
            mask = mask & squeeze(cluster_masks(cluster_number_mask, :, :));
        end
    end
end

function ret = containsMask(masks, masknameString) 
    ret = sum(strcmp(masks, masknameString)) > 0;
end

function [big_data, modelnames] = create_big_data(scen, cvar, dirString, month, year21, year20, type)
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_' scen '_.*_year' num2str(year21) '.mat']);
    contador = 1;
    truenames = cell(0);
    for i = 1:length(rexp)
        if not(isempty(rexp{i}))
            truenames(contador) = {names{i}};
            contador = contador + 1;
        end
    end

    %for each file, load the data variable
    big_data = [];
    contador = 1;
    modelnames = cell(0);
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        struc_Sresa2 = load(fullname, 'data', 'model', 'x', 'y');

        % sresa2_data is 12 x LATS x LONS
        sresa2_data = struc_Sresa2.data;
        if (month == 0)
            sresa2_data = squeeze(mean(sresa2_data, 1));
        else
            sresa2_data = squeeze(sresa2_data(month, :, :));
        end
        
        modelnames(contador) = {struc_Sresa2.model};
        contador = contador + 1;
        
        if type == 1 %21 century
            %add variable as a new dimension of big_data
            big_data = cat(3, big_data, sresa2_data);
            clear sresa2_data;
        elseif type == 2 % diff between 21 and 20 century
            fullname = fullfile(dirString, [cvar '_20c3m_' struc_Sresa2.model '_run1_year' num2str(year20) '.mat']);
            % c3m_data is 12 x LATS x LONS
            struc_20c3m = load(fullname, 'data');
            c3m_data = struc_20c3m.data;
            if (month == 0)
                c3m_data = squeeze(mean(c3m_data, 1));
            else
                c3m_data = squeeze(c3m_data(month, :, :));
            end
            data_difference = sresa2_data - c3m_data;
            %add variable as a new dimension of big_data
            big_data = cat(3, big_data, data_difference);
            clear sresa2_data;
            clear c3m_data;
        elseif type == 3 % 20 century
            fullname = fullfile(dirString, [cvar '_20c3m_' struc_Sresa2.model '_run1_year' num2str(year20) '.mat']);
            % c3m_data is 12 x LATS x LONS
            struc_20c3m = load(fullname, 'data');
            c3m_data = struc_20c3m.data;
            if (month == 0)
                c3m_data = squeeze(mean(c3m_data, 1));
            else
                c3m_data = squeeze(c3m_data(month, :, :));
            end
            %add variable as a new dimension of big_data
            big_data = cat(3, big_data, c3m_data);
            clear sresa2_data;
            clear c3m_data;
        elseif type == 4 % diff between 20 century and obs values
            fullname = fullfile(dirString, [cvar '_20c3m_' struc_Sresa2.model '_run1_year' num2str(year20) '.mat']);
            % c3m_data is 12 x LATS x LONS
            struc_20c3m = load(fullname, 'data');
            c3m_data = struc_20c3m.data;
            if (month == 0)
                c3m_data = squeeze(mean(c3m_data, 1));
            else
                c3m_data = squeeze(c3m_data(month, :, :));
            end
            %add variable as a new dimension of big_data
            big_data = cat(3, big_data, c3m_data);
            clear sresa2_data;
            clear c3m_data;
        else
            ['error! Unrecognized type: ' type]
        end
    end
end

