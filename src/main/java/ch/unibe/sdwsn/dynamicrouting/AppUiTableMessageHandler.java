/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.unibe.sdwsn.dynamicrouting;

import ch.unibe.sdwsn.dynamicrouting.models.SensorNodeInfo;
import ch.unibe.sdwsn.dynamicrouting.models.SensorNodeNeighborInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;

import org.onlab.osgi.DefaultServiceDirectory;
import org.onosproject.net.DeviceId;
import org.onosproject.net.SensorNode;
import org.onosproject.net.SensorNodeId;
import org.onosproject.net.SensorNodeNeighbor;
import org.onosproject.net.sensor.SensorNodeNeighborhood;
import org.onosproject.net.sensor.SensorNodeService;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.table.TableModel;
import org.onosproject.ui.table.TableRequestHandler;
import org.onosproject.ui.table.cell.NumberFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Override;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Skeletal ONOS UI Table-View message handler.
 */
public class AppUiTableMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_TABLE_DATA_REQ = "sampleTableDataRequest";
    private static final String SAMPLE_TABLE_DATA_RESP = "sampleTableDataResponse";
    private static final String SAMPLE_TABLES = "sampleTables";

    private static final String SAMPLE_TABLE_DETAIL_REQ = "sampleTableDetailsRequest";
    private static final String SAMPLE_TABLE_DETAIL_RESP = "sampleTableDetailsResponse";
    private static final String DETAILS = "details";
    private static final String NEIGHBORS = "neighbors";

    private static final String NO_ROWS_MESSAGE = "No items found";

    private static final String ID = "id";
    private static final String SINK_ID = "sinkId";
    private static final String BATTERY = "battery";
    private static final String TEMPERATURE = "temperature";
    private static final String HUMIDITY = "humidity";
    private static final String LIGHT1 = "light1";
    private static final String LIGHT2 = "light2";

    private static final String[] COLUMN_IDS = {ID, SINK_ID, BATTERY, TEMPERATURE, HUMIDITY, LIGHT1, LIGHT2};

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new SampleTableDataRequestHandler(),
                new SampleTableDetailRequestHandler()
        );
    }

    // handler for sample table requests
    private final class SampleTableDataRequestHandler extends TableRequestHandler {

        private SampleTableDataRequestHandler() {
            super(SAMPLE_TABLE_DATA_REQ, SAMPLE_TABLE_DATA_RESP, SAMPLE_TABLES);
        }

        // if necessary, override defaultColumnId() -- if it isn't "id"

        @Override
        protected String[] getColumnIds() {
            return COLUMN_IDS;
        }

        @Override
        protected TableModel createTableModel() {
            TableModel tm = super.createTableModel();
            tm.setFormatter(TEMPERATURE, new NumberFormatter("0.0"));
            tm.setFormatter(HUMIDITY, new NumberFormatter("0.0"));
            tm.setFormatter(LIGHT1, new NumberFormatter("0.0"));
            tm.setFormatter(LIGHT2, new NumberFormatter("0.0"));
            return tm;
        }

        @Override
        protected String noRowsMessage(ObjectNode payload) {
            return NO_ROWS_MESSAGE;
        }

        @Override
        protected void populateTable(TableModel tm, ObjectNode payload) {

            List<SensorNodeInfo> sensorNodeInfos = new ArrayList<>();
            SensorNodeService sensorNodeService = DefaultServiceDirectory.getService(SensorNodeService.class);

            try {
                sensorNodeService.getSensorNodes().forEach(device -> {
                    String id = device.deviceId().uri().toString().substring(device.deviceId().uri().toString().length()-2, device.deviceId().uri().toString().length());
                    sensorNodeInfos.add(new SensorNodeInfo(Integer.parseInt(id, 16)+"", device.deviceId().uri().toString(),
                            sensorNodeService.getSensorNodeBatteryLevel(device.id()), sensorNodeService.getSensorNodeTemperature(device.id()),
                            sensorNodeService.getSensorNodeHumidity(device.id()), sensorNodeService.getSensorNodeLight1(device.id()),
                            sensorNodeService.getSensorNodeLight2(device.id())));
                });
            } catch (Exception e) {
                log.error("Some error occured", e);
            }

            // fake data for demonstration purposes...
            for (SensorNodeInfo sensorNodeInfo : sensorNodeInfos) {
                populateRow(tm.addRow(), sensorNodeInfo);
            }
        }

        private void populateRow(TableModel.Row row, SensorNodeInfo sensorNodeInfo) {
            row.cell(ID, sensorNodeInfo.getId())
                    .cell(SINK_ID, sensorNodeInfo.getSinkId())
                    .cell(BATTERY, sensorNodeInfo.getBattery())
                    .cell(TEMPERATURE, sensorNodeInfo.getTemperature())
                    .cell(HUMIDITY, sensorNodeInfo.getHumidity())
                    .cell(LIGHT1, sensorNodeInfo.getLight1())
                    .cell(LIGHT2, sensorNodeInfo.getLight2());
        }
    }


    // handler for sample item details requests
    private final class SampleTableDetailRequestHandler extends RequestHandler {

        private SampleTableDetailRequestHandler() {
            super(SAMPLE_TABLE_DETAIL_REQ);
        }

        @Override
        public void process(ObjectNode payload) {

            SensorNodeService sensorNodeService = DefaultServiceDirectory.getService(SensorNodeService.class);
            String id = string(payload, ID, "(none)");
            id = "sdnwise:00:00:00:01:00:" + Integer.toHexString(Integer.parseInt(id));
            SensorNode sensorNode = sensorNodeService.getSensorNode(DeviceId.deviceId(id));
            SensorNodeInfo sensorNodeInfo = new SensorNodeInfo(sensorNode.deviceId().uri().toString(), sensorNode.associatedSink().deviceId().uri().toString(),
                    sensorNodeService.getSensorNodeBatteryLevel(sensorNode.id()), sensorNodeService.getSensorNodeTemperature(sensorNode.id()),
                    sensorNodeService.getSensorNodeHumidity(sensorNode.id()), sensorNodeService.getSensorNodeLight1(sensorNode.id()),
                    sensorNodeService.getSensorNodeLight2(sensorNode.id()));
            sensorNodeService.getSensorNodeTemperature(sensorNode.id());
            ObjectNode rootNode = objectNode();
            ObjectNode data = objectNode();
            ObjectNode neighbors = objectNode();
            rootNode.set(DETAILS, data);

            ObjectMapper mapper = new ObjectMapper();
            List<SensorNodeNeighborInfo> sensorNodeNeighborInfoList = new ArrayList<>();
            for(Map.Entry<SensorNodeId, SensorNodeNeighbor> entry : sensorNodeService.getSensorNodeNeighbors(sensorNode.id()).entrySet()) {
                SensorNodeNeighborInfo sensorNodeNeighborInfo = new SensorNodeNeighborInfo(entry.getKey(), entry.getValue());
                sensorNodeNeighborInfoList.add(sensorNodeNeighborInfo);
            }
            ArrayNode sensorNodeNeighborInfoArray = mapper.valueToTree(sensorNodeNeighborInfoList);
            rootNode.set(NEIGHBORS, sensorNodeNeighborInfoArray);

            if (sensorNodeInfo == null) {
                log.warn("attempted to get sensorNodeInfo detail for id '{}'", id);
            } else {
                data.put(ID, sensorNodeInfo.getId());
                data.put(SINK_ID, sensorNodeInfo.getSinkId());
                data.put(BATTERY, sensorNodeInfo.getBattery());
                data.put(TEMPERATURE, sensorNodeInfo.getTemperature());
                data.put(HUMIDITY, sensorNodeInfo.getHumidity());
                data.put(LIGHT1, sensorNodeInfo.getLight1());
                data.put(LIGHT2, sensorNodeInfo.getLight2());
            }

            sendMessage(SAMPLE_TABLE_DETAIL_RESP, rootNode);
        }
    }
}
