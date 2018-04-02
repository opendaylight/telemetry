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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class NodeSubscriptionChangeListener implements DataTreeChangeListener<TelemetryNode> {
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
        dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<TelemetryNode>(LogicalDatastoreType.CONFIGURATION, IidConstants.TELEMETRY_IID.child(TelemetryNode.class)), this);
        LOG.info("Begin to Listen the node subscription changes");
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<TelemetryNode>> changes) {
        for (DataTreeModification<TelemetryNode> change : changes) {
            DataObjectModification<TelemetryNode> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    LOG.info("The subscription of {} was added, before:{}, after:{}", rootNode.getDataAfter().getNodeId(),
                            rootNode.getDataBefore(), rootNode.getDataAfter());
                    TelemetrySystem telemetrySystemAdded = changeNorthDataToSouth(rootNode.getDataAfter().getTelemetrySubscription());
                    configurationWriter.writeTelemetryConfig(ConfigurationType.ADD, rootNode.getDataAfter().getNodeId(), telemetrySystemAdded);
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("The subscription of {} was modified, before:{}, after:{}", rootNode.getDataAfter().getNodeId(),
                            rootNode.getDataBefore(), rootNode.getDataAfter());
                    TelemetrySystem telemetrySystemModified = changeNorthDataToSouth(rootNode.getDataAfter().getTelemetrySubscription());
                    configurationWriter.writeTelemetryConfig(ConfigurationType.MODIFY, rootNode.getDataAfter().getNodeId(), telemetrySystemModified);
                    break;
                case DELETE:
                    LOG.info("The all subscription of {} was deleted, before:{}, after:{}", rootNode.getDataBefore().getNodeId(),
                            rootNode.getDataBefore(), rootNode.getDataAfter());
                    configurationWriter.writeTelemetryConfig(ConfigurationType.DELETE, rootNode.getDataBefore().getNodeId(), null);
                    break;
                default:
                    break;
            }
        }
    }

    private TelemetrySystem changeNorthDataToSouth(List<TelemetrySubscription> subscriptionList) {
        List<TelemetrySensorGroup> sensorGroupList = dataProcessor.getSensorGroupDetailById(
                subscriptionList);
        List<TelemetryDestinationGroup> destinationGroupList = dataProcessor.getDestinationGroupDetailById(
                subscriptionList);
        return dataProcessor.convertDataToSouth(sensorGroupList, destinationGroupList, subscriptionList);
    }

}
