/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import java.util.concurrent.Future;
import java.util.List;

import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.ConfigureResult.Result;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfiguratorServiceImpl implements TelemetryConfiguratorApiService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguratorServiceImpl.class);

    private DataProcessor dataProcessor;
    private static final String INPUT_NULL = "Input is null";
    private static final String SENSOR_GROUP_NULL = "There is no sensor group provided by input!";
    private static final String SENSOR_PATHS = " sensor paths not provided by input!";
    private static final String SENSOR_GROUP_EXIST = "There are sensor groups Exist!";
    private static final String NO_SENSOR_GROUP = "No sensor group configured!";
    private static final String SENSOR_GROUP_ID_NULL = "There is no sensor group id provided by input!";

    private static final String DES_GROUP_NULL = "There is no destination group provided by input!";
    private static final String DES_FILE = " destination profile not provided by input!";
    private static final String DES_GROUP_EXIST = "There are destination groups Exist!";
    private static final String NO_DES_GROUP = "No destination group configured!";

    public ConfiguratorServiceImpl(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    @Override
    public Future<RpcResult<AddTelemetrySensorOutput>> addTelemetrySensor(AddTelemetrySensorInput input) {
        AddTelemetrySensorOutputBuilder builder = new AddTelemetrySensorOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        List<TelemetrySensorGroup> sensorGroupList = input.getTelemetrySensorGroup();
        if (null == sensorGroupList || sensorGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, SENSOR_GROUP_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            if (null == sensorGroup.getTelemetrySensorPaths() || sensorGroup.getTelemetrySensorPaths().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, sensorGroup.getTelemetrySensorGroupId() + SENSOR_PATHS));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        LOG.info("Check sensor group whether exist");
        if (checkSensorGroupExistedInDataStore(sensorGroupList)) {
            builder.setConfigureResult(getConfigResult(false, SENSOR_GROUP_EXIST));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        LOG.info("Write add telemetry sensor config to dataStore");
        dataProcessor.addSensorGroupToDataStore(sensorGroupList);
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<QueryTelemetrySensorOutput>> queryTelemetrySensor(QueryTelemetrySensorInput input) {
        if (null == input) {
            return rpcErr(INPUT_NULL);
        }

        List<TelemetrySensorGroup> allSensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allSensorGroupList || allSensorGroupList.isEmpty()) {
            return rpcErr(NO_SENSOR_GROUP);
        }
        QueryTelemetrySensorOutputBuilder builder = new QueryTelemetrySensorOutputBuilder();
        builder.setTelemetrySensorGroup(allSensorGroupList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteTelemetrySensorOutput>> deleteTelemetrySensor(DeleteTelemetrySensorInput input) {
        DeleteTelemetrySensorOutputBuilder builder = new DeleteTelemetrySensorOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        if (null == input.getTelemetrySensorGroup() || input.getTelemetrySensorGroup().isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, SENSOR_GROUP_ID_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        for (int i = 0; i < input.getTelemetrySensorGroup().size(); i++) {
            dataProcessor.deleteSensorGroupFromDataStore(input.getTelemetrySensorGroup().get(i).getSensorGroupId());
        }

        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<AddTelemetryDestinationOutput>> addTelemetryDestination(AddTelemetryDestinationInput input) {
        AddTelemetryDestinationOutputBuilder builder = new AddTelemetryDestinationOutputBuilder();
        if (null == input) {
            builder.setConfigureResult(getConfigResult(false, INPUT_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        List<TelemetryDestinationGroup> destinationGroupList = input.getTelemetryDestinationGroup();
        if (null == destinationGroupList || destinationGroupList.isEmpty()) {
            builder.setConfigureResult(getConfigResult(false, DES_GROUP_NULL));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }
        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            if (null == destinationGroup.getDestinationProfile() || destinationGroup.getDestinationProfile().isEmpty()) {
                builder.setConfigureResult(getConfigResult(false, destinationGroup.getDestinationGroupId() + DES_FILE));
                return RpcResultBuilder.success(builder.build()).buildFuture();
            }
        }

        LOG.info("Check destination group whether exist");
        if (checkDesGroupExistedInDataStore(destinationGroupList)) {
            builder.setConfigureResult(getConfigResult(false, DES_GROUP_EXIST));
            return RpcResultBuilder.success(builder.build()).buildFuture();
        }

        LOG.info("Write add telemetry sensor config to dataStore");
        dataProcessor.addDestinationGroupToDataStore(destinationGroupList);
        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<QueryTelemetryDestinationOutput>> queryTelemetryDestination(QueryTelemetryDestinationInput input) {
        if (null == input) {
            return rpcErr(INPUT_NULL);
        }

        List<TelemetryDestinationGroup> allDestinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);
        if (null == allDestinationGroupList || allDestinationGroupList.isEmpty()) {
            return rpcErr(NO_DES_GROUP);
        }
        QueryTelemetryDestinationOutputBuilder builder = new QueryTelemetryDestinationOutputBuilder();
        builder.setTelemetryDestinationGroup(allDestinationGroupList);
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<DeleteTelemetryDestinationOutput>> deleteTelemetryDestination(DeleteTelemetryDestinationInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> configureNodeTelemetrySubscription(ConfigureNodeTelemetrySubscriptionInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<QueryNodeTelemetrySubscriptionOutput>> queryNodeTelemetrySubscription(QueryNodeTelemetrySubscriptionInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<DeleteNodeTelemetrySubscriptionOutput>> deleteNodeTelemetrySubscription(DeleteNodeTelemetrySubscriptionInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<DeleteNodeTelemetrySubscriptionSensorOutput>> deleteNodeTelemetrySubscriptionSensor(DeleteNodeTelemetrySubscriptionSensorInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<DeleteNodeTelemetrySubscriptionDestinationOutput>> deleteNodeTelemetrySubscriptionDestination(DeleteNodeTelemetrySubscriptionDestinationInput input) {
        return null;
    }

    private boolean checkSensorGroupExistedInDataStore(List<TelemetrySensorGroup> sensorGroupList) {
        LOG.info("Get sensor group from data store");
        List<TelemetrySensorGroup> allSensorGroupList = dataProcessor.getSensorGroupFromDataStore(IidConstants.TELEMETRY_IID);

        if (null == allSensorGroupList || allSensorGroupList.isEmpty()) {
            return false;
        }

        for (TelemetrySensorGroup sensorGroup : sensorGroupList) {
            for (TelemetrySensorGroup allSensorGroup : allSensorGroupList) {
                if (sensorGroup.getTelemetrySensorGroupId().equals(allSensorGroup.getTelemetrySensorGroupId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDesGroupExistedInDataStore(List<TelemetryDestinationGroup> destinationGroupList) {
        LOG.info("Get destination group from data store");
        List<TelemetryDestinationGroup> allDestinationGroupList = dataProcessor.getDestinationGroupFromDataStore(IidConstants.TELEMETRY_IID);

        if (null == allDestinationGroupList || allDestinationGroupList.isEmpty()) {
            return false;
        }

        for (TelemetryDestinationGroup destinationGroup : destinationGroupList) {
            for (TelemetryDestinationGroup allDestinationGroup : allDestinationGroupList) {
                if (destinationGroup.getDestinationGroupId().equals(allDestinationGroup.getDestinationGroupId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ConfigureResult getConfigResult(boolean result, String errorCause) {
        ConfigureResultBuilder cfgResultBuilder  = new ConfigureResultBuilder();
        if (result) {
            cfgResultBuilder.setResult(Result.SUCCESS);
        } else {
            cfgResultBuilder.setResult(Result.FAILURE);
            cfgResultBuilder.setErrorCause(errorCause);
        }
        return cfgResultBuilder.build();
    }

    private <T> Future<RpcResult<T>> rpcErr(String errMsg) {
        return RpcResultBuilder.<T>failed().withError(RpcError.ErrorType.APPLICATION, errMsg).buildFuture();
    }

}
