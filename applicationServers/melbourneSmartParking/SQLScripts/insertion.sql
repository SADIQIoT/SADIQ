
CREATE TABLE IF NOT EXISTS 'IdealNoDrop' (
'DeviceID'	TEXT NOT NULL,
'Location100m'	TEXT,
'Areaname'      TEXT,
'SamplesNumber'	REAL,
PRIMARY KEY(DeviceID));

.separator ","

.import ../IdealNoDrop.csv IdealNoDrop




CREATE TABLE IF NOT EXISTS 'TailDrop' (
'DeviceID'	TEXT NOT NULL,
'Location100m'	TEXT,
'Areaname'      TEXT,
'SamplesNumber'	REAL,
PRIMARY KEY(DeviceID));

.separator ","

.import ../TailDrop.csv TailDrop



CREATE TABLE IF NOT EXISTS 'SmartDrop' (
'DeviceID'	TEXT NOT NULL,
'Location100m'	TEXT,
'Areaname'      TEXT,
'SamplesNumber'	REAL,
PRIMARY KEY(DeviceID));

.separator ","

.import ../SmartDrop.csv SmartDrop


CREATE TABLE IF NOT EXISTS 'DeviceGPS' (
'DeviceID'	TEXT NOT NULL,
'lat'	TEXT,
'long'      TEXT,
PRIMARY KEY(DeviceID));

.separator ","

.import ../DeviceGPS.csv DeviceGPS


CREATE TABLE IF NOT EXISTS 'IdealPackets' (
'NewTime'       TEXT,
'FullLocation'	TEXT,
'Location100m'	TEXT,
'Location1km'	TEXT,
'Status'	TEXT,
'DeviceID'	TEXT NOT NULL,
'originalTime'  TEXT,
'Areaname'      TEXT,
'Occ100m'	REAL,
'Occ1km' 	REAL,
'OccArea' 	REAL);

.separator ","

.import ../Idealpackets.csv IdealPackets


CREATE TABLE IF NOT EXISTS 'TailPackets' (
'NewTime'       TEXT,
'FullLocation'	TEXT,
'Location100m'	TEXT,
'Location1km'	TEXT,
'Status'	TEXT,
'DeviceID'	TEXT NOT NULL,
'originalTime'  TEXT,
'Areaname'      TEXT,
'Occ100m'	REAL,
'Occ1km' 	REAL,
'OccArea' 	REAL);


.separator ","

.import ../Tailpackets.csv TailPackets


CREATE TABLE IF NOT EXISTS 'SmartPackets' (
'NewTime'       TEXT,
'FullLocation'	TEXT,
'Location100m'	TEXT,
'Location1km'	TEXT,
'Status'	TEXT,
'DeviceID'	TEXT NOT NULL,
'originalTime'  TEXT,
'Areaname'      TEXT,
'Occ100m'	REAL,
'Occ1km' 	REAL,
'OccArea' 	REAL);

.separator ","

.import ../Smartpackets.csv SmartPackets

/*Tail Drop Tables*/
insert into TailDrop select DeviceID,Location100m,Areaname,0
from IdealNoDrop
where DeviceID not in (select DeviceID from TailDrop );

create table ErrorsTailDrop as
select IdealNoDrop.DeviceID, IdealNoDrop.Areaname, IdealNoDrop.SamplesNumber as OriginalCount, 
TailDrop.SamplesNumber as NewCount, (IdealNoDrop.SamplesNumber-TailDrop.SamplesNumber ) as DroppedPackets, 
abs((((TailDrop.SamplesNumber-IdealNoDrop.samplesNumber ) /IdealNoDrop.samplesNumber) * 100)) as DropPercntage

from TailDrop INNER JOIN IdealNoDrop
ON TailDrop.DeviceID= IdealNoDrop.DeviceID
order by DroppedPackets DESC;

create table ErrorsTailDropByArea as
select areaname, sum(Originalcount) as orgcount, sum(newcount)as newcount,sum(DroppedPackets) as dropped from errorstaildrop group by areaname order by dropped desc;


Create table GPSMissingPacketsTail as
select ErrorsTailDrop.DeviceID,DeviceGPS.lat, DeviceGPS.long, ErrorsTailDrop.DroppedPackets
from ErrorsTailDrop INNER JOIN DeviceGPS
on ErrorsTailDrop.DeviceID==DeviceGPS.DeviceID
where ErrorsTailDrop.DroppedPackets <>0 ;


Create table TailLostPackets as
select A.newtime,A.Fulllocation,A.Location100m ,A.Location1km,  A.areaname, A.DeviceID,A.originaltime, A.status , A.Occ100m,A.occ1km,A.occArea  from idealpackets as A
left join tailpackets as B on (A.DeviceID = B.DeviceID and A.originaltime=B.originalTime and A.status=B.status)
where B.DeviceID is null;



/*Smart Drop Tables*/

insert into SmartDrop select DeviceID,Location100m,Areaname,0
from IdealNoDrop
where DeviceID not in (select DeviceID from SmartDrop );



create table ErrorsSmartDrop as
select IdealNoDrop.DeviceID, IdealNoDrop.Areaname, IdealNoDrop.SamplesNumber as OriginalCount, 
SmartDrop.SamplesNumber as NewCount, (IdealNoDrop.SamplesNumber-SmartDrop.SamplesNumber ) as DroppedPackets, 
abs((((SmartDrop.SamplesNumber-IdealNoDrop.samplesNumber ) /IdealNoDrop.samplesNumber) * 100)) as DropPercntage

from SmartDrop INNER JOIN IdealNoDrop
ON SmartDrop.DeviceID= IdealNoDrop.DeviceID
order by DroppedPackets DESC;

create table ErrorsSmartDropByArea as
select areaname, sum(Originalcount) as orgcount, sum(newcount)as newcount,sum(DroppedPackets) as dropped from errorsSmartDrop group by areaname order by dropped desc;



Create table GPSMissingPacketsSmart as
select ErrorsSmartDrop.DeviceID,DeviceGPS.lat, DeviceGPS.long, ErrorsSmartDrop.DroppedPackets
from ErrorsSmartDrop INNER JOIN DeviceGPS
on ErrorsSmartDrop.DeviceID==DeviceGPS.DeviceID
where ErrorsSmartDrop.DroppedPackets <>0 ;


Create table SmartLostPackets as
select A.newtime,A.Fulllocation,A.Location100m ,A.Location1km,  A.areaname, A.DeviceID,A.originaltime, A.status , A.Occ100m,A.occ1km,A.occArea  from idealpackets as A
left join smartpackets as B on (A.DeviceID = B.DeviceID and A.originaltime=B.originalTime and A.status=B.status)
where B.DeviceID is null;

