%Made by Luciano, so you know whom to address for errors.

%Displays one map for each model. The map is the difference between
% 'mean model data in 2075-2100' and 'mean model data in 1975-2000'
function ipccmodel_meansc_diff_gfdl(scen, cvar)
dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
%dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
if (dirString == 0)
    % no directory was chosen, exit program
    return;
else
    model_name = 'gfdl_cm2_0';
    fullname = fullfile(dirString, [cvar '_' scen '_' model_name '_run1_allyears.mat']);
    fullname20 = fullfile(dirString, [cvar '_20c3m_' model_name '_run1_allyears.mat']);
    do(cvar, fullname, fullname20, model_name);
end

return
end

function do(cvar, file_scen21, file_scen20, model_name)

%load scen
struc_scen21 = load(file_scen21, 'data', 'x', 'y');
data_scen21_1 = squeeze(mean(struc_scen21.data(1:12,:,:), 1));

struc_scen20 = load(file_scen20, 'data', 'x', 'y');
data_scen20_end = squeeze(mean(struc_scen20.data(end-11:end,:,:), 1));

%load axis
x = struc_scen21.x;
y = struc_scen21.y;


%Do difference (model minus real data)
data = data_scen21_1 - data_scen20_end;

%Handle NaNs
jnan=find(isnan(data_scen21_1) == 1 | isnan(data_scen20_end) == 1);
data(jnan)=NaN;
    
% Begin drawing...
[plon,plat] = meshgrid(x,y);
if strcmp(cvar, 'pr') == 1
    cmin = -4;
    cmax = 4;
else %tas
    cmin = -10;
    cmax = 10;
end
ncol = 40;
    
load coast_world;
    
figure; %a4l   
map_global;tightmap;
%title ( titlename );
h = pcolorm(plat, plon, data);
caxis([cmin, cmax]);
cmap=colormap(jet(ncol));                     % set N. of colors.
if cmin < 0
    cmap([ncol/2 ncol/2+1],:)=1;
end
colormap(cmap);
colorbar('horizon');
 shading interp;
%hold on;
% Re-Draw the map
plotm(latW,lonW,'k')
%hold on;
drawnow;

%Save picture as PNG.
%eval(['print -dpng ' cvar '_diff_' model_name '_21_20_' titstr{month+1}]);

return
end