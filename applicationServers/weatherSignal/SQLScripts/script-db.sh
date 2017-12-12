#!/bin/sh

sqlite3 ../output/WS.s3db < insertion.sql
sqlite3 ../output/WS.s3db < deleteControlregion.sql
sqlite3 ../output/WS.s3db < error.sql
sqlite3 ../output/WS.s3db < improvment.sql
sqlite3 ../output/WS.s3db < results.sql


