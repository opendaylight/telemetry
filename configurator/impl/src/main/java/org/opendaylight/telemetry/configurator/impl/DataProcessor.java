/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.paths.TelemetrySensorPaths;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.TelemetrySystem;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.TelemetrySystemBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.*;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.DestinationGroups;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.DestinationGroupsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.DestinationGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.DestinationGroupBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.DestinationGroupKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.Destinations;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.DestinationsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.destinations.Destination;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.destinations.DestinationBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.destination.groups.destination.group.destinations.DestinationKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.SensorGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.SensorGroupBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.SensorGroupKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.SensorPaths;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.SensorPathsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.sensor.paths.SensorPath;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.sensor.paths.SensorPathBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.sensor.groups.sensor.group.sensor.paths.SensorPathKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.PersistentBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.Subscription;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.SubscriptionBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.SubscriptionKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.*;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.SensorProfile;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.SensorProfileBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.SensorProfileKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.types.rev170824.*;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170824.Dscp;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170824.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.destination.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.input.telemetry.node.TelemetryNodeSubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.sensor.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionSensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.Telemetry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfile;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensor;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DataProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DataProcessor.class);



    private final DataBroker dataBroker;

    public DataProcessor(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public List<TelemetrySensorGroup> getSensorGroupFromDataStore(InstanceIdentifier<Telemetry> path) {
        final ReadTransaction readTransaction = dataBroker.newReadOnlyTransaction();
        Optional<Telemetry> telemetry = null;
        try {
            telemetry = readTransaction.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (telemetry.isPresent()) {
                LOG.info("Telemetry data from controller data store is not null");
                return telemetry.get().getTelemetrySensorGroup();
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        LOG.info("Telemetry data from controller data store is null");
        return null;
    }

    public List<TelemetryDestinationGroup> getDestinationGroupFromDataStore(InstanceIdentifier<Telemetry> path) {
        final ReadTransaction readTransaction = dataBroker.newReadOnlyTransaction();
        Optional<Telemetry> telemetry = null;
        try {
            telemetry = readTransaction.read(LogicalDatastoreType.CONFIGURATION, path).checkedGet();
            if (telemetry.isPresent()) {
                LOG.info("Telemetry data from controller data store is not null");
                return telemetry.get().getTelemetryDestinationGroup();
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        LOG.info("Telemetry data from controller data store is null");
        return null;
    }

    public void addSensorGroupToDataStore(List<TelemetrySensorGroup> sensorGroupList) {
        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            operateDataStore(ConfigurationType.ADD, sensorGroup, IidConstants.getSensorGroupPath(sensorGroup.getTelemetrySensorGroupId()));
        }
    }

    public void addDestinationGroupToDataStore(List<TelemetryDestinationGroup> destinationGroupList) {
        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            operateDataStore(ConfigurationType.ADD, destinationGroup, IidConstants.getDestinationGroupPath(destinationGroup.getDestinationGroupId()));
        }
    }

    public void addNodeSubscriptionToDataStore(List<TelemetryNode> nodeGroupList) {
        for (TelemetryNode nodeGroup : nodeGroupList) {
            operateDataStore(ConfigurationType.MODIFY, nodeGroup, IidConstants.getNodeGroupPath(nodeGroup.getNodeId()));
        }
    }

    private <T extends DataObject> CheckedFuture<Void, TransactionCommitFailedException> operateDataStore(
            ConfigurationType type, T data, InstanceIdentifier<T> path) {
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        switch (type) {
            case ADD:
                writeTransaction.put(LogicalDatastoreType.CONFIGURATION, path, data, true);
                break;
            case MODIFY:
                writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, path, data, true);
                break;
            case DELETE:
                writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, path);
                break;
            default:
                break;
        }
        final CheckedFuture<Void, TransactionCommitFailedException> submitResult = writeTransaction.submit();
        return submitResult;
    }

    public void deleteSensorGroupFromDataStore(String sensorGroupId) {
        operateDataStore(ConfigurationType.DELETE, null, IidConstants.getSensorGroupPath(sensorGroupId));
    }

    public void deleteDestinationGroupFromDataStore(String destinationGroupId) {
        operateDataStore(ConfigurationType.DELETE, null, IidConstants.getDestinationGroupPath(destinationGroupId));
    }

}
