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
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.ConfigureResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.ConfigureResult.Result;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfiguratorServiceImpl implements TelemetryConfiguratorApiService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguratorServiceImpl.class);

    private static final String INPUT_NULL = "Input is null";
    private static final String SENSOR_GROUP_NULL = "There is no sensor group provided by input!";
    private static final String SENSOR_PATHS = " sensor paths not provided by input!";

    public ConfiguratorServiceImpl() {
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

        builder.setConfigureResult(getConfigResult(true, ""));
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<QueryTelemetrySensorOutput>> queryTelemetrySensor(QueryTelemetrySensorInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<DeleteTelemetrySensorOutput>> deleteTelemetrySensor(DeleteTelemetrySensorInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<AddTelemetryDestinationOutput>> addTelemetryDestination(AddTelemetryDestinationInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<QueryTelemetryDestinationOutput>> queryTelemetryDestination(QueryTelemetryDestinationInput input) {
        return null;
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

}
