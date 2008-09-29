%Made by Luciano, so you know whom to address for errors.

%scen can be 'sresa2' or 'srea1b'
%cvar must be 'tas' to make sense
%n_degree can be 1 or 2 degrees above average (3 goes beyond year 2100)
%latitudes: border latitudes, uses both hemispheres
function year_avg = ipccmodel_meansc_visu_ndegrees(scen, cvar, n_degree, latitudes)
    %construye el promedio de los puntos de la grilla del modelo para un
    %periodo de 25 anios centrado en aquel anio cuyo valor promedio anual
    %es N grados mayor al valor promedio anual de los anios 1975-2000.

if strcmp(scen,'20c3m') == 1
    'A 21th century scen is needed.'
    return;
end

if strcmp(cvar,'tas') == 0
    'Only TAS makes sense with degrees. (for now al least)' 
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
load([cvar '_sresa2_smoothed_years_lats' latsString '.mat'], 'models', 'runs', 'smoothed_years','years', 'latitudes');

smoothed_years = squeeze(smoothed_years(1,:,:)); %use the first latitude
nbmod = size(smoothed_years, 1);

avg = zeros(nbmod, 1);
year_avg = zeros(nbmod, 1);
year_1987 = 1987 - years(1); % 1987 is avg of 1975-2000
year_2087 = 2087 - years(1); % 2087 is avg of 2075-2100
for i = 1:nbmod
    year_n_degree = find(smoothed_years(i,:) >= smoothed_years(i,year_1987) + n_degree & smoothed_years(i,:));
    if isempty(year_n_degree)
        avg(i) = -1;
    elseif year_n_degree(1) > year_2087
        avg(i) = -1;
    else
        avg(i) = year_n_degree(1);
    end
    
    if avg(i) == -1
        year_avg(i) = -1;
    else
        year_avg(i) = years(avg(i));
    end
end

%show models and average year
models
year_avg'

end
