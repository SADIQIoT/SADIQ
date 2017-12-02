package net.floodlightcontroller.iotclassifier;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class GetIoTQueueStatsResource extends ServerResource {

@Get("json")
public String GetHighQueueDropRate()
{
	 IIoTQoSService IoTSer = (IIoTQoSService)getContext().getAttributes().get(IIoTQoSService.class.getCanonicalName());
	//needs to call iotclassifier to call python script
	 String ServerIP="";
	 String droprate=IoTSer.GetHighQueueDropRate(ServerIP);
	//then need to return value to user by REST JOSN object
	 return droprate;
}



}
