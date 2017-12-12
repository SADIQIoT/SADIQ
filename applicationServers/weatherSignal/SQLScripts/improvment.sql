

create table ImprovmentSmartDrop as
select ErrorsSmartDrop.CityName, (ErrorsTailDrop.TempError-ErrorsSmartDrop.TempError) as Improvment 
from ErrorsSmartDrop INNER JOIN ErrorsTailDrop
ON ErrorsSmartDrop.cityName= ErrorsTailDrop.cityName
order by Improvment;


create table ImprovmentSmartDropNoAppContext as
select ErrorsSmartDropNoAppContext.CityName, (ErrorsTailDrop.TempError-ErrorsSmartDropNoAppContext.TempError) as Improvment 
from ErrorsSmartDropNoAppContext INNER JOIN ErrorsTailDrop
ON ErrorsSmartDropNoAppContext.cityName= ErrorsTailDrop.cityName
order by Improvment;

create table ImprovmentSmartDropNoNetContext as
select ErrorsSmartDropNoNetContext.CityName, (ErrorsTailDrop.TempError-ErrorsSmartDropNoNetContext.TempError) as Improvment 
from ErrorsSmartDropNoNetContext INNER JOIN ErrorsTailDrop
ON ErrorsSmartDropNoNetContext.cityName= ErrorsTailDrop.cityName
order by Improvment;