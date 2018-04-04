/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.opendaylight.telemetry.collector.dataserver.notification.TelemetryNotification;
import org.opendaylight.telemetry.collector.dataserver.utils.RPCFutures;
import org.opendaylight.telemetry.proto.*;
import org.opendaylight.telemetry.proto.KeyValue;

import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.TelemetryDatastorageService;

import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.telemetry.data.model.TelemetryData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.telemetry.data.model.TelemetryDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.telemetry.data.model.TelemetryData;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * gRPC server that serve the Telemetry service.
 */
public class DataServer {
    private static final Logger LOG = LoggerFactory.getLogger(DataServer.class);
    private final TelemetryDatastorageService telemetryDatastorageService;
    private int port = 50051;
    private Server server;

    public DataServer(final TelemetryDatastorageService telemetryDatastorageService) {
        this.telemetryDatastorageService = telemetryDatastorageService;
        this.server = ServerBuilder.forPort(port).addService(new TelemetryService()).build();
    }

    public void init() {
        try {
            server.start();
            LOG.info("Telemetry data server started, listening on port " + port);
        } catch (IOException e) {
            LOG.error("Telemetry data server start failed.");
            e.printStackTrace();
        }
    }

    public void close() {
        if (server != null) {
            server.shutdown();
        }
        LOG.info("Telemetry data server closed.");
    }
    
    private class TelemetryService extends TelemetryGrpc.TelemetryImplBase {
        private String getCollectorId() {
            String hostname;
            try {
                hostname =  InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostname = "Unknown";
            }
            return hostname;
        }

        private void dataStorage(TelemetryStreamRequest telemetryStreamRequest) {
            DataStoreInputBuilder inputBuilder = new DataStoreInputBuilder();
            String nodeId = telemetryStreamRequest.getNodeId();
            List<TelemetryData> telemetryDataList = new ArrayList<>();

            for(RequestField requestField : telemetryStreamRequest.getFieldsList()) {
                TelemetryDataBuilder telemetryDataBuilder = new TelemetryDataBuilder();
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326
                        .telemetry.data.model.telemetry.data.KeyValue> keyValueList = new ArrayList<>();

                for(KeyValue keyValue : requestField.getKvList()) {
                    org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326
                            .telemetry.data.model.telemetry.data.KeyValueBuilder keyValueBuilder =
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326
                                    .telemetry.data.model.telemetry.data.KeyValueBuilder();
                    keyValueBuilder.setKey(keyValue.getKey());
                    keyValueBuilder.setValue(new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage
                            .rev180326.telemetry.data.model.telemetry.data.KeyValue.Value(keyValue.getValue().getUint64Val()));
                    keyValueList.add(keyValueBuilder.build());
                }
                telemetryDataBuilder.setBasePath(requestField.getBasePath());
                telemetryDataBuilder.setSampleInterval(BigInteger.valueOf(requestField.getSampleInterval()));
                telemetryDataBuilder.setTimestamp(BigInteger.valueOf(requestField.getSampleInterval()));
                telemetryDataBuilder.setKeyValue(keyValueList);
            }

            inputBuilder.setNodeId(nodeId);
            inputBuilder.setTelemetryData(telemetryDataList);
            Future<RpcResult<Void>> future = telemetryDatastorageService.dataStore(inputBuilder.build());
            RPCFutures.logResult(future, "data-storage", LOG);
        }

        public StreamObserver<TelemetryStreamRequest> publish(StreamObserver<TelemetryStreamResponse> responseObserver) {
            return new StreamObserver<TelemetryStreamRequest>() {
                @Override
                public void onNext(TelemetryStreamRequest telemetryStreamRequest) {
                    dataStorage(telemetryStreamRequest);
                    TelemetryNotification.publish(telemetryStreamRequest);
                    TelemetryStreamResponse.Builder builder = TelemetryStreamResponse.newBuilder();
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
