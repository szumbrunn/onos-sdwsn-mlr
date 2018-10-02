package ch.unibe.sdwsn.dynamicrouting.topology;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.onlab.graph.Weight;
import org.onosproject.net.DeviceId;
import org.onosproject.net.SensorNode;
import org.onosproject.net.SensorNodeNeighbor;
import org.onosproject.net.sensor.SensorNodeService;
import org.onosproject.net.sensor.SensorNodeStore;
import org.onosproject.net.topology.*;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class DRLinkWeigher implements LinkWeigher {

    private final Logger log = getLogger(getClass());

    private static final double ALPHA = 0.1;
    private static final double BETA = 0.5;
    private static final double RSSI_THRESHOLD = 20;

    SensorNodeService sensorNodeService;
    SensorNodeStore sensorNodeStore;
    TopologyService topologyService;

    public DRLinkWeigher(SensorNodeService sensorNodeService, SensorNodeStore sensorNodeStore,
                         TopologyService topologyService) {
        this.sensorNodeService = sensorNodeService;
        this.sensorNodeStore = sensorNodeStore;
        this.topologyService = topologyService;
    }

    /**
     * Calculates the weight of the topology edge
     * @param topologyEdge
     * @return weight of the topology edge
     */
    @Override
    public Weight weight(TopologyEdge topologyEdge) {

        DeviceId srcNodeId = topologyEdge.link().src().deviceId();
        DeviceId dstNodeId = topologyEdge.link().dst().deviceId();

        topologyEdge.link().annotations().keys().stream().forEach(key -> log.info(key));


        SensorNode srcNode = sensorNodeService.getSensorNode(srcNodeId);
        SensorNode dstNode = sensorNodeService.getSensorNode(dstNodeId);
        int RSSI = sensorNodeStore.getSensorNodeNeighbors(srcNode.id()).get(dstNode.id()).getRssi();

        double trafficOnLink = (sensorNodeStore.getSensorNodeNeighbors(srcNode.id())
                    .get(dstNode.id()).getRxCount()
                + sensorNodeStore.getSensorNodeNeighbors(srcNode.id())
                    .get(dstNode.id()).getTxCount()
                + sensorNodeStore.getSensorNodeNeighbors(dstNode.id())
                    .get(srcNode.id()).getRxCount()
                + sensorNodeStore.getSensorNodeNeighbors(dstNode.id()).get(srcNode.id()).getTxCount()
            )/2.0;

        int overallTraffic = overallTraffic();

        double edgeBetweennessCentrality = getEdgeCentralityBetweenness(srcNode, dstNode);

        double cost = ALPHA + BETA*trafficOnLink/overallTraffic + (1-BETA) * edgeBetweennessCentrality;

        return new DRLinkWeight(cost);
    }

    /**
     * Calculates the edge centralityBetweenness as avarage of the two corresponding nodes
     * @param src node
     * @param dst node
     * @return edgeCentralityBetweenness
     */
    public double getEdgeCentralityBetweenness(SensorNode src, SensorNode dst) {

        TopologyGraph topologyGraph = this.topologyService
                .getGraph(this.topologyService.currentTopology());
        Graph graph = new SingleGraph("TopologyGraph");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        for(TopologyVertex node : topologyGraph.getVertexes()) {
            graph.addNode(node.deviceId().toString());
        }

        for(TopologyEdge link : topologyGraph.getEdges()) {
            String edgenName = link.src().deviceId().toString() + " - " + link.dst().deviceId().toString();
            graph.addEdge(edgenName,
                    link.src().deviceId().toString(),
                    link.dst().deviceId().toString(), false);
        }

        BetweennessCentrality bcb = new BetweennessCentrality();
        bcb.setUnweighted();
        bcb.init(graph);
        bcb.compute();

        double centrality = 0;
        for(Node node : graph.getEachNode()) {
            centrality += (double)node.getAttribute("Cb");
        }

        Node a = graph.getNode(src.deviceId().toString());
        Node b = graph.getNode(dst.deviceId().toString());
        return ((double)a.getAttribute("Cb") + (double)b.getAttribute("Cb"))/(2*centrality);
    }

    /**
     * Returns the number of sent packets in the last period
     * @return overallTraffic
     */
    public int overallTraffic() {
        int traffic = 0;
        for(SensorNode srcNode : sensorNodeStore.getSensorNodes()) {
            for(SensorNodeNeighbor neighbor : sensorNodeStore
                    .getSensorNodeNeighbors(srcNode.id()).values()){
                traffic += neighbor.getRxCount() + neighbor.getTxCount();
            }
        }
        return traffic;
    }

    /**
     *
     * @return initial weight
     */
    @Override
    public Weight getInitialWeight() {
        return new DRLinkWeight(this.ALPHA);
    }

    @Override
    public Weight getNonViableWeight() {
        return null;
    }
}
