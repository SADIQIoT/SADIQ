CREATE TABLE IF NOT EXISTS `IdealNoDrop` (
`CityName`	TEXT NOT NULL,
`Battemp`	REAL,
`SamplesNumber`	REAL,
PRIMARY KEY(CityName));

.separator ","

.import ../output/IdealNoDrop.csv IdealNoDrop


CREATE TABLE IF NOT EXISTS `TailDrop` (
`CityName`	TEXT NOT NULL,
`Battemp`	REAL,
`SamplesNumber`	REAL,
PRIMARY KEY(CityName));

.separator ","

.import ../output/TailDrop.csv TailDrop


CREATE TABLE IF NOT EXISTS `SmartDrop` (
`CityName`	TEXT NOT NULL,
`Battemp`	REAL,
`SamplesNumber`	REAL,
PRIMARY KEY(CityName));

.separator ","

.import ../output/SmartDrop.csv SmartDrop





CREATE TABLE IF NOT EXISTS `SmartDropNoAppContext` (
`CityName`	TEXT NOT NULL,
`Battemp`	REAL,
`SamplesNumber`	REAL,
PRIMARY KEY(CityName));

.separator ","

.import ../output/SmartDropNoAppContext.csv SmartDropNoAppContext


CREATE TABLE IF NOT EXISTS `SmartDropNoNetContext` (
`CityName`	TEXT NOT NULL,
`Battemp`	REAL,
`SamplesNumber`	REAL,
PRIMARY KEY(CityName));

.separator ","

.import ../output/SmartDropNoNetContext.csv SmartDropNoNetContext




