package net.floodlightcontroller.iotclassifier;

public class IoTRegion
{
	String address; //Full MGRS address
	int gran_level; // Granularity-Level Range [1-6]
	
	public IoTRegion()
	{
		
	}
	
	public IoTRegion(String address, int gran_level)
	{
		this.address=address;
		this.gran_level=gran_level;
	}
	
	public boolean equals(Object anObject) {
		if(this == anObject) return true;
		if(!(anObject instanceof IoTRegion)) return false; 

		IoTRegion tempRegion = (IoTRegion)anObject; 

		  return this.address.equals(tempRegion.address) && this.gran_level == tempRegion.gran_level;
		}
}
