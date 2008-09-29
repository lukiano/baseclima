%Modified by Luciano. TODO: javadoc

function ipccmodel_meansc_avg_model21_model20(scen, cvar, year21, year20)

    % this program reads the matlab files del ciclo estacional de todos los
    % modelos y crea un archivo global de todos los modelos incluyendo varios
    % tests.

    dirString = uigetdir('./modelos','Choose data directory');
    if (dirString == 0)
        % no directory was chosen, exit program
        return;
    else
        files = dir(dirString); % obtain file names
        names = transpose({files.name});
        % only retain those file names we are interested in
        rexp = regexp(names, [cvar '_' scen '_.*_year' num2str(year21) '.mat']);
        contador = 1;
        truenames = cell(0);
        for i = 1:length(rexp)
            if not(isempty(rexp{i}))
                truenames(contador) = {names{i}};
                contador = contador + 1;
            end
        end
        fmod = []; x = 0; y = 0; nbmod = 0;
        for tn = truenames
            fullname = fullfile(dirString, tn{1});
            run = get_run(tn{1});
            struc_Sresa2 = load(fullname, 'model');
            model_name = struc_Sresa2.model;
            fullname21 = fullfile(dirString, [cvar '_' scen '_' model_name '_' run '_year' num2str(year21) '.mat']);
            fullname20 = fullfile(dirString, [cvar '_20c3m_' model_name '_' run '_year' num2str(year20) '.mat']);
            [fmod, x, y] = do_diff_model21_model20(fmod, fullname21, fullname20, model_name);
            nbmod = nbmod + 1;
        end

        npj = length(y); npi = length(x);
        
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
        fileout=[datadirout cvar '_' scen '_year' num2str(year21) '_year' num2str(year20) '.mat' ];
        save(fileout,'nbmod','x','y','npi','npj','fmod','fmodm','fmodsc',...
                     'fmoddiagm','fmoddiagsc');

    end
end

function [fmod,x,y] = do_diff_model21_model20(fmod, file_scen21, file_scen20, model_name)

    %load scen21
    struc_scen21 = load(file_scen21, 'data', 'x', 'y');
    
    data_scen21 = struc_scen21.data;
    %if month == 0
    %    data_scen21 = squeeze(mean(struc_scen21.data, 1));
    %else
    %   data_scen21 = squeeze(struc_scen21.data(month,:,:));
    %end

    %load axis
    x = struc_scen21.x;
    y = struc_scen21.y;

    %load scen20
    struc_scen20 = load(file_scen20, 'data');
    data_scen20 = struc_scen20.data;
    %if month == 0
    %    data_scen20 = squeeze(mean(struc_scen20.data, 1));
    %else
    %    data_scen20 = squeeze(struc_scen20.data(month,:,:));
    %end
    fmod(size(fmod, 1) + 1, :, :, :) = data_scen21 - data_scen20;
end

    