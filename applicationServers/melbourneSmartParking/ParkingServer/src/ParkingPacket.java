import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;


public class ParkingPacket   {
	String fullregion;
	String deviceID;
	int status;
	Date orgTimeStamp;
	String areaname;
	String lat;
	String longat;
	String stringTimeStamp;
	Date orgtime;
	
	
	public ParkingPacket(String pkt){
		try{
		String[] tokens = pkt.split(",");
		this.fullregion = tokens[0];
		this.deviceID = tokens[2];
		
		this.stringTimeStamp=tokens[3];
		this.status = Integer.parseInt(tokens[1]);
		this.areaname = tokens[6];
		this.lat=tokens[4];
		this.longat=tokens[5];
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			System.out.println("worng packet format");
		}
	}
	public ParkingPacket(){}

	 

	

	

}
