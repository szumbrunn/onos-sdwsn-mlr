package ch.unibe.sdwsn.dynamicrouting.models;

// The node model used to present it in the java
public class SensorNodeInfo {
    private final String id;
    private final String sinkId;
    private final float battery;
    private final double temperature;
    private final double humidity;
    private final double light1;
    private final double light2;

    public SensorNodeInfo(String id, String sinkId, float battery,
                   double temperature, double humidity, double light1, double light2) {
        this.id = id;
        this.sinkId = sinkId;
        this.battery = battery;
        this.temperature = temperature;
        this.humidity = humidity;
        this.light1 = light1;
        this.light2 = light2;
    }

    public String getId() {
        return id;
    }

    public String getSinkId() {
        return sinkId;
    }

    public float getBattery() {
        return battery;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getLight1() {
        return light1;
    }

    public double getLight2() {
        return light2;
    }

}