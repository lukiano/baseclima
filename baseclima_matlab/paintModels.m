function paintModels(month, lowerpart, models)
    if (lowerpart == 1)
        load(['binarydata_0to25_' num2str(month) '.mat'], 'big_binary_data', 'gridpoints', 'dims');
    else
        load(['binarydata_75to100_' num2str(month) '.mat'], 'big_binary_data', 'gridpoints', 'dims');
    end

    filecmap='precip_7903_meansc.mat';
    load(filecmap,'npi','npj','xgrid','ygrid');
    x = xgrid;
    y = ygrid;
    % Begin drawing...
    [plon,plat] = meshgrid(x,y);
    cmin = 0;
    cmax = power(2, length(models));
    ncol = cmax;
    
    data = NaN(length(y), length(x));
    for i = 1:length(gridpoints)
        value = 0;
        for j = 1:length(models)
            model = models(j);
            if big_binary_data(i, model) == 1
                value = value + power(2, j - 1);
            end
        end
        if (value > 0)
            index = gridpoints(i);
            [dim1, dim2] = index2grid(index, dims);
            data(dim1, dim2) = value;
        end
    end
    load coast_world;
    figure; %a4l;
    map_global; 
    tightmap;
    title ( 'title' );
    pcolorm(plat, plon, data);
    caxis([cmin, cmax]);
    cmap=colormap(jet(ncol));                     % set N. of colors.
    colormap(cmap);
    colorbar('horizon');
    shading interp;
    hold on;
    % Re-Draw the map
    plotm(latW, lonW, 'k');
    hold on;
    drawnow;

end

function [dim1, dim2] = index2grid(index, dims)
    index = index - 1;
    dim2 = floor(index/dims(1));
    base = dim2 * dims(1);
    dim1 = index - base + 1;
    dim2 = dim2 + 1;
end