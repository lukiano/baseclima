function [bmus, labels] = RecuperarNeuronaDato( sMap, sData )
% Identifica la neurona asignada a cada dato de sData. Devuelve los bmus y
% los labels correspondientes.

labels = sData.labels;
[bmus, v] = som_bmus(sMap, sData);

end