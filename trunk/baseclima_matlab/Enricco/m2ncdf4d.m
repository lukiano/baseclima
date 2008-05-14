function m2ncdf4d(m,varname,lev,lat,lon,output)
% M2NETCDF create output file in netcdf format (output.nc), in the 
% current directory.
%
% Usage:
%               M2NETCDF(m,varname,lev,lat,lon,output,precision)
%
% m          Input field. Its size must be N*M*L*I.
%                           N = n° of modes.
%                           M = n° of levels.
%                           L = n° of latitudes.
%                           I = n° of longitudes.
% varname    variable name. 
% lev        level vector.
% lat        latitude vector.
% lon        longitude vector.
% output     output file name.
%___________________________________________________________________________
if length(size(m)) ~=4, error('m must be N*M*L*I '); end
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
m(isnan(m))=-999;
%% ----------------------------- DEFINE THE FILE --------------------------- %
ncquiet                                          % No NetCDF warnings.
nc = netcdf(output, 'clobber');                  % Create NetCDF file.
nc.description = 'NetCDF file EOFs exe';         % Global attributes.
nc.author = 'Enrico';
nc.date = 'Jan 20, 2006';
%% ---------------------------- DEFINE DIMENSIONS ---------------------------
% %
nc('latitude') = size(lat,1);                    
nc('longitude') = size(lon,2);
nc('lev') = size(lev,1);
nc('mode')=size(m,1);
%% ---------------------------- DEFINE VARIABLES ---------------------------
% %
nc{'latitude'} = 'latitude';                   
nc{'longitude'} = 'longitude';
nc{'lev'} = {'lev'};
nc{'mode'} = {'mode'};
nc{varname} = {'mode', 'lev', 'latitude', 'longitude'};
%% ---------------------------- DEFINE ATTRIBUTES ---------------------------
% %
nc{'latitude'}.units = 'degrees';                % Attributes.
nc{'longitude'}.units = 'degrees';
nc{'lev'}.units = '[]';
nc{'mode'}.units = '[]';
nc{varname}.units = '[]';
nc{varname}.undef = '-999';
%% ---------------------------- STORE THE DATA ---------------------------- %

nc{'latitude'}(:) = lat;                        
nc{'longitude'}(:) = lon;
nc{'lev'}(:) = lev;
nc{'mode'}(:) = [1:1:size(m,1)];
nc{varname}(:) = m;
nc = close(nc);                                 % Close the file.

