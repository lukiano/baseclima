load 'latW.txt'
load 'lonW.txt'
fileout=['/Applications/Matlab71/LatLon/coast_world'];
save(fileout,'lonW','latW');

load 'latsa.txt'
load 'lonsa.txt'
fileout=['/Applications/Matlab71/LatLon/coast_sa'];
save(fileout,'lonsa','latsa');

load 'latar.txt'
load 'lonar.txt'
fileout=['/Applications/Matlab71/LatLon/coast_argentina'];
save(fileout,'lonar','latar');

load 'latDA.txt'
load 'lonDA.txt'
fileout=['/Applications/Matlab71/LatLon/coast_DAargentina'];
save(fileout,'lonDA','latDA');
