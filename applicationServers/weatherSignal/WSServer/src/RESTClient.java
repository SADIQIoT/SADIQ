import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.wink.json4j.OrderedJSONObject;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;

public class RESTClient {

	private static final String QOSURI = "http://10.85.38.28:8080/wm/IoT/json/QoS";
	private static final String QUEUESTATSURI = "http://10.85.38.28:8080/wm/IoT/json/QueueStats";

	Logger logger;

	public RESTClient() {
		logger = Logger.getLogger("MyLog");
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

	public JSONObject buildQoSJsonObj(Vector<String> elephants) {
		try {
			OrderedJSONObject mainobj = new OrderedJSONObject();
			JSONArray high = new JSONArray();

			JSONArray low = new JSONArray();

			for (String entry : elephants) {
				// JSONObject innerobj = new JSONObject();
				OrderedJSONObject innerobj = new OrderedJSONObject();
				innerobj.put("1region", entry);
				innerobj.put("2granularity-Level", 2);

				low.add(innerobj);
			}
			// mainobj.put("IP", GetIPv4ofEth0());
			mainobj.put("IP", "10.0.0.3");
			mainobj.put("Low Priority", low);
			mainobj.put("High Priority", high);
			//System.out.println(mainobj);

			return mainobj;

		} catch (Exception e) {
			logger.info(e.toString());
			return null;
		}
	}

	public boolean sendQoStoController(JSONObject obj) {

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

	public String GetHighQueueDropRate() {
		String droprate = "";
		try {
			System.out.println("Sending a request of high queue drop rate");
			logger.info("Inside GetHighQueueDropRate");
			URL url = new URL(QUEUESTATSURI);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			// connection.setRequestProperty("Accept", "application/json");
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			connection.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			// droprate=in.readLine();
			String s = "0";
			if ((s = in.readLine()) != null) {
				droprate = s;
			}
			System.out.println("\n REST Service Invoked Successfully.. DropRate= "+droprate);
			in.close();
		} catch (Exception e) {
			System.out.println("\nError while calling REST Service");
			logger.info(e.toString());
			return "0";
		}

		return droprate;
	}

	private String GetIPv4ofEth0() {
		try {
			String interfaceName = "eth0";
			String ip = "";
			NetworkInterface networkInterface;

			networkInterface = NetworkInterface.getByName(interfaceName);

			Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
			InetAddress currentAddress;
			currentAddress = inetAddress.nextElement();
			while (inetAddress.hasMoreElements()) {
				currentAddress = inetAddress.nextElement();
				if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
					ip = currentAddress.toString();
					break;
				}
			}
			ip = ip.substring(1);
			return ip;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "NULL";
		}

	
}
	
}