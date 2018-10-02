package ch.unibe.sdwsn.dynamicrouting.profile;

import org.onosproject.net.SensorNode;

public class DRSensorNode {
    private SensorNode sensorNode;
    private int temperature;
    private int lightPhotosyntetic;
    private int lightSolar;
    private int battery;

    DRSensorNode(SensorNode sensorNode) {
        this.sensorNode = sensorNode;
    }
}
