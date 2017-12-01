
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IoTServer {
	
	
	static ByteBuffer[] pktarray = new ByteBuffer[12200000];
	static CopyOnWriteArrayList<DatagramPacket> cpOnwt=new CopyOnWriteArrayList<DatagramPacket>();
	
	private  static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	private static Lock readLock = readWriteLock.readLock();
	
	//protected static int i = 0;

	private static Lock writeLock = readWriteLock.writeLock();
	public static void main(String args[]) throws Exception {

		try {

			// create thread for the UDP packets listener
			UDPListener udplisten = new UDPListener(pktarray);
			Thread udpthread = new Thread(udplisten);

			// create thread for the pkt processing
			DataProcessor dp = new DataProcessor(pktarray);
			Thread dpthread = new Thread(dp);

			// start both threads
			udpthread.start();
			dpthread.start();

		} catch (Exception ex) {
			System.out.println("Error: " + ex.toString());
			ex.printStackTrace();
			System.exit(1);

		}
	}

}
