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
import org.onosproject.ui.topo.LinkHighlight;
import org.onosproject.ui.topo.LinkHighlight.Flavor;

/**
 * Sensor Link concrete class to represent a link on the topology.
 */
public class SensorLinkInfo extends BiLink {

    private boolean important = false;
    private String label = null;


    public SensorLinkInfo(LinkKey key, Link link) {
        super(key, link);
    }

    public SensorLinkInfo makeImportant() {
        important = true;
        return this;
    }

    public SensorLinkInfo setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public LinkHighlight highlight(Enum<?> anEnum) {
        Flavor flavor = important ? Flavor.PRIMARY_HIGHLIGHT
                : Flavor.SECONDARY_HIGHLIGHT;
        return new LinkHighlight(this.linkId(), flavor)
                .setLabel(label);
    }
}
