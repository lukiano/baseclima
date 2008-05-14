function map_sa( input_args )

% Plot a map in a sps projection
% Andrea 11/1/06

a4
axesm(...
         'MapProjection','eqdcylin',...
         'MapLatLimit',[-60 20],...
         'flatlimit', [-Inf 50],...
         'parallellabel','off',...
         'meridianlabel','off',...
         'labelformat','compass',...
         'grid','on',...
         'fontsize',8,...
         'origin',[-20 -20 0]);
    
load coast
plotm(lat,long,'k')

% Shading%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% h=pcolorm(axllat,axllon,u1e);
% caxis([-3 3]);
% colormap(jet(30));
% % % h=colormap;
% % % h(1,:)=1;
% % % colormap(h);
% colorbar('horizon');
% shading interp;
% % % % patchm(lat,long,[0.9 0.9 0.9]);

% Contour %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Negative values
% hold on;
% [c1,h1]=contourm(axllat,axllon,var,[-4:0.2:-0.2],'b');
% set(h1,'linestyle','--','linewidth',1.0,'visible','on')

% Positive values
% hold on;
% [c2,h2]=contourm(axllat,axllon,var,[0.2:0.2:2],'r');
% set(h2,'linestyle','-','linewidth',1.0,'visible','on')

% Zero
% hold on;
% [c3,h3]=contourm(axllat,axllon,var,[ 0 0 ],'k');
% set(h3,'linestyle','-','linewidth',1.5,'visible','on')
% hold on;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
return
