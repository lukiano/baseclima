%year: must be greater than 2000 for 'sresa2' and 'sresa1b' and greater than 1900 for '20c3m'
function ipccmodel_meansc_ll(scen, type, cvar, year)

% This program computes the interpolation of the IPCC runs onto the 
% CRU datagrid
% model_interp('20c3m','ipsl_cm4',1,'tas')
% year si activated for climate change scenarios in order to choose the
% 25-year period and for 20th century to compare the two 25year periods.

dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
%dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
if (dirString == 0)
    % no directory was chosen, exit program
    return;
end

dirfile=['/Users/Shared/IPCC/' scen '_' type '_mo_' cvar '/'];
models=dir([dirfile '*']);
nbmod=length(models);
nn=0;
for i=1:nbmod
    if strncmp(models(i).name,'.',1) == 1
        nn=nn+1;
    end
end
models=models(nn+1:nbmod);
nbmod=length(models);

smooth = 12;

for imod=1:nbmod
    
    model = models(imod).name
    runs=dir(['/Users/Shared/IPCC/' scen '_' type '_mo_' cvar '/' model '/run*']);
    run=runs(1).name;
    
    %if strcmp(model, 'gfdl_cm2_0') == 0
    %    continue;
    %end
    
    load([ dirString '/' cvar '_' scen '_' model '_' run '_allyears.mat'], 'data', 'x', 'y');
    
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




