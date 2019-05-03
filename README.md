# SADIQ 
(This repo is for the artifact evaluation of the paper: Location-Aware, Context-Driven QoS for IoT Applications. https://ieeexplore.ieee.org/document/8640087. You will be able to install SADIQ and reproduce all the results that appear in the paper.)

SADIQ is an SDN-based system for providing IoT applications with location aware and context-driven QoS. SADIQ is built on top of the floodlight controller and can be run on switches that support OpenFlow 1.3. We include in this repo the SADIQ controller modules, real data traces from two IoT application: weather signal www.weathersignal.com/ and Melbourne smart parking https://data.melbourne.vic.gov.au/, end server simulation of these two applications, and some useful scripts to regenerate the sensor traces and to evaluate the results. 

![Alt text](SADIQ.png?raw=true "SADIQ Architecture")

# Getting Started
To run SADIQ you first need to create a test bed that should include:
1. Host to run the controller
2. Host to run the end application server simulator
3. Host(s) to run traffic generators
4. Openflow 1.3 enabled switches.

For reference, our test bed consisted of three 1Gbps HP Aruba 2930F switches connected in a tree topology, as shown in the figure below, a single controller that runs on a Dell workstation (8core, 2.67GHz Intel Xeon processor), an application server running on a Lenovo  workstation  (24  core,  2.5GHz  IntelXeon processor), and 20 traffic generators running on 4 Shuttle XPCs (dual core, 2.93GHz Intel CoreTM).

![Alt text](TopoLarge.png?raw=true "Topology")

Some important configurations for the switches:
1.	The switches should support priority queuing (at least 2 priority queues) and should have the DSCP mapping to these queues enabled. 
2. The switches should support Experimenter flow match fields oxm_class=OFPXMC_EXPERIMENTER of Openflow 1.3.
 

# Running SADIQ
Each component in the repo contains instructions for installation and running that component, however, we will provide here an overall summery of the SADIQ workflow:

1.	After creating the topology and installing each component on the selected host (controller, app server, traffic generators) begin by running the controller. The first step the controller will do is to send OFP_TABLE_MOD request to all connected switches to define the custom table pipeline that contains the new experimenter IoT address match field. 
2.	Once all switches are successfully connected to the controller and have successfully created the table pipeline, you can start the app server (either the weather signal or the smart parking application based on which experiment you are running). The app server will start sending REST calls to the controller, however, with no policies set since it still hasn’t received any data.
3.	Run the traffic generators, which will start to send data to the application server over UDP at the selected rate. The server will preform some processing and generate new policies periodically and send it to the controller over the RESTFull API.
4.	The controller receives the new policies from the application server and the statistics collected from the switches and generates new Openflow rules for each switch.
5.	Once all data is sent, the traffic generators will send an end of transmission packet which when received by the server will make it end the experiment and write the output files. Use the sql-scripts to generate the results of that experiment.

# Comparison

In our work we compare the results of the following three methods:
1.	No QoS
2.	Static QoS
3.	And SADIQ (Context-driven, Location-aware QoS)

To configure the No QoS experiment you just simply need to configure one queue on your switches and disable priority queuing (all packets will end up in a single FIFO queue with drop tail ).

To configure the static QoS refer to the instructions described in the application server.
