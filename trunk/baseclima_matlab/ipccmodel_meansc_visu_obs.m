%Made by Luciano, so you know whom to address for errors.

%Displays one map for each model. The map is'mean model data in year+-12'
function ipccmodel_meansc_visu_obs(cvar, month, year20, regional_masks, cluster_filename, cluster_number_mask)
%dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
dirString = uigetdir('./modelos','Choose data directory');

if (dirString == 0)
    % no directory was chosen, exit program
    return;
else
    load coast_world;
    scen = 'obs';
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_' scen '_.*_year' num2str(year20) '.mat']);
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
        fullname = fullfile(dirString, [cvar '_' scen '_' model_name '_' run '_year' num2str(year20) '.mat']);
        do_model(cvar, fullname, model_name, month, regional_masks, cluster_filename, cluster_number_mask);
    end
end

return
end

function do_model(cvar, file_scen, model_name, month, regional_masks, cluster_filename, cluster_number_mask)

%load scen
struc_scen = load(file_scen, 'data', 'x', 'y');
if month == 0
    data = squeeze(mean(struc_scen.data, 1));
else
    data = squeeze(struc_scen.data(month,:,:));
end

mask = getMasks(regional_masks, cluster_filename, cluster_number_mask);
mask(find(mask ==0)) = NaN;
data = data.* mask;

%load axis
x = struc_scen.x;
y = struc_scen.y;

%Friendly month title.
titstr={'MEAN','JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'};
titlename = [regexprep(model_name,'\_','\\_') ' ' titstr{month+1}];

% Begin drawing...
x = x';
y = y';
x=circshift(x,[72 1]);
x(1:72)=x(1:72)-360;
x = x - 0.5;
[plon,plat] = meshgrid(x,y);
% if strcmp(cvar, 'pr') == 1
%     cmin = -4;
%     cmax = 4;
% else %tas
%     cmin = -6;
%     cmax = 6;
%     cmin = 0;
%     cmax = 40;
% end
% ncol = 40;
    
figure; %a4l   
map_globe;
tightmap;
title ( titlename );
data = circshift(data, [0 72]);
pcolorm(plat, plon, data);
%caxis([5, 30]);
% cmap=colormap(jet(ncol));                     % set N. of colors.
% if cmin < 0
%     cmap([ncol/2 ncol/2+1],:)=1;
% end
% colormap(cmap);
colorbar('horizon');
 shading interp;
hold on;
% Re-Draw the map
plotm(latW,lonW,'k')
%hold on;
drawnow;

%Save picture as PNG.
%eval(['print -dpng ' cvar '_' model_name '_20_' titstr{month+1}]);

return
end

