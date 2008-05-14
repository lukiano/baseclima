%Made by Luciano, so you know whom to address for errors.

%latitudes: border latitudes, uses both hemispheres
function ipccmodel_meansc_ndegrees_2(scen, cvar, n_degree, latitudes)
    %construye el promedio de los puntos de la grilla del modelo para un
    %periodo de 25 anios centrado en aquel anio cuyo valor promedio anual
    %es N grados mayor al valor promedio anual de los anios 1972-2000.

    %used by ipccmodel_meansc_diff_sresa2_sresa1b_ndegree()
if strcmp(scen,'20c3m') == 1
    'A 21th century scen is needed.'
    return;
end

dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
%dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
if (dirString == 0)
    % no directory was chosen, exit program
    return;
end


% This program computes the interpolation of the IPCC runs onto the 
% CRU datagrid

latsString = '';
for i = 1:size(latitudes, 1)
   latsString = [latsString '_' num2str(latitudes(i, 1)) '_' num2str(latitudes(i, 2)) ];
end
load([cvar '_' scen '_smoothed_years_lats' latsString '.mat'], 'models', 'runs', 'smoothed_years', 'years', 'latitudes');

nbmod = size(smoothed_years, 1);

avg = zeros(nbmod, 1);
year_avg = zeros(nbmod, 1);
year_1987 = 1987 - years(1); % 1987 is avg of 1975-2000
for i = 1:nbmod
    xx = find(smoothed_years(i,:) >= smoothed_years(i,year_1987) + n_degree);
    if isempty(xx)
        avg(i) = -1;
    else
        avg(i) = xx(1);
    end
    
    if avg(i) == -1
        year_avg(i) = -1;
    else
        year_avg(i) = years(avg(i));
    end
end

models
year_avg'

smooth = 12;

for imod=1:nbmod
    if avg(imod) == -1
        continue;
    end
    
    model = models{imod}
    run = runs{imod};
    
    load([ dirString '/' cvar '_' scen '_' model '_' run '_allyears.mat'], 'data', 'x', 'y');
    
    ini_year = avg(i) - 100; %year_avg(imod) - smooth - 2000;
    end_year = ini_year + 2*smooth + 1;
    
    ini_month = (ini_year - 1) * 12 + 1; %resto un anio, paso a meses y sumo un mes (la numeracion empieza de 1)
    cant_meses = (2*smooth + 1) * 12;
    
    slice_data = data(ini_month:ini_month+cant_meses-1, :, :);
    slice_data = reshape(slice_data, (2*smooth + 1), 12, size(slice_data, 2), size(slice_data, 3));
    
    data = squeeze(mean(slice_data, 2));
    

    %Save the interpolated field
    fileout=[dirString '/' cvar '_' scen '_' model '_' run '_' num2str(n_degree) 'degree_2.mat' ];
    
    save(fileout, 'model', 'x', 'y','data');
end

end
