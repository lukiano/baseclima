%cvar: 'tas', 'pr'
function ipccmodel_meansc_cru_allyears(cvar)

% This program computes the interpolation of the IPCC runs onto the 
% CRU datagrid
% model_interp('20c3m','ipsl_cm4',1,'tas')
% per si activated for climate change scenarios in order to choose the
% 25-y period and for 20th century to compare the two 25year periods.

    filenc1=['cru2.10_' cvar '_1901_2002.nc']
    %nc=netcdf(filenc1,'nowrite');
    %interpolacion
    % we extract the last 102 years to get the same size as the CRU data
    xgrid=nc_varget(filenc1,'longitude');
    ygrid=nc_varget(filenc1,'latitude');
    time=nc_varget(filenc1,'time_counter');
    npi=length(xgrid);
    npj=length(ygrid);
    jpt=length(time);
    if jpt ~= 1200
        'STOP'
        jpt
    end
    ini_day = 1;
    end_day = 1200;
    if strcmp(cvar,'pr') == 1
        data=nc_varget(filenc1,'pre');
    else
        data=nc_varget(filenc1,'tmp');
    end                
    data=data(ini_day:end_day,:,:);
    datadirout = 'modelos/';
    fileout=[datadirout cvar '_obs_cru_run1_allyears.mat' ];
    model = 'cru';
    run = 'run1';
    x = npi;
    y = npj;
    save(fileout,'model','run','time','x','y','data','npi','npj'); %'datani','xgrid','ygrid'
end
