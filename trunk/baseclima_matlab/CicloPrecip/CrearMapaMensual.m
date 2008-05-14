%Made by Juan
function [sD, sMap, positions, ids] = CrearMapaMensual( dataMatrix, labelsEstacion, mes, desiredUnits )
%   Clasificacion de ciclos estacionales usando SOM
%   La variable dataMatrix tiene una fila por cada estacion por cada mes, con
%   informacion de precipitaciones para el mes de la matriz.
%   La variable labels tiene informacion de identificacion para cada vector de la
%   matriz.

    allColumns = {'ID', 'Lat Original', 'Lon Original', 'Mes',...
        'Latitud','Longitud','Altura','Coc.Dias.Precip',...
        'p5','p15','p25','p35','p45','p55','p65','p75','p85','p95',...
       'tn5','tn15','tn25','tn35','tn45','tn55','tn65','tn75','tn85','tn95',...
        'tx5','tx15','tx25','tx35','tx45','tx55','tx65','tx75','tx85','tx95'};

    %Filtro las columnas que me interesan para el mapa.
    %p5, p35, p65 y p95 de cada variable, mas coc y altura, y posicion
    columnFilter = [5,6,7,9,12,15,18,19,22,25,28,29,32,35,38];

    selectedColumns = allColumns(columnFilter);

    % Se arman la matriz de datos y el arreglo de etiquetas filtrando solo
    % la informacion correspondiente al mes indicado
    % (como maximo hay un vector por estacion).
    monthLabels = cell(0);
    j = 0;
    for i = 1:length(dataMatrix)
        if dataMatrix(i, 4) == mes
            j=j+1;
            monthLabels(j) = labelsEstacion(i);
            monthData(j,:) = dataMatrix(i, columnFilter);
            positions(j,:) = dataMatrix(i, 2:3);
            ids(j) = dataMatrix(i,1);
        end
    end

    monthData(:,4:7) = monthData(:,4:7) * 0.2;
    monthData(:,8:15) = monthData(:,8:15) * 0.125;

    colSum = sum(abs(monthData));
    %disp('Creating map, using cols: ');
    %disp(selectedColumns);

    disp('with sums: ');
    disp(num2str(colSum));

    % Se genera la estructura SOM.
    sD = som_data_struct(monthData, 'comp_names', selectedColumns, 'labels', monthLabels');

    % Se construye y entrena la red.
    sMap = som_make(sD, 'lattice', 'hexa', 'munits', desiredUnits, 'tracking', 0);
end