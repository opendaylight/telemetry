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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.OptimisticLockFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.TelemetrySystem;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.Subscriptions;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.Persistent;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.Subscription;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.SubscriptionKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.DestinationGroups;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.SensorProfiles;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.destination.groups.DestinationGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.destination.groups.DestinationGroupKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.SensorProfile;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.top.telemetry.system.subscriptions.persistent.subscription.sensor.profiles.SensorProfileKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationWriter {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationWriter.class);

    public enum OperateType {
        CREATE,
        REPLACE,
        MERGE,
        DELETE;

        OperateType() {
        }
    }

    private final MountPointService mountPointService;
    private static final int RETRY_WRITE_MAX = 3;
    private static final String LOCK_FAILED_RETRY = "Got OptimisticLockFailedException - trying again";
    private static final String OPERATE_TYPE = " [Operate Type] ";
    private static final String DATA_INFO = " [Data Info] ";
    private static final String BLANK = "  ";
    private static final String NETCONF_LOCK_FAILUE = "Device is busy,try again later. ";
    private static final String NETCONF_EDIT_FAILUE = "Netconf edit config failed with error message :";
    private static final String NETCONF_COMMIT_FAILUE = "Netconf edit config commit failed with error message :";

    public ConfigurationWriter(final MountPointService mountPointService) {
        this.mountPointService = mountPointService;
    }

    public CheckedFuture<Void, TransactionCommitFailedException> writeTelemetryConfig(
            ConfigurationType type, String nodeId, String subscriptionName, TelemetrySystem data) {
        if (type == ConfigurationType.DELETE) {
        	  LOG.info("access delete write");
            return write(OperateType.DELETE, nodeId, IidConstants.TELEMETRY_SYSTEM_IID.child(Subscriptions.class)
                    .child(Persistent.class).child(Subscription.class, new SubscriptionKey(subscriptionName)), null);
        }
        LOG.info("access merge write");
        return write(OperateType.MERGE, nodeId, IidConstants.TELEMETRY_SYSTEM_IID, data);
    }

    public CheckedFuture<Void, TransactionCommitFailedException> delSubscription(String nodeId, String subscriptionName) {
        return write(OperateType.DELETE, nodeId, IidConstants.TELEMETRY_SYSTEM_IID.child(Subscriptions.class)
                .child(Persistent.class).child(Subscription.class, new SubscriptionKey(subscriptionName)), null);
    }

    public CheckedFuture<Void, TransactionCommitFailedException> delSubscriptionSensor(String nodeId, String subscriptionName,
                                                                                       String sensorId) {
        return write(OperateType.DELETE, nodeId, IidConstants.TELEMETRY_SYSTEM_IID.child(Subscriptions.class)
                .child(Persistent.class).child(Subscription.class, new SubscriptionKey(subscriptionName))
                .child(SensorProfiles.class).child(SensorProfile.class, new SensorProfileKey(sensorId)), null);
    }

    public CheckedFuture<Void, TransactionCommitFailedException> delSubscriptionDestination(String nodeId, String subscriptionName,
                                                                                            String destinationId) {
        return write(OperateType.DELETE, nodeId, IidConstants.TELEMETRY_SYSTEM_IID.child(Subscriptions.class)
                .child(Persistent.class).child(Subscription.class, new SubscriptionKey(subscriptionName))
                .child(DestinationGroups.class).child(DestinationGroup.class, new DestinationGroupKey(destinationId)), null);
    }

    private <T extends DataObject> CheckedFuture<Void, TransactionCommitFailedException> write(
            OperateType type, String nodeId, InstanceIdentifier<T> path, T data) {
        LOG.info("already entered write");
        final DataBroker dataBroker = getDataBroker(nodeId, mountPointService);
        if (null == dataBroker) {
            LOG.info("write process data broker is null");
            return null;
        }
        LOG.info("write process data broker is not null");
        return operate(type, dataBroker, RETRY_WRITE_MAX, path, data);
    }

    private DataBroker getDataBroker(String nodeId, MountPointService mountPointService) {
        MountPoint mountPoint = getMountPoint(nodeId, mountPointService);
        if (null == mountPoint) {
            LOG.info("mount point is null");
            return null;
        }
        LOG.info("mount point is not null");
        Optional<DataBroker> nodeBroker = mountPoint.getService(DataBroker.class);
        if (!nodeBroker.isPresent()) {
            return null;
        }
        return nodeBroker.get();
    }

    private MountPoint getMountPoint(String nodeId, MountPointService mountPointService) {
        if (null == mountPointService) {
            LOG.info("mount point service is null");
            return null;
        }
        LOG.info("mount point service is not null");
        Optional<MountPoint> nodeMountPoint = mountPointService.getMountPoint(IidConstants.NETCONF_TOPO_IID
                .child(Node.class, new NodeKey(new NodeId(nodeId))));

        if (!nodeMountPoint.isPresent()) {
            return null;
        }

        return nodeMountPoint.get();
    }

    private <T extends DataObject> CheckedFuture<Void, TransactionCommitFailedException> operate(
            OperateType type, DataBroker dataBroker, final int tries, InstanceIdentifier<T> path, T data) {
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        switch (type) {
            case CREATE:
            case REPLACE:
                writeTransaction.put(LogicalDatastoreType.CONFIGURATION,path,data,true);
                break;
            case MERGE:
                writeTransaction.merge(LogicalDatastoreType.CONFIGURATION,path,data,true);
                break;
            case DELETE:
                writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, path);
                break;
            default:
                break;

        }

        final CheckedFuture<Void, TransactionCommitFailedException> submitResult = writeTransaction.submit();
        Futures.addCallback(submitResult, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
            }

            @Override
            public void onFailure(final Throwable throwable) {
                operateFail(throwable,type,dataBroker,path,data,tries);
            }
        });

        return submitResult;
    }

    private <T extends DataObject> void operateFail(final Throwable throwable,OperateType type,
                                                          DataBroker dataBroker,InstanceIdentifier<T> path,
                                                          T data,final int tries) {
        String detailedInfo = OPERATE_TYPE + type.toString();
        detailedInfo = detailedInfo + DATA_INFO;
        if (data != null) {
            detailedInfo = detailedInfo + data.toString();
        }

        if (throwable instanceof OptimisticLockFailedException) {
            if ((tries - 1) > 0) {
                LOG.info(LOCK_FAILED_RETRY);
                operate(type, dataBroker, tries - 1, path, data);
            } else {
                LOG.warn(NETCONF_LOCK_FAILUE + detailedInfo);
                //report NETCONF_LOCK_FAILUE

            }

        } else if (throwable instanceof TransactionCommitFailedException) {
            LOG.warn(NETCONF_COMMIT_FAILUE + detailedInfo);
        } else {
            LOG.warn(NETCONF_EDIT_FAILUE + detailedInfo + BLANK + throwable.getMessage());
        }
    }

}
