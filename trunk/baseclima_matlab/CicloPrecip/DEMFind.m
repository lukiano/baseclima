function [x, y] = DEMFind(dem, lat0, lon0)

    FILE_LAT_0 = -75;
    FILE_LAT_1 = -50;
    FILE_LON_0 = -55;
    FILE_LON_1 = -20;
    
    WID = (FILE_LAT_1 - FILE_LAT_0) / size(dem, 1);
    HEI = (FILE_LON_1 - FILE_LON_0) / size(dem, 2);

    x = round((lat0 - FILE_LAT_0) / WID);
    y = round((lon0 - FILE_LON_0) / HEI);
end
