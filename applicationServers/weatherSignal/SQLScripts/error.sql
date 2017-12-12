insert into TailDrop select cityname,0,0
from IdealNoDrop
where cityname not in (select cityname from TailDrop );



create table ErrorsTailDrop as
select IdealNoDrop.cityName, IdealNoDrop.samplesNumber as OriginalCount, 
TailDrop.SamplesNumber as NewCount, (IdealNoDrop.samplesNumber-TailDrop.SamplesNumber ) as DroppedPackets, 
abs((((TailDrop.SamplesNumber-IdealNoDrop.samplesNumber ) /IdealNoDrop.samplesNumber) * 100)) as DropPercntage,
(abs(IdealNoDrop.Battemp -TailDrop.Battemp) )as TempError

from TailDrop INNER JOIN IdealNoDrop
ON TailDrop.cityName= IdealNoDrop.cityName
order by TempError DESC;






insert into SmartDrop select cityname,0,0
from IdealNoDrop
where cityname not in (select cityname from SmartDrop );



create table ErrorsSmartDrop as
select IdealNoDrop.cityName, IdealNoDrop.samplesNumber as OriginalCount, 
SmartDrop.SamplesNumber as NewCount, (IdealNoDrop.samplesNumber-SmartDrop.SamplesNumber ) as DroppedPackets, 
abs((((SmartDrop.SamplesNumber -IdealNoDrop.samplesNumber) /IdealNoDrop.samplesNumber) * 100)) as DropPercntage,
(abs(IdealNoDrop.Battemp -SmartDrop.Battemp) )as TempError

from SmartDrop INNER JOIN IdealNoDrop
ON SmartDrop.cityName= IdealNoDrop.cityName
order by TempError DESC;





insert into SmartDropNoAppContext select cityname,0,0
from IdealNoDrop
where cityname not in (select cityname from SmartDropNoAppContext );



create table ErrorsSmartDropNoAppContext as
select IdealNoDrop.cityName, IdealNoDrop.samplesNumber as OriginalCount, 
SmartDropNoAppContext.SamplesNumber as NewCount, (IdealNoDrop.samplesNumber-SmartDropNoAppContext.SamplesNumber ) as DroppedPackets, 
abs((((SmartDropNoAppContext.SamplesNumber -IdealNoDrop.samplesNumber) /IdealNoDrop.samplesNumber) * 100)) as DropPercntage,
(abs(IdealNoDrop.Battemp -SmartDropNoAppContext.Battemp) )as TempError

from SmartDropNoAppContext INNER JOIN IdealNoDrop
ON SmartDropNoAppContext.cityName= IdealNoDrop.cityName
order by TempError DESC;




insert into SmartDropNoNetContext select cityname,0,0
from IdealNoDrop
where cityname not in (select cityname from SmartDropNoNetContext );



create table ErrorsSmartDropNoNetContext as
select IdealNoDrop.cityName, IdealNoDrop.samplesNumber as OriginalCount, 
SmartDropNoNetContext.SamplesNumber as NewCount, (IdealNoDrop.samplesNumber-SmartDropNoNetContext.SamplesNumber ) as DroppedPackets, 
abs((((SmartDropNoNetContext.SamplesNumber -IdealNoDrop.samplesNumber) /IdealNoDrop.samplesNumber) * 100)) as DropPercntage,
(abs(IdealNoDrop.Battemp -SmartDropNoNetContext.Battemp) )as TempError

from SmartDropNoNetContext INNER JOIN IdealNoDrop
ON SmartDropNoNetContext.cityName= IdealNoDrop.cityName
order by TempError DESC;






