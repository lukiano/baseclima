function gridInfo = CrearGrilla( clusterInfo )
    gridInfo = struct();
    gridInfo.map = struct();
    gridInfo.map.res = 0.5;
    gridInfo.map.minLat = -55.5;
    gridInfo.map.maxLat = -21.5;
    gridInfo.map.minLon = 286;
    gridInfo.map.maxLon = 307;

    for i = 1:clusterInfo.numOfClusters
        [polygon, clusPoly] = calculateMinPolygon(clusterInfo, i);

        gridInfo.clusters(i).polygon = polygon;
        gridInfo.clusters(i).points = clusPoly;

        % MIN_LAT, MIN_LON - 360, MAX_LAT, MAX_LON - 360, GRID_RES
        gridInfo.clusters(i).influence = gridInfluence(gridInfo, i);
    end
end

function influence = gridInfluence(gridInfo, clusNum)
    influenced = 0;
    influence = [];

    minX = gridInfo.map.minLat;
    maxX = gridInfo.map.maxLat;
    minY = gridInfo.map.minLon - 360;
    maxY = gridInfo.map.maxLon - 360;
    res = gridInfo.map.res;

    polygon = gridInfo.clusters(clusNum).polygon;

    if (length(polygon) == 0)
        influenced = [];
        return;
    end

    [polyX, polyY] = poly2cw(polygon(:,1),polygon(:,2));

    %Optimization #1: Minimize grid area to check polygon intersection
    minX = minX + res * floor((min(polyX) - minX) / res);
    minY = minY + res * floor((min(polyY) - minY) / res);
    maxX = maxX - res * floor((maxX - max(polyX)) / res);
    maxY = maxY - res * floor((maxY - max(polyY)) / res);

    for x = minX:res:maxX
        for y = minY:res:maxY
            sqrX = [x, x, x+res, x+res, x];
            sqrY = [y, y+res, y+res, y, y];

            [intX, intY] = polybool('intersection',polyX,polyY,sqrX,sqrY);

            if (length(intX) > 2)
                % Have intersection
                influenced = influenced + 1;
                influence(1, influenced) = x;
                influence(2, influenced) = y;
                influence(3, influenced) = 1;
            end
        end
    end
end

function [polygon, clusPoly] = calculateMinPolygon(clustersInfo, i)
    k = 1;
    aCluster = [];
    for j = 1:size(clustersInfo.positions,1)
        if (clustersInfo.clusterMap(clustersInfo.bmus(j)) == i)
            aCluster(k) = j;
            k = k+1;
        end
    end

    clusPoly = clustersInfo.positions(aCluster,:);

    if length(aCluster) < 3
        polygon = [];
        return;
    end

    xp = clusPoly(:,1);
    yp = clusPoly(:,2);
    verts = convhull(xp, yp);

    polygon = [xp(verts), yp(verts)];
end