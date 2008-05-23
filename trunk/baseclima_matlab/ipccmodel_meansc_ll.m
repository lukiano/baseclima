%year: must be greater than 2000 for 'sresa2' and 'sresa1b' and greater than 1900 for '20c3m'
function ipccmodel_meansc_ll(scen, cvar, year)

% This program computes the interpolation of the IPCC runs onto the 
% CRU datagridty
% model_interp('20c3m','ipsl_cm4',1,'tas')
% year si activated for climate change scenarios in order to choose the
% 25-year period and for 20th century to compare the two 25year periods.

dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
%dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
if (dirString == 0)
    % no directory was chosen, exit program
    return;
end

dirfile=[dirString '/' cvar '_' scen '_*_allyears.mat'];
models = dir([dirfile]);
nbmod=length(models);

smooth = 12;

for imod=1:nbmod
    
    fullname = models(imod).name
    
    %if strcmp(model, 'gfdl_cm2_0') == 0
    %    continue;
    %end
    
    load([ dirString '/' fullname], 'data', 'x', 'y');
    model = fullname(length([cvar '_' scen])+2:strfind(fullname, '_run')-1);
    run = fullname(length([cvar '_' scen '_' model])+2:strfind(fullname, '_allyears')-1);
    offset = 2000;
    if strcmp(scen, '20c3m') == 1
        offset = 1900;
    end
    ini_year = year - smooth - offset;
    
    ini_month = (ini_year - 1) * 12 + 1; %resto un anio, paso a meses y sumo un mes (la numeracion empieza de 1)
    cant_meses = (2*smooth + 1) * 12;
    
    slice_data = data(ini_month:ini_month+cant_meses-1, :, :);
    slice_data = reshape(slice_data, (2*smooth + 1), 12, size(slice_data, 2), size(slice_data, 3));
    
    data = squeeze(mean(slice_data, 1));
    

    %Save the interpolated field
    fileout=[dirString '/' cvar '_' scen '_' model '_' run '_year' num2str(year) '.mat' ];
    
    save(fileout, 'model', 'x', 'y','data');
end

return 
end




