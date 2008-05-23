function run = get_run(string)
    run = regexp(string, 'run[0-9]*', 'match');
    run = run{1};
end