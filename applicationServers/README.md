# Application Servers
Two java applications are included that simulate the end server applications of the Weather Signal and the Melbourne Smart Parking. 
The server application receives the IoT UDP packets on the port: 9999 and performs some application specific calculations: in the weather signal it calculates the avg temperature for each region, and for the smart parking it calculates the occupancy ratio of each region. The application periodically sends the dynamic QoS policies to the controller using a RESTful API based on the calculation output. 
When the stream is over, the application will write output files with the results, which can be better processed and understood using the SQL scripts. 

# Running the application Simulators 
Depending on which experiment you are running (the Weather Signal or the Smart Parking) select a host in your test bed and run a single instance of the java application. Make sure to set the controller IP address correctly to the controller server (which will be used to send the REST calls). Once the experiment is over, run the corresponding SQLite3 scripts of each application to see the results (script-db.sh).

You can compare the results of SADIQ with static QoS by disabling the periodic update by setting the flag stopUpdate = true.

