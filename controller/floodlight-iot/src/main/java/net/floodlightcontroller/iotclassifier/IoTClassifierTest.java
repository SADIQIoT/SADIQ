package net.floodlightcontroller.iotclassifier;

import java.util.ArrayList;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionGotoTable;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.Masked;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U128;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.IOT;

public class IoTClassifierTest {
	public static void testIoTAddressMatchRateExact(IOFSwitch sw) {
		/*
		 * Install a flow entry that has an Exact match on an IoT address to
		 * test the matching rate on a custom field
		 * Note: Need to disable metering in the flow entry of table 1. 
		 * or install the go to output port here
		 */
		OFFactory factory = sw.getOFFactory();
		String iotzone = "13TDE91114516870";
		int granlevel = 1;
		String fulladdrHex = IoTClassifier.asciiToHex(iotzone);
		String addr1hex = fulladdrHex.substring(0, Math.min(fulladdrHex.length(), 16));
		String addr2hex = fulladdrHex.substring(16, Math.min(fulladdrHex.length(), 32));
		long iotaddr1 = Long.parseLong(addr1hex, 16);
		long iotaddr2 = Long.parseLong(addr2hex, 16);
		IoTAddrMask mask = new IoTAddrMask();
		IoTClassifier.getIoTaddMask(granlevel, mask);

		Match m = factory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort))
				//.setExact(MatchField.IOT_ADDR, U128.of(iotaddr1, iotaddr2))
				.build();

		//OFInstructionGotoTable gotoTable = factory.instructions().gotoTable(TableId.of(1));
		ArrayList<OFAction> actions_1 = new ArrayList<OFAction>(1);
		actions_1.add(factory.actions().output(OFPort.of(3), 0xffFFffFF));
		OFInstructionApplyActions applyActions = factory.instructions().buildApplyActions().setActions(actions_1)
				.build();
		ArrayList<OFInstruction> instlist = new ArrayList<OFInstruction>();
		instlist.add(applyActions);
		ArrayList<OFMessage> flows = new ArrayList<OFMessage>();
		OFFlowAdd flow = factory.buildFlowAdd().setTableId(TableId.of(0)).setPriority(1000).setMatch(m)
				.setInstructions(instlist).build();
		flows.add(flow);
		sw.write(flows);
	}

	static void testIoTAddressMatchRateWildcard(IOFSwitch sw) {
		/*
		 * Install a flow entry that has a Wildcard match on an IoT address to
		 * test the matching rate on a custom field
		 */

		/*
		 * Install a flow entry that has an Exact match on an IoT address to
		 * test the matching rate on a custom field
		 */
		OFFactory factory = sw.getOFFactory();
		String iotzone = "13TDE91114516870";
		int granlevel = 3;
		String fulladdrHex = IoTClassifier.asciiToHex(iotzone);
		String addr1hex = fulladdrHex.substring(0, Math.min(fulladdrHex.length(), 16));
		String addr2hex = fulladdrHex.substring(16, Math.min(fulladdrHex.length(), 32));
		long iotaddr1 = Long.parseLong(addr1hex, 16);
		long iotaddr2 = Long.parseLong(addr2hex, 16);
		IoTAddrMask mask = new IoTAddrMask();
		IoTClassifier.getIoTaddMask(granlevel, mask);

		Match m = factory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort))
				.setMasked(MatchField.IOT_ADDR, Masked.of(U128.of(iotaddr1, iotaddr2),
						U128.of(mask.iot_add_mask_part_1, mask.iot_add_mask_part_2)))
				.build();
		OFInstructionGotoTable gotoTable = factory.instructions().gotoTable(TableId.of(1));
		ArrayList<OFInstruction> instlist = new ArrayList<OFInstruction>();
		instlist.add(gotoTable);
		ArrayList<OFMessage> flows = new ArrayList<OFMessage>();
		OFFlowAdd flow = factory.buildFlowAdd().setTableId(TableId.of(0)).setPriority(1000).setMatch(m)
				.setInstructions(instlist).build();
		flows.add(flow);
		sw.write(flows);

	}

}
