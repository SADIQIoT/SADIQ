package net.floodlightcontroller.iotclassifier;

import java.util.Vector;

import net.floodlightcontroller.core.module.IFloodlightService;

public interface IIoTQoSService extends IFloodlightService {
	
	 public boolean DeleteIoTEntries(String ServerIP);
	 public boolean CreateInitialQosEntry(String ServerIP);
	 public boolean CreateHighQosEntries(String ServerIP,Vector<IoTRegion> high);
	 public boolean CreateMidQosEntries(String ServerIP,Vector<IoTRegion> Mid);
	 public boolean CreateLowQosEntries(String ServerIP,Vector<IoTRegion> Low);
	
	 public String GetHighQueueDropRate(String ServerIP);
}
