function TestGridding( data, textdata, mes, x, y )
    clusts = ClusterFromRaw( data, textdata, 0, 11, 0);
    ClusterFromRaw( data, textdata, mes, mes, 1);

    load('DEMS');
    ests = SeleccionEstacionesGridding(0, data, clusts);
    ests2 = SeleccionEstacionesDEMGridding(0, data, ests, dem);

    figure;
    
    axesm('MapProjection','eqdcylin', 'MapLatLimit',[-55.5 -21.5], 'Maplonlimit',[286 307], 'grid','on', 'MeridianLabel', 'on', 'ParallelLabel', 'on');
    load coast_argentina; 
    plotm(latar,lonar,'k');

    scatterm(y, x, 25, 'filled', 'b');
    
    for i = 1:length(ests)
        pos = getPosicionEstacion(ests(i), mes, data);
        if length(find(ests2 == ests(i)) > 0)
            col = 'r';
        else
            col = 'y';
        end
        scatterm(data(pos, 2), data(pos, 3), 20, 'filled', col);
    end
end

function pos = getPosicionEstacion(id, mes, datamatrix)
    for i = 1:size(datamatrix, 1)
        if (datamatrix(i, 1) == id && datamatrix(i, 4) == mes)
            pos = i;
            break;
        end
    end
end
