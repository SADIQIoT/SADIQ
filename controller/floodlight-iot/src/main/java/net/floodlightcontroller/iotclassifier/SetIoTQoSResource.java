package net.floodlightcontroller.iotclassifier;

import java.io.IOException;
import java.util.Vector;

import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

public class SetIoTQoSResource extends ServerResource {
	protected static Logger log = LoggerFactory.getLogger(SetIoTQoSResource.class);

	public static final String HIGHPRIORITY = "High Priority";
	public static final String MIDPRIORITY = "Mid Priority";
	public static final String LOWPRIORITY = "Low Priority";

	public static final String REGION = "1region";
	public static final String GRANULEVEL = "2granularity-Level";
	public static final String IP = "IP";

	@Post
	public String addQoS(String qosJson) {
		try {

			IIoTQoSService IoTSer = (IIoTQoSService) getContext().getAttributes()
					.get(IIoTQoSService.class.getCanonicalName());

			if (qosJson == null) {
				return "{\"status\" : \"Error! No data posted.\"}";
			}
			//System.out.println("Got an add QoS Post Request " + qosJson);
			Vector<IoTRegion> highRegions = jsonGetHighQoSRegions(qosJson);
			Vector<IoTRegion> lowRegions = jsonGetLowQoSRegions(qosJson);
			Vector<IoTRegion> midRegions = jsonGetMidQoSRegions(qosJson);

			String ServerIP = jsonGetServerIP(qosJson);
			System.out.println("ServerIP=" + ServerIP);

			if (lowRegions.size() == 0 && highRegions.size() == 0 ) {
               // initial case send everything into the high meter.
				IoTSer.CreateInitialQosEntry(ServerIP);
			}
			else
			{

			IoTSer.CreateLowQosEntries(ServerIP, lowRegions);
			IoTSer.CreateHighQosEntries(ServerIP, highRegions);
			IoTSer.CreateMidQosEntries(ServerIP, midRegions);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return null;
	}

	public static String jsonGetServerIP(String qosJson) {
		try {
			String ip = "";
			MappingJsonFactory f = new MappingJsonFactory();
			JsonParser jp;

			jp = f.createParser(qosJson);
			jp.nextToken();
			if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected START_OBJECT");
			}

			while (jp.nextToken() != null) {

				String n = jp.getText();

				// Get High Priority regions
				if (n.equals(IP)) {
					jp.nextToken();
					ip = jp.getText();
					break;
				}

			}
			return ip;
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}

	}

	public static Vector<IoTRegion> jsonGetHighQoSRegions(String qosJson) {

		try {
			Vector<IoTRegion> high = new Vector<IoTRegion>();
			MappingJsonFactory f = new MappingJsonFactory();
			JsonParser jp;

			jp = f.createParser(qosJson);

			jp.nextToken();
			if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected START_OBJECT");
			}

			while (jp.nextToken() != null) {

				String n = jp.getText();

				// Get High Priority regions
				if (n.equals(HIGHPRIORITY)) {

					if (jp.nextToken() == JsonToken.START_ARRAY) {
						// Get Regions until end of the high Array
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							n = jp.getText();
							if (n.equals(REGION)) {
								IoTRegion r = new IoTRegion();
								jp.nextToken();
								r.address = jp.getText();
								jp.nextToken();
								jp.nextToken();
								r.gran_level = jp.getIntValue();
								high.add(r);
								//System.out.println(r.address);
								//System.out.println(r.gran_level);
							}
						}
						break;
					}
				}

			}
			return high;
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}

	}

	public static Vector<IoTRegion> jsonGetLowQoSRegions(String qosJson) {
		Vector<IoTRegion> low = new Vector<IoTRegion>();
		MappingJsonFactory f = new MappingJsonFactory();
		JsonParser jp;
		try {
			jp = f.createParser(qosJson);

			jp.nextToken();
			if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected START_OBJECT");
			}

			while (jp.nextToken() != null) {

				String n = jp.getText();

				// Get High Priority regions
				if (n.equals(LOWPRIORITY)) {

					if (jp.nextToken() == JsonToken.START_ARRAY) {
						// Get Regions until end of the high Array
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							n = jp.getText();
							if (n.equals(REGION)) {
								IoTRegion r = new IoTRegion();
								jp.nextToken();
								r.address = jp.getText();
								jp.nextToken();
								jp.nextToken();
								r.gran_level = jp.getIntValue();
								low.add(r);
								// System.out.println(r.address);
								// System.out.println(r.gran_level);
							}
						}
					}
				}

			}

			return low;
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}

	public static Vector<IoTRegion> jsonGetMidQoSRegions(String qosJson) {
		Vector<IoTRegion> mid = new Vector<IoTRegion>();
		MappingJsonFactory f = new MappingJsonFactory();
		JsonParser jp;
		try {
			jp = f.createParser(qosJson);

			jp.nextToken();
			if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected START_OBJECT");
			}

			while (jp.nextToken() != null) {

				String n = jp.getText();

				// Get High Priority regions
				if (n.equals(MIDPRIORITY)) {

					if (jp.nextToken() == JsonToken.START_ARRAY) {
						// Get Regions until end of the high Array
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							n = jp.getText();
							if (n.equals(REGION)) {
								IoTRegion r = new IoTRegion();
								jp.nextToken();
								r.address = jp.getText();
								jp.nextToken();
								jp.nextToken();
								r.gran_level = jp.getIntValue();
								mid.add(r);
								// System.out.println(r.address);
								// System.out.println(r.gran_level);
							}
						}
					}
				}

			}

			return mid;
		} catch (Exception e) {
			System.out.println(e.toString());
			return null;
		}
	}

}
