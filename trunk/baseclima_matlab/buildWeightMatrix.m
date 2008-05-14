function m = buildWeightMatrix()
    theta = 2.5 * pi / 180;
    lats = pi / theta;
    longs = 2*pi / theta;
    m = zeros(lats, longs);
    for i = 1:lats
       for j = 1:longs
           m(i,j) = theta * theta * cos(i * theta - (pi/2));
       end
    end
    sum(sum(m))
end