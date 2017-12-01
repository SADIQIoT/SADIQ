
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;
import java.util.Vector;

import com.google.common.collect.*;

public class DataProcessor implements Runnable {

	ByteBuffer[] pktarray;

	final LinkedBlockingDeque<DatagramPacket> queue2 = new LinkedBlockingDeque<DatagramPacket>();
	Map<String, String> deviecLocation = new ConcurrentHashMap<String, String>();
	ListMultimap<String, String> deviceStatuses;
	Map<String, String> regionFullregion = new ConcurrentHashMap<String, String>();
	/**
	 * mapping // 1km // region // to // full // region
	 */
	Map<String, String> deviceGPS = new ConcurrentHashMap<String, String>();
	/**
	 * map device ID to GPS
	 */
	Map<String, Double> ParkingPercent100m = new ConcurrentHashMap<String, Double>();
	/**
	 * percent of occupied parking
	 */
	Map<String, Double> OccupancyCount100m = new ConcurrentHashMap<String, Double>();
	/**
	 * number of occupied parking
	 */
	// Map<String, Integer> PacketCount100m = new ConcurrentHashMap<String,
	// Integer>();
	/**
	 * number of occupied parking
	 */

	Map<String, Double> ParkingPercent1k = new ConcurrentHashMap<String, Double>();
	/**
	 * percent of occupied parking
	 */
	Map<String, Double> OccupancyCount1k = new ConcurrentHashMap<String, Double>();
	/**
	 * number of occupied parking
	 */
	Map<String, Integer> PacketCount1k = new ConcurrentHashMap<String, Integer>();
	/**
	 * number of occupied parking
	 */

	Map<String, Double> ParkingPercentArea = new ConcurrentHashMap<String, Double>();
	/**
	 * percent of occupied parking
	 */
	Map<String, Double> OccupancyCountArea = new ConcurrentHashMap<String, Double>();
	/**
	 * number of occupied parking
	 */
	Map<String, Integer> PacketCountArea = new ConcurrentHashMap<String, Integer>();
	/**
	 * number of occupied parking
	 */

	Map<String, Double> totalParking = new HashMap<String, Double>();
	/**
	 * "static" total number of parking spaces in each 100m MGRS
	 */

	Map<String, Double> totalParking1k = new HashMap<String, Double>();
	/**
	 * "static" total number of parking spaces in each 1k MGRS
	 */

	Map<String, Double> totalParkingArea = new HashMap<String, Double>();
	/**
	 * "static" total number of parking spaces in each area
	 */
	Vector<Integer> eventRegionsSize = new Vector<Integer>();
	Vector<Integer> mid100RegionsSize = new Vector<Integer>();
	Vector<Double> dropRate = new Vector<Double>();

	// File Area = new File(HOME_DIR +
	Vector<String> pkts = new Vector<String>();
	Vector<ParkingPacket> pktBuffer = new Vector<ParkingPacket>();
	static final double pktSize = 1500 * 8;// in bits
	private int durationCounter = 0;
	private int periodicUpdate = 3;

	private RESTClient restclient;
	private double highOccThershold = 0.4;
	private double midOccThershold = 0.35;
	private int maxNumberOfHighregions = 1400;
	
	Double count, count1, count2;
	Logger logger;
	public static String HOME_DIR = "/home/ahmadem/";

	private boolean stopUpdate = false;// change this flag to true when running
										// NoAppContext
	private int stopUpdateCounter = 3;
	private double dropthershold = 4.0;
	double currentDrop = 0.0;
	double previousDrop = 0.0;

	DataProcessor(ByteBuffer[] pktarray) {
		totalParkingList100m();
		totalParkingList1k();
		totalParkingListArea();

		this.pktarray = pktarray;

		deviceStatuses = ArrayListMultimap.create();
		restclient = new RESTClient();
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

	// **************************************************************************************************
	// total number of parking in each 100m MGRS
	public void totalParkingList100m() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(HOME_DIR
					+ "Server/ParkingServer/src/totalParking100m.csv"));
			String myLine;
			while ((myLine = in.readLine()) != null) {
				String parts[] = myLine.split(",");
				totalParking.put(parts[1], Double.parseDouble(parts[0]));

			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
			logger.info(e.toString());
		}
	}

	// total number of parking in each 1k MGRS
	public void totalParkingList1k() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(HOME_DIR
					+ "Server/ParkingServer/src/totalParking1k.csv"));
			String myLine;
			while ((myLine = in.readLine()) != null) {
				String parts[] = myLine.split(",");
				totalParking1k.put(parts[1], Double.parseDouble(parts[0]));

			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
			logger.info(e.toString());
		}
	}

	// total number of parking in each Area

	public void totalParkingListArea() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(HOME_DIR
					+ "Server/ParkingServer/src/totalParkingArea.csv"));
			String myLine;
			while ((myLine = in.readLine()) != null) {
				String parts[] = myLine.split(",");
				totalParkingArea.put(parts[1], Double.parseDouble(parts[0]));

			}
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
			logger.info(e.toString());
		}
	}

	// **************************************************************************************************

	public String zeroPadRegion(String region, int granlevel) {
		String paddedRegion = "";
		switch (granlevel) {
		case 3:
			paddedRegion = region.substring(0, 7) + "000" + region.substring(7)
					+ "0000";
			break;
		case 4:
			paddedRegion = region.substring(0, 8) + "00"
					+ region.substring(8, 11) + "000";
			break;
		}
		return paddedRegion;
	}

	public void run() {
		System.out.println("Hello from a thread!");

		// Create the Scheduled communication with the controller
		ScheduledExecutorService exec = Executors
				.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new ControllerCommunication(), 0,
				periodicUpdate, TimeUnit.SECONDS);

		String data = "";
		ByteBuffer buffer;
		int j = 0;
		try {

			while (true) {

				if ((buffer = pktarray[j]) != null) {

					// buffer = pktarray1[j];

					j++;

					buffer.flip();
					int limits = buffer.limit();
					byte bytes[] = new byte[limits];
					buffer.get(bytes, 0, limits);
					data = new String(bytes);
					if (data.substring(0, 19).equals("END OF TRANSMISSION")) {
						System.out.println(data.trim());
						System.out.println(j);
						exec.shutdown();
						writeOutput();
						break;
					}

					ParkingPacket pPkt = new ParkingPacket(data);
					updateOccupancy(pPkt);
					pPkt = null;

				}

				else {
					TimeUnit.MICROSECONDS.sleep(100000);
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(data.trim());
			System.out.println(e.toString());
			e.printStackTrace();
			logger.info(e.toString());
		}
	}

	public TreeMap<String, Double> sortMapByValue(HashMap<String, Double> map) {
		Comparator<String> comparator = new ValueComparator(map);
		// TreeMap is a map sorted by its value.
		// The comparator is used to sort the TreeMap by value.
		TreeMap<String, Double> result = new TreeMap<String, Double>(comparator);
		result.putAll(map);
		return result;
	}

	class ValueComparator implements Comparator<String> {

		HashMap<String, Double> map = new HashMap<String, Double>();

		public ValueComparator(HashMap<String, Double> map) {
			this.map.putAll(map);
		}

		@Override
		public int compare(String s1, String s2) {
			if (map.get(s1) <= map.get(s2)) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	public class ControllerCommunication implements Runnable {
		private int updatecount = 0;

		public void run() {
			try {

				System.out.println("Inside ControllerCommunication");
				logger.info("Inside ControllerCommunication");

				Vector<String> eventRegions = new Vector<String>();

				Vector<String> Regions1KM = new Vector<String>();
				Vector<String> mid100Regions = new Vector<String>();
				Vector<String> mid100RegionsTemp = new Vector<String>();
				// File Area = new File(HOME_DIR +
				// "Server/outputParking/HighPrioirtyAreas.csv");

				// FileOutputStream fosArea = new FileOutputStream(Area, true);
				// BufferedWriter bwarea = new BufferedWriter(new
				// OutputStreamWriter(fosArea));

				// Sort list
				Map<String, Double> ParkingPercent100mCopy = new HashMap<String, Double>(
						ParkingPercent1k);
				TreeMap<String, Double> sortedMap = sortMapByValue((HashMap) ParkingPercent100mCopy);
				int k = 0;
				int sumOfParkingDevices = 0;
                int totalNumberofhighregions=0;
				// Get the droprate that could indicate a congestion
				double droprate = Double.parseDouble(restclient
						.GetHighQueueDropRate());
				currentDrop=droprate;

				for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
					
				//			|| sumOfParkingDevices >= maxNumberOfHighDevices)
						//break;
					if (entry.getValue() >= highOccThershold) {
						totalNumberofhighregions++;
						if (k < maxNumberOfHighregions ){//&& totalParking1k.get(entry.getKey())>=100){
						
						String region100m = entry.getKey();
						if (! eventRegions.contains(zeroPadRegion(region100m,3))){
						eventRegions.addElement(zeroPadRegion(region100m,3));
						k++;
					}
						if (mid100Regions.contains(zeroPadRegion(region100m,3))){
							mid100Regions.remove(zeroPadRegion(region100m,3));
							}
						mid100RegionsTemp.add(region100m.substring(0,5)+ (Integer.parseInt(region100m.substring(5,7)) + 1 )+ (Integer.parseInt(region100m.substring(7))+0));
						mid100RegionsTemp.add(region100m.substring(0,5)+ (Integer.parseInt(region100m.substring(5,7)) + 1 )+ (Integer.parseInt(region100m.substring(7))+1));
						mid100RegionsTemp.add(region100m.substring(0,5)+ (Integer.parseInt(region100m.substring(5,7)) + 1 )+ (Integer.parseInt(region100m.substring(7))-1));
						mid100RegionsTemp.add(region100m.substring(0,5)+ (Integer.parseInt(region100m.substring(5,7)) - 1 )+ (Integer.parseInt(region100m.substring(7))+0));
						mid100RegionsTemp.add(region100m.substring(0,5)+ (Integer.parseInt(region100m.substring(5,7)) - 1 )+ (Integer.parseInt(region100m.substring(7))+1));
						mid100RegionsTemp.add(region100m.substring(0,5)+ (Integer.parseInt(region100m.substring(5,7)) - 1 )+ (Integer.parseInt(region100m.substring(7))-1));
						mid100RegionsTemp.add(region100m.substring(0,5)+ (Integer.parseInt(region100m.substring(5,7)) + 0 )+ (Integer.parseInt(region100m.substring(7))+1));
						mid100RegionsTemp.add(region100m.substring(0,5)+ (Integer.parseInt(region100m.substring(5,7)) + 0 )+ (Integer.parseInt(region100m.substring(7))-1));
				
						for(String tmpmidregion : mid100RegionsTemp)
						{
							if  (totalParking1k.containsKey(tmpmidregion) &&
						    	(! mid100Regions.contains(zeroPadRegion(tmpmidregion,3))) &&
						    	(! eventRegions.contains(zeroPadRegion(tmpmidregion,3)))&&
						    	ParkingPercent100mCopy.containsKey(tmpmidregion)&&
						    	ParkingPercent100mCopy.get(tmpmidregion) > midOccThershold)
						    	
						        {
						    		mid100Regions.addElement(zeroPadRegion(tmpmidregion,3));
						    	}
							
						}
						
						mid100RegionsTemp.clear();	
						
						
					/*	String region1km = region100m.substring(0,
								Math.min(region100m.length(), 7))
								+ region100m.charAt(8) + region100m.charAt(9);

						// get the 1km around the high priority 100m
						if (!Regions1KM.contains(zeroPadRegion(region1km, 3))) {
							sumOfParkingDevices += totalParking1k
									.get(region1km);
							Regions1KM.addElement(zeroPadRegion(region1km, 3));
						}
*/
						// System.out.println(eventRegions);
						// System.out.println(region1km);
						// bwarea.write("MGRS1KM" + "," + region1km + "MGRS100m"
						// + "," + entry.getKey() + "," + "percent: "
						// + entry.getValue());
						// bwarea.newLine();
						
					}
				}}

				// get the 100m inside the same 1km that is above the
				// midoccupancy
			/*	for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
					if (entry.getValue() >= midOccThershold
							&& entry.getValue() < highOccThershold) {

						String region100m = entry.getKey();
						String region1km = region100m.substring(0,
								Math.min(region100m.length(), 7))
								+ region100m.charAt(8) + region100m.charAt(9);

						if (Regions1KM.contains(zeroPadRegion(region1km, 3))) {
							// then add the 100m to the priority list
							// mid100Regions.addElement(zeroPadRegion(region100m,
							// 4));
							// System.out.println("Mid Region Add");
						}
					}
				}*/

				// bwarea.newLine();
				System.out.println("Total Number of devices "
						+ sumOfParkingDevices);
				logger.info("Selected event regions, sending them to REST API 100m :"
						+ eventRegions.size() + " 1km :" + Regions1KM.size());

				if (stopUpdate && updatecount >= stopUpdateCounter) {
					System.out.println("Update stopped");
				} else {
					// Simple scenario: if the droprate is above a then stop
					// sending MID regions
					if (droprate >= dropthershold && currentDrop >= previousDrop) {
						 previousDrop=currentDrop;
						 //highOccThershold=0.4;
						//Regions1KM.removeAllElements();
						mid100Regions.removeAllElements();
					}
			//		else {
				//		highOccThershold=0.45;
						
			//		}
					
					dropRate.add(droprate);
					mid100Regions.clear();
					System.out.println("Number of high regions "
							+ eventRegions.size() + "out of : "+ totalNumberofhighregions);
					eventRegionsSize.add(eventRegions.size());
					System.out.println("Number of Mid regions "
							+ mid100Regions.size());
					mid100RegionsSize.add( mid100Regions.size());
					restclient.sendQoStoController(restclient.buildQoSJsonObj(
							eventRegions, mid100Regions));
					logger.info("Event regions sent");
				
					if ( eventRegions.size() != 0) {
						durationCounter++;
						updatecount++;
					}
					
					System.out.println("updatecount: " + updatecount);
				}

			} catch (Exception e) {
				System.out.println(e.toString());
				logger.info(e.toString());
			}

		}
	}

	private boolean updateOccupancy(ParkingPacket pkt) {
		// for (ParkingPacket pkt : buffredPkts) {

		String region100m = pkt.fullregion.substring(0, 8).concat(
				pkt.fullregion.substring(10, 13));

		// 1k region
		String region1k = pkt.fullregion.substring(0, 7).concat(
				pkt.fullregion.substring(10, 12));

		String fullLocation = region100m + "," + pkt.areaname;

		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());

		String logpkt = timeStamp + "," + pkt.fullregion + "," + region100m
				+ "," + region1k + "," + pkt.status + "," + pkt.deviceID + ","
				+ pkt.stringTimeStamp + "," + pkt.areaname;
		regionFullregion.put(pkt.fullregion, region100m);
		deviecLocation.put(pkt.deviceID + fullLocation, fullLocation);
		deviceStatuses.put(pkt.deviceID + fullLocation,
				String.valueOf(pkt.status));
		deviceGPS.put(pkt.deviceID, pkt.lat + "," + pkt.longat);

		// ********************************************100m MGRS
		// ********************************************

		Double total100mParking = totalParking.get(region100m);

		if (!ParkingPercent100m.containsKey(region100m)) {

			if (pkt.status == 1) {
				OccupancyCount100m.put(region100m, 1.0);
				ParkingPercent100m.put(region100m, (1 / total100mParking));
				regionFullregion.put(region100m, pkt.fullregion);
			}

		} else {

			if (pkt.status == 1) {
				count = OccupancyCount100m.get(region100m);
				count++;
				OccupancyCount100m.put(region100m, count);
				ParkingPercent100m.put(region100m, (count / total100mParking));

			} else {
				count = OccupancyCount100m.get(region100m);
				count--;

				OccupancyCount100m.put(region100m, count);
				ParkingPercent100m.put(region100m, (count / total100mParking));

			}

		}
		logpkt = logpkt + ","
				+ String.valueOf((ParkingPercent100m.get(region100m)));
		// **************************************************************************************************

		// ********************************************1k MGRS
		// ********************************************

		Double total1kParking = totalParking1k.get(region1k);

		if (!ParkingPercent1k.containsKey(region1k)) {

			if (pkt.status == 1) {
				OccupancyCount1k.put(region1k, 1.0);
				ParkingPercent1k.put(region1k, (1 / total1kParking));
			}
		} else {

			if (pkt.status == 1) {
				count1 = OccupancyCount1k.get(region1k);
				count1++;
				OccupancyCount1k.put(region1k, count1);
				ParkingPercent1k.put(region1k, (count1 / total1kParking));

			} else {
				count1 = OccupancyCount1k.get(region1k);
				count1--;
				OccupancyCount1k.put(region1k, count1);
				ParkingPercent1k.put(region1k, (count1 / total1kParking));

			}

		}
		logpkt = logpkt + ","
				+ String.valueOf((ParkingPercent1k.get(region1k)));

		/*
		 * //
		 * *******************************************************************
		 * *******************************
		 * 
		 * // ******************************************** Area //
		 * ************************************************
		 * 
		 * Double totalAreaParking = totalParkingArea.get(pkt.areaname);
		 * 
		 * if (!PacketCountArea.containsKey(pkt.areaname)) {
		 * 
		 * PacketCountArea.put(pkt.areaname, 1); } else {
		 * PacketCountArea.put(pkt.areaname, PacketCountArea.get(pkt.areaname) +
		 * 1); }
		 * 
		 * if (!ParkingPercentArea.containsKey(pkt.areaname)) {
		 * 
		 * if (pkt.status == 1) { OccupancyCountArea.put(pkt.areaname, 1.0);
		 * ParkingPercentArea.put(pkt.areaname, (1 / totalAreaParking));
		 * 
		 * logpkt = logpkt + "," +
		 * String.valueOf((ParkingPercentArea.get(pkt.areaname))); } } else {
		 * 
		 * if (pkt.status == 1) { count2 = OccupancyCountArea.get(pkt.areaname);
		 * count2++; OccupancyCountArea.put(pkt.areaname, count2);
		 * ParkingPercentArea.put(pkt.areaname, (count2 / totalAreaParking));
		 * 
		 * logpkt = logpkt + "," +
		 * String.valueOf((ParkingPercentArea.get(pkt.areaname))); } else {
		 * count2 = OccupancyCountArea.get(pkt.areaname); count2--;
		 * OccupancyCountArea.put(pkt.areaname, count2);
		 * ParkingPercentArea.put(pkt.areaname, (count2 / totalAreaParking));
		 * 
		 * logpkt = logpkt + "," +
		 * String.valueOf((ParkingPercentArea.get(pkt.areaname)));
		 * 
		 * }
		 * 
		 * }
		 */

		// **************************************************************************************************

		pkts.addElement(logpkt);

		// }

		return true;

	}

	private void writeOutput() {
		try {
			logger.info("Writing the output files");
			File fout1 = new File(HOME_DIR
					+ "Server/outputParking/highAndMedSize.csv");
			// File fout2 = new File(HOME_DIR
			// + "Server/outputParking/highAndMedSRegions.csv");
			File fout3 = new File(HOME_DIR
					+ "Server/outputParking/Droprate.csv");
			File fout4 = new File(HOME_DIR
					+ "Server/outputParking/ParkingPercent100M.csv");
			File fout5 = new File(HOME_DIR + "Server/outputParking/packets.csv");

			FileOutputStream fos1 = new FileOutputStream(fout1);
			// FileOutputStream fos2 = new FileOutputStream(fout2);
			FileOutputStream fos3 = new FileOutputStream(fout3);
			FileOutputStream fos4 = new FileOutputStream(fout4);
			FileOutputStream fos5 = new FileOutputStream(fout5);

			BufferedWriter bw1 = new BufferedWriter(
					new OutputStreamWriter(fos1));
			// BufferedWriter bw2 = new BufferedWriter(
			// new OutputStreamWriter(fos2));
			BufferedWriter bw3 = new BufferedWriter(
					new OutputStreamWriter(fos3));
			BufferedWriter bw4 = new BufferedWriter(
					new OutputStreamWriter(fos4));
			BufferedWriter bw5 = new BufferedWriter(
					new OutputStreamWriter(fos5));
			int counter = 0;
			for (Entry<String, String> entry : deviecLocation.entrySet()) {
				// bw1.write(entry.getKey() + "," + entry.getValue() + ",");
				// bw3.write(entry.getKey() + "," +
				// deviceGPS.get(entry.getKey()));
				// bw2.write(entry.getKey() + "," + entry.getValue() + ",");
				List<String> list = deviceStatuses.get(entry.getKey());
				// bw2.write(Integer.toString(list.size()));
				int i = 0;
				boolean correctPattern = true;
				for (String value : list) {
					// bw1.write(value + ",");

					if ((i % 2 == 0 && value.equals("0"))
							|| (i % 2 == 1 && value.equals("1"))) {
						// System.out.println("i="+i+"  vlaue="+value+
						// " i%2="+i%2+"  i%2==0 && value.equals(0))= "+(i%2==0
						// &&
						// value.equals("0"))+"  (i%2==1 && value.equals(1))="+(i%2==1
						// && value.equals("1")));
						correctPattern = false;
					}
					i++;
				}

				if (!correctPattern) {
					counter++;
					// System.out.println(entry.getKey() + " " + list);
				}

				// bw1.newLine();
				// bw2.newLine();
				// bw3.newLine();

			}

			Iterator i = eventRegionsSize.iterator();
			Iterator j = mid100RegionsSize.iterator();
			while (i.hasNext() && j.hasNext()) {
				bw1.write("" + i.next() + ",");
				bw1.write("" + j.next());
				bw1.newLine();

			}
			Iterator drop = dropRate.iterator();

			while (drop.hasNext()) {

				bw3.write("" + drop.next());
				bw3.newLine();

			}
			/*
			 * Iterator ii = eventRegions.iterator(); Iterator
			 * jj=mid100Regions.iterator(); while (ii.hasNext() && jj.hasNext())
			 * { bw2.write("High "+ii.next()+","); bw2.newLine();
			 * bw2.write("Med: "+jj.next()); bw2.newLine();
			 * 
			 * 
			 * }
			 */

			System.out.println(counter);
			for (Map.Entry<String, Double> entry2 : ParkingPercent100m
					.entrySet()) {
				bw4.write((entry2.getKey() + "," + entry2.getValue()));
				bw4.newLine();
			}

			for (String pkt : pkts) {
				bw5.write(pkt);
				bw5.newLine();
			}

			bw1.close();
			// bw2.close();
			bw3.close();
			bw4.close();
			bw5.close();
		} catch (Exception e) {
			logger.info(e.toString());
			System.out.println(e.toString());
		}
	}

	/*
	 * private boolean parsePacket(String pkt) { try {
	 * 
	 * String[] tokens = pkt.split(",");
	 * 
	 * if (tokens.length >= 4) { String fullregion = tokens[0]; String deviceID
	 * = tokens[2]; String orgTimeStamp = tokens[3]; String status = tokens[1];
	 * String areaname = tokens[6]; // There is a problem when parsing the
	 * street name: unexpected // \00 character is present // String
	 * streetname=tokens[tokens.length-1];
	 * 
	 * // 100m region String region100m = fullregion.substring(0,
	 * Math.min(fullregion.length(), 8)) + fullregion.charAt(10) +
	 * fullregion.charAt(11) + fullregion.charAt(12);
	 * 
	 * // 1k region String region1k = fullregion.substring(0,
	 * Math.min(fullregion.length(), 7)) + fullregion.charAt(10) +
	 * fullregion.charAt(11);
	 * 
	 * String fullLocation = region100m + "," + areaname;
	 * 
	 * String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
	 * 
	 * String logpkt = timeStamp + "," + fullregion + "," + region100m + "," +
	 * region1k + "," + status + "," + deviceID + "," + orgTimeStamp + "," +
	 * areaname; regionFullregion.put(fullregion, region100m);
	 * deviecLocation.put(deviceID, fullLocation); deviceStatuses.put(deviceID,
	 * status); deviceGPS.put(deviceID, tokens[4] + "," + tokens[5]);
	 * 
	 * // ********************************************100m MGRS //
	 * ********************************************
	 * 
	 * Double total100mParking = totalParking.get(region100m);
	 * 
	 * 
	 * if (!ParkingPercent100m.containsKey(region100m)) {
	 * 
	 * if (Integer.parseInt(status) == 1) { OccupancyCount100m.put(region100m,
	 * 1.0); ParkingPercent100m.put(region100m, (1 / total100mParking));
	 * regionFullregion.put(region100m, fullregion);
	 * 
	 * logpkt = logpkt + "," +
	 * String.valueOf((ParkingPercent100m.get(region100m))); } } else {
	 * 
	 * if (Integer.parseInt(status) == 1) { count =
	 * OccupancyCount100m.get(region100m); count++;
	 * OccupancyCount100m.put(region100m, count);
	 * ParkingPercent100m.put(region100m, (count / total100mParking));
	 * 
	 * logpkt = logpkt + "," +
	 * String.valueOf((ParkingPercent100m.get(region100m))); } else { count =
	 * OccupancyCount100m.get(region100m); count--;
	 * OccupancyCount100m.put(region100m, count);
	 * ParkingPercent100m.put(region100m, (count / total100mParking));
	 * 
	 * logpkt = logpkt + "," +
	 * String.valueOf((ParkingPercent100m.get(region100m)));
	 * 
	 * }
	 * 
	 * }
	 * 
	 * //
	 * ***********************************************************************
	 * ***************************
	 * 
	 * // ********************************************1k MGRS //
	 * ********************************************
	 * 
	 * Double total1kParking = totalParking1k.get(region1k);
	 * 
	 * if (!PacketCount1k.containsKey(region1k)) {
	 * 
	 * PacketCount1k.put(region1k, 1); } else { PacketCount1k.put(region1k,
	 * PacketCount1k.get(region1k) + 1); }
	 * 
	 * if (!ParkingPercent1k.containsKey(region1k)) {
	 * 
	 * if (Integer.parseInt(status) == 1) { OccupancyCount1k.put(region1k, 1.0);
	 * ParkingPercent1k.put(region1k, (1 / total1kParking));
	 * 
	 * logpkt = logpkt + "," + String.valueOf((ParkingPercent1k.get(region1k)));
	 * } } else {
	 * 
	 * if (Integer.parseInt(status) == 1) { count1 =
	 * OccupancyCount1k.get(region1k); count1++; OccupancyCount1k.put(region1k,
	 * count1); ParkingPercent1k.put(region1k, (count1 / total1kParking));
	 * 
	 * logpkt = logpkt + "," + String.valueOf((ParkingPercent1k.get(region1k)));
	 * } else { count1 = OccupancyCount1k.get(region1k); count1--;
	 * OccupancyCount1k.put(region1k, count1); ParkingPercent1k.put(region1k,
	 * (count1 / total1kParking));
	 * 
	 * logpkt = logpkt + "," + String.valueOf((ParkingPercent1k.get(region1k)));
	 * 
	 * }
	 * 
	 * }
	 * 
	 * //
	 * ***********************************************************************
	 * ***************************
	 * 
	 * // ******************************************** Area //
	 * ************************************************
	 * 
	 * Double totalAreaParking = totalParkingArea.get(areaname);
	 * 
	 * if (!PacketCountArea.containsKey(areaname)) {
	 * 
	 * PacketCountArea.put(areaname, 1); } else { PacketCountArea.put(areaname,
	 * PacketCountArea.get(areaname) + 1); }
	 * 
	 * if (!ParkingPercentArea.containsKey(areaname)) {
	 * 
	 * if (Integer.parseInt(status) == 1) { OccupancyCountArea.put(areaname,
	 * 1.0); ParkingPercentArea.put(areaname, (1 / totalAreaParking));
	 * 
	 * logpkt = logpkt + "," +
	 * String.valueOf((ParkingPercentArea.get(areaname))); } } else {
	 * 
	 * if (Integer.parseInt(status) == 1) { count2 =
	 * OccupancyCountArea.get(areaname); count2++;
	 * OccupancyCountArea.put(areaname, count2);
	 * ParkingPercentArea.put(areaname, (count2 / totalAreaParking));
	 * 
	 * logpkt = logpkt + "," +
	 * String.valueOf((ParkingPercentArea.get(areaname))); } else { count2 =
	 * OccupancyCountArea.get(areaname); count2--;
	 * OccupancyCountArea.put(areaname, count2);
	 * ParkingPercentArea.put(areaname, (count2 / totalAreaParking));
	 * 
	 * logpkt = logpkt + "," +
	 * String.valueOf((ParkingPercentArea.get(areaname)));
	 * 
	 * }
	 * 
	 * }
	 * 
	 * //
	 * ***********************************************************************
	 * ***************************
	 * 
	 * pkts.addElement(logpkt); return true;
	 * 
	 * } else { logger.info("incorrect packet format :" + pkt);
	 * System.out.println("incorrect packet format :" + pkt); return false; }
	 * 
	 * } catch (Exception e) { // TODO Auto-generated catch block
	 * System.out.println(pkt);
	 * 
	 * System.out.println(e.toString()); e.printStackTrace();
	 * logger.info(e.toString()); return false; }
	 * 
	 * }
	 */

	class WorkerThread implements Runnable {
		BlockingQueue<DatagramPacket> queue2;

		public WorkerThread(BlockingQueue<DatagramPacket> queue2) {
			this.queue2 = queue2;
		}

		public void run() {
			try {
				DatagramPacket pkt;
				int i = 0;
				String data = "";

				while (true) {
					if ((pkt = queue2.poll(1, TimeUnit.MICROSECONDS)) != null) {
						i++;
						System.out.println("q2 " + i);

						// byte[] x=new byte[pkt.getLength()];
						// System.out.println(pkt.getPort());
						// System.out.println("pkt "+pkt.getLength());
						// pkt.getData();
						// data=new String(pkt.getData(), 0, pkt.getLength());

						// System.out.println(data.trim());

						// if (data.substring(0, 19).equals("END OF
						// TRANSMISSION")) {
						// TimeUnit.SECONDS.sleep(20);
						// System.out.println(data.trim());
						// System.out.println(i);
						// exec.shutdown();
						// writeOutput();
						// break;
						// }

						// Runnable worker = new WorkerThread(data);
						// executor.execute(worker);//calling execute method of
						// ExecutorService
						// parsePacket(data);
					}

				}
			} catch (Exception e) {

				System.out.println(e.toString());
				e.printStackTrace();
				logger.info(e.toString());

			}

		}

	}
}