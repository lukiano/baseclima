function mask = getMasks(regional_masks, cluster_filename, cluster_number_mask)
    load('land_masks.mat');
    if isempty(regional_masks)
        %transparent mask
        mask = ones(size(land_mask));
    elseif length(regional_masks) == 1 && strcmp(regional_masks{1}, 'land') == 1
        mask = land_mask;
    else
        mask = zeros(size(land_mask));
        for i = 1:length(regional_masks)
            if strcmp(regional_masks{i}, 'southamerica') == 1
                mask = mask | southamerica_mask;
            elseif strcmp(regional_masks{i}, 'northamerica') == 1
                mask = mask | northamerica_mask;
            elseif strcmp(regional_masks{i}, 'europe') == 1
                mask = mask | europe_mask;
            elseif strcmp(regional_masks{i}, 'siberia') == 1
                mask = mask | siberia_mask;
            elseif strcmp(regional_masks{i}, 'india') == 1
                mask = mask | india_mask;
            elseif strcmp(regional_masks{i}, 'australia') == 1
                mask = mask | australia_mask;
            elseif strcmp(regional_masks{i}, 'africa') == 1
                mask = mask | africa_mask;
            elseif strcmp(regional_masks{i}, 'north') == 1
                mask = mask | north_mask;
            elseif strcmp(regional_masks{i}, 'south') == 1
                mask = mask | south_mask;
            end
        end
        if containsMask(regional_masks, 'land')
            mask = mask & land_mask;
        elseif containsMask(regional_masks, 'ocean')
            mask = mask & ocean_mask;
        end
    end
    
    if ~isempty(cluster_filename)
        load(cluster_filename, 'cluster_masks');
        if cluster_number_mask > 0 && cluster_number_mask <= size(cluster_masks, 1)
            mask = mask & squeeze(cluster_masks(cluster_number_mask, :, :));
        end
    end
    mask = double(mask);
end

function ret = containsMask(masks, masknameString) 
    ret = sum(strcmp(masks, masknameString)) > 0;
end
