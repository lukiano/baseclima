%Made by Luciano, so you know whom to address for errors.

function modelnames() 
    dirString = uigetdir('/Users/Shared','Choose data directory');
    if (dirString == 0)
        % no directory was chosen, exit program
        return;
    end

    files = dir(dirString); % obtain file names
    names = transpose({files.name});
    % only retain those file names we are interested in
    rexp = regexp(names, 'pr_sresa2_.*_per4.mat'); % 'pr_20c3m_.*_per2.mat'
    contador = 1;
    truenames = cell(0);
    for i = 1:length(rexp)
        if not(isempty(rexp{i}))
            truenames(contador) = {names{i}};
            contador = contador + 1;
        end
    end

    %for each file, load the data variable
    for tn = truenames
        sprintf('%s',tn{1})
    end
end
