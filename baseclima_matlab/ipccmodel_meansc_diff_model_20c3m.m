%Made by Luciano, so you know whom to address for errors.

%Displays one map for each model. The map is the difference between
% 'mean model data in 1975-2000' and real observations in that period.

function ipccmodel_meansc_diff_model_20c3m(cvar, month)
dirString = uigetdir('/Users/Shared','Choose data directory');
if (dirString == 0)
    % no directory was chosen, exit program
    return;
else
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_20c3m_.*_per2.mat']);
    contador = 1;
    truenames = cell(0);
    for i = 1:length(rexp)
        if not(isempty(rexp{i}))
            truenames(contador) = {names{i}};
            contador = contador + 1;
        end
    end
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        run = get_run(tn{1});
        struc_Sresa2 = load(fullname, 'model');
        model_name = struc_Sresa2.model;
        fullname = fullfile(dirString, [cvar '_20c3m_' model_name '_' run '_per2.mat']);
        do_diff_model_20c3m(cvar, fullname, model_name, month);
    end
end

return
end

function do_diff_model_20c3m(cvar, file_scen, model_name, month)

%load scen
struc_scen = load(file_scen, 'data');
if month == 0
    data_scen = squeeze(mean(struc_scen.data, 1));
else
    data_scen = squeeze(struc_scen.data(month,:,:));
end
    
%load obs
if strcmp(cvar, 'pr') == 1
    filecmap='precip_7903_meansc.mat'; % Assume file is in the same directory.
else
    filecmap='tempObs1975_2000.mat';
end
load(filecmap, 'xgrid', 'ygrid', 'cmapsc');

%Friendly month title.
titstr={'MEAN','JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'};

titlename = ['diff\_' regexprep(model_name,'\_','\\_') '\_20c3m\_' titstr{month+1}];

%Variable aliasing.
fobs = cmapsc;
x = xgrid;
y = ygrid;

%Obtain data for the desired month.
if month == 0
    data_20c3m = squeeze(mean(fobs, 1));
else
    data_20c3m = squeeze(fobs(month,:,:));
end

%Do difference (model minus real data)
data=data_scen - data_20c3m;

%Handle NaNs
jnan=find((data_20c3m) < 0 | isnan(data_scen) == 1);
data(jnan)=NaN;

% Begin drawing...
x=circshift(x,[72 1]);
x(1:72)=x(1:72)-360;
x = x - 0.5;
[plon,plat] = meshgrid(x,y);
cmin = -10;
cmax = 10;
ncol = 40;
    
load coast_world;
    
figure; %a4l   
map_globe;tightmap;
title ( titlename );
data = circshift(data, [0 72]);
h = pcolorm(plat, plon, data);
caxis([cmin, cmax]);
cmap=colormap(jet(ncol));                     % set N. of colors.
if cmin < 0
    cmap([ncol/2 ncol/2+1],:)=1;
end
colormap(cmap);
colorbar('horizon');
 shading interp;
hold on;
% Re-Draw the map
plotm(latW,lonW,'k')
hold on;
drawnow

%Save picture as PNG.
eval(['print -dpng ' cvar '_diff_' model_name '_20c3m_' titstr{month+1}]);

return
end