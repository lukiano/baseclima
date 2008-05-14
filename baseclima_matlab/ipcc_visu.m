function ipcc_visu(scen, cvar, ndegree, month)

% This program visualizes the climate change diagnostics

datadirout=[''];
fileout=[datadirout cvar '_' scen '_' num2str(ndegree) 'degree.mat' ];
load(fileout,'nbmod','models','x','y','npi','npj','fmod','fmodm','fmodsc',...
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


titstr=[' MEAN ','MEDIAN',' MIN  ',' MAX  ',' STD  ',' 25%  ',' 50%  ',' 75%  ',' 90%  '];
titstr=reshape(titstr,6,9)';

nip=[1 2 5 3 6 7 4 9 8];
[plon,plat]=meshgrid(x,y);
a4
if strcmp(scen,'20c3m') == 1
    cmin=[-4 -4 0 -4 -4 -4 -4 -4 -4];
    cmax=[+4 +4 2 +4 +4 +4 +4 +4 +4];
    cmin=cmin*2;
    cmax=cmax*2;
    ncol=20;
else
    cmin=[-2 -2 0 -2 -2 -2 -2 -2 -2];
    cmax=[+2 +2 1 +2 +2 +2 +2 +2 +2];
    cmin=cmin;
    cmax=cmax;
    ncol=20;
end

for ip=1:9
    subplot(3,3,ip);
    mode=ip;
    map_sa;tightmap;
    title ( titstr(nip(ip),1:6) );
    h=pcolorm(plat,plon,squeeze(fdiag(nip(ip),:,:)));
    caxis([cmin(ip), cmax(ip)]);
    cmap=colormap(jet(ncol));                     % set N. of colors.
    if cmin(ip) < 0
        cmap([ncol/2 ncol/2+1],:)=1;
    end
    colormap(cmap);
    colorbar('horizon');
    shading interp;
    hold on;
    [c3,h3]=contourm(plat,plon,squeeze(fdiag(nip(ip),:,:)),[ 0 0 ],'k');
    set(h3,'linestyle','-','linewidth',1.2,'visible','on')
    hold on;
    % Re-Draw the map
    load coast_sa
    plotm(latsa,lonsa,'k')
    hold on;
end
drawnow
savfig=1;
if savfig == 1
    eval(['print -dbmp FIGALL/' cvar '_diag' mm '_' scen '_per' num2str(per) ]);
end


opt=1;
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



