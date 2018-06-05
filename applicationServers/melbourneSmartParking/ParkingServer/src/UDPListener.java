import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static java.lang.Runtime.getRuntime;
public class UDPListener implements Runnable {

	ByteBuffer[] pktarray;


	public UDPListener(ByteBuffer[] pktarray) {

		this.pktarray = pktarray;
		
		
	}

	@Override
	public void run() {
		try {
			System.out.println(Runtime.getRuntime().maxMemory());
			
			
			DatagramChannel channel = DatagramChannel.open();
			channel.socket().bind(new InetSocketAddress(9999));
			channel.setOption(StandardSocketOptions.SO_RCVBUF,1000000000);
			ByteBuffer buf = ByteBuffer.allocate(150);
			buf.clear();
			System.out.println(channel.getLocalAddress());
			DatagramPacket receivePacket = null;
			System.out.println("Waiting for datagram packet");
		

			int i = 0;
			while (true) {
				try{
			
					
			    buf = ByteBuffer.allocate(150);
				
			    channel.receive(buf);
				pktarray[i]=buf;
                i++;
				

				} catch (Exception ex) {
				System.out.println("Error: " + ex.toString());
				}
			}
			
		} catch (Exception ex) {
			System.out.println("Error: " + ex.toString());

		

		}

	}

}
