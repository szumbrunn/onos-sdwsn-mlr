package ch.unibe.sdwsn.dynamicrouting.protocol;


import org.apache.felix.scr.annotations.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.sensor.SensorNodeService;
import org.onosproject.net.sensor.SensorNodeStore;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class DRForwarding {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected SensorNodeService sensorNodeService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected SensorNodeStore sensorNodeStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    private ApplicationId appId;
    PacketProcessor packetProcessor = new DRPacketProcessor();

    @Activate
    public void activate() {
        appId = coreService.registerApplication("ch.unibe.cds.dynamicrouting");
        packetService.addProcessor(packetProcessor, 11);
        log.info("Started Application with ID {}", appId.id());
    }

    @Deactivate
    public void deactivate() {
        packetService.removeProcessor(packetProcessor);
        packetProcessor = null;
        log.info("Stopped Application with ID {}", appId.id());
    }

}
