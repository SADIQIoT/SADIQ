select avg(ImprovmentAggregation.Improvment) from ImprovmentAggregation where Improvment<>0 ;

select avg(ImprovmentSmartDrop.Improvment) from ImprovmentSmartDrop where Improvment<>0 ;

select avg(ImprovmentAggregationAndSmartDrop.Improvment) from ImprovmentAggregationAndSmartDrop where Improvment<>0 ;


select sum(IdealNoDrop.SamplesNumber) from IdealNoDrop;