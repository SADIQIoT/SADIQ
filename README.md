# SADIQ

SADIQ is an SDN-based system for providing IoT applications with location aware and context-driven QoS. SADIQ is built on top of the floodlight controller and can be run on switches that support OpenFlow 1.3. We include in this repo the SADIQ controller modules, real data traces from two IoT application: weather signal www.weathersignal.com/ and Melbourne smart parking https://data.melbourne.vic.gov.au/, end server simulation of these two applications, and some useful scripts to regenerate the sensor traces and to evaluate the results. 

![Alt text](SADIQ.png?raw=true "SADIQ Architecture")

# Getting Started
To run SADIQ you first need to create a test bed that should include:
1. a Server to run the controller
2. Host to run the end application server simulator
3. Host(s) to run traffic generators
4. and, Openflow 1.3 switches.

For reference, our test bed  consists of three 1Gbps HP Aruba 2930Fswitches connected in a tree topology, as shown in the figure below, a single controller that runs on a Dell workstation (8core, 2.67GHz Intel Xeon processor), an application server running  on  a  Lenovo  workstation  (24  core,  2.5GHz  IntelXeon processor), and 20 traffic generators running on 4 Shuttle XPCs (dual core, 2.93GHz Intel CoreTM).

![Alt text](TopoLarge.png?raw=true "Topology")

The switches should have at least 2 priority queues and enable the DSCP mapping to these queues. 

Follow the instructions of each component for installing and running SADIQ.
