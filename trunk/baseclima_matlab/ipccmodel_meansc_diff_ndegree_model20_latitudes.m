%Made by Luciano, so you know whom to address for errors.

function ipccmodel_meansc_diff_ndegree_model20_latitudes(scen, cvar, ndegree, month)
dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
%dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
if (dirString == 0)
    % no directory was chosen, exit program
    return;
else
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
%    truenames(1) = [];
%    truenames(3) = [];
%    truenames(13) = [];
    figure;
    colors={'b-', 'g-', 'r-', 'c-', 'm-', 'b-.', 'g-.', 'r-.', 'c-.', 'm-.', 'b--', 'g--', 'r--', 'c--', 'm--', 'b:', 'g:', 'r:', 'c:', 'm:','b-','g-','r-'};
    contador = 1;
    models = cell(0);
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        struc_Sresa2 = load(fullname, 'model');
        model_name = struc_Sresa2.model;
        fullname21 = fullfile(dirString, [cvar '_' scen '_' model_name '_' run '_' num2str(ndegree) 'degree.mat']);
        fullname20 = fullfile(dirString, [cvar '_20c3m_' model_name '_' run '_per2.mat']);
        do_diff_model21_model20(fullname21, fullname20, month, colors{contador});
        models{contador} = model_name;
        contador = contador + 1;
    end
    legend(models, 'location','EastOutside');
    title(['Latitudes - ' scen ' ' num2str(ndegree) ' degrees']);
    grid on;
    drawnow;
end

return
end

function do_diff_model21_model20(file_scen21, file_scen20, month, color)

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

%Do difference (model minus real data)
data = data_scen21 - data_scen20;

%Handle NaNs
jnan=find(isnan(data_scen21) == 1 | isnan(data_scen20) == 1);
data(jnan)=NaN;
    
mean_lat = squeeze(mean(data, 2));
hold on;
plot(y, mean_lat, color);
hold on;

%Save picture as PNG.
%eval(['print -dpng ' cvar '_diff_' model_name '_21_20_' titstr{month+1}]);

return
end