/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.configure.result.*;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfiguratorServiceImpl implements TelemetryConfiguratorApiService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguratorServiceImpl.class);

    public ConfiguratorServiceImpl() {
    }

    @Override
    public Future<RpcResult<AddTelemetrySensorOutput>> addTelemetrySensor(AddTelemetrySensorInput input) {
        return null;
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

}
