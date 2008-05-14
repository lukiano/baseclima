%Made by Luciano, so you know who to address for errors.

function ipccmodel_meansc_visu20c3m(cvar, month) %cvar is not currently used.

%Load the big data. Assume the file is in the same directory.
filecmap='precip_7903_meansc.mat';
load(filecmap,'npi','npj','xgrid','ygrid','cmapsc');

%Friendly month title.
titstr={'JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'};
titlename = ['20c3m\_' titstr{month}];

%Variable aliasing.
fobs = cmapsc;
x = xgrid;
y = ygrid;

%Obtain data for the desired month.
data_slice = squeeze(fobs(month,:,:));

% Begin drawing...
x=circshift(x,[72 1]);
x(1:72)=x(1:72)-360;
x = x - 0.5;
[plon,plat] = meshgrid(x,y);
cmin = 0;
cmax = 10;
ncol = 20;

load coast_world;
   
a4l   
map_globe;tightmap;
title ( titlename );
data_slice = circshift(data_slice, [0 72]);
h = pcolorm(plat, plon, data_slice);
caxis([cmin, cmax]);
cmap=colormap(jet(ncol));                     % set N. of colors.
colormap(cmap);
colorbar('horizon');
 shading interp;
hold on;
% Re-Draw the map
plotm(latW,lonW,'k')
hold on;
drawnow

%Save picture as PNG.
eval(['print -dpng 20c3m_' titstr{month}]);

return
end


function test
%%%%
cvar='pr'

for month=1:12
    ipccmodel_meansc_visu20c3m(cvar,2);
end

return
end
