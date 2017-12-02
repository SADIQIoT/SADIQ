package net.floodlightcontroller.iotclassifier;

public class IoTApp {
	int appID;
	String ip;
	long totalRate;
	AppMeter totalMeter;
	AppMeter lowMeter;
	AppMeter meduimMeter;
	AppMeter highMeter;
	

	private class AppMeter
	{
		int id;
		double ratePercentage;
		
		
	}

}
