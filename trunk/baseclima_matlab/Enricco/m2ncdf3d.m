function m2ncdf3d(data,varname,time,lat,lon,output,unitt,unitvar,description)
% M2NETCDF create output file in netcdf format (output.nc), in the 
% current directory.
%
% Usage:
%               M2NETCDF(data,varname,time,lat,lon,output)
%
% m          Input field. Its size must be N*L*I.
%                           N = n° of time steps.
%                           L = n° of latitudes.
%                           I = n° of longitudes.
% varname    variable name. 
% lat        latitude vector.
% lon        longitude vector.
% output     output file name.
%___________________________________________________________________________
if length(size(data)) ~=3, error('m must be N*L*I '); end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
data(isnan(data))=-999;
%% ----------------------------- DEFINE THE FILE --------------------------- %
ncquiet                                          % No NetCDF warnings.
nc = netcdf(output, 'clobber');                  % Create NetCDF file.
nc.description = description;					 % Global attributes.
nc.author = 'Jean-Philippe Boulanger';
nc.date = date;
%% ---------------------------- DEFINE DIMENSIONS ---------------------------
% %
nc('latitude') = size(lat,1);                    
nc('longitude') = size(lon,1);
nc('time')=size(time,2);
%% ---------------------------- DEFINE VARIABLES ---------------------------
% %
nc{'latitude'} = 'latitude';                   
nc{'longitude'} = 'longitude';
nc{'time'} = {'time'};
nc{varname} = {'time','latitude', 'longitude'};
%% ---------------------------- DEFINE ATTRIBUTES ---------------------------
% %
nc{'latitude'}.units = 'degrees';                % Attributes.
nc{'longitude'}.units = 'degrees';
nc{'time'}.units = unitt;
nc{varname}.units = unitvar;
nc{varname}.undef = '-999';
%% ---------------------------- STORE THE DATA ---------------------------- %

nc{'latitude'}(:) = lat;                        
nc{'longitude'}(:) = lon;
nc{'time'}(:) = time;
nc{varname}(:) = data;
nc = close(nc);                                 % Close the file.

