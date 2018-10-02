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

import ch.unibe.sdwsn.dynamicrouting.protocol.DRPacketProcessor;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.packet.*;
import org.onosproject.net.sensor.SensorNodeEvent;
import org.onosproject.net.sensor.SensorNodeListener;
import org.onosproject.net.sensor.SensorNodeService;
import org.onosproject.net.sensor.SensorNodeStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public final class AppComponent {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected CoreService coreService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected PacketService packetService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
	protected FlowRuleService flowRuleService;

	private Object eventLock = new Object();

	private ApplicationId appId;

	PacketProcessor packetProcessor = new DRPacketProcessor();

	@Activate
	protected void activate() {
		appId = coreService.registerApplication("ch.unibe.sdwsndynamicrouting");
		packetService.addProcessor(packetProcessor,11);
        log.info("Started Application with ID {}", appId.id());
    }

	@Deactivate
	protected void deactivate() {
        packetService.removeProcessor(packetProcessor);
        packetProcessor = null;
	    log.info("Stopped Application with ID {}", appId.id());
	}

}
