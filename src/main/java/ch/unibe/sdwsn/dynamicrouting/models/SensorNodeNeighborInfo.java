package ch.unibe.sdwsn.dynamicrouting.models;

import org.onosproject.net.SensorNodeId;
import org.onosproject.net.SensorNodeNeighbor;

public class SensorNodeNeighborInfo extends SensorNodeNeighbor {
    String sensorNodeId;

    public SensorNodeNeighborInfo(SensorNodeId sensorNodeId, SensorNodeNeighbor sensorNodeNeighbor) {
        super(sensorNodeNeighbor.getRssi(), sensorNodeNeighbor.getRxCount(), sensorNodeNeighbor.getTxCount());
        this.sensorNodeId = sensorNodeId.toString();
    }

    public String getSensorNodeId() {
        return sensorNodeId;
    }

    public void setSensorNodeId(String sensorNodeId) {
        this.sensorNodeId = sensorNodeId;
    }

}