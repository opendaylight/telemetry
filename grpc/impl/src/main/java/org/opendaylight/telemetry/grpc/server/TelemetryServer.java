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
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            TelemetryServer.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    /**
     * Stop gRPC server.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the gRPC server uses daemon threads.
     * @throws InterruptedException
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private static class TelemetryService extends TelemetryGrpc.TelemetryImplBase {
        public StreamObserver<TelemetryStreamRequest> report(StreamObserver<TelemetryStreamResponse> responseObserver) {
            return new StreamObserver<TelemetryStreamRequest>() {
                @Override
                public void onNext(TelemetryStreamRequest telemetryStreamRequest) {
                    TelemetryNotification.publish(telemetryStreamRequest);
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.info("report error");
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
