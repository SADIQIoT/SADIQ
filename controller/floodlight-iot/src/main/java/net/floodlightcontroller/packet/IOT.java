package net.floodlightcontroller.packet;

import java.nio.ByteBuffer;

public class IOT extends BasePacket {
	
public static int IOTServicePort=9999;
private  long iotaddr1; // MSBs
private  long iotaddr2; // LSBs

/**
 * @return the iotaddr
 */
public long getIoTAddr1() {
    return iotaddr1;
}
public long getIoTAddr2() {
    return iotaddr2;
}

/**
 * @param sourcePort the sourcePort to set
 */
public IOT setIoTAddr(long iotAddr1,long iotAddr2) {
    this.iotaddr1 = iotAddr1;
    this.iotaddr2=iotAddr2;
    return this;
}
	@Override
	public byte[] serialize() {
		byte[] payloadData = null;
        if (payload != null) {
            payload.setParent(this);
            payloadData = payload.serialize();
        }
;

        byte[] data = new byte[16];
        ByteBuffer bb = ByteBuffer.wrap(data);

        bb.putLong(this.iotaddr1); 
        bb.putLong(this.iotaddr2); 
        
        if (payloadData != null)
            bb.put(payloadData);
        
        return data;
	}

	@Override
	public IPacket deserialize(byte[] data, int offset, int length) 
			throws PacketParsingException {
		ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
		this.iotaddr1 = bb.getLong();
		this.iotaddr2 = bb.getLong();
		this.payload = new Data();
        this.payload = payload.deserialize(data, bb.position(), bb.limit()-bb.position());
        this.payload.setParent(this);
        return this;
	

}
}
