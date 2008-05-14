function MapDEMData( map )
    figure;

%    axesm('MapProjection','eqdcylin', 'grid','on');
    axesm('MapProjection','eqdcylin', 'grid','on');

    load coast_argentina; 
    [latar, lonar] = poly2cw(latar, lonar);
    [latar, lonar] = polybool('union', latar, lonar, latar, lonar);
    plotm(latar,lonar,'k');

        lat = [-75, -50] + 360;
        lon = [-55, -20];

        %Preprocess
        %data(:,:) = map(size(map,1):-1:1,:);
        surfm(lat, lon, log2(map));

    tightmap;
end

