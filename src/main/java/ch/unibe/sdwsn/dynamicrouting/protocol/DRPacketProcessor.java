package ch.unibe.sdwsn.dynamicrouting.protocol;

import ch.unibe.sdwsn.dynamicrouting.topology.DRLinkWeigher;
import org.apache.felix.scr.annotations.Component;
import org.onlab.osgi.DefaultServiceDirectory;
import org.onlab.packet.Ethernet;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Path;
import org.onosproject.net.SensorNode;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleProvider;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.sensor.SensorNodeService;
import org.onosproject.net.sensor.SensorNodeStore;
import org.onosproject.net.sensorflow.SensorEnabledTrafficSelector;
import org.onosproject.net.sensorflow.SensorEnabledTrafficTreatment;
import org.onosproject.net.sensorflow.SensorTrafficSelector;
import org.onosproject.net.sensorflow.SensorTrafficTreatment;
import org.onosproject.net.sensorpacket.SensorInboundPacket;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Component(immediate = true)
public class DRPacketProcessor implements PacketProcessor {

    private static final int TIMEOUT = 10;
    private static final int PRIORITY = 10;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void process(PacketContext packetContext) {

        if (packetContext.isHandled()) {
            return;
        }

        InboundPacket pkt = packetContext.inPacket();
        Ethernet ethPkt = pkt.parsed();

        if (isControlPacket(ethPkt) || isIpv6Multicast(ethPkt) || ethPkt.isMulticast()) {
            return;
        }

        Path path;

        DeviceId incomingDeviceId = pkt.receivedFrom().deviceId();

        if (isSensorNode(incomingDeviceId)) {
            SensorInboundPacket packet = (SensorInboundPacket) pkt;

            // only calculate routes fore REQUEST packets
            if (!packet.sensorPacketType().typeName().equals("REQUEST")) {
                log.info("THIS WAS NOT A REQUEST PACKET");
                return;
            }

            packetContext.block();
            handleDestinationSensor(packet);

        }
    }

    public String getHexString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++){
            if (i > 0)
                sb.append(':');
            sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private boolean isSink(SensorNode sensorNode) {
        return sensorNode == null ? false : sensorNode.associatedSink().equals(sensorNode);
    }

    private void installRulesOpenPath(SensorNode srcSensorNode, SensorNode dstSensorNode,
                                      Path path, InboundPacket pkt) {
        CoreService coreService = DefaultServiceDirectory.getService(CoreService.class);
        FlowRuleProvider flowRuleProvider = DefaultServiceDirectory.getService(FlowRuleProvider.class);

        SensorTrafficSelector sensorTrafficSelector =
                (SensorTrafficSelector) SensorEnabledTrafficSelector.builder()
                        .matchNodeSrcAddr(srcSensorNode.nodeAddress())
                        .matchNodeDstAddr(dstSensorNode.nodeAddress())
                        .build();
        SensorTrafficTreatment sensorTrafficTreatment = SensorEnabledTrafficTreatment.builder()
                .setOpenPath(path)
                .buildSensorFlow();
        ApplicationId appId = coreService.getAppId("ch.unibe.sdwsndynamicrouting");


        FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(path.src().deviceId())
                .withTreatment(sensorTrafficTreatment)
                .withSelector(sensorTrafficSelector)
                .withPriority(PRIORITY)
                .fromApp(appId)
                .withIdleTimeout(TIMEOUT)
                .build();
        flowRuleProvider.applyFlowRule(flowRule);
        //flowRuleService.applyFlowRules(flowRule);
    }

    private Path handleDestinationSensor(SensorInboundPacket pkt) {
        SensorNodeService sensorNodeService = DefaultServiceDirectory.getService(SensorNodeService.class);
        SensorNodeStore sensorNodeStore = DefaultServiceDirectory.getService(SensorNodeStore.class);
        TopologyService topologyService = DefaultServiceDirectory.getService(TopologyService.class);

        DRLinkWeigher drLinkWeigher = new DRLinkWeigher(sensorNodeService, sensorNodeStore, topologyService);

        DeviceId srcDeviceId, dstDeviceId;

        byte[] packetBytes = pkt.parsed().getPayload().serialize();

        byte[] dstMac = {0,0,0,(byte)pkt.parsed().getVlanID(),packetBytes[5],packetBytes[6]};
        String id = "sdnwise:" + getHexString(dstMac);
        srcDeviceId = pkt.receivedFrom().deviceId();
        dstDeviceId = DeviceId.deviceId(id);

        Set<Path> paths = null;
        paths = topologyService.getPaths(topologyService.currentTopology(), dstDeviceId, srcDeviceId, drLinkWeigher);

        Path path = null;
        if ((paths != null) && (paths.size() > 0)) {
            path = paths.iterator().next();
        }

        SensorNode srcSensorNode = sensorNodeService.getSensorNode(srcDeviceId);
        SensorNode dstSensorNode = sensorNodeService.getSensorNode(dstDeviceId);
        installRulesOpenPath(srcSensorNode, dstSensorNode, path, pkt);
        return path;
    }

    // Indicates whether this is a control packet, e.g. LLDP, BDDP
    private boolean isControlPacket(Ethernet eth) {
        short type = eth.getEtherType();
        return type == Ethernet.TYPE_LLDP || type == Ethernet.TYPE_BSN;
    }

    // Indicated whether this is an IPv6 multicast packet.
    private boolean isIpv6Multicast(Ethernet eth) {
        return eth.getEtherType() == Ethernet.TYPE_IPV6 && eth.isMulticast();
    }

    private boolean isSensorNode(DeviceId deviceId) {
        SensorNodeService sensorNodeService = DefaultServiceDirectory.getService(SensorNodeService.class);
        SensorNode node = sensorNodeService.getSensorNode(deviceId);

        return node == null ? false : true;
    }
}
