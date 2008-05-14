%Made by Luciano, so you know whom to address for errors.

function clusters_value = ipccmodel_meansc_clusters_model21_model20(scen, cvar, month, year, numclusters)
dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
%dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');

if (dirString == 0)
    % no directory was chosen, exit program
    return;
else
    load('model_range.mat', 'Cmus');
    Cmus = squeeze(Cmus(:,:,numclusters - 3)); % start at 4
    
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_' scen '_.*_year' num2str(year) '.mat']);
    contador = 1;
    truenames = cell(0);
    for i = 1:length(rexp)
        if not(isempty(rexp{i}))
            truenames(contador) = {names{i}};
            contador = contador + 1;
        end
    end
    contador = 1;
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        struc_Sresa2 = load(fullname, 'model');
        model_name = struc_Sresa2.model;
        fullname21 = fullfile(dirString, [cvar '_' scen '_' model_name '_run1_year' num2str(year) '.mat']);
        fullname20 = fullfile(dirString, [cvar '_20c3m_' model_name '_run1_per2.mat']);        
        clusters_value(contador, :) = do_diff_model21_model20(fullname21, fullname20, model_name, month, Cmus, numclusters);
        contador = contador + 1;
    end
end

return
end

function clusters_value = do_diff_model21_model20(file_scen21, file_scen20, model_name, month, Cmus, numclusters)

%load scen21
struc_scen21 = load(file_scen21, 'data', 'x', 'y');
if month == 0
    data_scen21 = squeeze(mean(struc_scen21.data, 1));
else
    data_scen21 = squeeze(struc_scen21.data(month,:,:));
end

%load axis
x = struc_scen21.x;
y = struc_scen21.y;

%load scen20
struc_scen20 = load(file_scen20, 'data');
if month == 0
    data_scen20 = squeeze(mean(struc_scen20.data, 1));
else
    data_scen20 = squeeze(struc_scen20.data(month,:,:));
end

%Friendly month title.
titstr={'MEAN','JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'};
titlename = ['diff\_' regexprep(model_name,'\_','\\_') '\_21\_20\_' titstr{month+1}];

%Do difference (model minus real data)
data = data_scen21 - data_scen20;

%Handle NaNs
jnan=find(isnan(data_scen21) == 1 | isnan(data_scen20) == 1);
data(jnan)=NaN;

clusters_value = zeros(numclusters, 1);
for i = 1:numclusters
    gridpoints = find(Cmus == i);
    clusters_value(i) = mean(data(gridpoints));
end

return
end