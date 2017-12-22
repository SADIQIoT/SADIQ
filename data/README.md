
This directory contains the data traces from the weather signal and Melbourne Smart parking. The following is a description of these files:

melbourneSmartParking→traceAnalysisData→ newyear24hours.csv: is the data set used to generate the heat map (figure 1 in the paper). The data set contains the parking actions on new year for each location. The format is:

Hour; LocationName; Number of Parking Actions; Ratio of Parking Actions.


weatherSignal→traceAnalysisData→ SpatialCount6months:  is the data set used to generate the six months CDF (figure 2 in the paper). The data set contains the number of weather signal samples of each city. The format is:

StateName; CityName; Number of Samples over Six Months

melbourneSmartParking→ experimentData: contains the data sets of sensor traces that should be used by the generator. The format is:

MGRS Address; TypeOfParkingAction ; ParkingSpaceID; TimeOfParkingAction ;Longitude; Latitude; AraeaName; StreetName

weatherSignal→ experimentData: contains the data sets of sensor traces that should be used by the generator. The format is:

MGRS Address; TemperatureValue; TimeOfSample; ;Longitude; Latitude
