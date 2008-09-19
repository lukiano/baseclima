%Made by Luciano, so you know whom to address for errors.

%Displays one map for each model. The map is the difference between
% 'mean model data in 1975-2000' and real observations in that period.

function ipccmodel_meansc_diff_model_20c3m(cvar, year20, month)
%dirString = uigetdir('/Users/Shared','Choose data directory');
dirString = uigetdir('./modelos','Choose data directory');
if (dirString == 0)
    % no directory was chosen, exit program
    return;
else
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_20c3m_.*_year' num2str(year20) '.mat']);
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
        file_20c3m = fullfile(dirString, [cvar '_20c3m_' model_name '_run1_year' num2str(year20) '.mat']);
        file_cru = fullfile(dirString, [cvar '_obs_cru_run1_year' num2str(year20) '.mat']);
        do_diff_model_20c3m(cvar, file_20c3m, file_cru, model_name, month);
    end
end

return
end

function do_diff_model_20c3m(cvar, file_scen, file_cru, model_name, month)

%load scen
struc_scen = load(file_scen, 'data');
if month == 0
    data_scen = squeeze(mean(struc_scen.data, 1));
else
    data_scen = squeeze(struc_scen.data(month,:,:));
end
    
%load obs
struc_cru = load(file_cru, 'data');
if month == 0
    data_cru = squeeze(mean(struc_cru.data, 1));
else
    data_cru = squeeze(struc_cru.data(month,:,:));
end
load(file_scen, 'x', 'y');

%Friendly month title.
titstr={'MEAN','JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'};

titlename = ['diff\_' regexprep(model_name,'\_','\\_') '\_20c3m\_' titstr{month+1}];

%Variable aliasing.
%x = xgrid;
%y = ygrid;

%Do difference (model minus real data)
data = data_scen - data_cru;

%Handle NaNs
jnan=find((data_cru) < 0 | isnan(data_scen) == 1);
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
%eval(['print -dpng ' cvar '_diff_' model_name '_20c3m_' titstr{month+1}]);

return
end