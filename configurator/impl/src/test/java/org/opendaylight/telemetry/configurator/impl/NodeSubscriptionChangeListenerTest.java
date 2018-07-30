/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.paths.TelemetrySensorPaths;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.paths.TelemetrySensorPathsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.paths.TelemetrySensorPathsKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroupBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroupKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170824.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.Telemetry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfile;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfileBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfileKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscriptionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscriptionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestinationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensorKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NodeSubscriptionChangeListenerTest extends AbstractConcurrentDataBrokerTest {

    @Mock
    private MountPointService mountPointService;
    private DataProcessor dataProcessor;
    private ConfigurationWriter configurationWriter;
    private NodeSubscriptionChangeListener listener;

    @Before
    public void setUp() {
        dataProcessor = new DataProcessor(getDataBroker());
        configurationWriter = new ConfigurationWriter(mountPointService);
        listener = new NodeSubscriptionChangeListener(getDataBroker(), configurationWriter, dataProcessor);
        getDataBroker().registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Telemetry.class)
                .child(TelemetryNode.class).child(TelemetrySubscription.class)), listener);
    }

    @Test
    public void testAddSubscription() {
        addNodeToDatastore("1", null);
        addNodeToDatastore("1", constructSubList("sub1", "10.12.13.14", "sensor1",
                "100", "des1"));

        addSensorToDataStore("sensor1", "path1");
        addDesToDataStore("des1", "10.42.89.15", 50051);
        addNodeToDatastore("2", constructSubList("sub1", "10.12.13.14", "sensor1",
                "100", "des1"));
    }

    @Test
    public void testModifySubscription() {
        addNodeToDatastore("1", constructSubList("sub1", "10.12.13.14", "sensor1",
                "100", "des1"));
        addNodeToDatastore("1", constructSubList("sub1", "10.15.16.17", "sensor1",
                "100", "des1"));
        addNodeToDatastore("1", constructSubList("sub1", "10.15.16.17", "sensor1",
                "200", "des1"));
        addNodeToDatastore("1", constructSubList("sub1", "10.15.16.17", null,
                null, "des1"));
        addNodeToDatastore("1", constructSubList("sub1", "10.15.16.17", null,
                null, null));
    }

    @Test
    public void testDelSubscription() {
        addNodeToDatastore("1", constructSubList("sub1", "10.12.13.14", "sensor1",
                "100", "des1"));
        addNodeToDatastore("1", null);
    }

    private List<TelemetrySubscription> constructSubList(String subId, String ip, String sensorId, String sam,
                                                         String desId) {
        List<TelemetrySubscription> list = new ArrayList<>();
        list.add(constructNodeSub(subId, ip, sensorId, sam, desId));
        return list;
    }

    private TelemetrySubscription constructNodeSub(String subId, String ip, String sensorId, String sam, String desId) {
        TelemetrySubscriptionBuilder subscriptionBuilder = new TelemetrySubscriptionBuilder();
        subscriptionBuilder.withKey(new TelemetrySubscriptionKey(subId));
        subscriptionBuilder.setSubscriptionName(subId);
        subscriptionBuilder.setLocalSourceAddress(new Ipv4Address(ip));

        if (null == sensorId) {
            subscriptionBuilder.setTelemetrySensor(null);
        } else {
            TelemetrySensorBuilder sensorBuilder = new TelemetrySensorBuilder();
            sensorBuilder.withKey(new TelemetrySensorKey(sensorId));
            sensorBuilder.setSensorGroupId(sensorId);
            sensorBuilder.setSampleInterval(new BigInteger(sam));
            List<TelemetrySensor> sensorList = new ArrayList<>();
            sensorList.add(sensorBuilder.build());
            subscriptionBuilder.setTelemetrySensor(sensorList);
        }

        if (null == desId) {
            subscriptionBuilder.setTelemetryDestination(null);
        } else {
            TelemetryDestinationBuilder destinationBuilder = new TelemetryDestinationBuilder();
            destinationBuilder.withKey(new TelemetryDestinationKey(desId));
            destinationBuilder.setDestinationGroupId(desId);
            List<TelemetryDestination> destinationList = new ArrayList<>();
            destinationList.add(destinationBuilder.build());
            subscriptionBuilder.setTelemetryDestination(destinationList);
        }

        return subscriptionBuilder.build();
    }

    private void addNodeToDatastore(String nodeId, List<TelemetrySubscription> subscriptionList) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TelemetryNode> path = InstanceIdentifier.create(Telemetry.class)
                .child(TelemetryNode.class, new TelemetryNodeKey(nodeId));
        TelemetryNodeBuilder nodeBuilder = new TelemetryNodeBuilder();
        nodeBuilder.withKey(new TelemetryNodeKey(nodeId));
        nodeBuilder.setNodeId(nodeId);
        nodeBuilder.setTelemetrySubscription(subscriptionList);
        tx.put(LogicalDatastoreType.CONFIGURATION, path, nodeBuilder.build(), true);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private void addSensorToDataStore(String sensor, String path) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TelemetrySensorGroup> sensorGroupPath = InstanceIdentifier.create(Telemetry.class)
                .child(TelemetrySensorGroup.class, new TelemetrySensorGroupKey(sensor));
        TelemetrySensorGroupBuilder builder = new TelemetrySensorGroupBuilder();
        builder.withKey(new TelemetrySensorGroupKey(sensor));
        builder.setTelemetrySensorGroupId(sensor);
        TelemetrySensorPathsBuilder pathsBuilder = new TelemetrySensorPathsBuilder();
        pathsBuilder.withKey(new TelemetrySensorPathsKey(path));
        pathsBuilder.setTelemetrySensorPath(path);
        List<TelemetrySensorPaths> list = new ArrayList<>();
        list.add(pathsBuilder.build());
        builder.setTelemetrySensorPaths(list);

        tx.put(LogicalDatastoreType.CONFIGURATION, sensorGroupPath, builder.build(), true);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private void addDesToDataStore(String desId, String ip, Integer port) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TelemetryDestinationGroup> desGroupPath = InstanceIdentifier.create(Telemetry.class)
                .child(TelemetryDestinationGroup.class, new TelemetryDestinationGroupKey(desId));
        TelemetryDestinationGroupBuilder builder = new TelemetryDestinationGroupBuilder();
        builder.withKey(new TelemetryDestinationGroupKey(desId));
        builder.setDestinationGroupId(desId);
        DestinationProfileBuilder profileBuilder = new DestinationProfileBuilder();
        profileBuilder.withKey(new DestinationProfileKey(new Ipv4Address(ip), port));
        profileBuilder.setDestinationAddress(new Ipv4Address(ip));
        profileBuilder.setDestinationPort(port);
        List<DestinationProfile> list = new ArrayList<>();
        list.add(profileBuilder.build());
        builder.setDestinationProfile(list);

        tx.put(LogicalDatastoreType.CONFIGURATION, desGroupPath, builder.build(), true);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

}
