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

import ch.unibe.sdwsn.dynamicrouting.models.SensorLinkInfo;
import ch.unibe.sdwsn.dynamicrouting.models.SensorNodeLinkMap;
import ch.unibe.sdwsn.dynamicrouting.models.SensorNodeNeighborInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.onlab.osgi.DefaultServiceDirectory;
import org.onlab.osgi.ServiceDirectory;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.sensor.SensorNodeService;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiConnection;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.topo.DeviceHighlight;
import org.onosproject.ui.topo.Highlights;
import org.onosproject.ui.topo.NodeBadge;
import org.onosproject.ui.topo.NodeBadge.Status;
import org.onosproject.ui.topo.TopoJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Skeletal ONOS UI Topology-Overlay message handler.
 */
public class AppUiTopovMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_TOPOV_DISPLAY_START = "sampleTopovDisplayStart";
    private static final String SAMPLE_TOPOV_DISPLAY_UPDATE = "sampleTopovDisplayUpdate";
    private static final String SAMPLE_TOPOV_DISPLAY_STOP = "sampleTopovDisplayStop";

    private static final String ID = "id";
    private static final String MODE = "mode";

    private static final long UPDATE_PERIOD_MS = 1000;

    private static final Link[] EMPTY_LINK_SET = new Link[0];

    private enum Mode { IDLE, MOUSE, LINK }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private DeviceService deviceService;
    private SensorNodeService sensorNodeService;
    private HostService hostService;
    private LinkService linkService;

    private final Timer timer = new Timer("sample-overlay");
    private TimerTask demoTask = null;
    private Mode currentMode = Mode.IDLE;
    private Element elementOfNote;
    private Link[] linkSet = EMPTY_LINK_SET;
    private int linkIndex;


    // ===============-=-=-=-=-=-======================-=-=-=-=-=-=-================================


    @Override
    public void init(UiConnection connection, ServiceDirectory directory) {
        super.init(connection, directory);
        deviceService = directory.get(DeviceService.class);
        sensorNodeService = directory.get(SensorNodeService.class);
        hostService = directory.get(HostService.class);
        linkService = directory.get(LinkService.class);
    }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new DisplayStartHandler(),
                new DisplayUpdateHandler(),
                new DisplayStopHandler()
        );
    }

    // === -------------------------
    // === Handler classes

    private final class DisplayStartHandler extends RequestHandler {
        public DisplayStartHandler() {
            super(SAMPLE_TOPOV_DISPLAY_START);
        }

        @Override
        public void process(ObjectNode payload) {
            String mode = string(payload, MODE);

            log.debug("Start Display: mode [{}]", mode);
            clearState();
            clearForMode();

            switch (mode) {
                case "mouse":
                    currentMode = Mode.MOUSE;
                    cancelTask();
                    sendMouseData();
                    break;

                case "link":
                    currentMode = Mode.LINK;
                    scheduleTask();
                    initLinkSet();
                    sendLinkData();
                    break;

                default:
                    currentMode = Mode.IDLE;
                    cancelTask();
                    break;
            }
        }
    }

    private final class DisplayUpdateHandler extends RequestHandler {
        public DisplayUpdateHandler() {
            super(SAMPLE_TOPOV_DISPLAY_UPDATE);
        }

        @Override
        public void process(ObjectNode payload) {
            String id = string(payload, ID);
            log.debug("Update Display: id [{}]", id);
            if (!Strings.isNullOrEmpty(id)) {
                updateForMode(id);
            } else {
                clearForMode();
            }
        }
    }

    private final class DisplayStopHandler extends RequestHandler {
        public DisplayStopHandler() {
            super(SAMPLE_TOPOV_DISPLAY_STOP);
        }

        @Override
        public void process(ObjectNode payload) {
            log.debug("Stop Display");
            cancelTask();
            clearState();
            clearForMode();
        }
    }

    // === ------------

    private void clearState() {
        currentMode = Mode.IDLE;
        elementOfNote = null;
        linkSet = EMPTY_LINK_SET;
    }

    private void updateForMode(String id) {
        log.debug("host service: {}", hostService);
        log.debug("device service: {}", deviceService);

        try {
            HostId hid = HostId.hostId(id);
            log.debug("host id {}", hid);
            elementOfNote = hostService.getHost(hid);
            log.debug("host element {}", elementOfNote);

        } catch (Exception e) {
            try {
                DeviceId did = DeviceId.deviceId(id);
                log.debug("device id {}", did);
                elementOfNote = deviceService.getDevice(did);
                log.debug("device element {}", elementOfNote);

            } catch (Exception e2) {
                log.debug("Unable to process ID [{}]", id);
                elementOfNote = null;
            }
        }

        switch (currentMode) {
            case MOUSE:
                sendMouseData();
                break;

            case LINK:
                sendLinkData();
                break;

            default:
                break;
        }

    }

    private void clearForMode() {
        sendHighlights(new Highlights());
    }

    private void sendHighlights(Highlights highlights) {
        sendMessage(TopoJson.highlightsMessage(highlights));
    }


    private void sendMouseData() {
        if (elementOfNote != null && elementOfNote instanceof Device) {
            DeviceId devId = (DeviceId) elementOfNote.id();
            SensorNode sensorNode = sensorNodeService.getSensorNode(devId);
            int temperature = (int) sensorNodeService.getSensorNodeTemperature(sensorNode.id());
            Set<Link> links = linkService.getDeviceEgressLinks(devId);
            Highlights highlights = fromLinks(links, sensorNode);
            addDeviceBadge(highlights, devId, temperature);
            sendHighlights(highlights);
        }
        // Note: could also process Host, if available
    }

    private void addDeviceBadge(Highlights h, DeviceId devId, int n) {
        DeviceHighlight dh = new DeviceHighlight(devId.toString());
        dh.setBadge(createBadge(n));
        h.add(dh);
    }

    private NodeBadge createBadge(int temperature) {
        return NodeBadge.number(temperature);
    }

    private Highlights fromLinks(Set<Link> links, SensorNode sensorNode) {

        ObjectMapper mapper = new ObjectMapper();
        List<SensorNodeNeighborInfo> sensorNodeNeighborInfoList = new ArrayList<>();
        for(Map.Entry<SensorNodeId, SensorNodeNeighbor> entry : sensorNodeService.getSensorNodeNeighbors(sensorNode.id()).entrySet()) {
            SensorNodeNeighborInfo sensorNodeNeighborInfo = new SensorNodeNeighborInfo(entry.getKey(), entry.getValue());
            sensorNodeNeighborInfoList.add(sensorNodeNeighborInfo);
        }

        SensorNodeLinkMap linkMap = new SensorNodeLinkMap();
        if (links != null) {
            log.debug("Processing {} links", links.size());
            links.forEach(link -> {
                int rssi =sensorNodeNeighborInfoList.stream().filter(neighbor -> neighbor.getSensorNodeId()
                        .substring(0,neighbor.getSensorNodeId().length() -2)
                        .equalsIgnoreCase(link.dst().deviceId().toString().substring(8,link.dst().deviceId().toString().length()))).findFirst().get().getRssi();
                linkMap.add(link).makeImportant().setLabel(Integer.toString(rssi));
            });
        } else {
            log.debug("No egress links found for device {}", sensorNode.id());
        }

        Highlights highlights = new Highlights();
        for (SensorLinkInfo dlink : linkMap.biLinks()) {
            highlights.add(dlink.highlight(null));
        }
        return highlights;
    }

    private void initLinkSet() {
        Set<Link> links = new HashSet<>();
        for (Link link : linkService.getActiveLinks()) {
            links.add(link);
        }
        linkSet = links.toArray(new Link[links.size()]);
        linkIndex = 0;
        log.debug("initialized link set to {}", linkSet.length);
    }

    private void sendLinkData() {
        SensorNodeLinkMap linkMap = new SensorNodeLinkMap();
        for (Link link : linkSet) {
            linkMap.add(link);
        }
        SensorLinkInfo dl = linkMap.add(linkSet[linkIndex]);
        dl.makeImportant().setLabel(Integer.toString(linkIndex));
        log.debug("sending link data (index {})", linkIndex);

        linkIndex += 1;
        if (linkIndex >= linkSet.length) {
            linkIndex = 0;
        }

        Highlights highlights = new Highlights();
        for (SensorLinkInfo dlink : linkMap.biLinks()) {
            highlights.add(dlink.highlight(null));
        }

        sendHighlights(highlights);
    }

    private synchronized void scheduleTask() {
        if (demoTask == null) {
            log.debug("Starting up demo task...");
            demoTask = new DisplayUpdateTask();
            timer.schedule(demoTask, UPDATE_PERIOD_MS, UPDATE_PERIOD_MS);
        } else {
            log.debug("(demo task already running");
        }
    }

    private synchronized void cancelTask() {
        if (demoTask != null) {
            demoTask.cancel();
            demoTask = null;
        }
    }


    private class DisplayUpdateTask extends TimerTask {
        @Override
        public void run() {
            try {
                switch (currentMode) {
                    case LINK:
                        sendLinkData();
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                log.warn("Unable to process demo task: {}", e.getMessage());
                log.debug("Oops", e);
            }
        }
    }

}
