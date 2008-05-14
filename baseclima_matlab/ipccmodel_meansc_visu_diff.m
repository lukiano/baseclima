%Made by Luciano, so you know who to address for errors.

%Displays data built in 'ipccmodel_meansc_diag_diff()'
function ipccmodel_meansc_visu_diff(month)

load('ipccmodel_meansc_diag_diff.mat', 'x', 'y', 'models_size', 'modelnames', 'big_data', 'big_binary_data_25', 'big_binary_data_75', 'big_binary_data_min', 'big_binary_data_max', 'big_binary_data_50', 'big_binary_data_sd');

load coast_world;

x=circshift(x,[72 1]);
x(1:72)=x(1:72)-360;
x = x - 0.5;
for model = 1:models_size
    a4l;
    [plon,plat] = meshgrid(x,y);
    cmin = -10;
    cmax = 10;
    ncol = 20;
    
    subplot(2,3,1);
    data_slice = squeeze(big_data(month, :, :, model));
    binary_data_slice = squeeze(big_binary_data_25(month, :, :, model));
    for d1 = 1:size(data_slice, 1)
        for d2 = 1:size(data_slice, 2)
            if binary_data_slice(d1, d2) == 0
                data_slice(d1, d2) = 0;
            end
        end
    end
    
    map_globe;tightmap;
    title ('50%');
    data_slice = circshift(data_slice, [0 72]);
    h = pcolorm(plat, plon, data_slice);
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
    
    subplot(2,3,2);
    data_slice = squeeze(big_data(month, :, :, model));
    binary_data_slice = squeeze(big_binary_data_25(month, :, :, model));
    for d1 = 1:size(data_slice, 1)
        for d2 = 1:size(data_slice, 2)
            if binary_data_slice(d1, d2) == 0
                data_slice(d1, d2) = 0;
            end
        end
    end
    
    map_global;tightmap;
    title ('25%');
    h = pcolorm(plat, plon, data_slice);
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

    subplot(2,3,3);
    data_slice = squeeze(big_data(month, :, :, model));
    binary_data_slice = squeeze(big_binary_data_min(month, :, :, model));
    for d1 = 1:size(data_slice, 1)
        for d2 = 1:size(data_slice, 2)
            if binary_data_slice(d1, d2) == 0
                data_slice(d1, d2) = 0;
            end
        end
    end
    
    map_global;tightmap;
    title ('min');
    h = pcolorm(plat, plon, data_slice);
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

    subplot(2,3,4);
    data_slice = squeeze(big_data(month, :, :, model));
    binary_data_slice = squeeze(big_binary_data_25(month, :, :, model));
    for d1 = 1:size(data_slice, 1)
        for d2 = 1:size(data_slice, 2)
            if binary_data_slice(d1, d2) == 0
                data_slice(d1, d2) = 0;
            end
        end
    end
    
    map_global;tightmap;
    title ('SD');
    h = pcolorm(plat, plon, data_slice);
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

    subplot(2,3,5);
    data_slice = squeeze(big_data(month, :, :, model));
    binary_data_slice = squeeze(big_binary_data_75(month, :, :, model));
    for d1 = 1:size(data_slice, 1)
        for d2 = 1:size(data_slice, 2)
            if binary_data_slice(d1, d2) == 0
                data_slice(d1, d2) = 0;
            end
        end
    end
    
    map_global;tightmap;
    title ('75%');
    h = pcolorm(plat, plon, data_slice);
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

    subplot(2,3,6);
    data_slice = squeeze(big_data(month, :, :, model));
    binary_data_slice = squeeze(big_binary_data_max(month, :, :, model));
    for d1 = 1:size(data_slice, 1)
        for d2 = 1:size(data_slice, 2)
            if binary_data_slice(d1, d2) == 0
                data_slice(d1, d2) = 0;
            end
        end
    end
    
    map_global;tightmap;
    title ('max');
    h = pcolorm(plat, plon, data_slice);
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

    drawnow;
end


%Friendly month title.
%titstr={'JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'};
%titlename = ['diff\_' modelnames{model} titstr{month}];

end