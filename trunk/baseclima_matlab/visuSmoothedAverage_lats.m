%Made by Luciano, so you know whom to address for errors.

%common_models 0: all models for each scen; 1: only models available in all scens
%latitudes: border latitudes, uses both hemispheres
function visuSmoothedAverage_lats(cvar, common_models, latitudes)

   latsString = '';
   for i = 1:size(latitudes, 1)
       latsString = [latsString '_' num2str(latitudes(i, 1)) '_' num2str(latitudes(i, 2)) ];
   end
   struct_sresa2 = load([cvar '_sresa2_smoothed_years_lats' latsString '.mat'], 'models', 'smoothed_years','years', 'latitudes');
   %struct_sresa1b = load([cvar '_sresa1b_smoothed_years_lats' latsString '.mat'], 'models', 'smoothed_years','years', 'latitudes');
   
   years = struct_sresa2.years;
   
   indices = find(years >=1976 & years <= 2085);
   years = years(indices);
   
    sresa2_models = struct_sresa2.models;
%    if common_models == 1
%         erased = 0;
%         for i = 1:length(struct_sresa2.models)
%             model_sresa2 = struct_sresa2.models{i};
%             found = 0;
%             for j = 1:length(struct_sresa1b.models)
%                 model_sresa1b = struct_sresa1b.models{j};
%                 if strcmp(model_sresa2, model_sresa1b) == 1
%                     found = 1;
%                     break;
%                 end
%             end
%             if found == 0
%                 sresa2_models(i - erased) = [];
%                 struct_sresa2.smoothed_years(:, i - erased, :) = [];
%                 erased = erased + 1;
%             end
%         end
%     end
% 
%     sresa1b_models = struct_sresa1b.models;
%     if common_models == 1
%         erased = 0;
%         for i = 1:length(struct_sresa1b.models)
%             model_sresa1b = struct_sresa1b.models{i};
%             found = 0;
%             for j = 1:length(struct_sresa2.models)
%                 model_sresa2 = struct_sresa2.models{j};
%                 if strcmp(model_sresa1b, model_sresa2) == 1
%                     found = 1;
%                     break;
%                 end
%             end
%             if found == 0
%                 sresa1b_models(i - erased) = [];
%                 struct_sresa1b.smoothed_years(:, i - erased, :) = [];
%                 erased = erased + 1;
%             end
%         end
%     end

   colors={'b-', 'g-', 'r-', 'c-', 'm-', 'b-.', 'g-.', 'r-.', 'c-.', 'm-.', 'b--', 'g--', 'r--', 'c--', 'm--', 'b:', 'g:', 'r:', 'c:', 'm:','b-','g-','r-'};

   for i = 1:size(struct_sresa2.smoothed_years, 2)
          sresa2_models{i} = regexprep(sresa2_models{i},'\_','\\_');
   end
   
   for lat = 1:size(struct_sresa2.latitudes, 1)
       figure;
       hold on
       for i = 1:size(struct_sresa2.smoothed_years, 2)
           sm_years = squeeze(struct_sresa2.smoothed_years(lat, i, indices));
           sm_years = sm_years - sm_years(1);
           plot(years, sm_years, colors{i});
       end
       legend(sresa2_models,'location','EastOutside');
       title(['sresa2 ' num2str(struct_sresa2.latitudes(lat,1)) ' - ' num2str(struct_sresa2.latitudes(lat,2))]);
       grid on;
       hold off;
       %axis([1900 2100 -5 5]);
       drawnow;
   end
   
%   for lat = 1:size(struct_sresa1b.latitudes, 1)
%     figure;
%     hold on
%     for i = 1:size(struct_sresa1b.smoothed_years, 2)
%         sm_years = squeeze(struct_sresa1b.smoothed_years(lat, i, indices));
%         sm_years = sm_years - sm_years(1);
%         plot(years, sm_years, colors{i});
%         sresa1b_models{i} = regexprep(sresa1b_models{i},'\_','\\_');
%     end
%     legend(sresa1b_models,'location','EastOutside');
%     title(['sresa1b ' num2str(struct_sresa1b.latitudes(lat,1)) ' - ' num2str(struct_sresa2.latitudes(lat,2))]);
%     grid on;
%     hold off;
%     axis([1980 2100 -0.5 4.5]);
%     drawnow;
%   end
end