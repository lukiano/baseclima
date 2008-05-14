function ipccmodel_meansc_visu_ndegree(scen, cvar, ndegree, month)

% This program visualizes the climate change diagnostics

datadirout=[''];
filein=[datadirout cvar '_' scen '_' num2str(ndegree) 'degree.mat' ];
load(filein,'nbmod','models','x','y','npi','npj','fmod','fmodm','fmodsc',...
    'fmoddiagm','fmoddiagsc');

if month == 0
    fdiag=fmoddiagm;
    fmall=fmodm;
    mm='00';
else
    fdiag=squeeze(fmoddiagsc(:,month,:,:));
    fmall=squeeze(fmodsc(:,month,:,:));
    if month < 10
        mm=['0' num2str(month)];
    else
        mm=num2str(month);
    end
end
%fdiag=fdiag/86400;
%     for ip=1:nbmod
%         %x: 109 a 113
%         %y: 31 a 36
%         ind(ip)=mean(mean(fmall(ip,31:36,109:113)));
%     end
%     ind


titstr=[' MEAN ',' MIN  ',' MAX  ',' STD  ',' 25%  ',' 75%  '];
titstr=reshape(titstr,6,6)';

nip=[1 3 4 5 6 8];
x=circshift(x,[72 1]);
x(1:72)=x(1:72)-360;
x = x - 0.5;
[plon,plat]=meshgrid(x,y);
figure; %a4
if strcmp(scen,'20c3m') == 1
    cmin=[-4 -4 -4 0 -4 -4];
    cmax=[+4 +4 -4 2 +4 +4];
    cmin=cmin*2;
    cmax=cmax*2;
    ncol=20;
else
    deg_min = -4;
    deg_max = +4;
    cmin=[deg_min deg_min deg_min 0 deg_min deg_min];
    cmax=[deg_max deg_max deg_max 1 deg_max deg_max];
    cmin=cmin;
    cmax=cmax;
    ncol=20;
end

for ip=1:6
    subplot(2,3,ip);
    mode=ip;
    map_globe;tightmap;
    title ( titstr(ip,1:6) );
    dots = squeeze(fdiag(nip(ip),:,:));
    dots = circshift(dots, [0 72]);
    h=pcolorm(plat, plon, dots);
    caxis([cmin(ip), cmax(ip)]);
    cmap=colormap(jet(ncol));                     % set N. of colors.
    %if cmin(ip) < 0
    %    cmap([ncol/2 ncol/2+1],:)=1;
    %end
    colormap(cmap);
    colorbar('horizon');
    shading interp;
    hold on;
    %[c3,h3]=contourm(plat,plon,squeeze(fdiag(nip(ip),:,:)),[ 0 0 ],'k');
    %set(h3,'linestyle','-','linewidth',1.2,'visible','on')
    hold on;
    % Re-Draw the map
    load coast_world
    plotm(latW,lonW,'k')
    hold on;
end
drawnow
savfig=0;
if savfig == 1
    eval(['print -dbmp FIGALL/' cvar '_diag' mm '_' scen '_' num2str(ndegree) 'degree' ]);
end


opt=0;
if opt == 1
    a4
%     cmin=[-4 -4 0 -4 -4 -4 -4 -4 -4];
%     cmax=[+4 +4 4 +4 +4 +4 +4 +4 +4];

    if strcmp(scen,'20c3m') == 1
        titstr=[' BCC1  ',' BCC2  ','CCCMA1 ','CCCMA2 ',' CNRM  ',...
                ' CSIRO ',' GFDL1 ',' GFDL2 ',...
                ' GISS1 ',' GISS2 ',' GISS3 ',...
                '  IAP  ',' INGV  ','  INM  ',' IPSL  ',...
                'MIROCHR','MIROCMR',' MIUB  ','  MPI  ','  MRI  ',...
                ' NCAR1 ',' NCAR2 ',...
                ' UKMO1 ',' UKMO2 '];
        titstr=reshape(titstr,7,nbmod)'
        cmin=-10;
        cmax=-cmin;
        posc=[0.78 0.15 0.15 0.015]; % OK
    elseif strcmp(scen,'sresa2') == 1
        titstr=[' BCC2  ','CCCMA1 ',' CNRM  ',...
                ' CSIRO ',' GFDL1 ',' GFDL2 ',...
                ' GISS3 ',...
                '  INM  ',' IPSL  ','MIROCMR',' MIUB  ','  MPI  ','  MRI  ',...
                ' NCAR1 ',' NCAR2 ',...
                ' UKMO1 ',' UKMO2 '];
        titstr=reshape(titstr,7,nbmod)'
        cmin=-2;
        cmax=-cmin;
        posc=[0.50 0.35 0.15 0.015]; % OK
    elseif strcmp(scen,'sresb1') == 1
        titstr=[' BCC2  ','CCCMA1 ','CCCMA2 ',' CNRM  ',...
                ' CSIRO ',' GFDL1 ',' GFDL2 ',...
                ' GISS1 ',' GISS3 ',...
                '  IAP  ','  INM  ',' IPSL  ','MIROCHR','MIROCMR',' MIUB  ','  MPI  ','  MRI  ',...
                ' NCAR1 ',' NCAR2 ',...
                ' UKMO1 '];
        titstr=reshape(titstr,7,nbmod)'
        cmin=-5;
        cmax=-cmin;
    else %strcm(scen,'sresa1b') == 1
        titstr=['CCCMA1 ','CCCMA2 ',' CNRM  ',...
                ' CSIRO ',' GFDL1 ',' GFDL2 ',...
                ' GISS1 ',' GISS2 ',' GISS3 ',...
                '  INM  ',' IPSL  ','MIROCHR','MIROCMR',' MIUB  ','  MPI  ','  MRI  ',...
                ' NCAR1 ',' NCAR2 ',...
                ' UKMO1 ',' UKMO2 '];
        titstr=reshape(titstr,7,nbmod)'
        cmin=-5;
        cmax=-cmin;
    end

    for ip=1:nbmod
        model=models(ip).name
        subplot(5,5,ip)
        mode=ip
        map_sa;tightmap;
        title ( titstr(ip,:) );
        h=pcolorm(plat,plon,squeeze(fmall(ip,:,:)));
        
        %caxis([cmin(ip), cmax(ip)]);
        caxis([cmin cmax]);
        ncol=20;
        cmap=colormap(jet(ncol));                     % set N. of colors.
        cmap([ncol/2 ncol/2+1],:)=1;
        colormap(cmap);
        %colorbar('horizon');
        shading interp;
        hold on;
        [c3,h3]=contourm(plat,plon,squeeze(fmall(ip,:,:)),[ 0 0 ],'k');
        set(h3,'linestyle','-','linewidth',1.2,'visible','on')
        hold on;
        % Re-Draw the map
        load coast_sa
        plotm(latsa,lonsa,'k')
        hold on;
    end
    subplot('position',[-1 -1 0.1 0.1]);
    map_sa;tightmap;
    h=pcolorm(plat,plon,squeeze(fmall(ip,:,:)));
    caxis([cmin cmax]);
    colormap(cmap);
    h=colorbar('horiz');
    set(h,'position',posc);
    colormap(cmap);
    drawnow
    if savfig == 1
        eval(['print -dbmp FIGALL/' cvar '_allm' mm '_' scen '_per' num2str(per) ]);
    end
end    

return
end


function test

cvar='pr';
scen='20c3m';
per=2;
month=0;
ipccmodel_meansc_visu(scen,cvar,per,month)

cvar='pr';
scen='sresa2';
per=4;
month=0;
ipccmodel_meansc_visu(scen,cvar,per,month)


return
end



