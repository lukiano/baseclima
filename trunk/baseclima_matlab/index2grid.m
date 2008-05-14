function [dim1, dim2] = index2grid(index, dims)
    index = index - 1;
    dim2 = floor(index/dims(1));
    base = dim2 * dims(1);
    dim1 = index - base + 1;
    dim2 = dim2 + 1;
end