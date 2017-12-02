package net.floodlightcontroller.iotclassifier;

import java.util.HashSet;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.python.core.exceptions;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.statistics.SwitchPortBandwidth;

public class NetworkContextModule implements Runnable {
	private IStatisticsService statApiService;
	protected IOFSwitchService switchService;
	public static double DROP_THERSHOLD = 5.0;
	public static int PKT_SIZE_BYTES=1500;
	StringBuilder dropRateofS1_Inst3;

	public NetworkContextModule(IStatisticsService statApiService, IOFSwitchService switchService,
			 StringBuilder dropRateofS1_Inst3) {
		this.statApiService = statApiService;
		this.switchService = switchService;
		this.dropRateofS1_Inst3 = dropRateofS1_Inst3;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		collectPortStats();
	}

	void collectPortStats() {
		try {
			/*
			 * Call this method periodically, if a dropped in a certain switch
			 * is above the threshold then call the tuning methods and pass that
			 * switch DatapathId
			 */
			Set<DatapathId> switchesIDs = this.switchService.getAllSwitchDpids();
			for (DatapathId dpid : switchesIDs) {
				// SwitchPortBandwidth spb
				// =statApiService.getBandwidthConsumption(id, OFPort.ALL);
				Set<SwitchPortBandwidth> spbs;
				for (OFPortDesc pd : switchService.getSwitch(dpid).getPorts()) {
					SwitchPortBandwidth spb = statApiService.getBandwidthConsumption(dpid, pd.getPortNo());
					if (spb != null) {
						// spbs.add(spb);
						System.out.print("PortNumber : " + pd.getName() + ", ");
						long droppedPkts = spb.getDroppedPkts().getValue();
						long sentBytes = spb.getPriorByteValueTx().getValue();
						double dropRatio = 0;
						if (sentBytes != 0) {
							dropRatio = ((float) droppedPkts / (sentBytes/PKT_SIZE_BYTES)) * 100;
						}
					//	System.out.print("DroppedPkts: " + droppedPkts + ", ");
					//	System.out.print("SentPkts: " + (sentBytes/PKT_SIZE_BYTES) + ", ");
					//	System.out.println("Drop Ratio: " + dropRatio);

						if (dpid.equals(DatapathId.of(IoTClassifier.S1))
								&& pd.getName().equals(IoTClassifier.serverPort)) {
							// send the drop rate of inst3 to app server
							dropRateofS1_Inst3.delete(0, dropRateofS1_Inst3.length());
							dropRateofS1_Inst3.append(String.valueOf(dropRatio));
							System.out.println("dropRateofInst3: " + dropRateofS1_Inst3);
						}

						if (dropRatio >= DROP_THERSHOLD) {
							tuneQoSFlowEntries(dpid);
						}

					}

				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());

		}
	}

	void tuneQoSFlowEntries(DatapathId congestedSwitch) {
		/*
		 * For the parking application: if a switch is highly congested then
		 * give priority only to the exact locations and not nearby locations.
		 * Which means all MID priority regions should become low And the high
		 * priority meter should get the mid priority meter bandwidth
		 */

		// delete strict MID priority flow entries.
		deleteMidPrioFlows();
		// Edit high priority meter
		editHighMeterRate();

		// for the Weather Signal inform the application to increase the
		// elephant threshold
	}

	boolean deleteMidPrioFlows() { // use the midPrioirty cache and delete all
									// flows
									// make sure that no new flows are created,
									// maybe have a flag
		return false;
	}

	boolean editHighMeterRate() {
		return false;
	}
}
