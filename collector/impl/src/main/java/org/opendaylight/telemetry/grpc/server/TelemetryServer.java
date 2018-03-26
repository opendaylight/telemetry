/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.opendaylight.telemetry.grpc.notification.TelemetryNotification;
import org.opendaylight.telemetry.channel.proto.TelemetryGrpc;
import org.opendaylight.telemetry.channel.proto.TelemetryStreamRequest;
import org.opendaylight.telemetry.channel.proto.TelemetryStreamResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * gRPC server that serve the Telemetry service.
 */
public class TelemetryServer {
    private static final Logger LOG = LoggerFactory.getLogger(TelemetryServer.class);
    private int port;
    private Server server;

    /**
     * Create a server listening on {@code port}.
     * @param port listening port, e.g. 50051;
     */
    public TelemetryServer(int port) {
        this.port = port;
        server = ServerBuilder.forPort(port).addService(new TelemetryService()).build();
    }

    /**
     * Start gRPC server.
     * @throws IOException
     */
    public void start() throws IOException {
        server.start();
        LOG.info("Telemetry server started, listening on port " + port);
    }

    /**
     * Stop gRPC server.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
            LOG.info("Telemetry server stopped.");
        }
    }

    private static class TelemetryService extends TelemetryGrpc.TelemetryImplBase {
        private String getCollectorId() {
            String hostname;
            try {
                hostname =  InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostname = "Unknown";
            }
            return hostname;
        }

        public StreamObserver<TelemetryStreamRequest> publish(StreamObserver<TelemetryStreamResponse> responseObserver) {
            return new StreamObserver<TelemetryStreamRequest>() {
                @Override
                public void onNext(TelemetryStreamRequest telemetryStreamRequest) {
                    //TODO write tsdr
                    TelemetryNotification.publish(telemetryStreamRequest);
                    TelemetryStreamResponse.Builder builder = TelemetryStreamResponse.newBuilder();
                    builder.setCollectorId(getCollectorId());
                    responseObserver.onNext(builder.build());
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.error("Telemetry stream request error.");
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    LOG.info("Telemetry stream request completed.");
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
