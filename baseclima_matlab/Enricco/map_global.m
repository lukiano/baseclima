function map_global( input_args )

% Plot a global map in eqd projection
% Andrea 29/1/06

% getm('axes') %info about properties

%a4l
axesm(...
       'MapProjection','eqdcylin',...
       'MapLatLimit',[-90 90],...
       'Maplonlimit',[-180 180],...
       'parallellabel','on',...
       'meridianlabel','on',...
       'labelformat','compass',...
       'grid','on',...
       'fontsize',6,...
       'flatlimit', [-90 90]...
   );

% axesm(...
%         'MapProjection','eqdcylin',...
%         'MapLatLimit',[-90 90],...
%         'Maplonlimit',[-180 180],...
%         'parallellabel','on',...
%         'PlabelMeridian', [ -180 ],... %longitude to place parallel labels
%         'meridianlabel','on',...
%         'labelformat','compass',...
%         'grid','on',...
%         'fontsize',6,...
%         'MlabelParallel', [ -90 ],... %latitude to place meridional labels
%         'origin',[0 180  0]...
%     );

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


