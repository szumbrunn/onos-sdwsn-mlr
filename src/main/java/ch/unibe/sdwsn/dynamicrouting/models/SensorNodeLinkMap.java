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
package ch.unibe.sdwsn.dynamicrouting.models;

import org.onosproject.net.Link;
import org.onosproject.net.LinkKey;
import org.onosproject.ui.topo.BiLink;
import org.onosproject.ui.topo.BiLinkMap;
import org.onosproject.ui.topo.TopoUtils;

/**
 * Our concrete link map.
 */
public class SensorNodeLinkMap extends BiLinkMap<SensorLinkInfo> {
    @Override
    protected SensorLinkInfo create(LinkKey linkKey, Link link) {
        return new SensorLinkInfo(linkKey, link);
    }

}