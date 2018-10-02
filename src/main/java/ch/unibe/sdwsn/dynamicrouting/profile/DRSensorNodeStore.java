package ch.unibe.sdwsn.dynamicrouting.profile;

import java.util.ArrayList;

public class DRSensorNodeStore {
    private static DRSensorNodeStore drSensorNodeStore = null;
    private static ArrayList<DRSensorNode> drSensorNodes;

    protected DRSensorNodeStore() {
        drSensorNodes = new ArrayList<DRSensorNode>();
    }
    public static DRSensorNodeStore getInstance() {
        if (drSensorNodeStore == null) {
            drSensorNodeStore = new DRSensorNodeStore();
        }
        return drSensorNodeStore;
    }
}
