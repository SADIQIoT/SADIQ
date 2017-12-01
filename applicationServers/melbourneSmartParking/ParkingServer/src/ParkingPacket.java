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
	
	//DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss a +S");
	// SimpleDateFormat ft1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a +S"); 
	// SimpleDateFormat ft2 = new SimpleDateFormat("HH:mm:ss a +S"); 
	public ParkingPacket(String pkt){
		try{
		String[] tokens = pkt.split(",");
		this.fullregion = tokens[0];
		this.deviceID = tokens[2];
		//06/12/2014 12:00:00 AM +0000
		this.stringTimeStamp=tokens[3];
		//this.orgTimeStamp = ft1.parse(tokens[3]);
		//this.orgtime=ft2.parse(tokens[3].substring(11,tokens[3].length()));
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
	/* @Override
	  public int compareTo(ParkingPacket o) {
	    return orgTimeStamp.compareTo(o.orgTimeStamp);
	  }*/
	 

	

	

}
