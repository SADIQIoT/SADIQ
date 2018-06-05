import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat.Field;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.wink.json4j.OrderedJSONObject;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RESTClient 
{

	
private static final String QOSURI="http://10.85.38.28:8080/wm/IoT/json/QoS";
private static final String QUEUESTATSURI="http://10.85.38.28:8080/wm/IoT/json/QueueStats";

Logger logger;
public RESTClient()
{
	 logger=Logger.getLogger("MyLog"); 
	 FileHandler fh;  
	 try {  

	        // This block configure the logger with handler and formatter  
	        fh = new FileHandler("MyLogFile.log");  
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  
	  } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
}



public OrderedJSONObject buildQoSJsonObj (Vector<String> eventRegions_100m,Vector<String> eventRegions_1km)
{ try{
	org.apache.wink.json4j.OrderedJSONObject mainobj = new org.apache.wink.json4j.OrderedJSONObject();
	org.apache.wink.json4j.JSONArray high = new org.apache.wink.json4j.JSONArray ();
	org.apache.wink.json4j.JSONArray  mid = new org.apache.wink.json4j.JSONArray ();
	
	org.apache.wink.json4j.JSONArray  low = new org.apache.wink.json4j.JSONArray ();
	
	for (String entry : eventRegions_100m)
	{
		org.apache.wink.json4j.OrderedJSONObject innerobj = new OrderedJSONObject();
		innerobj.put("1region", entry);
		innerobj.put("2granularity-Level", 3);
		
		
		
		high.add(innerobj);
	}
	
	for (String entry : eventRegions_1km)
	{
		//JSONObject innerobj = new JSONObject();
		org.apache.wink.json4j.OrderedJSONObject innerobj_1 = new OrderedJSONObject();
		innerobj_1.put("1region", entry);
		innerobj_1.put("2granularity-Level", 3);
		
		
		
		mid.add(innerobj_1);
	}
	mainobj.put("Low Priority", low);
	mainobj.put("High Priority", high);
	mainobj.put("Mid Priority", mid);
	System.out.println(mainobj.toString());
	
	
	return mainobj;

} catch (Exception e) {
	logger.info(e.toString());
	return null;
}
}


public boolean sendQoStoController(OrderedJSONObject obj)
{

	try {

	

		// Step2: Now pass JSON File Data to REST Service
		try {
			URL url = new URL(QOSURI);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Accept", "application/json");
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			connection.setRequestMethod("POST");
			logger.info("Inside sendQoStoController");
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(obj.toString());
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			while (in.readLine() != null) {
			}
			System.out.println("\n REST Service Invoked Successfully..");
			logger.info("\n REST Service Invoked Successfully..");
			in.close();
			
			
			
		} catch (Exception e) {
			System.out.println("\nError while calling REST Service");
			logger.info("\nError while calling REST Service");
			System.out.println(e.toString());
			logger.info(e.toString());
			return false;
		}

		
	} catch (Exception e) {
		logger.info(e.toString());
		return false;
	}

	return true;
}

public String GetHighQueueDropRate ()
{ String droprate="";
	try {
		System.out.println("Sending a request of high queue drop rate");
		logger.info("Inside GetHighQueueDropRate");
		URL url = new URL(QUEUESTATSURI);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setConnectTimeout(2000);
		connection.setReadTimeout(2000);
		connection.setRequestMethod("GET");


		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String s="0";
		if ((s=in.readLine()) != null) {
			droprate=s;
	    }
		System.out.println("\n REST Service Invoked Successfully..");
		in.close();
	} catch (Exception e) {
		System.out.println("\nError while calling REST Service");
		logger.info(e.toString());
		return "0";
	}

	return droprate;
}

}
