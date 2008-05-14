%Made by Luciano, so you know whom to address for errors.

%Calculates, for each model, the difference between 
% 2075-2100 and 1975-2000. Then sorts those differences for each
% grid point and builds map based on min, max, 25%, 50%, 75% and
% standard deviation.

%Note: I think.. this is deprecated. 
%Should use 'ipccmodel_meansc_diagscen()' instead.
function ipccmodel_meansc_diag_diff()
    dirString = uigetdir('/Users/Shared','Choose data directory');
    if (dirString == 0)
        % no directory was chosen, exit program
        return;
    else
        files = dir(dirString); % obtain file names
        names = transpose({files.name});
        % only retain those file names we are interested in
        rexp = regexp(names, 'pr_sresa2_.*_per4.mat'); % 'pr_20c3m_.*_per2.mat'
        contador = 1;
        truenames = cell(0);
        for i = 1:length(rexp)
            if not(isempty(rexp{i}))
                truenames(contador) = {names{i}};
                contador = contador + 1;
            end
        end
        x = [];
        y = [];
        %for each file, load the data variable
        big_data = [];
        contador = 1;
        modelnames = cell(0);
        for tn = truenames
            fullname = fullfile(dirString, tn{1});
            struc_Sresa2 = load(fullname, 'data', 'model', 'x', 'y');

            sresa2_data = struc_Sresa2.data;
            
            x = struc_Sresa2.x;
            y = struc_Sresa2.y;
            
            modelnames(contador) = {struc_Sresa2.model};
            contador = contador + 1;
            fullname = fullfile(dirString, ['pr_20c3m_' struc_Sresa2.model '_run1_per2.mat']);
            struc_20c3m = load(fullname, 'data');
            c3m_data = struc_20c3m.data;
            
            data_difference = sresa2_data - c3m_data;
            %add variable as a new dimension of bigdata
            
            big_data = cat(4, big_data, data_difference);
        end
        % now bigdata contains four dimensions.
        % 1) size 12, one for each month
        % 2) size 72, one for each latitude
        % 3) size 144, one for each longitude
        % 4) variable size, one for each model

        models_size = size(big_data, 4);
        
        big_binary_data_25 = [];
        for d1 = 1:size(big_data, 1)
            for d2 = 1:size(big_data, 2)
                for d3 = 1:size(big_data, 3)
                    differences_slice = reshape(big_data(d1, d2, d3, :), models_size, 1);
                    [ordered, indices] = sort(differences_slice);
                    
                    % remain with first 25% of the models
                    models_to_one = indices(1:ceil(length(indices) * 25 / 100)); 
                    all_of_them = transpose(1:models_size);
                    binary = ismember(all_of_them, models_to_one);
                    big_binary_data_25(d1, d2, d3, :) = binary;
                end
            end
        end

        big_binary_data_75 = [];
        for d1 = 1:size(big_data, 1)
            for d2 = 1:size(big_data, 2)
                for d3 = 1:size(big_data, 3)
                    differences_slice = reshape(big_data(d1, d2, d3, :), models_size, 1);
                    [ordered, indices] = sort(differences_slice);
                    
                    % remain with last 25% of the models
                    fin = length(indices);
                    com = floor(length(indices) * 75 / 100)+1;
                    models_to_one = indices(com:fin); 
                    all_of_them = transpose(1:models_size);
                    binary = ismember(all_of_them, models_to_one);
                    big_binary_data_75(d1, d2, d3, :) = binary;
                end
            end
        end

        big_binary_data_min = [];
        for d1 = 1:size(big_data, 1)
            for d2 = 1:size(big_data, 2)
                for d3 = 1:size(big_data, 3)
                    differences_slice = reshape(big_data(d1, d2, d3, :), models_size, 1);
                    [ordered, indices] = sort(differences_slice);
                    
                    % remain with first model
                    models_to_one = indices(1); 
                    all_of_them = transpose(1:models_size);
                    binary = ismember(all_of_them, models_to_one);
                    big_binary_data_min(d1, d2, d3, :) = binary;
                end
            end
        end

        big_binary_data_max = [];
        for d1 = 1:size(big_data, 1)
            for d2 = 1:size(big_data, 2)
                for d3 = 1:size(big_data, 3)
                    differences_slice = reshape(big_data(d1, d2, d3, :), models_size, 1);
                    [ordered, indices] = sort(differences_slice);
                    
                    % remain with last model
                    models_to_one = indices(length(indices)); 
                    all_of_them = transpose(1:models_size);
                    binary = ismember(all_of_them, models_to_one);
                    big_binary_data_max(d1, d2, d3, :) = binary;
                end
            end
        end

        big_binary_data_sd = [];
        big_binary_data_50 = [];
        
        save('ipccmodel_meansc_diag_diff.mat', 'x', 'y', 'models_size', 'modelnames', 'big_data', 'big_binary_data_25', 'big_binary_data_75', 'big_binary_data_min', 'big_binary_data_max', 'big_binary_data_50', 'big_binary_data_sd');
    end
end