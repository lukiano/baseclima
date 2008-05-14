%Made by Luciano, so you know whom to address for errors.

%Displays one map for each model. 
%cvar: 'tas' ('pr' doesn't make much of a sense right now).
%n_degree: load averages centered in this number of degrees.
%kind_visu: 
%   1: displays 0 or 1 for each model according to passing or not the student's t-test
%   2: displays t-test number for each model 
%   31: displays average for each model (sresa2)
%   32: displays average for each model (sresa1b)
%   33: displays average for each model (sresa2 - sresa1b)
%   34: displays average for each model (sresa2) - (sresa1b)
%   41: displays standard deviation for each model (sresa2)
%   42: displays standard deviation for each model (sresa1b)
%   43: displays standard deviation for each model (sresa2 - sresa1b)
%   44: displays standard deviation for each model (sresa2) - (sresa1b)
function ipccmodel_meansc_diff_sresa2_sresa1b_ndegree(cvar, n_degree, kind_visu)
dirString = uigetdir('/Users/Shared/IPCC','Choose data directory');
%dirString = uigetdir('g:\workspace\BaseClima\matlab','Choose data directory');
if (dirString == 0)
    % no directory was chosen, exit program
    return;
else
    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, [cvar '_sresa2_.*_' num2str(n_degree) 'degree_2.mat']);
    contador = 1;
    truenames = cell(0);
    for i = 1:length(rexp)
        if not(isempty(rexp{i}))
            truenames(contador) = {names{i}};
            contador = contador + 1;
        end
    end
    for tn = truenames
        fullname = fullfile(dirString, tn{1});
        struc_Sresa2 = load(fullname, 'model');
        model_name = struc_Sresa2.model;
        fullnamesresa2 = fullfile(dirString, [cvar '_sresa2_' model_name '_run1_' num2str(n_degree) 'degree_2.mat']);
        fullnamesresa1b = fullfile(dirString, [cvar '_sresa1b_' model_name '_run1_' num2str(n_degree) 'degree_2.mat']);
        
        do_diff_modelsresa2_modelsresa1b(fullnamesresa2, fullnamesresa1b, model_name, kind_visu);
        
    end
end

return
end

function do_diff_modelsresa2_modelsresa1b(file_scenSresa2, file_scenSresa1b, model_name, kind_visu)

%load scenSresa2
struc_scenSresa2 = load(file_scenSresa2, 'data', 'x', 'y');
data_scenSresa2 = struc_scenSresa2.data;

%load axis
x = struc_scenSresa2.x;
y = struc_scenSresa2.y;

%load scenSresa1b

fid = fopen(file_scenSresa1b);
if fid == -1
    return; %file does not exist
else
    fclose(fid);
end

struc_scenSresa1b = load(file_scenSresa1b, 'data');
data_scenSresa1b = struc_scenSresa1b.data;

titlename = ['diff\_' regexprep(model_name,'\_','\\_') ];

data = zeros(size(data_scenSresa2, 2), size(data_scenSresa2, 3));

for i=1:size(data_scenSresa2, 2)
    for j=1:size(data_scenSresa2, 3)
        d1 = data_scenSresa2(:,i,j);
        d2 = data_scenSresa1b(:,i,j);
        %Handle NaNs
        if sum(isnan(d1)) || sum(isnan(d2))
            data(i,j) = NaN;
        else
            if kind_visu == 1
                data(i,j) = ttest2(d1, d2, 0.05, 'both', 'unequal');
            elseif kind_visu == 2
                [h,p,ci,stats] = ttest2(d1, d2, 0.05, 'both', 'unequal');
                data(i,j) = stats.tstat;
            elseif kind_visu == 31
                data(i,j) = mean(d1);
            elseif kind_visu == 32
                data(i,j) = mean(d2);
            elseif kind_visu == 33
                data(i,j) = mean(d1 - d2);
            elseif kind_visu == 34
                data(i,j) = mean(d1) - mean(d2);
            elseif kind_visu == 41
                data(i,j) = std(d1);
            elseif kind_visu == 42
                data(i,j) = std(d2);
            elseif kind_visu == 43
                data(i,j) = std(d1 - d2);
            elseif kind_visu == 44
                data(i,j) = std(d1) - std(d2);
            end
        end
    end
end


    
% Begin drawing...
x=circshift(x,[72 1]);
x(1:72)=x(1:72)-360;
x = x - 0.5;
[plon,plat] = meshgrid(x,y);
cmin = 0;
cmax = 2;

ncol = 10;
    
load coast_world;
    
figure; %a4l   
map_globe;tightmap;
title ( titlename );

data = circshift(data, [0 72]);
pcolorm(plat, plon, data);
caxis([cmin, cmax]);
cmap=colormap(jet(ncol));                     % set N. of colors.
if cmin < 0
    cmap([ncol/2 ncol/2+1],:)=1;
end
colormap(cmap);
colorbar('horizon');
 shading interp;
%hold on;
% Re-Draw the map
plotm(latW,lonW,'k')
%hold on;
drawnow;

%Save picture as PNG.
%eval(['print -dpng ' cvar '_diff_' model_name '_21_20_' titstr{month+1}]);

return
end