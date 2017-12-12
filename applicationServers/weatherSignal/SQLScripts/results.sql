
CREATE TABLE IF NOT EXISTS ResultsTailDrop (
NumberOfDroppedPackets Real,
RatioOfDroppedPackets Real,	
CountErrorCities	Real,
CountErrorCitiesOver01	Real,
CountErrorCitiesOver1	Real,
PercantageErrorCities	REAL,
LostCitites	REAL,
MaxError	REAL,
AvgError Real,
TotalAvgError Real);


Insert into ResultsTailDrop values (
	(select sum (DroppedPackets) from ErrorsTailDrop),
	(select(select sum (DroppedPackets) from ErrorsTailDrop)/ sum(OriginalCount) from ErrorsTailDrop),

(select count(cityname) from ErrorsTailDrop where TempError >0)  ,

(select count(cityname) from ErrorsTailDrop where TempError >=0.1)  ,
(select count(cityname) from ErrorsTailDrop where TempError >=1)  ,
(select (select count(cityname)* 100.0 from ErrorsTailDrop where TempError >0) / count(cityname) from ErrorsTailDrop),
(select count(cityname)  from ErrorsTailDrop where DroppedPackets==OriginalCount),
(select max(TempError)  from ErrorsTailDrop ),
(select Avg(TempError) from ErrorsTailDrop where TempError >0 ),
(select Avg(TempError) from ErrorsTailDrop)) ;



CREATE TABLE IF NOT EXISTS ResultsSmartDrop (
NumberOfDroppedPackets Real,
RatioOfDroppedPackets Real,	
CountErrorCities	Real,
CountErrorCitiesOver01	Real,
CountErrorCitiesOver1	Real,
PercantageErrorCities	REAL,
LostCitites	REAL,
MaxError	REAL,
AvgError Real,
TotalAvgError Real,
AvgErrorImprovment Real,
TotalErrorImprovment Real,
AvgImprovmentNonZer Real);


Insert into ResultsSmartDrop values (
(select sum (DroppedPackets) from ErrorsSmartDrop),
	(select(select sum (DroppedPackets) from ErrorsSmartDrop)/ sum(OriginalCount) from ErrorsSmartDrop),

(select count(cityname) from ErrorsSmartDrop where TempError >0)  ,

(select count(cityname) from ErrorsSmartDrop where TempError >=0.1)  ,
(select count(cityname) from ErrorsSmartDrop where TempError >=1)  ,
(select (select count(cityname)* 100.0 from ErrorsSmartDrop where TempError >0) / count(cityname) from ErrorsSmartDrop),
(select count(cityname)  from ErrorsSmartDrop where DroppedPackets==OriginalCount),
(select max(TempError)  from ErrorsSmartDrop ),
(select Avg(TempError) from ErrorsSmartDrop where TempError >0 ),
(select Avg(TempError) from ErrorsSmartDrop),
(select (((Avg(ErrorsSmartDrop.TempError)-ResultsTailDrop.AvgError) /ResultsTailDrop.AvgError)*100) from ErrorsSmartDrop,ResultsTailDrop where ErrorsSmartDrop.TempError >0 ),
(select (((Avg(ErrorsSmartDrop.TempError)-ResultsTailDrop.TotalAvgError) /ResultsTailDrop.TotalAvgError)*100) from ErrorsSmartDrop,ResultsTailDrop ),
(select avg(ImprovmentSmartDrop.Improvment) from ImprovmentSmartDrop where Improvment<>0)) ;






CREATE TABLE IF NOT EXISTS ResultsSmartDropNoAppContext (
NumberOfDroppedPackets Real,
RatioOfDroppedPackets Real,	
CountErrorCities	Real,
CountErrorCitiesOver01	Real,
CountErrorCitiesOver1	Real,
PercantageErrorCities	REAL,
LostCitites	REAL,
MaxError	REAL,
AvgError Real,
TotalAvgError Real,
AvgErrorImprovment Real,
TotalErrorImprovment Real,
AvgImprovmentNonZer Real);


Insert into ResultsSmartDropNoAppContext values (
(select sum (DroppedPackets) from ErrorsSmartDropNoAppContext),
	(select(select sum (DroppedPackets) from ErrorsSmartDropNoAppContext)/ sum(OriginalCount) from ErrorsSmartDropNoAppContext),

(select count(cityname) from ErrorsSmartDropNoAppContext where TempError >0)  ,

(select count(cityname) from ErrorsSmartDropNoAppContext where TempError >=0.1)  ,
(select count(cityname) from ErrorsSmartDropNoAppContext where TempError >=1)  ,
(select (select count(cityname)* 100.0 from ErrorsSmartDropNoAppContext where TempError >0) / count(cityname) from ErrorsSmartDropNoAppContext),
(select count(cityname)  from ErrorsSmartDropNoAppContext where DroppedPackets==OriginalCount),
(select max(TempError)  from ErrorsSmartDropNoAppContext ),
(select Avg(TempError) from ErrorsSmartDropNoAppContext where TempError >0 ),
(select Avg(TempError) from ErrorsSmartDropNoAppContext),
(select (((Avg(ErrorsSmartDropNoAppContext.TempError)-ResultsTailDrop.AvgError) /ResultsTailDrop.AvgError)*100) from ErrorsSmartDropNoAppContext,ResultsTailDrop where ErrorsSmartDropNoAppContext.TempError >0 ),
(select (((Avg(ErrorsSmartDropNoAppContext.TempError)-ResultsTailDrop.TotalAvgError) /ResultsTailDrop.TotalAvgError)*100) from ErrorsSmartDropNoAppContext,ResultsTailDrop ),
(select avg(ImprovmentSmartDropNoAppContext.Improvment) from ImprovmentSmartDropNoAppContext where Improvment<>0)) ;




CREATE TABLE IF NOT EXISTS ResultsSmartDropNoNetContext (
NumberOfDroppedPackets Real,
RatioOfDroppedPackets Real,	
CountErrorCities	Real,
CountErrorCitiesOver01	Real,
CountErrorCitiesOver1	Real,
PercantageErrorCities	REAL,
LostCitites	REAL,
MaxError	REAL,
AvgError Real,
TotalAvgError Real,
AvgErrorImprovment Real,
TotalErrorImprovment Real,
AvgImprovmentNonZer Real);


Insert into ResultsSmartDropNoNetContext values (
(select sum (DroppedPackets) from ErrorsSmartDropNoNetContext),
	(select(select sum (DroppedPackets) from ErrorsSmartDropNoNetContext)/ sum(OriginalCount) from ErrorsSmartDropNoNetContext),

(select count(cityname) from ErrorsSmartDropNoNetContext where TempError >0)  ,

(select count(cityname) from ErrorsSmartDropNoNetContext where TempError >=0.1)  ,
(select count(cityname) from ErrorsSmartDropNoNetContext where TempError >=1)  ,
(select (select count(cityname)* 100.0 from ErrorsSmartDropNoNetContext where TempError >0) / count(cityname) from ErrorsSmartDropNoNetContext),
(select count(cityname)  from ErrorsSmartDropNoNetContext where DroppedPackets==OriginalCount),
(select max(TempError)  from ErrorsSmartDropNoNetContext ),
(select Avg(TempError) from ErrorsSmartDropNoNetContext where TempError >0 ),
(select Avg(TempError) from ErrorsSmartDropNoNetContext),
(select (((Avg(ErrorsSmartDropNoNetContext.TempError)-ResultsTailDrop.AvgError) /ResultsTailDrop.AvgError)*100) from ErrorsSmartDropNoNetContext,ResultsTailDrop where ErrorsSmartDropNoNetContext.TempError >0 ),
(select (((Avg(ErrorsSmartDropNoNetContext.TempError)-ResultsTailDrop.TotalAvgError) /ResultsTailDrop.TotalAvgError)*100) from ErrorsSmartDropNoNetContext,ResultsTailDrop ),
(select avg(ImprovmentSmartDropNoNetContext.Improvment) from ImprovmentSmartDropNoNetContext where Improvment<>0)) ;


CREATE TABLE IF NOT EXISTS ResultsAll (
Method TEXT,
NumberOfDroppedPackets Real,
RatioOfDroppedPackets Real,	
CountErrorCities	Real,
CountErrorCitiesOver01	Real,
CountErrorCitiesOver1	Real,
PercantageErrorCities	REAL,
LostCitites	REAL,
MaxError	REAL,
AvgError Real,
TotalAvgError Real);

Insert into ResultsAll (Method,NumberOfDroppedPackets,RatioOfDroppedPackets,CountErrorCities,CountErrorCitiesOver01,CountErrorCitiesOver1,PercantageErrorCities,LostCitites,MaxError,AvgError,TotalAvgError) 
 select 'TailDrop',NumberOfDroppedPackets,RatioOfDroppedPackets,CountErrorCities,CountErrorCitiesOver01,CountErrorCitiesOver1,PercantageErrorCities,LostCitites,MaxError,AvgError,TotalAvgError  from ResultsTailDrop;

Insert into ResultsAll (Method,NumberOfDroppedPackets,RatioOfDroppedPackets,CountErrorCities,CountErrorCitiesOver01,CountErrorCitiesOver1,PercantageErrorCities,LostCitites,MaxError,AvgError,TotalAvgError) 
 select 'SmartDropNoAppContext',NumberOfDroppedPackets,RatioOfDroppedPackets,CountErrorCities,CountErrorCitiesOver01,CountErrorCitiesOver1,PercantageErrorCities,LostCitites,MaxError,AvgError,TotalAvgError  from ResultsSmartDropNoAppContext;

Insert into ResultsAll (Method,NumberOfDroppedPackets,RatioOfDroppedPackets,CountErrorCities,CountErrorCitiesOver01,CountErrorCitiesOver1,PercantageErrorCities,LostCitites,MaxError,AvgError,TotalAvgError) 
 select 'SmartDropNoNetContext',NumberOfDroppedPackets,RatioOfDroppedPackets,CountErrorCities,CountErrorCitiesOver01,CountErrorCitiesOver1,PercantageErrorCities,LostCitites,MaxError,AvgError,TotalAvgError  from ResultsSmartDropNoNetContext;

Insert into ResultsAll (Method,NumberOfDroppedPackets,RatioOfDroppedPackets,CountErrorCities,CountErrorCitiesOver01,CountErrorCitiesOver1,PercantageErrorCities,LostCitites,MaxError,AvgError,TotalAvgError) 
 select 'SADIQ',NumberOfDroppedPackets,RatioOfDroppedPackets,CountErrorCities,CountErrorCitiesOver01,CountErrorCitiesOver1,PercantageErrorCities,LostCitites,MaxError,AvgError,TotalAvgError  from ResultsSmartDrop;



