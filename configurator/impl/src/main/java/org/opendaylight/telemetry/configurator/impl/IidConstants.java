/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroupKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.TelemetrySystem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.common.rev131028.rpc.routing.table.Routes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.sensor.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionSensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.sensor.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionSensorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.Telemetry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.TelemetryNodeSubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscriptionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestinationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensorKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IidConstants {

    public static final InstanceIdentifier<Topology> NETCONF_TOPO_IID = InstanceIdentifier
            .create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    public static final InstanceIdentifier<TelemetrySystem> TELEMETRY_SYSTEM_IID = InstanceIdentifier.create(TelemetrySystem.class);

    public static final InstanceIdentifier<Telemetry> TELEMETRY_IID = InstanceIdentifier
            .create(Telemetry.class);

    public static final InstanceIdentifier<TelemetrySensorGroup> getSensorGroupPath(String sensorGroupId) {
        return InstanceIdentifier.create(Telemetry.class)
                .child(TelemetrySensorGroup.class, new TelemetrySensorGroupKey(sensorGroupId));
    }

    public static final InstanceIdentifier<TelemetryDestinationGroup> getDestinationGroupPath(String destinationGroupId) {
        return InstanceIdentifier.create(Telemetry.class)
                .child(TelemetryDestinationGroup.class, new TelemetryDestinationGroupKey(destinationGroupId));
    }

    public static final InstanceIdentifier<TelemetryNode> getNodeGroupPath(String nodeGroupId) {
        return InstanceIdentifier.create(Telemetry.class).child(TelemetryNode.class, new TelemetryNodeKey(nodeGroupId));
    }

    public static final InstanceIdentifier<TelemetrySubscription> getSubscriptionPath(String nodeId, String name) {
        return InstanceIdentifier.create(Telemetry.class).child(TelemetryNode.class, new TelemetryNodeKey(nodeId))
                .child(TelemetrySubscription.class, new TelemetrySubscriptionKey(name));
    }

    public static final InstanceIdentifier<TelemetrySensor> getSubscriptionSensorPath(String nodeId, String name, String sensorId) {
        return InstanceIdentifier.create(Telemetry.class).child(TelemetryNode.class, new TelemetryNodeKey(nodeId))
                .child(TelemetrySubscription.class, new TelemetrySubscriptionKey(name))
                .child(TelemetrySensor.class, new TelemetrySensorKey(sensorId));
    }

    public static final InstanceIdentifier<TelemetryDestination> getSubscriptionDestinationPath(String nodeId, String name, String destinationId) {
        return InstanceIdentifier.create(Telemetry.class).child(TelemetryNode.class, new TelemetryNodeKey(nodeId))
                .child(TelemetrySubscription.class, new TelemetrySubscriptionKey(name))
                .child(TelemetryDestination.class, new TelemetryDestinationKey(destinationId));
    }
}
