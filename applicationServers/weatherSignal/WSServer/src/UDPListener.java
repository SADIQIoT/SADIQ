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
			//System.out.println(getRuntime().availableProcessors());
			//DatagramSocket serverSocket = new DatagramSocket(9999);
			//serverSocket.setReceiveBufferSize(Integer.MAX_VALUE);
			
			DatagramChannel channel = DatagramChannel.open();
			channel.socket().bind(new InetSocketAddress(9999));
			channel.setOption(StandardSocketOptions.SO_RCVBUF,1000000000);
			ByteBuffer buf = ByteBuffer.allocate(150);
			buf.clear();
			System.out.println(channel.getLocalAddress());
			DatagramPacket receivePacket = null;//new DatagramPacket(new byte[100], 100);
			System.out.println("Waiting for datagram packet");
			/*
			ByteBuffer[] buffers = new ByteBuffer[12000000];
			for (int i = 0; i < buffers.length; i++)
				buffers[i] = ByteBuffer.allocate(150);*/
			
			//System.out.println("Buffers initialized!");

			int i = 0;
			while (true) {
				try{
			
					//receivePacket = new DatagramPacket(new byte[150], 150);
					//serverSocket.receive(receivePacket);

				//}
			//	writeLock.unlock();
				// String sentence = new String(receivePacket.getData());
			

				//queue.put(receivePacket);
			    buf = ByteBuffer.allocate(150);
				//buf = buffers[IoTServer.i];
			    channel.receive(buf);
				
			   
			    /*buf.flip();
				     int limits = buf.limit();
				     byte bytes[] = new byte[limits];
				     buf.get(bytes, 0, limits);
				      String msg = new String(bytes);

				      System.out.println("Client at   says: " + msg);
				      //buf.rewind();
				   
				     // buf.clear();
*/
				
					//if (i % 50000 == 0)// || (i > 70000 && i % 1000 == 0) )
					//if (i > 78908)
					//	System.out.println( i);
//					pktarray[i]=receivePacket;
				  pktarray[i]=buf;
				    i++;
				// System.out.println("*size "+queue.size());
				// if (sentence.trim().equals("END OF TRANSMISSION")){

				// queue.put(sentence);

				// break;
				// }
				// else{

				// get packet data without the garbage
				// String[] packet =
				// sentence.split(System.getProperty("line.separator"));
				// queue.put(packet[0]);

				// }

				} catch (Exception ex) {
				System.out.println("Error: " + ex.toString());
				ex.printStackTrace();
				}
			}
			//serverSocket.close();
		} catch (Exception ex) {
			System.out.println("Error: " + ex.toString());
			ex.printStackTrace();

			//System.exit(1);

		}

	}

}
