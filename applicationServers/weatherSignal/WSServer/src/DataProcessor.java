

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Comparator;

import java.util.TreeMap;
import java.util.Vector;

public class DataProcessor implements Runnable {
	Map<String, Integer> regionscount = new ConcurrentHashMap<String, Integer>();
	Map<String, Double> regionsTemp = new ConcurrentHashMap<String, Double>();
	Map<String, String> regionFullregion = new ConcurrentHashMap<String, String>();// mapping
																					// 10km
																					// region
																					// to
																					// full
																					// region
	Vector<String> packets = new Vector<String>();
	Vector<String> droprateVSelepahnts = new Vector<String>();
	static final double M = 2.55;
	static final double K = -60.52;
	static final double assigendRate = 10;// in mbps
	static final double pktSize = 1500 * 8;// in bits
	private int durationCounter = 0;
	private int periodicUpdate = 5;
	private RESTClient restclient;
	private double dropthershold = 2;
	private double OrgelephantRatio = 0.35;
	private double elephantRatio = 0.35;
	private double maxElephantRatio = 0.5;
	ByteBuffer[] pktarray;
	private int maxNumberOfelephants = 1500;

	private boolean stopUpdate = true;// change this flag to true when running
										// NoAppContext
	private int stopUpdateCounter = 4;

	private boolean updateElephantRatio = false;// change this flag to true when
												// running SADIQ
	Logger logger;
	public static String HOME_DIR = "/home/ahmadem/";

	// Register the base time of first packet and reset it every one hour.
	DataProcessor(ByteBuffer[] pktarray) {

		this.pktarray = pktarray;

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

					// buffer = pktarray[j];

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

					parsePacket(data);
				} else {

					TimeUnit.MICROSECONDS.sleep(1000);
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

	private boolean parsePacket(String pkt) {
		try {
			// packets.add(pkt);
			String[] tokens = pkt.split(",");
			// check if battemp is a valid number
			if (tokens.length > 4) {
				Double battemp = Double.valueOf(tokens[1]);
				// System.out.println ("Message: " + tokens[0]);

				String fullregion = tokens[0];
				String region = fullregion.substring(0,
						Math.min(fullregion.length(), 6))
						+ fullregion.charAt(10);

				battemp = (double) Math.round(battemp / 10);
				double editedTemp = (battemp * M) + K;

				if (!regionsTemp.containsKey(region)) {
					// New Region
					// System.out.println ("New Region");
					regionscount.put(region, 1);
					regionsTemp.put(region, editedTemp);
					regionFullregion.put(region, fullregion);
				} else {
					// already seen region just update
					// System.out.println ("already Seen Region");
					int count = regionscount.get(region);
					double newTemp = (editedTemp + (regionsTemp.get(region) * count))
							/ (count + 1);
					regionscount.put(region, count + 1);
					regionsTemp.put(region, newTemp);

				}
				return true;

			} else {
				return false;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(pkt);

			System.out.println(e.toString());
			e.printStackTrace();
			logger.info(e.toString());
			return false;
		}

	}

	private void writeOutput() {
		try {
			File fout = new File(HOME_DIR + "Server/output/output.csv");
			FileOutputStream fos = new FileOutputStream(fout);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			for (Entry<String, Integer> entry : regionscount.entrySet()) {

				bw.write(entry.getKey() + "," + regionsTemp.get(entry.getKey())
						+ "," + entry.getValue());

				bw.newLine();
			}

			bw.close();
			writeElephants();

		} catch (Exception e) {
			logger.info(e.toString());
			System.out.println(e.toString());
		}
	}
	
	private void writeElephants() {
		try {
			File fout = new File(HOME_DIR + "Server/output/Elephants.csv");
			FileOutputStream fos = new FileOutputStream(fout, true);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					fos));

			for (String entry : droprateVSelepahnts) {

				bw.write(entry);

				bw.newLine();
			}

			bw.close();

		} catch (Exception e) {
			System.out.println(e.toString());
			logger.info(e.toString());
		}
	}

	private void writeAllPkts() {
		try {
			File fout = new File(HOME_DIR + "Server/output/packets.csv");
			FileOutputStream fos = new FileOutputStream(fout);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

			for (String entry : packets) {

				bw.write(entry);

				bw.newLine();
			}

			bw.close();

		} catch (Exception e) {
			logger.info(e.toString());
			System.out.println(e.toString());
		}
	}

	public class ControllerCommunication implements Runnable {
		private int updatecount = 0;

		public void run() {
			try {
				Vector<String> elephants = new Vector<String>();
				if (durationCounter == 0) {
					// first time send empty high and low lists to init
					// connection with controller
					durationCounter++;
					restclient.sendQoStoController(restclient
							.buildQoSJsonObj(elephants));
				} else {
					// First get dropRate from controller to decide if we need
					// to change the ratio
				
					// get a copy of the regions count map
					Map<String, Integer> regionscountCopy = new HashMap<String, Integer>(
							regionscount);

					// Sum all samples
					double sum = 0;
					for (int samples : regionscountCopy.values()) {
						sum += samples;
					}
					System.out.println("sum=" + sum);
					// Sort list
					TreeMap<String, Integer> sortedMap = sortMapByValue((HashMap) regionscountCopy);

					double elephantsSum = 0;
					double droprate = Double.parseDouble(restclient
							.GetHighQueueDropRate());
					// increase the elephant's ratio if the drop rate is higher
					// than the threshold and decrease it again when the drop rate lowers
					if (droprate >= dropthershold
							&& elephantRatio < maxElephantRatio
							&& updateElephantRatio) {
						System.out.println("old Ratio =" + elephantRatio);
						elephantRatio = elephantRatio + 0.05;
						elephantRatio = Math.round(elephantRatio * 100.0) / 100.0;
						System.out.println("New Ratio is =" + elephantRatio);
					} else if (droprate < dropthershold
							&& elephantRatio > OrgelephantRatio
							&& updateElephantRatio)
					{System.out.println("old Ratio =" + elephantRatio);
					elephantRatio = elephantRatio - 0.05;
					elephantRatio = Math.round(elephantRatio * 100.0) / 100.0;
					System.out.println("New Ratio is =" + elephantRatio);
						
					}
					
					for (Map.Entry<String, Integer> entry : sortedMap
							.entrySet()) {
						elephantsSum += entry.getValue();

						if (((elephantsSum / sum) <= elephantRatio)
								&& elephants.size() < maxNumberOfelephants) {
							// add elephant

							elephants.addElement(regionFullregion.get(entry
									.getKey()) + "0");

						} else {
							break;
						}
					}

					System.out.println("elephantsSum=" + elephantsSum);
					System.out
							.println("elephantsSum/sum=" + elephantsSum / sum);
					System.out.println("ElephantCount=" + elephants.size());
					// if (durationCounter ==1)
					droprateVSelepahnts.add(droprate + "," + elephants.size());
				

					if (stopUpdate && updatecount >= stopUpdateCounter) {
						System.out.println("Update stopped");
					} else {
						logger.info("Selected elephants, sending them to REST API "
								+ elephants.size());
						restclient.sendQoStoController(restclient
								.buildQoSJsonObj(elephants));
						logger.info("Elephants sent");

						if (elephants.size() != 0) {
							durationCounter++;
							updatecount++;
						}
						System.out.println("updatecount: " + updatecount);
					}

				}

			} catch (Exception e) {
				System.out.println(e.toString());
				logger.info(e.toString());
			}

		}

		public TreeMap<String, Integer> sortMapByValue(
				HashMap<String, Integer> map) {
			Comparator<String> comparator = new ValueComparator(map);
			// TreeMap is a map sorted by its value.
			// The comparator is used to sort the TreeMap by value.
			TreeMap<String, Integer> result = new TreeMap<String, Integer>(
					comparator);
			result.putAll(map);
			return result;
		}

		class ValueComparator implements Comparator<String> {

			HashMap<String, Integer> map = new HashMap<String, Integer>();

			public ValueComparator(HashMap<String, Integer> map) {
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

		

	}
}
