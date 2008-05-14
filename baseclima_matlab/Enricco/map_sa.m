function map_sa( input_args )

% Plot a map in a sps projection
% Andrea 11/1/06


axesm(...
         'MapProjection','eqdcylin',...
         'MapLonLimit',[-90 -20],...
         'MapLatLimit',[-55 15],...
         'flatlimit', [-55 15],...
         'parallellabel','on',...
         'meridianlabel','on',...
         'labelformat','compass',...
         'grid','on',...
         'MLabelParallel','south',...
         'fontsize',8);
    
load coast_sa
plotm(latsa,lonsa,'k')

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
