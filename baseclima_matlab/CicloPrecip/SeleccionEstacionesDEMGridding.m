function goodEsts = SeleccionEstacionesDEMGridding(mes, datamatrix, ests, dem)
    lat = -60;
    lon = -30;

    [px0, py0] = DEMFind(dem, lat - 0.05, lon - 0.05);
    [px1, py1] = DEMFind(dem, lat + 0.05, lon + 0.05);
    [px, py] = DEMFind(dem, lat, lon);

    sm = dem(px0 + 1 : px1, py0 + 1 : py1);
    pointAlt = max(max(sm));

    j = 1;
    for i = 1:size(ests,2)
        pos = getPosicionEstacion(ests(i), mes, datamatrix);
        est = datamatrix(pos, :);

        [ex, ey] = DEMFind(dem, est(3), est(2));

        sm = dem(ex - 2 : ex + 2, ey - 2 : ey + 2);
        estAlt = max(max(sm));

        inBet = inBetween(px, py, ex, ey, dem);

        alturaEnCamino = prctile(inBet, 90);
        minAlt = min(inBet);
        if (isOrographyOK(estAlt, pointAlt, alturaEnCamino, minAlt))
            goodEsts(j) = est(1);
            j = j+1;
        else
            a = 1;
        end
    end
end

function t = isOrographyOK(estAlt, pointAlt, alturaEnCamino, minAlt)
    t = alturaEnCamino < max(estAlt, pointAlt);
end

function vals = inBetween(xp, yp, xe, ye, dem)
    xv = [xp, xe, xp + 0.001];
    yv = [yp, ye, yp];
    [xv, yv] = poly2cw(xv, yv); 

    k = 1;
    for i = min([xp, xe]):max([xp, xe])
        for j = min([yp, ye]):max([yp, ye])
            d = p_poly_dist(i, j, xv, yv);
            if (d < 2)
                %ps(k, 1) = i;
                %ps(k, 2) = j;
                vals(k) = dem(i, j);
                k = k + 1;
            end
        end
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
