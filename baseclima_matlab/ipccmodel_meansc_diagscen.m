%Modified by Luciano. TODO: javadoc

function ipccmodel_meansc_diagscen(scen, cvar, year)

% this program reads the matlab files del ciclo estacional de todos los
% modelos y crea un archivo global de todos los modelos incluyendo varios
% tests.

datadirout=['/Users/Shared/IPCC/Interp2Cru/'];
dirfile=['/Users/Shared/IPCC/' scen '_atm_mo_' cvar '/'];
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

['nombre de modelos ' scen ' ' num2str(nbmod)]

    % First read the 20c3m file
    for imod=1:nbmod
        model=models(imod).name
        runs=dir(['/Users/Shared/IPCC/20c3m_atm_mo_' cvar '/' model '/run*']);
        run=runs(1).name;
        filein=[datadirout cvar '_20c3m_' model '_' run '_per2.mat' ];
        load(filein,'x','y','data');
        data1=data;

% Now read the model projections
        runs=dir(['/Users/Shared/IPCC/' scen '_atm_mo_' cvar '/' model '/run*']);
        run=runs(1).name;
        filein=[datadirout cvar '_' scen '_' model '_' run '_year' num2str(year) '.mat' ];
        load(filein,'x','y','data');
        fmod(imod,:,:,:)=data-data1;
        
        npi = length(x);
        npj = length(y);
        
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
    sfmodm=sort(afmodm,1);
    % Now compute the 25% threshold
    ith=ceil(nbmod*0.25);
    fmoddiagm(6,:,:)=sfmodm(ith,:,:);
    % Now compute the 50% threshold
    ith=ceil(nbmod*0.5);
    fmoddiagm(7,:,:)=sfmodm(ith,:,:);
    % Now compute the 75% threshold
    ith=floor(nbmod*0.75);
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
        sfmods=sort(afmods,1);
        % Now compute the 25% threshold
        ith=ceil(nbmod*0.25);
        fmoddiagsc(6,im,:,:)=sfmods(ith,:,:);
        % Now compute the 50% threshold
        ith=ceil(nbmod*0.5);
        fmoddiagsc(7,im,:,:)=sfmods(ith,:,:);
        % Now compute the 75% threshold
        ith=floor(nbmod*0.75);
        fmoddiagsc(8,im,:,:)=sfmods(ith,:,:);
        % Now compute the 90% threshold
        ith=floor(nbmod*0.9);
        fmoddiagsc(9,im,:,:)=sfmods(ith,:,:);
    end
    
    datadirout = '';
    fileout=[datadirout cvar '_' scen '_year' num2str(year) '.mat' ];
    save(fileout,'nbmod','x','y','npi','npj','models','fmod','fmodm','fmodsc',...
                 'fmoddiagm','fmoddiagsc');


return
end

    