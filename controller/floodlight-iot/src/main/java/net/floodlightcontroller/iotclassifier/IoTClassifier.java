package net.floodlightcontroller.iotclassifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowDelete;
import org.projectfloodlight.openflow.protocol.OFFlowDeleteStrict;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFGroupType;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMeterMod;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.actionid.OFActionId;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionGotoTable;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionMeter;
import org.projectfloodlight.openflow.protocol.instructionid.OFInstructionId;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBand;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBandDscpRemark;
import org.projectfloodlight.openflow.types.*;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFTableFeatureProp;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropApplyActions;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropApplyActionsMiss;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropInstructions;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropInstructionsMiss;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropMatch;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropNextTables;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropNextTablesMiss;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropWildcards;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropWriteActions;
import org.projectfloodlight.openflow.protocol.OFTableFeaturePropWriteActionsMiss;
import org.projectfloodlight.openflow.protocol.OFTableFeatures;
import org.projectfloodlight.openflow.protocol.OFTableFeaturesStatsReply;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.internal.OFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.ForwardingBase;
import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.statistics.IStatisticsService;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.util.MatchUtils;
import net.floodlightcontroller.util.OFDPAUtils;
import net.floodlightcontroller.util.OFPortMode;
import net.floodlightcontroller.util.OFPortModeTuple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import net.floodlightcontroller.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Vector;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.collect.*;

public class IoTClassifier extends ForwardingBase
		implements IOFMessageListener, IFloodlightModule, IOFSwitchListener, IIoTQoSService {
	protected IFloodlightProviderService floodlightProvider;
	protected static Logger logger;
	// protected IOFSwitchService switchService;
	protected IRestApiService restApiService;
	protected IStatisticsService statApiService;

	private Vector<IoTRegion> lowcache = new Vector<IoTRegion>();
	private Vector<IoTRegion> highcache = new Vector<IoTRegion>();
	private Vector<IoTRegion> midcache = new Vector<IoTRegion>();
	
	private Vector<IoTRegion> lowcache_S2 = new Vector<IoTRegion>();
	private Vector<IoTRegion> highcache_S2 = new Vector<IoTRegion>();
	private Vector<IoTRegion> midcache_S2 = new Vector<IoTRegion>();
	private Vector<IoTRegion> highcache_Agg = new Vector<IoTRegion>();

	private static int CROSS_TARFFIC_ENTRY_PRIORITY = 50;
	private static int INITIAL_ENTRY_PRIORITY = 100;
	private static int GENERAL_ENTRY_PRIORITY = 200;
	private static int MID_ENTRY_PRIORITY = 300;// for now set it as the high
												// priority
	private static int SPECIFIC_ENTRY_PRIORITY = 300;

	// The data path ID of S1
	public static String S1 = "00:02:e0:07:1b:c4:c7:e0";

	// The data path ID of S2
	public static String S2 = "00:02:e0:07:1b:c4:c7:60";

	// The data path ID of S3
	public static String S3 = "00:02:48:0f:cf:0c:b1:c0";

	public static String serverPort = "24";
	private StringBuilder dropRateofS1_Inst3 = new StringBuilder(
			"0.0");/* the drop rate of the last switch before the server */

	private long totalRate = 1000000;// in Kbps
	private long totalIoTAppRate = totalRate;// in Kbps
	// WS split=0.99
	// Parking split=0.7
	private double iotrateSplit = 0.99;
	private double pktSizeKb = 12;
	private double defualtBurstPer = 0.125; // burst is 1/8 of the assigned rate

	private Vector<Route> routes;

	public static enum METERS {
		HIGH_PRIOIRTY_METER(1), LOW_PRIOIRTY_METER(2), MID_PRIOIRTY_METER(3), TOTAL_IOT_METER(
				4), CROSS_TRAFFIC_TOTAL_METER(5);

		public int meterid;

		METERS(int id) {
			meterid = id;
		}

		public int getId() {
			return meterid;
		}
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IIoTQoSService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		m.put(IIoTQoSService.class, this);
		return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {

		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IRestApiService.class);
		l.add(IDeviceService.class);
		l.add(IRoutingService.class);
		l.add(ITopologyService.class);
		l.add(IDebugCounterService.class);
		l.add(IStatisticsService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		super.init();
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApiService = context.getServiceImpl(IRestApiService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		logger = LoggerFactory.getLogger(IoTClassifier.class);
		deviceManagerService = context.getServiceImpl(IDeviceService.class);
		routingEngineService = context.getServiceImpl(IRoutingService.class);
		topologyService = context.getServiceImpl(ITopologyService.class);
		statApiService = context.getServiceImpl(IStatisticsService.class);
		this.debugCounterService = context.getServiceImpl(IDebugCounterService.class);
		routes = new Vector<Route>();

		// Create the Scheduled network Context monitor
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new NetworkContextModule(statApiService, switchService, dropRateofS1_Inst3), 5, 4,
				TimeUnit.SECONDS);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// super.startUp();
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		restApiService.addRestletRoutable(new IoTWebRoutable());
		switchService.addOFSwitchListener(this);

	}

	@Override
	public String getName() {
		return IoTClassifier.class.getSimpleName();
	}

	@Override
	public void switchAdded(DatapathId switchId) {
		System.out.println("Switch is added!" + switchId);
	}

	@Override
	public void switchRemoved(DatapathId switchId) {
	}

	@Override
	public void switchActivated(DatapathId switchId) {
		System.out.println("Switch is activiated!");
		IOFSwitch sw = switchService.getSwitch(switchId);
		if (sw == null) {
			log.warn(
					"Switch {} was activated but had no switch object in the switch service. Perhaps it quickly disconnected",
					switchId);
			return;
		}

		// Only in the case of a 2920 switch, do not delete the flow entry
		// in table 0 because it is read-only
		if (switchId.equals(DatapathId.of(S3))) {
			sw.write(sw.getOFFactory().buildFlowDelete().setTableId(TableId.of(100)).build());
			sw.write(sw.getOFFactory().buildFlowDelete().setTableId(TableId.of(200)).build());
		} else {
			sw.write(sw.getOFFactory().buildFlowDelete().setTableId(TableId.ALL).build());
		}
		if (OFDPAUtils.isOFDPASwitch(sw)) {

			sw.write(sw.getOFFactory().buildGroupDelete().setGroup(OFGroup.ANY).setGroupType(OFGroupType.ALL).build());
			sw.write(sw.getOFFactory().buildGroupDelete().setGroup(OFGroup.ANY).setGroupType(OFGroupType.INDIRECT)
					.build());
			sw.write(sw.getOFFactory().buildBarrierRequest().build());

			List<OFPortModeTuple> portModes = new ArrayList<OFPortModeTuple>();
			for (OFPortDesc p : sw.getPorts()) {
				portModes.add(OFPortModeTuple.of(p.getPortNo(), OFPortMode.ACCESS));
			}
			if (log.isWarnEnabled()) {
				log.warn("For OF-DPA switch {}, initializing VLAN {} on ports {}",
						new Object[] { switchId, VlanVid.ZERO, portModes });
			}
			OFDPAUtils.addLearningSwitchPrereqs(sw, VlanVid.ZERO, portModes);

		}

		if (switchId.equals(DatapathId.of(S1)) || switchId.equals(DatapathId.of(S2))) {
			createCustomTablesPipeline(switchId);
			createAppMeters(switchId);
			createDefaultEntriesCustomPipeline(switchId);
		} else if (switchId.equals(DatapathId.of(S3))) {
			createDefaultStandredPipeline2920Switch(switchId);
		}

	}

	public void createCustomTablesPipeline(DatapathId switchId) {// I need to
																	// create
																	// two TCAM
																	// tables
		System.out.println("From createCustomTablesPipeline");
		OFSwitch sw = (OFSwitch) switchService.getSwitch(switchId);
		ListenableFuture<?> future;
		OFStatsRequest<?> req = null;
		List<OFTableFeaturesStatsReply> values = null;

		if (sw.getOFFactory().getVersion().compareTo(OFVersion.OF_10) > 0) {

			req = sw.getOFFactory().buildTableFeaturesStatsRequest().build();
		}

		try {
			if (req != null) {
				future = sw.writeStatsRequest(req);
				values = (List<OFTableFeaturesStatsReply>) future.get(10, TimeUnit.SECONDS);
				for (OFTableFeaturesStatsReply reply : values) {
					/*
					 * Add or update the features for a particular table.
					 */
					List<OFTableFeatures> tfs = reply.getEntries();

					List<OFTableFeatures> tables = new ArrayList<OFTableFeatures>();
					// I need the fourth table which is the TCAM table
					OFTableFeatures TCAMTable = null;
					for (OFTableFeatures tf : tfs) {

						TCAMTable = tf;
					}
					OFFactory factory = sw.getOFFactory();
					OFTableFeatures TCAMTable1 = TCAMTable;

					List<OFTableFeatureProp> prop = new ArrayList<OFTableFeatureProp>();
					List<OFTableFeatureProp> prop1 = new ArrayList<OFTableFeatureProp>();
					List<OFTableFeatureProp> temp = new ArrayList<OFTableFeatureProp>();
					temp = TCAMTable.getProperties();

					OFTableFeaturePropNextTables nexttable = factory
							.tableFeaturePropNextTables(
									(List<U8>) java.util.Collections.singletonList(U8.of((short) 1)))
							.createBuilder().build();

					OFTableFeaturePropNextTablesMiss nexttablemiss = factory
							.tableFeaturePropNextTablesMiss(
									(List<U8>) java.util.Collections.singletonList(U8.of((short) 1)))
							.createBuilder().build();

					// IoT addr length is 16 bytes (0x10)
					// UDP length is 8 bytes (0x8)

					U32 oxms[] = { U32.of(0x80000a00), U32.of(0x80001400), U32.of(0x80002002), U32.of(0x80001401),
							U32.of(0xffff0b0a), U32.of(0x00002481), U32.of(0x00030008), U32.of(0x0010ffff),
							U32.of(0x0d0a0000), U32.of(0x24810003), U32.of(0x00080004) };

					List oxmlist = new ArrayList(Arrays.asList(oxms));
					OFTableFeaturePropMatch matchprop = factory.tableFeaturePropMatch(oxmlist);

					// maybe I should add the wildcard of iot_addr here!
					U32 oxmsWildcards[] = { U32.of(0x80000a00), U32.of(0x80001400), U32.of(0x80002002),
							U32.of(0x80001401), U32.of(0xffff0b0a), U32.of(0x00002481), U32.of(0xffff0d0a),
							U32.of(0x0d0a2481) };
					List oxmWildcardslist = new ArrayList(Arrays.asList(oxmsWildcards));
					OFTableFeaturePropWildcards wildecards = factory.tableFeaturePropWildcards(oxmWildcardslist);

					OFActionId aid1 = factory.actionIds().output();
					OFActionId aid2 = factory.actionIds().group();
					OFActionId actions[] = { aid1, aid2 };
					List actionList = new ArrayList(Arrays.asList(actions));
					OFTableFeaturePropApplyActionsMiss actionMiss = factory
							.tableFeaturePropApplyActionsMiss(actionList);
					OFTableFeaturePropWriteActions writeactions = factory.tableFeaturePropWriteActions(actionList);
					OFTableFeaturePropWriteActionsMiss writeactionsMiss = factory
							.tableFeaturePropWriteActionsMiss(actionList);
					OFTableFeaturePropApplyActions applyActions = factory.tableFeaturePropApplyActions(actionList);

					OFInstructionId inst1 = factory.instructionIds().gotoTable();
					OFInstructionId inst2 = factory.instructionIds().applyActions();
					OFInstructionId insts[] = { inst1, inst2 };
					List instList = new ArrayList(Arrays.asList(insts));
					OFTableFeaturePropInstructionsMiss missInstruc = factory.tableFeaturePropInstructionsMiss(instList);

					OFInstructionId inst3 = factory.instructionIds().meter();
					OFInstructionId insts2[] = { inst1, inst2, inst3 };
					List instList2 = new ArrayList(Arrays.asList(insts2));
					OFTableFeaturePropInstructions Inst = factory.tableFeaturePropInstructions(instList2);

					for (OFTableFeatureProp p : temp) {
						prop1.add(p);

						if (nexttable.getType() == p.getType()) {

							prop.add(nexttable);

						}

						else if (nexttablemiss.getType() == p.getType()) {

							prop.add(nexttablemiss);

						} else if (matchprop.getType() == p.getType()) {

							prop.add(matchprop);
						}

						else if (wildecards.getType() == p.getType()) {
							prop.add(wildecards);
						}

						else {
							prop.add(actionMiss);
							prop.add(writeactions);
							prop.add(writeactionsMiss);
							prop.add(applyActions);
							prop.add(missInstruc);
							prop.add(Inst);
						}

					}

					TCAMTable1 = TCAMTable1.createBuilder().setName("Custom TCAM Table1").build().createBuilder()
							.setTableId(TableId.of(0)).build().createBuilder().setMaxEntries(1500).build()
							.createBuilder().setProperties(prop).build();

					OFTableFeatures TCAMTable2 = factory.buildTableFeatures().build().createBuilder()
							.setName("Custom TCAM Table2").build().createBuilder().setTableId(TableId.of(1)).build()
							.createBuilder().setConfig(0).build().createBuilder().setMaxEntries(100).build()
							.createBuilder().setProperties(prop1).build();

					System.out.println(TCAMTable1);
					System.out.println("Taaablllee");
					System.out.println(TCAMTable2);

					tables.add(TCAMTable1);
					tables.add(TCAMTable2);
					req = sw.getOFFactory().buildTableFeaturesStatsRequest().setEntries(tables).build();
				}

			}

			future = sw.writeStatsRequest(req);
			values = (List<OFTableFeaturesStatsReply>) future.get(10, TimeUnit.SECONDS);

		} catch (Exception e) {
			log.error("Failure retrieving statistics from switch " + sw, e);
			System.out.println(e.getMessage());
		}

	}

	private boolean createAppMeters(DatapathId switchId) {
		/*
		 * Create only per-application high-low meters without the totalapp
		 * meter
		 */
		try {

			// Select one of these two calls depending on the experiment
			
			createSmartDropAppMeters(switchId);
			//createTailDropAppMeters(switchId);

		} catch (Exception e) {
			System.out.print(e.toString());
			return false;
		}
		return true;

	}

	void createSmartDropAppMeters(DatapathId switchId) {
		/* Smart Drop experiment, should set initial DSCP=56 */
		OFSwitch sw = (OFSwitch) switchService.getSwitch(switchId);

		createDSCPMeter(sw, METERS.HIGH_PRIOIRTY_METER.meterid, (long) (totalIoTAppRate*2));
		createDSCPMeter(sw, METERS.LOW_PRIOIRTY_METER.meterid,
				(long) (totalIoTAppRate - (totalIoTAppRate * iotrateSplit)));
		//createDSCPMeter(sw, METERS.TOTAL_IOT_METER.meterid, totalRate);

		//createDSCPMeter(sw, METERS.CROSS_TRAFFIC_TOTAL_METER.meterid, (totalRate - totalIoTAppRate));
	}

	void createTailDropAppMeters(DatapathId switchId) {
		/*
		 * TailDrop experiment, create a single total meter. other meters will
		 * be dummy
		 */
		OFSwitch sw = (OFSwitch) switchService.getSwitch(switchId);
		createDSCPMeter(sw, METERS.HIGH_PRIOIRTY_METER.meterid, (long) (totalRate*2));
		createDSCPMeter(sw, METERS.LOW_PRIOIRTY_METER.meterid, (long) (totalRate*2));
		//createDSCPMeter(sw, METERS.TOTAL_IOT_METER.meterid, (long) (totalIoTAppRate));

		//createDSCPMeter(sw, METERS.CROSS_TRAFFIC_TOTAL_METER.meterid, (totalRate ));

	}

	private boolean createDSCPMeter(OFSwitch sw, int meterID, long rate) {
		try {
			OFFactory meterFactory = OFFactories.getFactory(OFVersion.OF_13);

			OFMeterMod.Builder meterModBuilder = meterFactory.buildMeterMod().setMeterId(meterID).setCommand(0);
			// Calculate the rate minus the burst value so meters will mark all
			// packets with burst=0
			// then, set the rate in pps

			rate = (long) (rate - (rate * defualtBurstPer));
			rate = (long) (rate / pktSizeKb);
			OFMeterBandDscpRemark.Builder bandBuilder = meterFactory.meterBands().buildDscpRemark().setRate(rate);

			OFMeterBand band = bandBuilder.build();
			List<OFMeterBand> bands = new ArrayList<OFMeterBand>();
			bands.add(band);
			meterModBuilder.setMeters(bands).setFlags(0x0002)// pps
					.build();

			sw.write(meterModBuilder.build());
		} catch (Exception e) {
			System.out.print(e.toString());
			return false;
		}
		return true;
	}

	

	private boolean createDSCPMeterTwoBands(OFSwitch sw, int meterID, long rateLower, long rateUpper) {
		try {
			OFFactory meterFactory = OFFactories.getFactory(OFVersion.OF_13);

			OFMeterMod.Builder meterModBuilder = meterFactory.buildMeterMod().setMeterId(meterID).setCommand(0);
			// Calculate the rate minus the burst value so meters will mark all
			// packets with burst=0
			// then, set the rate in pps
			// for the standard pipeline the prec-level is used range (0-7), in
			// the custom it is ignored
			// So I set it here to take effect in the standard pipeline

			rateLower = (long) (rateLower - (rateLower * defualtBurstPer));
			rateLower = (long) (rateLower / pktSizeKb);
			OFMeterBandDscpRemark.Builder bandBuilder1 = meterFactory.meterBands().buildDscpRemark().setRate(rateLower)
					.setPrecLevel((short) 5);

			OFMeterBand band1 = bandBuilder1.build();

			rateUpper = (long) (rateUpper - (rateUpper * defualtBurstPer));
			rateUpper = (long) (rateUpper / pktSizeKb);

			OFMeterBandDscpRemark.Builder bandBuilder2 = meterFactory.meterBands().buildDscpRemark().setRate(rateUpper)
					.setPrecLevel((short) 0);

			OFMeterBand band2 = bandBuilder2.build();

			List<OFMeterBand> bands = new ArrayList<OFMeterBand>();

			bands.add(band1);
			bands.add(band2);

			meterModBuilder.setMeters(bands).setFlags(0x0002)// pps
					.build();

			sw.write(meterModBuilder.build());
		} catch (Exception e) {
			System.out.print(e.toString());
			return false;
		}
		return true;
	}

	private boolean createDefaultEntriesCustomPipeline(DatapathId switchId) {
		/*
		 * Creates 3 default entries Table 0 --> Table 1 Table 1 --> controller
		 */
		OFSwitch sw = (OFSwitch) switchService.getSwitch(switchId);
		OFFactory factory = sw.getOFFactory();

		OFInstruction inst = factory.instructions().gotoTable(TableId.of(1));

		ArrayList<OFAction> actions_1 = new ArrayList<OFAction>(1);
		actions_1.add(factory.actions().output(OFPort.CONTROLLER, 0xffFFffFF));

		ArrayList<OFMessage> flows = new ArrayList<OFMessage>();

		OFFlowDeleteStrict deleteFlow = factory.buildFlowDeleteStrict().setTableId(TableId.ALL).build();
		sw.write(deleteFlow);

		OFFlowAdd defaultFlow_0 = factory.buildFlowAdd().setTableId(TableId.of(0)).setPriority(0)
				.setInstructions(Collections.singletonList((OFInstruction) inst)).build();
		flows.add(defaultFlow_0);

		OFFlowAdd defaultFlow_1 = factory.buildFlowAdd().setTableId(TableId.of(1)).setPriority(0)
				.setInstructions(Collections.singletonList(
						(OFInstruction) factory.instructions().buildApplyActions().setActions(actions_1).build()))
				.build();
		flows.add(defaultFlow_1);

		sw.write(flows);
		return true;
	}

	


	

	private boolean createDefaultStandredPipeline2920Switch(DatapathId switchId) {
		/*
		 * I cannot create flow entries in table 0 in the 2920switch So I should
		 * keep the default flow entry in table 0 and only install the flow
		 * entry in table 100
		 */
		OFSwitch sw = (OFSwitch) switchService.getSwitch(switchId);
		OFFactory factory = sw.getOFFactory();

		ArrayList<OFAction> actions_1 = new ArrayList<OFAction>(1);
		actions_1.add(factory.actions().output(OFPort.CONTROLLER, 0xffFFffFF));

		ArrayList<OFMessage> flows = new ArrayList<OFMessage>();

		OFFlowAdd defaultFlow_1 = factory.buildFlowAdd().setTableId(TableId.of(100)).setPriority(0)
				.setInstructions(Collections.singletonList(
						(OFInstruction) factory.instructions().buildApplyActions().setActions(actions_1).build()))
				.build();
		flows.add(defaultFlow_1);

		sw.write(flows);
		return true;
	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
	}

	@Override
	public void switchChanged(DatapathId switchId) {
	}

	/*
	 * Overridden IOFMessageListener's receive() function.
	 */
	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		try {
			switch (msg.getType()) {
			case PACKET_IN:
				
				/* Retrieve the deserialized packet in message */
				Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
						IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

				/* Various getters and setters are exposed in Ethernet */
				MacAddress srcMac = eth.getSourceMACAddress();
				VlanVid vlanId = VlanVid.ofVlan(eth.getVlanID());
				System.out.println("Inside PACKET_IN " + sw.getId()+"eth.getEtherType() "+eth.getEtherType());
				/*
				 * Check the ethertype of the Ethernet frame and retrieve the
				 * appropriate payload. Note the shallow equality check. EthType
				 * caches and reuses instances for valid types.
				 */
				if (eth.getEtherType() == EthType.IPv4) {
					System.out.println("Inside IPv4");
					/* We got an IPv4 packet; get the payload from Ethernet */
					IPv4 ipv4 = (IPv4) eth.getPayload();

					/* Various getters and setters are exposed in IPv4 */
					byte[] ipOptions = ipv4.getOptions();
					IPv4Address dstIp = ipv4.getDestinationAddress();

					if (ipv4.getProtocol().equals(IpProtocol.UDP)) {
						/* We got a UDP packet; get the payload from IPv4 */
						UDP udp = (UDP) ipv4.getPayload();

						/* Various getters and setters are exposed in UDP */
						TransportPort srcPort = udp.getSourcePort();
						TransportPort dstPort = udp.getDestinationPort();

						if ((udp.getPayload() instanceof IOT)) {
							System.out.println("Inside IoT");
							/*
							 * I should find the route and cache it in a list
							 * All this logic copied from forwarding.java
							 */

							IDevice dstDevice = IDeviceService.fcStore.get(cntx, IDeviceService.CONTEXT_DST_DEVICE);
							if (dstDevice != null) {
								SwitchPort[] dstDaps = dstDevice.getAttachmentPoints();
								SwitchPort dstDap = null;
								for (SwitchPort ap : dstDaps) {
									if (topologyService.isEdge(ap.getSwitchDPID(), ap.getPort())) {
										dstDap = ap;
										break;
									}
								}
								// hard coded to only save routes that come form
								// the core switches, should be removed later
								// on!
								System.out.println(sw.getId());
								// if
								// (sw.getId().equals(DatapathId.of("00:00:00:00:00:00:00:01"))
								// ||
								// sw.getId().equals(DatapathId.of("00:00:00:00:00:00:00:02"))
								// ||
								// sw.getId().equals(DatapathId.of("00:00:00:00:00:00:00:03"))
								// ||
								// sw.getId().equals(DatapathId.of("00:00:00:00:00:00:00:04")))
								// {

								OFPacketIn pi = (OFPacketIn) msg;
								OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort()
										: pi.getMatch().get(MatchField.IN_PORT));
								Route route = routingEngineService.getRoute(sw.getId(), inPort, dstDap.getSwitchDPID(),
										dstDap.getPort(), U64.of(0)); // cookie
																		// =
																		// 0,
																		// i.e.,
																		// default
																		// route

								if (route != null) {
									System.out.println("pushRoute route={} " + route.getPath().toString());
									routes.add(route);
								}

								// }
								// Now that I have the routes I should install
								// the Total Meter entry in table 1
								// I am still assuming one IoT APP
								// I need a better way to identify unique routes
								// Needs Testing

								// if
								// (sw.getId().equals(DatapathId.of(S1_ofInst1)))
								// {
								System.out.println("*********************GOT an IOT PKT FROM " + sw.getId());
								createTotalIoTMeterEntry(sw.getId(), route, dstIp);
								createTotalCrossTrafficMeterEntry(sw.getId(), route, dstIp);
								// }
							} else {
								System.out.println("Destination unknown.");
								// should flood
							}
						}

					}
				}
				break;
			default:
				break;
			}

			return Command.CONTINUE;
		} catch (Exception ex) {
			System.out.println(ex.toString());

			return null;
		}

	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return (type.equals(OFType.PACKET_IN) && (name.equals("topology") || name.equals("devicemanager")));

	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public static String asciiToHex(String asciiValue) {
		char[] chars = asciiValue.toCharArray();
		StringBuffer hex = new StringBuffer();
		for (int i = 0; i < chars.length; i++) {
			hex.append(Integer.toHexString((int) chars[i]));
		}
		return hex.toString();
	}

	/*
	 * Which app IP, which total meter
	 */
	private boolean createTotalIoTMeterEntry(DatapathId switchDPID, Route IoTroute, IPv4Address dstIP) {
		/* Table 1 --> Total Meter --> Output port */
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_13);

		// IoT total meter
		Match match = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IPV4_DST, dstIP).setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort)).build();

		OFInstructionMeter meter = myFactory.instructions().buildMeter().setMeterId(METERS.TOTAL_IOT_METER.meterid)
				.build();

		OFFlowMod.Builder fmb = myFactory.buildFlowAdd();
		// for (Route route : IoTroutes) {

		List<NodePortTuple> switchPortList = IoTroute.getPath();
		for (int indx = switchPortList.size() - 1; indx > 0; indx -= 2) {

			DatapathId switchDPID1 = switchPortList.get(indx).getNodeId();
			System.out.println("*********************Node in path " + switchDPID1);
			IOFSwitch sw = switchService.getSwitch(switchDPID1);
			if (sw == null) {
				if (log.isWarnEnabled()) {
					log.warn("Unable to push route, switch at DPID {} " + "not available", switchDPID1);
				}
				return false;
			}

			OFActionOutput.Builder aob = sw.getOFFactory().actions().buildOutput();
			Match.Builder mb = MatchUtils.convertToVersion(match, sw.getOFFactory().getVersion());

			OFPort outPort = switchPortList.get(indx).getPortId();
			OFPort inPort = switchPortList.get(indx - 1).getPortId();
			mb.setExact(MatchField.IN_PORT, inPort);
			aob.setPort(outPort);

			ArrayList<OFAction> actionList = new ArrayList<OFAction>();
			actionList.add(aob.build());
			OFInstructionApplyActions applyActions = myFactory.instructions().buildApplyActions().setActions(actionList)
					.build();

			ArrayList<OFInstruction> instlist = new ArrayList<OFInstruction>();
			if (switchDPID1.equals(DatapathId.of(S1))||switchDPID1.equals(DatapathId.of(S2))) {
				// add metering action
				instlist.add(meter);
			}
			instlist.add(applyActions);

			fmb.setMatch(mb.build()).setIdleTimeout(0).setTableId(TableId.of(1))
					.setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT).setBufferId(OFBufferId.NO_BUFFER).setOutPort(outPort)
					.setPriority(SPECIFIC_ENTRY_PRIORITY).setInstructions(instlist);

			sw.write(fmb.build());
		}
		// pushRoute(route, m, null, null, U64.of(0), null, false,
		// OFFlowModCommand.ADD, LOW_PRIOIRTY_QUEUE,
		// entry_priority, IOT_IDLE_TIMEOUT);
		// I have the route should I just get the output port ?
		// }
		return true;
	}

	private boolean createTotalCrossTrafficMeterEntry(DatapathId switchDPID, Route IoTroute, IPv4Address dstIP) {
		/* Table 1 --> Total Meter --> Output port */
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_13);

		// IoT total meter
		Match match = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IPV4_DST, dstIP).build();

		OFInstructionMeter meter = myFactory.instructions().buildMeter()
				.setMeterId(METERS.CROSS_TRAFFIC_TOTAL_METER.meterid).build();

		OFFlowMod.Builder fmb = myFactory.buildFlowAdd();
		// for (Route route : IoTroutes) {

		List<NodePortTuple> switchPortList = IoTroute.getPath();
		for (int indx = switchPortList.size() - 1; indx > 0; indx -= 2) {

			// DatapathId switchDPID = switchPortList.get(indx).getNodeId();
			IOFSwitch sw = switchService.getSwitch(switchDPID);
			if (sw == null) {
				if (log.isWarnEnabled()) {
					log.warn("Unable to push route, switch at DPID {} " + "not available", switchDPID);
				}
				return false;
			}

			OFActionOutput.Builder aob = sw.getOFFactory().actions().buildOutput();
			Match.Builder mb = MatchUtils.convertToVersion(match, sw.getOFFactory().getVersion());

			OFPort outPort = switchPortList.get(indx).getPortId();
			OFPort inPort = switchPortList.get(indx - 1).getPortId();
			mb.setExact(MatchField.IN_PORT, inPort);
			aob.setPort(outPort);

			ArrayList<OFAction> actionList = new ArrayList<OFAction>();
			actionList.add(aob.build());
			OFInstructionApplyActions applyActions = myFactory.instructions().buildApplyActions().setActions(actionList)
					.build();

			ArrayList<OFInstruction> instlist = new ArrayList<OFInstruction>();
			instlist.add(meter);
			instlist.add(applyActions);

			fmb.setMatch(mb.build()).setIdleTimeout(0).setTableId(TableId.of(1))
					.setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT).setBufferId(OFBufferId.NO_BUFFER).setOutPort(outPort)
					.setPriority(CROSS_TARFFIC_ENTRY_PRIORITY).setInstructions(instlist);

			sw.write(fmb.build());
		}
		// pushRoute(route, m, null, null, U64.of(0), null, false,
		// OFFlowModCommand.ADD, LOW_PRIOIRTY_QUEUE,
		// entry_priority, IOT_IDLE_TIMEOUT);
		// I have the route should I just get the output port ?
		// }
		return true;
	}

	private boolean createLowPrioEntry(String dstIP, String iotzone, int granlevel)

	{ /*
		 * I need to create a flow entry in table 0 that directs packets into
		 * the low meter and then to table 1.
		 */
		try {

			OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_13);
			int entry_priority = 0;
			Set<DatapathId> switchesIDs = this.switchService.getAllSwitchDpids();
			Match m;
			if (iotzone.length() == 0) {
				// create a general entry into the low queue
				m = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
						.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
						.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort)).build();
				entry_priority = GENERAL_ENTRY_PRIORITY;
			} else {
				// create a specific entry of IoT region into low priority queue

				String fulladdrHex = asciiToHex(iotzone);
				String addr1hex = fulladdrHex.substring(0, Math.min(fulladdrHex.length(), 16));
				String addr2hex = fulladdrHex.substring(16, Math.min(fulladdrHex.length(), 32));
				long iotaddr1 = Long.parseLong(addr1hex, 16);
				long iotaddr2 = Long.parseLong(addr2hex, 16);
				IoTAddrMask mask = new IoTAddrMask();
				getIoTaddMask(granlevel, mask);

				m = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
						// .setExact(MatchField.IPV4_DST, IPv4Address.of(dstIP))
						.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
						.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort))
						.setMasked(MatchField.IOT_ADDR,
								Masked.of(U128.of(iotaddr1, iotaddr2),
										U128.of(mask.iot_add_mask_part_1, mask.iot_add_mask_part_2)))
						// .setExact(MatchField.IOT_ADDR,U128.of(iotaddr1,
						// iotaddr2))
						.build();

				entry_priority = SPECIFIC_ENTRY_PRIORITY;
			}
			// for (Route route : IoTroutes) {
			// pushRoute(route, m, null, null, U64.of(0), null, false,
			// OFFlowModCommand.ADD, LOW_PRIOIRTY_QUEUE,
			// entry_priority, IOT_IDLE_TIMEOUT);
			// }

			OFInstructionMeter meter = myFactory.instructions().buildMeter()
					// .setMeterId(METERS.LOW_PRIOIRTY_METER.meterid).build();
					.setMeterId(METERS.LOW_PRIOIRTY_METER.meterid).build();

			OFInstructionGotoTable gotoTable = myFactory.instructions().gotoTable(TableId.of(1));
			ArrayList<OFInstruction> instlist = new ArrayList<OFInstruction>();
			instlist.add(meter);
			instlist.add(gotoTable);
			ArrayList<OFMessage> flows = new ArrayList<OFMessage>();
			OFFlowAdd flow = myFactory.buildFlowAdd().setTableId(TableId.of(0)).setPriority(entry_priority).setMatch(m)
					.setInstructions(instlist).build();
			flows.add(flow);
			//System.out.println(flows);
			IOFSwitch s;
			// for (DatapathId id : switchesIDs) {
			// System.out.println(id.toString());
			s = switchService.getSwitch(DatapathId.of(S1));
			s.write(flows);
			s = switchService.getSwitch(DatapathId.of(S2));
			s.write(flows);
			// }
			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}

	private boolean createHighPrioEntry(String dstIP, String iotzone, int granlevel) {

		try {

			OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_13);
			Set<DatapathId> switchesIDs = this.switchService.getAllSwitchDpids();
			int entry_priority = 0;
			Match m;
			if (iotzone.length() == 0) {
				m = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
						.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
						.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort)).build();
				entry_priority = GENERAL_ENTRY_PRIORITY;
			} else {
				String fulladdrHex = asciiToHex(iotzone);
				String addr1hex = fulladdrHex.substring(0, Math.min(fulladdrHex.length(), 16));
				String addr2hex = fulladdrHex.substring(16, Math.min(fulladdrHex.length(), 32));
				long iotaddr1 = Long.parseLong(addr1hex, 16);
				long iotaddr2 = Long.parseLong(addr2hex, 16);
				IoTAddrMask mask = new IoTAddrMask();
				getIoTaddMask(granlevel, mask);

				m = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
						// .setExact(MatchField.IPV4_DST, IPv4Address.of(dstIP))
						.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
						.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort))
						.setMasked(MatchField.IOT_ADDR,
								Masked.of(U128.of(iotaddr1, iotaddr2),
										U128.of(mask.iot_add_mask_part_1, mask.iot_add_mask_part_2)))
						// .setExact(MatchField.IOT_ADDR,U128.of(iotaddr1,
						// iotaddr2))

						.build();
				entry_priority = SPECIFIC_ENTRY_PRIORITY;
			}

			// for (Route route : IoTroutes) {
			// pushRoute(route, m, null, null, U64.of(0), null, false,
			// OFFlowModCommand.ADD, HIGH_PRIOIRTY_QUEUE,
			// entry_priority, IOT_IDLE_TIMEOUT);
			// }
			OFInstructionMeter meter = myFactory.instructions().buildMeter()
					.setMeterId(METERS.HIGH_PRIOIRTY_METER.meterid).build();
			OFInstructionGotoTable gotoTable = myFactory.instructions().gotoTable(TableId.of(1));

			ArrayList<OFInstruction> instlist = new ArrayList<OFInstruction>();
			instlist.add(meter);
			instlist.add(gotoTable);

			ArrayList<OFMessage> flows = new ArrayList<OFMessage>();
			OFFlowAdd flow = myFactory.buildFlowAdd().setTableId(TableId.of(0)).setPriority(entry_priority)
					.setInstructions(instlist).setMatch(m).build();
			flows.add(flow);

			IOFSwitch s;
			// for (DatapathId id : switchesIDs) {
			// System.out.println(id.toString());
			s = switchService.getSwitch(DatapathId.of(S1));
			s.write(flows);
			s = switchService.getSwitch(DatapathId.of(S2));
			s.write(flows);

			// }
			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}

	}

	private boolean createMidPrioEntry(String dstIP, String iotzone, int granlevel) {

		try {

			OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_13);
			Set<DatapathId> switchesIDs = this.switchService.getAllSwitchDpids();
			int entry_priority = 0;
			Match m;

			String fulladdrHex = asciiToHex(iotzone);
			String addr1hex = fulladdrHex.substring(0, Math.min(fulladdrHex.length(), 16));
			String addr2hex = fulladdrHex.substring(16, Math.min(fulladdrHex.length(), 32));
			long iotaddr1 = Long.parseLong(addr1hex, 16);
			long iotaddr2 = Long.parseLong(addr2hex, 16);
			IoTAddrMask mask = new IoTAddrMask();
			getIoTaddMask(granlevel, mask);

			m = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
					// .setExact(MatchField.IPV4_DST, IPv4Address.of(dstIP))
					.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
					.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort))
					.setMasked(MatchField.IOT_ADDR,
							Masked.of(U128.of(iotaddr1, iotaddr2),
									U128.of(mask.iot_add_mask_part_1, mask.iot_add_mask_part_2)))
					// .setExact(MatchField.IOT_ADDR,U128.of(iotaddr1,
					// iotaddr2))

					.build();
			entry_priority = MID_ENTRY_PRIORITY;

			// for (Route route : IoTroutes) {
			// pushRoute(route, m, null, null, U64.of(0), null, false,
			// OFFlowModCommand.ADD, HIGH_PRIOIRTY_QUEUE,
			// entry_priority, IOT_IDLE_TIMEOUT);
			// }
			OFInstructionMeter meter = myFactory.instructions().buildMeter()
					.setMeterId(METERS.HIGH_PRIOIRTY_METER.meterid).build();
			OFInstructionGotoTable gotoTable = myFactory.instructions().gotoTable(TableId.of(1));

			ArrayList<OFInstruction> instlist = new ArrayList<OFInstruction>();
			instlist.add(meter);
			instlist.add(gotoTable);

			ArrayList<OFMessage> flows = new ArrayList<OFMessage>();
			OFFlowAdd flow = myFactory.buildFlowAdd().setTableId(TableId.of(0)).setPriority(entry_priority)
					.setInstructions(instlist).setMatch(m).build();
			flows.add(flow);

			IOFSwitch s;
			// for (DatapathId id : switchesIDs) {
			// System.out.println(id.toString());
			s = switchService.getSwitch(DatapathId.of(S1));
			s.write(flows);
			s = switchService.getSwitch(DatapathId.of(S2));
			s.write(flows);

			// }
			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}

	}

	@Override
	public boolean DeleteIoTEntries(String dstIP) {
		// Delete previous entries in order to add the new ones
		// Delete all entries matching UDP, 9999, and ServerIP

		try {
			Set<DatapathId> switchesIDs = this.switchService.getAllSwitchDpids();
			OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_13);
			OFFlowMod.Builder fmb;
			fmb = myFactory.buildFlowDelete();

			Match myMatch = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
					// .setExact(MatchField.IPV4_DST, IPv4Address.of(dstIP))
					.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
					.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort)).build();

			OFFlowDelete flowdelete = (OFFlowDelete) fmb.setBufferId(OFBufferId.NO_BUFFER).setHardTimeout(0)
					.setIdleTimeout(0).setPriority(GENERAL_ENTRY_PRIORITY).setMatch(myMatch).setTableId(TableId.of(0))
					.build();
			//System.out.println(flowdelete.toString());

			IOFSwitch s;
			// for (DatapathId id : switchesIDs) {
			// System.out.println(id.toString());
			s = switchService.getSwitch(DatapathId.of(S1));
			s.write(flowdelete);
			s = switchService.getSwitch(DatapathId.of(S2));
			s.write(flowdelete);
			// }

			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}

	}

	public boolean DeleteIoTEntry(String dstIP, String iotzone, int granlevel) {
		// Delete previous entries in order to add the new ones
		// Delete all entries matching UDP, 9999, and ServerIP
		try {

			Set<DatapathId> switchesIDs = this.switchService.getAllSwitchDpids();
			OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_13);
			OFFlowMod.Builder fmb;
			fmb = myFactory.buildFlowDelete();

			String fulladdrHex = asciiToHex(iotzone);
			String addr1hex = fulladdrHex.substring(0, Math.min(fulladdrHex.length(), 16));
			String addr2hex = fulladdrHex.substring(16, Math.min(fulladdrHex.length(), 32));
			long iotaddr1 = Long.parseLong(addr1hex, 16);
			long iotaddr2 = Long.parseLong(addr2hex, 16);

			IoTAddrMask mask = new IoTAddrMask();
			getIoTaddMask(granlevel, mask);

			Match myMatch = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
					// .setExact(MatchField.IPV4_DST, IPv4Address.of(dstIP))
					.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
					.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort))
					.setMasked(MatchField.IOT_ADDR, Masked.of(U128.of(iotaddr1, iotaddr2),
							U128.of(mask.iot_add_mask_part_1, mask.iot_add_mask_part_2)))
					.build();

			OFFlowDelete flowdelete = (OFFlowDelete) fmb.setBufferId(OFBufferId.NO_BUFFER).setHardTimeout(0)
					.setIdleTimeout(0).setPriority(SPECIFIC_ENTRY_PRIORITY).setMatch(myMatch).setTableId(TableId.of(0))
					.build();
			//System.out.println(flowdelete.toString());
			IOFSwitch s;
			// for (DatapathId id : switchesIDs) {
			// System.out.println(id.toString());
			s = switchService.getSwitch(DatapathId.of(S1));
			s.write(flowdelete);
			s = switchService.getSwitch(DatapathId.of(S2));
			s.write(flowdelete);
			// }

			return true;
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}

	}

	@Override
	public boolean CreateHighQosEntries(String SreverIP, Vector<IoTRegion> high) {
		// TODO Auto-generated method stub

		System.out.println("inside CreateHighQosEntries ");
		// Check if list is empty 
		if (high.size() == 0) {
			createHighPrioEntry(SreverIP, "", 0);
		} else {
			System.out.println("inside CreatehighQosEntries ");
			// loop over list of high regions and create high entries for them
			//System.out.println("Cached Number of high entries =" + highcache.size());
			System.out.println("Number of new high entries =" + high.size());
			// take a tempcopy
			Vector<IoTRegion> toAdd = new Vector<IoTRegion>(high);
			Vector<IoTRegion> toDelete = new Vector<IoTRegion>(highcache);
			toAdd.removeAll(highcache);
			toDelete.removeAll(high);
			//System.out.println("Number of high entries to add =" + toAdd.size());
			//System.out.println("Number of high entries to delete =" + toDelete.size());
			highcache = high;
			
			
			
						int flowEntryLimit=10000;
			
						//aggregate regions
						System.out.println("Number of highcache_Agg =" + highcache_Agg.size());
						Vector<IoTRegion> regions_Agg=aggregateRegions(100, high);
						
						
						//remove elements if above limit
						if (regions_Agg.size()>flowEntryLimit)
						{
							System.out.println("Number of regions_Agg =" + regions_Agg.size());
							regions_Agg.subList(flowEntryLimit, regions_Agg.size()).clear();
							System.out.println("Number of regions_Agg After removal=" + regions_Agg.size());
						}
						
						Vector<IoTRegion> intersection_Agg = new Vector<IoTRegion>(regions_Agg);
						intersection_Agg.retainAll(highcache_Agg);
						
						
						
						
						System.out.println("regions_Agg.removeAll(intersection_Agg) " + regions_Agg.removeAll(intersection_Agg));
						System.out.println("highcache_Agg.removeAll(intersection_Agg) " + highcache_Agg.removeAll(intersection_Agg));
						
						Vector<IoTRegion> toAdd_Agg = new Vector<IoTRegion>(regions_Agg);
						Vector<IoTRegion> toDelete_Agg = new Vector<IoTRegion>(highcache_Agg);
						
					
						System.out.println("Number of regions_Agg =" + regions_Agg.size());
						System.out.println("Number of Agg high entries to toAdd_agg =" + toAdd_Agg.size());
						System.out.println("Number of Agg high entries to toDelete_agg=" + toDelete_Agg.size());
						System.out.println("Number of highcache_Agg =" + highcache_Agg.size());
						highcache_Agg = regions_Agg;
			
			for (IoTRegion aentry : toAdd_Agg) {
				createHighPrioEntry(SreverIP, aentry.address, aentry.gran_level);
			}

			for (IoTRegion dentry : toDelete_Agg) {
				DeleteIoTEntry(SreverIP, dentry.address, dentry.gran_level);
			}
		}

		return true;
	}

	@Override
	public boolean CreateLowQosEntries(String dstip, Vector<IoTRegion> low) {
		// TODO Auto-generated method stub

		if (low.size() == 0) {
			// create a general entry for all traffic
			createLowPrioEntry(dstip, "", 0);
		} else {
			System.out.println("inside CreateLowQosEntries ");
			// loop over list of low regions and create low entries for them
			System.out.println("Cached Number of low entries =" + lowcache.size());
			System.out.println("Number of new low entries =" + low.size());
			// take a tempcopy
			Vector<IoTRegion> toAdd = new Vector<IoTRegion>(low);
			Vector<IoTRegion> toDelete = new Vector<IoTRegion>(lowcache);
			toAdd.removeAll(lowcache);
			toDelete.removeAll(low);
			System.out.println("Number of low entries to add =" + toAdd.size());
			System.out.println("Number of low entries to delete =" + toDelete.size());
			lowcache = low;
			for (IoTRegion aentry : toAdd) {
				createLowPrioEntry(dstip, aentry.address, aentry.gran_level);
			}

			for (IoTRegion dentry : toDelete) {
				DeleteIoTEntry(dstip, dentry.address, dentry.gran_level);
			}
		}
		return true;
	}

	@Override
	public boolean CreateMidQosEntries(String dstip, Vector<IoTRegion> mid) {
		if (mid.size() == 0) {
			// do nothing
		} else {
			System.out.println("inside CreateMidQosEntries ");
			// loop over list of low regions and create low entries for them
			//System.out.println("Cached Number of Mid entries =" + midcache.size());
			System.out.println("Number of new Mid entries =" + mid.size());
			// take a tempcopy
			Vector<IoTRegion> toAdd = new Vector<IoTRegion>(mid);
			Vector<IoTRegion> toDelete = new Vector<IoTRegion>(midcache);
			toAdd.removeAll(midcache);
			toDelete.removeAll(mid);
			//System.out.println("Number of Mid entries to add =" + toAdd.size());
			//System.out.println("Number of Mid entries to delete =" + toDelete.size());
			midcache = mid;
			
			
			// Repeat for the S2 upper grid entries 
			
			System.out.println("Cached Number of mid entries S2=" + midcache_S2.size());
			Vector<IoTRegion> upperGridMid = new Vector<IoTRegion>();
			//aggregate regions into the upper grid garnlevl
			for (IoTRegion region:mid)
			{
				
				IoTRegion regionUpper=getRegionUpperGrid(region);
				if (!upperGridMid.contains(regionUpper))
				{
					
					upperGridMid.add(regionUpper);
				}
			}
			upperGridMid.removeAll(highcache_S2);
			System.out.println("Number of new mid entries S2 =" + upperGridMid.size());
			// take a tempcopy
			
			Vector<IoTRegion> toAdd_S2 = new Vector<IoTRegion>(upperGridMid);
			Vector<IoTRegion> toDelete_S2 = new Vector<IoTRegion>(midcache_S2);
			toAdd.removeAll(midcache_S2);
			toDelete.removeAll(upperGridMid);
			//System.out.println("Number of Mid entries to add_S2 =" + toAdd_S2.size());
			//System.out.println("Number of Mid entries to delete_S2=" + toDelete_S2.size());
			midcache_S2 = upperGridMid;
			
			
			
			
			for (IoTRegion aentry : toAdd) {
				createMidPrioEntry(dstip, aentry.address, aentry.gran_level);
			}

			for (IoTRegion dentry : toDelete) {
				DeleteIoTEntry(dstip, dentry.address, dentry.gran_level);
			}
		}
		return true;
	}

	
	private Vector<IoTRegion> aggregateRegions(int agglevel,Vector<IoTRegion> fineGran)
	{
		System.out.println("Inside aggregateRegions");
		int countagg=0;
		Vector<IoTRegion> newRegions=new Vector<IoTRegion>();
		
		ListMultimap<String, IoTRegion> aggrgeateList=ArrayListMultimap.create();
		IoTRegion coarseregion=null;
		for (IoTRegion fineregion: fineGran)
		{
			 
			coarseregion=getRegionUpperGrid(fineregion);
		    aggrgeateList.put(coarseregion.address, fineregion);
		}
	
		for (String keyRegion:aggrgeateList.keySet())
		{
			if (aggrgeateList.get(keyRegion).size()>=agglevel)
			{
				//This region qualified for aggregation, add it to the newRegions, and do not add its list
				newRegions.addElement(new IoTRegion(keyRegion,coarseregion.gran_level));
				countagg++;
				//System.out.println("Region Aggrgated"+keyRegion+" size:"+aggrgeateList.get(keyRegion).size());
			}else
			{
				//this regions did not qualify for aggregation, do not add and add the list of fine grained regions instead
				
				
				//newRegions.addAll(aggrgeateList.get(keyRegion));
				for (IoTRegion r: aggrgeateList.get(keyRegion))
				{
					if(!newRegions.contains(r))
					newRegions.add(r);
				}
					
			}

		}
		
		System.out.println("Number of regions aggregated= "+countagg);
		
		return newRegions;
		
	}
	
	private IoTRegion getRegionUpperGrid(IoTRegion region)
	{
		
		StringBuilder regionStr = new StringBuilder(region.address);
		switch (region.gran_level)
		{
		case 2: // from 10km to 100km
			regionStr.setCharAt(5, '0');
			regionStr.setCharAt(10, '0');
		break;
		case 3: // from 1km to 10km
			regionStr.setCharAt(6, '0');
			regionStr.setCharAt(11, '0');
		break;	
		case 4: //from 100m to 1km 
			regionStr.setCharAt(7, '0');
			regionStr.setCharAt(12, '0');
		break;
			
		}
		
		return new IoTRegion(regionStr.toString(),(region.gran_level-1));
	}
	
	public static void getIoTaddMask(int granLevel, IoTAddrMask mask) {

		switch (granLevel) {

		case 1:// 100km
			mask.iot_add_mask_part_1 = 0xFFFFFFFFFF000000l;
			mask.iot_add_mask_part_2 = 0x0000000000000000l;
			break;
		case 2:// 10km
			mask.iot_add_mask_part_1 = 0xFFFFFFFFFFFF0000l;
			mask.iot_add_mask_part_2 = 0x0000FF0000000000l;
			break;
		case 3:// 1km
			mask.iot_add_mask_part_1 = 0xFFFFFFFFFFFFFF00l;
			mask.iot_add_mask_part_2 = 0x0000FFFF00000000l;
			break;
		case 4:// 100m
			mask.iot_add_mask_part_1 = 0xFFFFFFFFFFFFFFFFl;
			mask.iot_add_mask_part_2 = 0x0000FFFFFF000000l;
			break;
		case 5:// 10m
			mask.iot_add_mask_part_1 = 0xFFFFFFFFFFFFFFFFl;
			mask.iot_add_mask_part_2 = 0xFF00FFFFFFFF0000l;
			break;
		case 6:// 1m
			mask.iot_add_mask_part_1 = 0xFFFFFFFFFFFFFFFFl;
			mask.iot_add_mask_part_2 = 0xFFFFFFFFFFFFFF00l;
			break;
		default: // Exact
			mask.iot_add_mask_part_1 = 0xFFFFFFFFFFFFFFFFl;
			mask.iot_add_mask_part_2 = 0xFFFFFFFFFFFFFFFFl;

		}
	}

	@Override
	public String GetHighQueueDropRate(String ServerIP) {

		return dropRateofS1_Inst3.toString();
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi,
			IRoutingDecision decision, FloodlightContext cntx) {
		// TODO Auto-generated method stub
		return Command.CONTINUE;
	}

	@Override
	public boolean CreateInitialQosEntry(String ServerIP) {
		// create a general entry into the low queue
		System.out.println("Creating init prioirty");
		OFFactory myFactory = OFFactories.getFactory(OFVersion.OF_13);
		Set<DatapathId> switchesIDs = this.switchService.getAllSwitchDpids();
		int entry_priority = 0;
		Match m;
		m = myFactory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.UDP)
				.setExact(MatchField.UDP_DST, TransportPort.of(IOT.IOTServicePort)).build();
		entry_priority = INITIAL_ENTRY_PRIORITY;

		OFInstructionMeter meter = myFactory.instructions().buildMeter().setMeterId(METERS.LOW_PRIOIRTY_METER.meterid)
				.build();
		OFInstructionGotoTable gotoTable = myFactory.instructions().gotoTable(TableId.of(1));

		ArrayList<OFInstruction> instlist = new ArrayList<OFInstruction>();
		instlist.add(meter);
		instlist.add(gotoTable);

		ArrayList<OFMessage> flows = new ArrayList<OFMessage>();
		OFFlowAdd flow = myFactory.buildFlowAdd().setTableId(TableId.of(0)).setPriority(entry_priority)
				.setInstructions(instlist).setMatch(m).build();
		flows.add(flow);

		IOFSwitch s;
		// for (DatapathId id : switchesIDs) {
		// System.out.println(id.toString());
		s = switchService.getSwitch(DatapathId.of(S1));
		s.write(flows);
		s = switchService.getSwitch(DatapathId.of(S2));
		s.write(flows);
		return true;
	}

}
