%Made by Luciano, so you know whom to address for errors.

%Displays one map for each model. The map is the difference between
% 'mean model data in year+-12' and 'mean model data in 1975-2000'
function ipccmodel_meansc_diff_model21_model20(scen, cvar, month, year21, year20)
%dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
dirString = uigetdir('./modelos','Choose data directory');

if (dirString == 0)
    % no directory was chosen, exit program
    return;
else
    load coast_world;
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
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        struc_Sresa2 = load(fullname, 'model');
        run = get_run(tn{1});
        model_name = struc_Sresa2.model;
        fullname21 = fullfile(dirString, [cvar '_' scen '_' model_name '_' run '_year' num2str(year21) '.mat']);
        fullname20 = fullfile(dirString, [cvar '_20c3m_' model_name '_' run '_year' num2str(year20) '.mat']);        
        do_diff_model21_model20(cvar, fullname21, fullname20, model_name, month);
    end
end

return
end

function do_diff_model21_model20(cvar, file_scen21, file_scen20, model_name, month)

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
%data = data_scen21;

%Handle NaNs
jnan=find(isnan(data_scen21) == 1 | isnan(data_scen20) == 1);
data(jnan)=NaN;
    
% Begin drawing...
x = x';
y = y';
x=circshift(x,[72 1]);
x(1:72)=x(1:72)-360;
x = x - 0.5;
[plon,plat] = meshgrid(x,y);
if strcmp(cvar, 'pr') == 1
    cmin = -4;
    cmax = 4;
else %tas
    cmin = -6;
    cmax = 6;
    %cmin = 0;
    %cmax = 40;
end
ncol = 40;
    
figure; %a4l   
map_globe;
tightmap;
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
%hold on;
drawnow;

%Save picture as PNG.
%eval(['print -dpng ' cvar '_diff_' model_name '_21_20_' titstr{month+1}]);

return
end