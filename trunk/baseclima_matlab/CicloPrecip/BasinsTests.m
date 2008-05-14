function BasinsTests()

    gridInfo = struct();
    gridInfo.map = struct();
    gridInfo.map.res = 0.5;
    gridInfo.map.minLat = -65.5;
    gridInfo.map.maxLat = 21.5;
    gridInfo.map.minLon = 276;
    gridInfo.map.maxLon = 317;

    MIN_LAT = gridInfo.map.minLat;
    MAX_LAT = gridInfo.map.maxLat;
    MIN_LON = gridInfo.map.minLon;
    MAX_LON = gridInfo.map.maxLon;
    GRID_RES = gridInfo.map.res;

    lats = [MIN_LAT MAX_LAT];
    lons = [MIN_LON MAX_LON];

    figure;
    hold on

    %axesm('MapProjection','eqdcylin', 'MapLatLimit',[MIN_LAT MAX_LAT], 'Maplonlimit',[MIN_LON MAX_LON], 'grid','on');
    %load coast_sa;
    %plotm(latsa,lonsa,'k');

    axesm('MapProjection','eqaazim','MapLatLimit',[MIN_LAT MAX_LAT], 'Maplonlimit',[MIN_LON MAX_LON],...
        'grid', 'on', 'MeridianLabel', 'on', 'ParallelLabel', 'on');

    s = shaperead('basins/sa_bas.shp','UseGeoCoords', true)';

    for bN = 1:20
        basinX = [];
        basinY = [];
        for i = 1:length(s)
            if (s(i).LEVEL2 == bN)
                [basinX, basinY] = createMapPoly(s(i), basinX, basinY);
                [nX, nY] = mfwdtran(basinX, basinY);
            end
        end
        if (length(basinX) > 0)
            fillm(basinX, basinY, 'r');
        end
        disp(strcat('Done with Basin ', num2str(bN)));
    end

    hold off
end

function [lat, lon] = azimToCoords(x, y, lon0, lat0)
    D2R  = pi / 180.0;
    R2D  = 180.0 / pi;
    meridian = lon0;
	pole = lat0;
	psinp (lat0 * D2R);
	pcosp (lat0 * D2R);

	rho = hypot(x, y);
	c = 2.0 * d_asin (0.5 * rho / EQ_RAD);

    lat = d_asin (cos(c) * psinp + (y * sin(c) * pcosp / rho)) * R2D;
    if (pole == 90.0)
        lon = meridian + R2D * d_atan2(x, -y);
        else if (pole == -90.0)
            lon = meridian + R2D * d_atan2(x, y);
            else
            lon = meridian + R2D * d_atan2(x * sin(c), (rho * pcosp * cos(c) - y * psinp * sin(c)));
        end
    end
end

function a = d_atan2(x, y)
    if (x == 0 && y == 0)
        a = 0;
    else
        a = atan2 (x, y);
    end
end

function a = d_asin(x)
    if (abs(x) < 1)
        asin(x);
    end
end

function [rvX, rvY] = createMapPoly(aNewPoly, basinX, basinY)
    [rvX, rvY] = poly2cw(aNewPoly.Lat, aNewPoly.Lon);
    
    rvX = (rvX / 100000) - 15;
    rvY = (rvY / 100000) - 60;

    if (length(basinX) > 0)
        [rvX, rvY] = polybool('union', basinX, basinY, rvX, rvY);
    end
end