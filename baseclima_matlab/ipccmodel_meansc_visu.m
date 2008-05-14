%Modified by Luciano. TODO: document

%Displays data built by 'ipccmodel_meansc_diagscen()'?
function ipccmodel_meansc_visu(scen, cvar, year, month)

% This program visualizes the climate change diagnostics

datadirout = ''; %/Users/Shared/IPCC/Interp2Cru/';
filein=[datadirout cvar '_' scen '_year' num2str(year) '.mat' ];
load(filein,'nbmod','models','x','y','fmod','fmodm','fmodsc',...
    'fmoddiagm','fmoddiagsc');

if month == 0
    fdiag=fmoddiagm;
    fmall=fmodm;
    mm='00';
else
    fdiag=squeeze(fmoddiagsc(:,month,:,:));
    fmall=squeeze(fmodsc(:,month,:,:));
    if month < 10
        mm = ['0' num2str(month)]
    else
        mm = num2str(month)
    end
end

titstr=[' MEAN ',' MIN  ',' MAX  ',' STD  ',' 25%  ',' 75%  '];
titstr=reshape(titstr,6,6)';

nip=[1 3 4 5 6 8];
x=circshift(x, [72 1]);
x(1:72)=x(1:72)-360;
x = x - 0.5;
[plon,plat]=meshgrid(x,y);
figure; %a4
if strcmp(scen,'20c3m') == 1
    cmin=[-4 -4 -4 0 -4 -4];
    cmax=[+4 +4 -4 2 +4 +4];
    cmin=cmin*2;
    cmax=cmax*2;
    ncol=20;
else
    deg_min = -4;
    deg_max = +4;
    cmin=[deg_min deg_min deg_min 0 deg_min deg_min];
    cmax=[deg_max deg_max deg_max 1 deg_max deg_max];
    cmin=cmin;
    cmax=cmax;
    ncol=20;
end

for ip=1:6
    subplot(2,3,ip);
    mode=ip;
    map_globe;tightmap;
    title ( titstr(ip,1:6) );
    dots = squeeze(fdiag(nip(ip),:,:));
    dots = circshift(dots, [0 72]);
    h=pcolorm(plat, plon, dots);
    caxis([cmin(ip), cmax(ip)]);
    cmap=colormap(jet(ncol));                     % set N. of colors.
    %if cmin(ip) < 0
    %    cmap([ncol/2 ncol/2+1],:)=1;
    %end
    colormap(cmap);
    colorbar('horizon');
    shading interp;
    hold on;
    %[c3,h3]=contourm(plat,plon,squeeze(fdiag(nip(ip),:,:)),[ 0 0 ],'k');
    %set(h3,'linestyle','-','linewidth',1.2,'visible','on')
    hold on;
    % Re-Draw the map
    load coast_world
    plotm(latW,lonW,'k')
    hold on;
end


%Friendly month title.
%titstr={'JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'};
%if porc == 1
%    eval(['print -dpng ' cvar '_diag' mm '_' scen '_per' num2str(per) '_' titstr{1} '_porc' ]);
%else
%    eval(['print -dpng ' cvar '_diag' mm '_' scen '_per' num2str(per) '_' titstr{1} ]);
%end


return
end
