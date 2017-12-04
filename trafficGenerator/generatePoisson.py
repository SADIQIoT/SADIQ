import socket
import time
import random
import os
import datetime
import subprocess
import sys

rateMb=float(sys.argv[1])
filenum=int(sys.argv[2])
orgrate=rateMb
#rate = 10000000.0 #in bits/per-second
rateMb=rateMb+(rateMb*0.45)
rate=rateMb*1000000 #in bits/per-second
size= 1499.0 * 8.0 #in bits
packetPerSec=rate/size

f=0
while f<filenum:
	ofilename=str(f)+"poisson"+str(orgrate)+"Mbps.txt"
	ofile  = open(ofilename, "a")

	i=0
	while i<1000000:
	        writer = ofile.write(str(random.expovariate(packetPerSec))+'\n')
		i=i+1
	f=f+1
