%Made by Luciano, so you know whom to address for errors.

%scen can be 'sresa2' or 'srea1b'
%cvar must be 'tas' to make sense
%n_degree can be 1 or 2 degrees above average (3 goes beyond year 2100)
%use_average 0: build each matlab workspace centered on the year when model reaches n degrees
%            1: build each matlab workspace centered on the average year when all models reach n degrees
%latitudes: border latitudes, uses both hemispheres
function grilla = ipccmodel_showmean_ndegrees_lats(scen, cvar, n_degree, use_average, latitudes)
    %construye el promedio de los puntos de la grilla del modelo para un
    %periodo de 25 anios centrado en aquel anio cuyo valor promedio anual
    %es N grados mayor al valor promedio anual de los anios 1975-2000.

if strcmp(scen,'20c3m') == 1
    'A 21th century scen is needed.'
    return;
end

latsString = '';
for i = 1:size(latitudes, 1)
    latsString = [latsString '_' num2str(latitudes(i, 1)) '_' num2str(latitudes(i, 2)) ];
end
load([cvar '_' scen '_smoothed_years_lats' latsString '.mat'], 'models', 'smoothed_years','years', 'latitudes');

sum_smoothed_years = squeeze(sum(smoothed_years, 1));
nbmod = size(sum_smoothed_years, 1);

avg = zeros(nbmod, 1);
year_avg = zeros(nbmod, 1);
year_1987 = 1987 - years(1); % 1987 is avg of 1975-2000
for i = 1:nbmod
    year_n_degree = find(sum_smoothed_years(i,:) >= sum_smoothed_years(i, year_1987) + n_degree);
    if isempty(year_n_degree)
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

model_no_degree = find(year_avg == -1);
year_avg(model_no_degree) = []; %really they reach N degree beyond 2100
avg(model_no_degree) = [];
models(model_no_degree) = [];

if use_average == 1
    avg_year_avg = round(mean(year_avg, 1));
    year_avg(:) = avg_year_avg;
end

grilla = cell( length(models), 2 + 2 * size(smoothed_years, 1) );
for i = 1:length(models)
    grilla{i,1} = models{i};
    grilla{i,2} = year_avg(i);
    diferencia_total = sum_smoothed_years(i, avg(i)) - sum_smoothed_years(i, year_1987);
    for j = 1:size(smoothed_years, 1)
        diferencia = smoothed_years(j, i, avg(i)) - smoothed_years(j, i, year_1987);
        grilla{i,2*j+1} = diferencia;
        grilla{i,2*j+2} = diferencia / diferencia_total;
    end
end

end
