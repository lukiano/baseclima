function visu2(data, x, y, modelnames)

% This program visualizes the climate change diagnostics

models_size = size(modelnames, 1);

[plon,plat] = meshgrid(x,y);
    cmin = zeros(1, models_size);
    cmax = zeros(1, models_size) + 10;
    ncol = 10;
load coast_world
%models_size_sqrt = floor(sqrt(models_size)) + 1;
   
for ip = 1:models_size
    a4l   
    map_global;tightmap;
    modelname = modelnames(ip);
    title ( modelname{1} );
    data_slice = squeeze(data(:,:,ip));
    h = pcolorm(plat, plon, data_slice);
    caxis([cmin(ip), cmax(ip)]);
    cmap=colormap(jet(ncol));                     % set N. of colors.
    if cmin(ip) < 0
        cmap([ncol/2 ncol/2+1],:)=1;
    end
    colormap(cmap);
    colorbar('horizon');
     shading interp;
%     hold on;
%     [c3,h3] = contourm(plat, plon, data_slice, [ 0 0 ], 'k');
%     set(h3,'linestyle','-','linewidth',1.2,'visible','on')
    hold on;
    % Re-Draw the map
    plotm(latW,lonW,'k')
    hold on;
    drawnow
    savfig=0;
    if savfig == 1
         eval(['print -dpng ' modelname{1}]);
    end
end

end

