%Made by Luciano, so you know whom to address for errors.

%scen can be 'sresa2' or 'srea1b'
%n_degree can be 1 or 2 degrees above average (3 goes beyond year 2100)
%latitudes: border latitudes, uses both hemispheres
function ipccmodel_meansc_visu_years_ndegrees(scen, n_degree, year20, latitudes)
    %construye el promedio de los puntos de la grilla del modelo para un
    %periodo de 25 anios centrado en aquel anio cuyo valor promedio anual
    %es N grados mayor al valor promedio anual de los anios year20 
    %(centrado 25 anios).

if strcmp(scen,'20c3m') == 1
    'A 21th century scen is needed.'
    return;
end

% if strcmp(cvar,'tas') == 0
%     'Only TAS makes sense with degrees. (for now at least)' 
%     return;
% end

%dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
%dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
dirString = uigetdir('./modelos','Choose data directory');
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
load(['tas' '_' scen '_smoothed_years_lats' latsString '.mat'], 'models', 'runs', 'smoothed_years','years', 'latitudes');

smoothed_years = squeeze(smoothed_years(1,:,:)); %use the first latitude
nbmod = size(smoothed_years, 1);

avg = zeros(nbmod, 1);
year_avg = zeros(nbmod, 1);
%year_1987 = 1987 - years(1); % 1987 is avg of 1975-2000
%year_1976 = 1976 - years(1); % 1976 is avg of 1961-1990 (IPCC)
year20 = year20 - years(1);
year_2087 = 2087 - years(1); % 2087 is avg of 2075-2100 (max value)
for i = 1:nbmod
    year_n_degree = find(smoothed_years(i,:) >= smoothed_years(i,year20) + n_degree & smoothed_years(i,:));
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

sresa2_models = cell(nbmod, 1);
for i = 1:nbmod
    sresa2_models{i} = regexprep(models{i},'\_','\\_');
end
%show models and average year
colors={'b-', 'g-', 'r-', 'c-', 'm-', 'b-.', 'g-.', 'r-.', 'c-.', 'm-.', 'b--', 'g--', 'r--', 'c--', 'm--', 'b:', 'g:', 'r:', 'c:', 'm:','b-','g-','r-'};
colormap = hsv(nbmod);
figure;
hold on;

for i = 1:nbmod
    bar(i, year_avg(i), 'FaceColor', colormap(i,:), 'DisplayName', 'pepe');
    text(i-.3, year_avg(i) + .2, num2str(year_avg(i)));
end
legend(sresa2_models, 'location', 'EastOutside');
AXIS([0 nbmod+1 min(year_avg)-2 max(year_avg)+2]);
axis square;
xlabel('Models');
ylabel('Years');
hold off;
drawnow;

end