%Made by Luciano, so you know whom to address for errors.

%latitudes: border latitudes, uses both hemispheres
function buildSmoothedAverage_lats(scen, cvar, latitudes)
    %Construye la distribucion del promedio anual para todos los anios del
    %modelo, promediando con 25 anios.

    dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
    %dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
    if (dirString == 0)
        % no directory was chosen, exit program
        return;
    end
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_' scen '.*_allyears.mat']);
    contador = 1;
    truenames = cell(0);
    for i = 1:length(rexp)
        if not(isempty(rexp{i}))
            truenames(contador) = {names{i}};
            contador = contador + 1;
        end
    end
    
    load('weightMatrix.mat', 'weight_matrix');
    
    smooth = 12; %12 from each side plus middle number = 25 numbers averaged to one
    contador = 1;
    
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        struc = load(fullname, 'data', 'model', 'x', 'y');
        data = struc.data;
        run = get_run(tn{1});
        
        full20c3mname = fullfile(dirString, [cvar '_20c3m_' struc.model '_' run '_allyears.mat']);
        struc20c3m = load(full20c3mname, 'data', 'model');
        data = cat(1, struc20c3m.data, data);
        
        data(find(isnan(data))) = 0; % as we'll be doing a sum, zero means nothing

        for i = 1:size(data, 1)
            data(i,:,:) = squeeze(data(i,:,:)) .* weight_matrix;
        end

        for j = 1:size(latitudes, 1)
            data_tmp = data;
            fromLat = latitudes(j, 1);
            toLat = latitudes(j, 2);
            
            for lat_index = 1:size(data_tmp, 2) % second dimension is latitude
                lat = struc.y(lat_index);
                if abs(lat) < abs(fromLat) || abs(lat) > abs(toLat)
                %if lat < fromLat || lat > toLat
                    data_tmp(:, lat_index, :) = 0;
                end
            end
        
            months_years = squeeze(sum(sum(data_tmp, 2), 3));
        
            cant_my = length(months_years);
            if floor(cant_my / 12) ~= (cant_my / 12)
                missing = (floor(cant_my / 12) * 12 + 12) - cant_my;
                months_years = cat(1, months_years, zeros(missing, 1)); 
            end
        
            months_years = reshape(months_years, 12, length(months_years) / 12);
            years = sum(months_years, 1) / 12;
    
            for i = (1+smooth) : length(years)-smooth
                smoothed_years(j, contador, i - smooth) = sum(years(i-smooth:i+smooth)) / (smooth + smooth + 1);
            end
        end
        
        models{contador} = struc.model;
        runs{contador} = run;
        contador = contador + 1;
    end
    
    longitud = size(smoothed_years, 3);
    years = [];
    years(1:longitud) = (1901 + smooth : 1901 + smooth + longitud - 1);
    
    latsString = '';
    for i = 1:size(latitudes, 1)
        latsString = [latsString '_' num2str(latitudes(i, 1)) '_' num2str(latitudes(i, 2)) ];
    end
    save([cvar '_' scen '_smoothed_years_lats' latsString '.mat'], 'models', 'runs', 'smoothed_years', 'years', 'latitudes');
end
