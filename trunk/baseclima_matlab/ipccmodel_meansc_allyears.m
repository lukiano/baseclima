%Builds 'allyears' files from netcdf files. (SRES or 20c3m files)
%scen: 'sresa2', 'sresa1b', '20c3m', etc
%type: 'atm', 'ice'
%cvar: 'tas', 'pr', 'sic', etc
function ipccmodel_meansc_allyears(scen, type, cvar)

% This program computes the interpolation of the IPCC runs onto the 
% CRU datagrid
% model_interp('20c3m','ipsl_cm4',1,'tas')
% per si activated for climate change scenarios in order to choose the
% 25-y period and for 20th century to compare the two 25year periods.
%dir = '/Users/Shared/IPCC';
dirinput = '';
dirfile=[dirinput '/' scen '_' type '_mo_' cvar '/'];
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

for imod=1:nbmod
    model = models(imod).name
    
    %if strcmp(model, 'gfdl_cm2_0') == 0
    %    continue;
    %end

    runs=dir([dirinput '/' scen '_' type '_mo_' cvar '/' model '/run*']);
    run=runs(1).name;
    datadir=[dirinput '/' scen '_' type '_mo_' cvar '/' model '/' run '/'];
    files=dir([datadir cvar '*.nc']);
    filenc1=[datadir files(1).name]
    %nc=netcdf(filenc1,'nowrite');
    %interpolacion
    % we extract the last 102 years to get the same size as the CRU data
    xgrid=nc_varget(filenc1,'lon');
    ygrid=nc_varget(filenc1,'lat');
    time=nc_varget(filenc1,'time');
    npi=length(xgrid);
    npj=length(ygrid);
    jpt=length(time);
     if jpt ~= 1200
         'STOP'
         jpt
     end
    if strcmp(scen,'20c3m') == 1
        ini_day=jpt-1200+1;
        end_day=jpt;
        if strcmp(model,'giss_model_e_r') == 1
            jpt = jpt - 12 * 3; % ends in 'dec 2003'
            ini_day=jpt-1200+1;
            end_day=jpt;
        elseif strcmp(model, 'bcc_cm1') == 1
            % for 20c3m we use 1950-2000 data in sresa2 as 20c3m seems to
            % be faulty
            datadir=[dirinput '/sresa2_' type '_mo_' cvar '/bcc_cm1/' run '/'];
            files=dir([datadir cvar '*.nc']);
            filenc1=[datadir files(1).name];
            xgrid=nc_varget(filenc1,'lon');
            ygrid=nc_varget(filenc1,'lat');
            time=nc_varget(filenc1,'time');
            
            jpt = 600; %50 years * 12 months
            ini_day=1;
            end_day=jpt;
            
        %elseif strcmp(model,'mpi_echam5') == 1
        %    jpt = jpt - 12 * 100; % ends in 'dec 2100'
        %    ini_day=jpt-1200+1;
        %    end_day=jpt;
        end
        if strcmp(model,'ingv') ==1
            if strcmp(cvar,'pr') == 1
                data=nc_varget(filenc1,'precip');
                data=data(ini_day:end_day,:,:);
                data=data*1000.;
            else
                data=nc_varget(filenc1,'temp2');
                data=data(ini_day:end_day,:,:);
            end                
        else
            %data=nc_varget(filenc1,cvar,[ini_day - 1, 0, 0], [end_day - ini_day, npj, npi]);
            %data=data(ini_day:end_day,:,:);
        end
        time=time(ini_day:end_day);

%         data = nc_varget(filenc1, cvar);
%         [data, x ,y] = process(model, cvar, data, time, xgrid, ygrid);
%     
%         %Save the interpolated field
%         datadirout = '/Users/Shared/IPCC/Interp2Cru/';
%         fileout=[datadirout cvar '_' scen '_' model '_' run '_allyears.mat' ];
%         save(fileout,'model','run','time','x','y','data','npi','npj'); %'datani','xgrid','ygrid'
%         return;

    else
        %if strcmp(cvar,'tas') == 1
            if strcmp(scen,'sresa2') == 1 && strcmp(model, 'bcc_cm1') == 1
                ini_day = 600 + 1;
                data=nc_varget(filenc1,cvar);
                data=data(ini_day:end,:,:);
            elseif strcmp(scen,'sresa2') == 1 && strcmp(model, 'giss_model_er') == 1
                % Sresa2 starts in 2004, 20c3m ends in 2003.
                % habria que cargar los 3 ultimos anios del 20c3m
                datadir20=[dirinput '/20c3m_' type '_mo_' cvar '/' model '/' run '/'];
                files20=dir([datadir20 cvar '*.nc']);
                filenc1_20=[datadir20 files20(1).name];
                data20=nc_varget(filenc1_20,cvar);
                data20=data20(end-35:end,:,:);
                time20=nc_varget(filenc1_20,'time');
                time20=time20(end-35:end,:,:);
                ini_day=1;
                data=nc_varget(filenc1,cvar);
                cat(1, data20, data);
                cat(1, time20, time);
                data=data(ini_day:end,:,:);
            else
                ini_day=1;
                
                segments = 10;
                slice = jpt / segments;
                data = nc_varget(filenc1,cvar,[0, 0, 0], [slice, npj, npi]);
                [data, x ,y] = process(model, cvar, data, time(1:slice), xgrid, ygrid);
                for s = 1:(segments-1)
                    data2 = nc_varget(filenc1,cvar,[s * slice, 0, 0], [slice, npj, npi]);
                    [data2, x ,y] = process(model, cvar, data2, time(s*slice+1:(s+1)*slice), xgrid, ygrid);
                    data = cat(1, data, data2);
                    data2 = [];                   
                end
                
                %Save the interpolated field
                datadirout = [dirinput '/Interp2Cru/'];
                fileout=[datadirout cvar '_' scen '_' model '_' run '_allyears.mat' ];
    
                save(fileout,'model','run','time','x','y','data','npi','npj'); %'datani','xgrid','ygrid'
                
                return;
            end
        %elseif strcmp(cvar,'pr') == 1
%             if strcmp(scen,'sresa2') == 1 & strcmp(model, 'giss_model_e_r') == 1 & per > 1
%                 ini_day=1; % The file starts in 2004-1-1
%             elseif strcmp(scen,'sresa2') == 1 & strcmp(model, 'ukmo_hadgem1') == 1 & per == 4
%                 ini_day=1; % The file stops in Nov 2099
%             elseif strcmp(scen,'sresa1b') ==1 & strcmp(model, 'ncar_pcm1') == 1 & per == 4
%                 ini_day=1; % The file stops in Oct 2099
%             elseif strcmp(scen,'sresa1b') == 1 & strcmp(model, 'ukmo_hadgem1') == 1 & per == 4
%                 ini_day=1; % The file stops in Nov 2099
%             else
%                 ini_day=1;
%             end
%         else
        %    ini_day=1;
        %    data=nc_varget(filenc1,cvar);
        %    data=data(ini_day:end,:,:);
%            end
        %end
        ss=size(data);
        jpt=ss(1);
        time=time(ini_day:end);
    end
    
    timelength = end_day - ini_day + 1;
    
    segments = 10;
    slice = timelength / segments;
    init = jpt - timelength;
    data = nc_varget(filenc1,cvar,[init, 0, 0], [slice, npj, npi]);
    [data, x ,y] = process(model, cvar, data, time(1:slice), xgrid, ygrid);
    for s = 1:(segments-2)
        data2 = nc_varget(filenc1,cvar,[init + s * slice, 0, 0], [slice, npj, npi]);
        time2 = time(s*slice+1:(s+1)*slice);
        [data2, x ,y] = process(model, cvar, data2, time(s*slice+1:(s+1)*slice), xgrid, ygrid);
        data = cat(1, data, data2);
        data2 = [];                   
    end
    data2 = nc_varget(filenc1,cvar,[init + (segments-1) * slice, 0, 0], [timelength - ((segments-1) * slice), npj, npi]);
    time2 = time((segments-1)*slice+1:segments*slice);
    
    [data2, x ,y] = process(model, cvar, data2, time2, xgrid, ygrid);
    data = cat(1, data, data2);
    data2 = [];                   
    
    %Save the interpolated field
    datadirout = [dirinput '/Interp2Cru/'];
    fileout=[datadirout cvar '_' scen '_' model '_' run '_allyears.mat' ];
    save(fileout,'model','run','time','x','y','data','npi','npj'); %'datani','xgrid','ygrid'
end

return 
end

function [data, x ,y] = process(model, cvar, data, time, xgrid, ygrid)
    if strcmp(model,'bcc_cm1') ==1
        if strcmp(cvar,'pr') == 1
            data=data*4.;
        end
    end
    if strcmp(cvar, 'pr') == 1
        data=data*86400;
    elseif strcmp(cvar, 'tas') == 1
        data=data-273.15;
    end
    
    [data, x ,y] = interpolate(data, time, xgrid, ygrid);
end

function [data, x ,y] = interpolate(data, time, xgrid, ygrid)
    %Compute the interpolation
    %datani=data;
    
    %time=zeros(12,1);
    %for it=1:12
    %    time(it)=datenum(0000,it,15);
    %end
    
    %Now interpolate the seasonal field
    jpt=length(time);
    npX=length(xgrid);
    npY=length(ygrid);
 
    %IMPORTANT
    % Considering that all the fields are global fields, the longitudes are
    % periodic of period 360, this implies that the data and the xgrid MUST be
    % extended by one more point to the east to be sure the interpolation will
    % work properly
    xgrid2=[xgrid(:,1)' (xgrid(1,1)+360.)]';
    data_n=zeros(jpt,npY,npX+1);
    data_n(:,:,1:npX)=data(:,:,1:npX);
    data_n(:,:,npX+1)=data(:,:,1);
    npX=length(xgrid2);

    %newgrid
    npi=144;
    npj=72;
    nglon=(1:npi)*2.5-2.5/2;
    nglat=-90+(1:npj)*2.5-2.5/2;
    j=find(nglon >= xgrid(1));
    x=nglon(min(j(:)))+(0:npi-1)*2.5;
    y=nglat;
    nx=length(x);
    ny=length(y);

    [XG YG]=meshgrid(xgrid2,ygrid);
    [xgc ygc]=meshgrid(x,y);

    data=zeros(jpt,npj,npi);
    vue=0;
    for i=1:size(data_n,1)
        Z=squeeze(data_n(i,:,:));
        data(i,1:ny,1:nx)=interp2(XG,YG,Z,xgc,ygc,'linear');
        jfig=mod(i,2);
        if jfig==1 && vue == 1
            figure(1)
            ech(2)=max(abs(Z(:)));
            if strcmp(cvar,'tas') == 1
                ech(1)=273;
            else
                ech(1)=0;
            end

            subplot(2,1,1)
            m_proj('miller','lon',[XG(1) XG(end)],'lat',[YG(1) YG(end)]);
            m_contourf(XG,YG,Z);
            caxis(ech);
            colormap;
            hold on
            m_coast('color','k');
            m_grid('tickdir','out','linestyle','none','ticklen',.02);
            hidden off
            colorbar('SouthOutside')

            %ech=ech-273,15;
            subplot(2,1,2)
            m_proj('miller','lon',[xgc(1) xgc(end)],'lat',[ygc(1) ygc(end)]);
            m_contourf(xgc,ygc,squeeze(data(i,:,:)));
            caxis(ech);
            colormap;
            hold on
            m_coast('color','k');
            m_grid('tickdir','out','linestyle','none','ticklen',.02);
            hidden off
            colorbar('SouthOutside')

            hold off
            drawnow
        end
    end

    % In case the x grid does not start at nglon(1)
    jdec=min(j(:));
    if jdec > 1
        data=circshift(data,[0 0 -jdec+1]);
    end
end


function test

dirfile=['/Users/Shared/IPCC/' scen '_atm_mo_' cvar '/'];
models=dir([dirfile '*']);
nbmod=length(models);
models=models(4:nbmod);
nbmod=length(models);

for imod=1:nbmod
    model=models(imod).name
    runs=dir(['/Users/Shared/IPCC/' scen '_atm_mo_' cvar '/' model '/run*']);
    run=runs(1).name;
    datadir=['/Users/Shared/IPCC/' scen '_atm_mo_' cvar '/' model '/' run '/'];
    files=dir([datadir cvar '*.nc']);
    filenc1=[datadir files(1).name]
    nc=netcdf(filenc1,'nowrite');
    %interpolacion
    % we extract the last 102 years to get the same size as the CRU data
%     xgrid=nc{'lon'}(:);
%     ygrid=nc{'lat'}(:);
    time=nc{'time'}(:);
%     npi=length(xgrid);
%     npj=length(ygrid);
    jpt=length(time);
    if jpt ~= 1200
        'STOP'
        jpt
    end

end

%%%%

scen='sresa1b'
scen='sresb1'
scen='sresa2'
ipccmodel_meansc_allyears(scen,cvar)

scen='20c3m'
cvar='tas'
ipccmodel_meansc_allyears(scen,cvar)

return
end



