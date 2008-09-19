%Made by Luciano, so you know whom to address for errors.

%month 0: annual mean; 1 - 12: actual month
%difftype:
% n >= 1000 : "12-n+12 (model)" - "around year20 year(model)"
% 1 >= n > 1000: "n degree (model)" - "around year20 year (model)"
%use_range 0: put original values of models; 1: sort models and put their resulting rank
function build_range_data(scen, cvar, month, difftype, year20, use_range) 
    %dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
    %dirString = uigetdir('c:\eclipse\workspace\baseclima_matlab','Choose data directory');
    dirString = uigetdir('./modelos','Choose data directory');
    if (dirString == 0)
        % no directory was chosen, exit program
        return;
    end

    [big_data, dims, modelnames] = create_big_data(scen, difftype, year20, cvar, dirString, month);
    gridpoints = 1:size(big_data, 1);
    gridpoints = gridpoints';
    gridpoints2 = gridpoints;
        
    gridpoints2(any(isnan(big_data), 2), :) = -1;
        
    gridpoints(find(gridpoints2 == -1)) = [];

    big_range_data = create_big_range_data(big_data, gridpoints, use_range);

    if difftype >= 1000
        modeltype = ['Year' num2str(difftype) '-Year' num2str(year20)];
    else
        modeltype = ['Degree' num2str(difftype) '-_Year' num2str(year20)];
    end

    save([scen '_' cvar '_range_data_' num2str(month) '_' modeltype '.mat'], 'big_data', 'gridpoints', 'dims', 'big_range_data');
end

function [big_data, dims, modelnames] = create_big_data(scen, difftype, year20, cvar, dirString, month)

    if difftype >= 1000
        [big_data, dims, modelnames] = create_big_data_diff_model21_model20(scen, cvar, dirString, month, difftype, year20);
    else
        [big_data, dims, modelnames] = create_big_data_diff_ndegree_model20(scen, cvar, dirString, month, difftype, year20);
    end
end

function big_range_data = create_big_range_data(big_data, gridpoints, use_range)
    %models_size = size(big_data, 2);
    % TODO: Add big_range_data definition
    big_range_data = [];
    
    if isempty(big_data)
        return;
    end
    
    for i = 1:length(gridpoints)
        d1 = gridpoints(i);
        differences_slice = squeeze(big_data(d1, :));
        if use_range == 0
            big_range_data(i, :) = differences_slice;
        else
            [sorted_slice, indices] = sort(differences_slice);
            big_range_data(i, indices) = 1:length(indices);
        end
    end
end

function [big_data, dims, modelnames] = create_big_data_diff_model21_model20(scen, cvar, dirString, month, difftype, year20)
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_' scen '_.*_year' num2str(difftype) '.mat']);
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
    dims = [];
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        struc_Sresa2 = load(fullname, 'data', 'model', 'x', 'y');
        run = get_run(tn{1});
        
        % sresa2_data is 12 x LATS x LONS
        sresa2_data = struc_Sresa2.data;
        if (month == 0)
            sresa2_data = squeeze(mean(sresa2_data, 1));
        else
            sresa2_data = squeeze(sresa2_data(month, :, :));
        end
        
        modelnames(contador) = {struc_Sresa2.model};
        contador = contador + 1;
        fullname = fullfile(dirString, [cvar '_20c3m_' struc_Sresa2.model '_' run '_year' num2str(year20) '.mat']);
        
        % c3m_data is 12 x LATS x LONS
        struc_20c3m = load(fullname, 'data');
        c3m_data = struc_20c3m.data;
        if (month == 0)
            c3m_data = squeeze(mean(c3m_data, 1));
        else
            c3m_data = squeeze(c3m_data(month, :, :));
        end
        
        %now, sresa2_data is LATS x LONS, as well as c3m_data.
        
        dims = size(sresa2_data);
        lats = dims(1);
        lons = dims(2);
        
        sresa2_data = reshape(sresa2_data, lats*lons, 1);
        c3m_data = reshape(c3m_data, lats*lons, 1);
        
        %now, sresa2_data is (lats*lons)x1, as well as c3m_data.
        
        data_difference = sresa2_data - c3m_data;
        %add variable as a new dimension of big_data
        big_data = cat(2, big_data, data_difference);
        
        clear sresa2_data;
        clear c3m_data;
    end
    % now big_data contains two dimensions.
    % 1) size (lats*lons), one for each grid point
    % 2) variable size, on`````e for each model
end

function [big_data, dims, modelnames] = create_big_data_diff_ndegree_model20(scen, cvar, dirString, month, ndegree, year20)
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
    dims = [];
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        struc_Sresa2 = load(fullname, 'data', 'model', 'x', 'y');
        run = get_run(tn{1});
        
        % sresa2_data is 12 x LATS x LONS
        sresa2_data = struc_Sresa2.data;
        if (month == 0)
            sresa2_data = squeeze(mean(sresa2_data, 1));
        else
            sresa2_data = squeeze(sresa2_data(month, :, :));
        end
        
        modelnames(contador) = {struc_Sresa2.model};
        contador = contador + 1;
        fullname = fullfile(dirString, [cvar '_20c3m_' struc_Sresa2.model '_' run '_year' num2str(year20) '.mat']);
        
        % c3m_data is 12 x LATS x LONS
        struc_20c3m = load(fullname, 'data');
        c3m_data = struc_20c3m.data;
        if (month == 0)
            c3m_data = squeeze(mean(c3m_data, 1));
        else
            c3m_data = squeeze(c3m_data(month, :, :));
        end
        
        %now, sresa2_data is LATS x LONS, as well as c3m_data.
        
        dims = size(sresa2_data);
        lats = dims(1);
        lons = dims(2);
        
        sresa2_data = reshape(sresa2_data, lats*lons, 1);
        c3m_data = reshape(c3m_data, lats*lons, 1);
        
        %now, sresa2_data is (lats*lons)x1, as well as c3m_data.
        
        data_difference = sresa2_data - c3m_data;
        %add variable as a new dimension of big_data
        big_data = cat(2, big_data, data_difference);
        
        clear sresa2_data;
        clear c3m_data;
    end
    % now big_data contains two dimensions.
    % 1) size (lats*lons), one for each grid point
    % 2) variable size, one for each model
end