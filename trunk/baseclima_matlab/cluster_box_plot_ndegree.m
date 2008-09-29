%scen: 'sresa2' or 'sresa1b'
%cvar: 'tas', 'pr' or 'sic'
%ndegree: number of degrees from 20th century values
%year20: average centered year of 20th century
%type:
%1) absolute 21th century values
%2) difference between 21th century and 20th century values
%3) absolute 20th century values
%4) difference between 20th century and observed values (same period)
%month 0: annual mean; 1 - 12: actual month
%regional_masks: cell array with string with mask names. Ex: {'southamerica','africa'}
%cluster_filename: name of file created by som_range_data to be used for cluster masks
%cluster_number_mask: number of the cluster to be used as a mask
function cluster_box_plot_ndegree(scen, cvar, ndegree, year20, type, month, regional_masks, cluster_filename, number_of_masks)
    %dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
    %dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
    dirString = uigetdir('./modelos','Choose data directory');
    if (dirString == 0)
        % no directory was chosen, exit program
        return;
    end
    [big_data, modelnames_orig] = create_big_data(scen, cvar, dirString, month, ndegree, year20, type);
    modelnames = cell(length(modelnames_orig), 1);
    for i = 1:length(modelnames_orig)
        modelnames{i} = regexprep(modelnames_orig{i},'\_','\\_');
    end
    
    for i = 1:number_of_masks
        mask = getMasks(regional_masks, cluster_filename, i);
        sum_array(:, i) = sum_data_norm(big_data, mask);
    end
    figure;
    hold on;
    number_of_models = size(sum_array, 1);
    
%     half = floor(number_of_models / 2);
%     quarter = floor(number_of_models / 4);
    %cmap=colormap;
    %cmap=colormap(hsv(number_of_models));
    map = [0 .8 0; 1 0 1; 0 .8 .8; 0 0 1; .5 .5 .5];
%     map = map(1:quarter, :);
    cmap = cat(1, map, map, map, map);
    colormap(cmap);
    for j = 1:number_of_masks
        arr = sum_array(:, j);
        [sorted_arr, indices] = sort(arr);
        for i = 1:5
            x = mod(find(indices == i),3)-1;
            scatter(j+x*.2, sum_array(i,j),[], cmap(i,:), 'filled');
        end
        for i = 6:10
            x = mod(find(indices == i),3)-1;
            scatter(j+x*.2, sum_array(i,j),[], cmap(i,:),'x');
        end
        for i = 11:15
            x = mod(find(indices == i),3)-1;
            scatter(j+x*.2, sum_array(i,j),[], cmap(i,:),'*');
        end
        for i = 16:17
            x = mod(find(indices == i),3)-1;
            scatter(j+x*.2, sum_array(i,j),[], cmap(i,:), 'd');
        end
    end
    legend(modelnames,'location','EastOutside');

    boxplot(sum_array);
    xlabel('Clusters');
    
    if type == 1
        ylabel([cvar ' (avg. year ' num2str(ndegree) ' degree values)']);
    elseif type == 2
        ylabel([cvar ' ' num2str(ndegree) ' degrees - ' num2str(year20)]);
    elseif type == 3
        ylabel([cvar ' (avg. year ' num2str(year20) ' values)']);
    elseif type == 4
        ylabel([cvar ' ' num2str(year20) ' - obs ' num2str(year20)]);
    end
    
    titulo = [scen ' - degrees:' num2str(ndegree)];
    if ~isempty(regional_masks)
        titulo = [titulo ' - Reg. Masks:'];
        for i = 1:length(regional_masks)
            titulo = [titulo regional_masks{i} ' '];
        end
    end
%     if ~isempty(cluster_filename)
%         titulo = [titulo ' - Cluster Mask: ' regexprep(cluster_filename,'\_','\\_')];
%     end
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

function [big_data, modelnames] = create_big_data(scen, cvar, dirString, month, ndegree, year20, type)
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_' scen '_.*_' num2str(ndegree) 'degree.mat']);
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
            fullname = fullfile(dirString, [cvar '_obs_cru_run1_year' num2str(year20) '.mat']);
            % c3m_data is 12 x LATS x LONS
            struc_obs = load(fullname, 'data');
            obs_data = struc_obs.data;
            if (month == 0)
                obs_data = squeeze(mean(obs_data, 1));
            else
                obs_data = squeeze(obs_data(month, :, :));
            end
            data_difference = c3m_data - obs_data;
            %add variable as a new dimension of big_data
            big_data = cat(3, big_data, data_difference);
            clear sresa2_data;
            clear c3m_data;
            clear obs_data;
        else
            ['error! Unrecognized type: ' type]
        end
    end
end

