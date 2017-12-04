# SADIQ

SADIQ is an SDN-based system for providing IoT applications with location aware and context-driven QoS. SADIQ is built on top of the floodlight controller and can be run on switches that support OpenFlow 1.3. We include in this repo the SADIQ contoller modules, real data traces from two IoT application: weather signal www.weathersignal.com/ and melbourne smart parking https://data.melbourne.vic.gov.au/, end server simulation of these two applications, and some useful scripts to regenrate the sensor traces and to evalaute the results. 

![Alt text](SADIQ.png?raw=true "SADIQ Architecture")

# Getting Started
To run SADIQ you first need to create a test bed that should have:
1. a Server to run the contoroller
2. Host to run the end application server simulator
3. Host(s) to run traffic genratores
4. and, Openflow 1.3 switches.

For reference our  testbed  consists  of  three  1Gbps  HP  Aruba  2930Fswitches connected in a tree topology, as shown in the figure below, a single controller that runs on a Dell workstation (8core, 2.67GHz Intel Xeon processor), an application server running  on  a  Lenovo  workstation  (24  core,  2.5GHz  IntelXeon processor), and 20 traffic generators running on 4 Shuttle XPCs (dual core, 2.93GHz Intel CoreTM).

![Alt text](TopoLarge.png?raw=true "Topology")

The switches should have at least 2 prioirty queues and enable DSCP mapping to the queues. 

Follow the instructions of each componant for intalling and running SADIQ.