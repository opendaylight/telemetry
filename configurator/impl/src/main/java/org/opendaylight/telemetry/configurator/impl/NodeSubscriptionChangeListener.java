/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.TelemetrySystem;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NodeSubscriptionChangeListener implements DataTreeChangeListener<TelemetrySubscription> {
    private static final Logger LOG = LoggerFactory.getLogger(NodeSubscriptionChangeListener.class);

    private DataBroker dataBroker;
    private ConfigurationWriter configurationWriter;
    private DataProcessor dataProcessor;

    public NodeSubscriptionChangeListener(DataBroker dataBroker, ConfigurationWriter configurationWriter, DataProcessor dataProcessor) {
        this.dataBroker = dataBroker;
        this.configurationWriter = configurationWriter;
        this.dataProcessor = dataProcessor;
    }

    public void init() {
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<TelemetrySubscription>(LogicalDatastoreType.CONFIGURATION, IidConstants.TELEMETRY_IID.child(TelemetryNode.class).child(TelemetrySubscription.class)), this);
        LOG.info("Begin to Listen the node subscription changes");
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<TelemetrySubscription>> changes) {
        for (DataTreeModification<TelemetrySubscription> change : changes) {
            DataObjectModification<TelemetrySubscription> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    if (null == rootNode.getDataBefore()) {
                        LOG.info("The subscription of {} was added, before:{}, after:{}", change.getRootPath().getRootIdentifier()
                                        .firstIdentifierOf(TelemetrySubscription.class).firstKeyOf(TelemetryNode.class).getNodeId(),
                            rootNode.getDataBefore(), rootNode.getDataAfter());
                        processAddSubscription(change.getRootPath().getRootIdentifier()
                                .firstIdentifierOf(TelemetrySubscription.class)
                                .firstKeyOf(TelemetryNode.class).getNodeId(), rootNode.getDataAfter());
                    } else {
                        LOG.info("The subscription of {} was modified, before:{}, after:{}", change.getRootPath().getRootIdentifier()
                                        .firstIdentifierOf(TelemetrySubscription.class).firstKeyOf(TelemetryNode.class).getNodeId(),
                                rootNode.getDataBefore(), rootNode.getDataAfter());
                        processModifySubscription(change.getRootPath().getRootIdentifier()
                                .firstIdentifierOf(TelemetrySubscription.class)
                                .firstKeyOf(TelemetryNode.class).getNodeId(), rootNode.getDataBefore(), rootNode.getDataAfter());
                    }
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("The subscription of {} was modified, before:{}, after:{}", change.getRootPath().getRootIdentifier()
                                    .firstIdentifierOf(TelemetrySubscription.class).firstKeyOf(TelemetryNode.class).getNodeId(),
                            rootNode.getDataBefore(), rootNode.getDataAfter());
                    processModifySubscription(change.getRootPath().getRootIdentifier()
                            .firstIdentifierOf(TelemetrySubscription.class)
                            .firstKeyOf(TelemetryNode.class).getNodeId(), rootNode.getDataBefore(), rootNode.getDataAfter());
                    break;
                case DELETE:
                    LOG.info("The subscription of {} was deleted, before:{}, after:{}", change.getRootPath().getRootIdentifier()
                                    .firstIdentifierOf(TelemetrySubscription.class).firstKeyOf(TelemetryNode.class).getNodeId(),
                            rootNode.getDataBefore(), rootNode.getDataAfter());
                    processDelSubscription(change.getRootPath().getRootIdentifier()
                            .firstIdentifierOf(TelemetrySubscription.class).firstKeyOf(TelemetryNode.class).getNodeId(),
                            rootNode.getDataBefore().getSubscriptionName());
                    break;
                default:
                    break;
            }
        }
    }

    private void processAddSubscription(String nodeId, TelemetrySubscription subscription) {
        TelemetrySystem telemetrySystemAdded = changeNorthDataToSouth(subscription);
        LOG.info("telemetrySystemAdded is {}", telemetrySystemAdded);
        configurationWriter.writeTelemetryConfig(ConfigurationType.ADD, nodeId,
                subscription.getSubscriptionName(), telemetrySystemAdded);
    }

    private void processModifySubscription(String nodeId, TelemetrySubscription subscriptionBefore,
                                           TelemetrySubscription subscriptionAfter) {
        LOG.info(nodeId, subscriptionBefore, subscriptionAfter);
        if (checkDelSensor(subscriptionBefore, subscriptionAfter)) {
            processDelSensor(nodeId, subscriptionBefore.getSubscriptionName(), subscriptionBefore.getTelemetrySensor(),
                    subscriptionAfter.getTelemetrySensor());
            return;
        }

        if (checkDelDestination(subscriptionBefore, subscriptionAfter)) {
            processDelDestination(nodeId, subscriptionBefore.getSubscriptionName(), subscriptionBefore.getTelemetryDestination(),
                    subscriptionAfter.getTelemetryDestination());
            return;
        }

        TelemetrySystem telemetrySystemModified = changeNorthDataToSouth(subscriptionAfter);
        LOG.info("telemetrySystemModified is {}", telemetrySystemModified);
        configurationWriter.writeTelemetryConfig(ConfigurationType.MODIFY, nodeId, subscriptionBefore.getSubscriptionName(),
                telemetrySystemModified);
    }

    private void processDelSubscription(String nodeId, String subscriptionName) {
        configurationWriter.writeTelemetryConfig(ConfigurationType.DELETE, nodeId, subscriptionName, null);
    }

    private TelemetrySystem changeNorthDataToSouth(TelemetrySubscription subscription) {
        List<TelemetrySensorGroup> sensorGroupList = dataProcessor.getSensorGroupDetailById(subscription);
        List<TelemetryDestinationGroup> destinationGroupList = dataProcessor.getDestinationGroupDetailById(subscription);
        return dataProcessor.convertDataToSouth(sensorGroupList, destinationGroupList, subscription);
    }

    private boolean checkDelSensor(TelemetrySubscription before, TelemetrySubscription after) {
        if (null != before.getTelemetrySensor() && null == after.getTelemetrySensor()) {
            return true;
        }
        if (null != before.getTelemetrySensor() && null != after.getTelemetrySensor()
                && before.getTelemetrySensor().size() > after.getTelemetrySensor().size()) {
            return true;
        }
        return false;
    }

    private void processDelSensor(String nodeId, String subscriptionName, List<TelemetrySensor> before,
                                  List<TelemetrySensor> after) {
        List<TelemetrySensor> sensorChangeList = new ArrayList<>();
        for (TelemetrySensor sensor : before) {
            if (null == getSensorById(sensor.getSensorGroupId(), after)) {
                sensorChangeList.add(sensor);
            }
        }

        for (TelemetrySensor sensor : sensorChangeList) {
            configurationWriter.delSubscriptionSensor(nodeId, subscriptionName, sensor.getSensorGroupId());
        }
        return;
    }

    private TelemetrySensor getSensorById(String sensorId, List<TelemetrySensor> after) {
        if (null == after || after.isEmpty()) {
            return null;
        }
        for (TelemetrySensor sensor : after) {
            if (sensor.getSensorGroupId().equals(sensorId)) {
                return sensor;
            }
        }
        return null;
    }

    private boolean checkDelDestination(TelemetrySubscription before, TelemetrySubscription after) {
        if (null != before.getTelemetryDestination() && null == after.getTelemetryDestination()) {
            return true;
        }
        if (null != before.getTelemetryDestination() && null != after.getTelemetryDestination()
                && before.getTelemetryDestination().size() > after.getTelemetryDestination().size()) {
            return true;
        }
        return false;
    }

    private void processDelDestination(String nodeId, String subscriptionName, List<TelemetryDestination> before,
                                       List<TelemetryDestination> after) {
        List<TelemetryDestination> destinationChangeList = new ArrayList<>();
        for (TelemetryDestination destination : before) {
            if (null == getDestinationById(destination.getDestinationGroupId(), after)) {
                destinationChangeList.add(destination);
            }
        }

        for (TelemetryDestination destination : destinationChangeList) {
            configurationWriter.delSubscriptionDestination(nodeId, subscriptionName, destination.getDestinationGroupId());
        }
        return;
    }

    private TelemetryDestination getDestinationById(String destinationId, List<TelemetryDestination> after) {
        if (null == after || after.isEmpty()) {
            return null;
        }
        for (TelemetryDestination destination : after) {
            if (destination.getDestinationGroupId().equals(destinationId)) {
                return destination;
            }
        }
        return null;
    }

}
