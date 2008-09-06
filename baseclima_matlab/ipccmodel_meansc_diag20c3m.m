function ipccmodel_meansc_diag20c3m(cvar, year20)

% this program reads the matlab files del ciclo estacional de todos los
% modelos y crea un archivo global de todos los modelos incluyendo varios
% tests. Solamente para el siglo 20 y hace comparaciones con los datos de
% CMAP.

filecmap='/Users/jpb/Data/XIE/precip_7903_meansc.mat';
load(filecmap,'npi','npj','xgrid','ygrid','cmapsc');
fobs=cmapsc;
fobsm=squeeze(mean(fobs,1));

scen='20c3m';
datadirout=['/Users/jpb/Data/IPCC/Interp2Cru/'];
dirfile=['/Users/jpb/Data/IPCC/20c3m_atm_mo_' cvar '/'];
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
fmod=zeros(nbmod,12,72,144);

['nombre de modeles ' scen ' ' num2str(nbmod)]


% Read the 20c3m file
for imod=1:nbmod
    model=models(imod).name
    runs=dir(['/Users/jpb/Data/IPCC/20c3m_atm_mo_' cvar '/' model '/run*']);
    run=runs(1).name;
    filein=[datadirout cvar '_20c3m_' model '_' run '_year' num2str(year20) '.mat' ];
    load(filein,'x','y','data','npi','npj');
    fmod(imod,:,:,:)=data-fobs;
end

test=0;
if test == 1
    datam=data;
    titstr=['JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'];
    titstr=reshape(titstr,3,12)'
    [plon,plat]=meshgrid(x,y);
    a4
    for ip=1:12
        subplot(3,4,ip)
        mode=ip
        map_sa;tightmap;
        title ( titstr(ip,1:3) );
        h=pcolorm(plat,plon,squeeze(datam(ip,:,:)));
        caxis([0 20]);
        cmap=colormap(jet(20));                     % set N. of colors.
        %cmap([8 9],:)=1;
        colormap(cmap);
        colorbar('horizon');
        shading interp;
        hold on;
        [c3,h3]=contourm(plat,plon,squeeze(datam(ip,:,:)),[ 0 0 ],'k');
        set(h3,'linestyle','-','linewidth',1.2,'visible','on')
        hold on;
        % Re-Draw the map
        load coast_sa
        plotm(latsa,lonsa,'k')
        hold on;
    end
end

% Compute the annual mean
fmodm=squeeze(mean(fmod,2));
% Compute the anomalous seasonal cycle
for im=1:12
    fmodsc(:,im,:,:)=fmod(:,im,:,:)-reshape(fmodm,nbmod,1,npj,npi);
end

fmoddiagm=zeros(9,72,144);
% Now compute the mean of the ensemble
fmoddiagm(1,:,:)=squeeze(mean(fmodm,1));
% Now compute the median of the ensemble
fmoddiagm(2,:,:)=squeeze(median(fmodm,1));
% Now compute the min diff of the ensemble
g=mean(fmodm,1);
gg=ones(nbmod,1)*g(1,:);
afmodm=fmodm-reshape(gg,nbmod,npj,npi);
afmodm=fmodm;
fmoddiagm(3,:,:)=min(afmodm,[],1);
% Now compute the max diff of the ensemble
fmoddiagm(4,:,:)=max(afmodm,[],1);
% Now compute the std of the ensemble
fmoddiagm(5,:,:)=std(afmodm,0,1);
% Now compute the 10% threshold
sfmodm=sort(afmodm,1);
ith=ceil(nbmod*0.1);
fmoddiagm(6,:,:)=sfmodm(ith,:,:);
% Now compute the 20% threshold
ith=ceil(nbmod*0.2);
fmoddiagm(7,:,:)=sfmodm(ith,:,:);
% Now compute the 80% threshold
ith=floor(nbmod*0.8);
fmoddiagm(8,:,:)=sfmodm(ith,:,:);
% Now compute the 90% threshold
ith=floor(nbmod*0.9);
fmoddiagm(9,:,:)=sfmodm(ith,:,:);

fmoddiagsc=zeros(9,12,72,144);
for im=1:12
    fmods=squeeze(fmodsc(:,im,:,:));
    % Now compute the mean of the ensemble
    fmoddiagsc(1,im,:,:)=squeeze(mean(fmods,1));
    % Now compute the median of the ensemble
    fmoddiagsc(2,im,:,:)=squeeze(median(fmods,1));
    % Now compute the min diff of the ensemble
    g=mean(fmods,1);
    gg=ones(nbmod,1)*g(1,:);
    afmods=fmods-reshape(gg,nbmod,npj,npi);
    afmods=fmods;
    fmoddiagsc(3,im,:,:)=min(afmods,[],1);
    % Now compute the max diff of the ensemble
    fmoddiagsc(4,im,:,:)=max(afmods,[],1);
    % Now compute the std of the ensemble
    fmoddiagsc(5,im,:,:)=std(afmods,0,1);
    % Now compute the 10% threshold
    sfmods=sort(afmods,1);
    ith=ceil(nbmod*0.1);
    fmoddiagsc(6,im,:,:)=sfmods(ith,:,:);
    % Now compute the 20% threshold
    ith=ceil(nbmod*0.2);
    fmoddiagsc(7,im,:,:)=sfmods(ith,:,:);
    % Now compute the 80% threshold
    ith=floor(nbmod*0.8);
    fmoddiagsc(8,im,:,:)=sfmods(ith,:,:);
    % Now compute the 90% threshold
    ith=floor(nbmod*0.9);
    fmoddiagsc(9,im,:,:)=sfmods(ith,:,:);
end

datadirout=['/Users/jpb/Data/IPCC/Interp2Cru/'];
fileout=[datadirout cvar '_20c3m_per2.mat' ];
save(fileout,'nbmod','models','x','y','npi','npj','fmod','fmodm','fmodsc',...
    'fmoddiagm','fmoddiagsc');


return
end


function test
%%%%
cvar='pr'
per=2
ipccmodel_meansc_s3(cvar,per)

return
end

 