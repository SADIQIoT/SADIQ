# Traffic Generators
The IoT traffic generator is a simple C program which takes 3 inputs:
1. The data traces file
2. The Poisson distribution file 
3. and, the number of packets you want to send

The traffic generator will create UDP packets out of each line of the data traces and send them out according to the rate set by the Poisson file.

To create a Poisson file with a specific rate you need to simply run the generatePoisson.py with rate you want in mbps and the number of different files you need of that rate. The script will generate a file of Poisson numbers that will be used by the IoT Generator to set the rate of packet generation.

Make sure to change the SERVER IP address to your application server simulator IP address.

Here is an example for the command to run the traffic generator:

sudo  ./IoTGenerator datafile.csv poissonfile.txt 10000

It is recommended to run multiple instances of the traffic generator on each host.

Before running the generator you need to set the DSCP value for out going traffic on your OS to the DSCP mapping to the highest priority queue on your switches (i.e. by default all packets should go into the high priority queue unless the openflow rules decrease the DSCP).

The following example sets the DSCP value of output UDP traffic on a linux OS to 1:

iptables -t mangle -A FORWARD -p udp --dport 9999 -j DSCP --set-dscp 1


