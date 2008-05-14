function dem = ReadDEMData(dataDir, areaRatio)
    vals = struct();
    pattern = strcat(dataDir, '*.TIF');
    d = dir(pattern);

    LAT_0 = 360;
    LON_0 = 360;
    LAT_F = -360;
    LON_F = -360;

    for i = 1:8
        info = geotiffinfo(strcat(dataDir, d(i).name));
        LAT_0 = min(LAT_0, info.BoundingBox(1, 1));
        LON_0 = min(LON_0, info.BoundingBox(1, 2));

        LAT_F = max(LAT_F, info.BoundingBox(2, 1));
        LON_F = max(LON_F, info.BoundingBox(2, 2));

        ORIG_SIZE = [info.Height, info.Width];
    end

    xChunks = round(LAT_F - LAT_0) / 5;
    yChunks = round(LON_F - LON_0) / 5;

    xsize = xChunks * ORIG_SIZE(1) / areaRatio;
    ysize = yChunks * ORIG_SIZE(2) / areaRatio;

    %dem(1:ysize, 1:xsize) = NaN;
    
    for i = 1:8
        name = strcat(dataDir, d(i).name);
        data = geotiffread(name);
        info = geotiffinfo(name);

        datashort = ChangeRes(data, areaRatio);

        vals(i).box = info.BoundingBox;

        [x, y] = subMatPos(LAT_0, LON_0, info.BoundingBox(1, 1), info.BoundingBox(1, 2));
        [sx, sy] = size(datashort);

        xStart = x * sx + 1;
        xEnd = (x + 1) * sx;

        y = yChunks - y - 1;
        yStart = y * sy + 1;
        yEnd = (y + 1) * sy;

        dem(yStart : yEnd, xStart : xEnd) = datashort(:, sx : -1 : 1);

        display(strcat('Archivo ', num2str(i)));
    end
end

function [xsub, ysub] = subMatPos(lat0, lon0, latS0, lonS0)
    xsub = round(latS0 - lat0) / 5;
    ysub = round(lonS0 - lon0) / 5;
end

function rv = ChangeRes( mat, factor )
    for i = 0:(size(mat, 1)/factor)-1
        for j = 0:(size(mat, 2)/factor)-1
            ir = i*factor+1:(i+1)*factor;
            jr = j*factor+1:(j+1)*factor;

            submat = mat(ir, jr);
            submat(submat<0) = NaN;
            
            if length(find(submat>0)) == 0
                rv(i+1, j+1) = NaN;
            else
                rv(i+1, j+1) = mean(mean(submat));
            end
        end
    end
end

