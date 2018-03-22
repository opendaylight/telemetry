/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.grpc.server;

import com.google.common.util.concurrent.SettableFuture;
import io.grpc.Server;

import java.io.IOException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.telemetry.grpc.notification.TelemetryNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.grpc.rev170830.GetNotificationStatsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.grpc.rev170830.GetNotificationStatsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.grpc.rev170830.GrpcService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcProvider implements GrpcService {
    private static final Logger LOG = LoggerFactory.getLogger(GrpcProvider.class);
    private final DataBroker dataBroker;
    private TelemetryServer server;

    public GrpcProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() {
        int port = 50051;
        server = new TelemetryServer(port);
        try {
            server.start();
            LOG.info("gRPC Provider Initiated.");
        } catch (IOException e) {
            LOG.error("gRPC Provider Init Failed.");
        }
    }

    public void close() {
        LOG.info("gRPC Provider Closed.");
    }

    private void stop() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public Future<RpcResult<GetNotificationStatsOutput>> getNotificationStats() {
        GetNotificationStatsOutputBuilder builder = new GetNotificationStatsOutputBuilder();
        builder.setDropCount(TelemetryNotification.getDropCount());
        builder.setDropCount(TelemetryNotification.getPublishCount());
        builder.setDropCount(TelemetryNotification.getConsumeCount());
        return RpcResultBuilder.success(builder).buildFuture();
    }
}
