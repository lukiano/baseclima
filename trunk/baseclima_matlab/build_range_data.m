%Made by Luciano, so you know whom to address for errors.

%month 0: annual mean; 1 - 12: actual month
%difftype -1: "1975-2000 (model)" - "1975-2000 (obs)"
% n >= 1000 : "12-n+12 (model)" - "1975-2000 (model)"
% 1 >= n > 1000: "n degree (model)" - "1975-2000 (model)"
%use_range 0: put original values of models; 1: sort models and put their resulting rank
function build_range_data(scen, cvar, month, difftype, use_range) 
    dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
    %dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
    if (dirString == 0)
        % no directory was chosen, exit program
        return;
    end

    [big_data, dims, modelnames] = create_big_data(scen, difftype, cvar, dirString, month);
    gridpoints = 1:size(big_data, 1);
    gridpoints = gridpoints';
    gridpoints2 = gridpoints;
        
    gridpoints2(any(isnan(big_data), 2), :) = -1;
        
    gridpoints(find(gridpoints2 == -1)) = [];

    big_range_data = create_big_range_data(big_data, gridpoints, use_range);

    if difftype < 0
        modeltype = '20th-_obs';
    elseif difftype >= 1000
        modeltype = ['21th-Year' num2str(difftype) '-_20th'];
    else
        modeltype = ['21th-Degree' num2str(difftype) 'deg-_20th'];
    end

    save([scen '_' cvar '_range_data_' num2str(month) '_' modeltype '.mat'], 'big_data', 'gridpoints', 'dims', 'big_range_data');
end

function [big_data, dims, modelnames] = create_big_data(scen, difftype, cvar, dirString, month)

    if difftype < 0
        [big_data, dims, modelnames] = create_big_data_diff_model20_obs(scen, cvar, dirString, month);
    elseif difftype >= 1000
        [big_data, dims, modelnames] = create_big_data_diff_model21_model20(scen, cvar, dirString, month, difftype);
    else
        [big_data, dims, modelnames] = create_big_data_diff_ndegree_model20(scen, cvar, dirString, month, difftype);
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
        if use_range == 1
            big_range_data(i, :) = differences_slice;
        else
            [sorted_slice, indices] = sort(differences_slice);
            big_range_data(i, indices) = 1:length(indices);
        end
    end
end

function [big_data, dims, modelnames] = create_big_data_diff_model21_model20(scen, cvar, dirString, month, difftype)
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
        fullname = fullfile(dirString, [cvar '_20c3m_' struc_Sresa2.model '_' run '_per2.mat']);
        
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

function [big_data, dims, modelnames] = create_big_data_diff_ndegree_model20(scen, cvar, dirString, month, ndegree)
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
        fullname = fullfile(dirString, [cvar '_20c3m_' struc_Sresa2.model '_' run '_per2.mat']);
        
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

function [big_data, dims, modelnames] = create_big_data_diff_model20_obs(scen, cvar, dirString, month)
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_' scen '_.*_per4.mat']); % 'pr_20c3m_.*_per2.mat'
    contador = 1;
    truenames = cell(0);
    for i = 1:length(rexp)
        if not(isempty(rexp{i}))
            truenames(contador) = {names{i}};
            contador = contador + 1;
        end
    end

    %load obs
    if strcmp(cvar, 'pr') == 1
        %load precipitation obs
        fileobs = 'precip_7903_meansc.mat'; % Assume file is in the same directory.
        load(fileobs, 'cmapsc');
        if month == 0
            data_obs = squeeze(mean(cmapsc, 1));
        else
            data_obs = squeeze(cmapsc(month,:,:));
        end
    else
        %load temperature obs
        fileobs = 'tempObs1975_2000.mat'; % Assume file is in the same directory.
        load(fileobs, 'cmapsc');
        if month == 0
            data_obs = squeeze(mean(cmapsc, 1));
        else
            data_obs = squeeze(cmapsc(month,:,:));
        end
    end
    
    dims = size(data_obs);
    lats = dims(1);
    lons = dims(2);

    data_obs = reshape(data_obs, lats*lons, 1);
    
    %for each file, load the data variable
    big_data = [];
    contador = 1;
    modelnames = cell(0);
    
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        struc_Sresa2 = load(fullname, 'model');
        run = get_run(tn{1});

        modelnames(contador) = {struc_Sresa2.model};
        contador = contador + 1;
        fullname = fullfile(dirString, [cvar '_20c3m_' struc_Sresa2.model '_' run '_per2.mat']);
        
        % c3m_data is 12 x LATS x LONS
        struc_20c3m = load(fullname, 'data');
        c3m_data = struc_20c3m.data;
        if (month == 0)
            c3m_data = squeeze(mean(c3m_data, 1));
        else
            c3m_data = squeeze(c3m_data(month, :, :));
        end
        
        
        %now, c3m_data is LATS x LONS
        
        c3m_data = reshape(c3m_data, lats*lons, 1);
        
        %now, sresa2_data is (lats*lons)x1, as well as c3m_data.
        
        data_difference = c3m_data - data_obs;
        
        %add variable as a new dimension of big_data
        big_data = cat(2, big_data, data_difference);
        
        clear c3m_data;
    end
    % now big_data contains two dimensions.
    % 1) size (lats*lons), one for each grid point
    % 2) variable size, one for each model
end
