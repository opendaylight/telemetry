/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.server;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.opendaylight.telemetry.collector.dataserver.notification.TelemetryNotification;
import org.opendaylight.telemetry.collector.dataserver.utils.RPCFutures;
import org.opendaylight.telemetry.proto.*;
import org.opendaylight.telemetry.proto.KeyValue;

import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.TelemetryDatastorageService;

import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.telemetry.data.model.TelemetryData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.telemetry.data.model.TelemetryDataBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * gRPC server that serve the Telemetry service.
 */
public class DataServerImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DataServerImpl.class);
    private final TelemetryDatastorageService datastorageService;
    private Server server;

    public DataServerImpl(final int port, final TelemetryDatastorageService datastorageService) {
        this.datastorageService = datastorageService;
        this.server = ServerBuilder.forPort(port).addService(new TelemetryService()).build();
    }

    public void start() throws IOException {
        server.start();
    }

    public void stop() {
        server.shutdownNow();
    }

    private class TelemetryService extends TelemetryGrpc.TelemetryImplBase {
        private void dataStorage(TelemetryStreamRequest telemetryStreamRequest) {
            DataStoreInputBuilder inputBuilder = new DataStoreInputBuilder();
            String nodeId = telemetryStreamRequest.getNodeId();
            List<TelemetryData> telemetryDataList = new ArrayList<>();

            for (RequestField requestField : telemetryStreamRequest.getFieldsList()) {
                TelemetryDataBuilder telemetryDataBuilder = new TelemetryDataBuilder();
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326
                        .telemetry.data.model.telemetry.data.KeyValue> keyValueList = new ArrayList<>();

                for (KeyValue keyValue : requestField.getKvList()) {
                    org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326
                            .telemetry.data.model.telemetry.data.KeyValueBuilder keyValueBuilder =
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326
                                    .telemetry.data.model.telemetry.data.KeyValueBuilder();
                    keyValueBuilder.setKey(keyValue.getKey());
                    keyValueBuilder.setValue(new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage
                            .rev180326.telemetry.data.model.telemetry.data.KeyValue.Value(keyValue.getValue()
                            .getUint64Val()));
                    keyValueList.add(keyValueBuilder.build());
                }
                telemetryDataBuilder.setBasePath(requestField.getBasePath());
                telemetryDataBuilder.setSampleInterval(BigInteger.valueOf(requestField.getSampleInterval()));
                telemetryDataBuilder.setTimestamp(BigInteger.valueOf(requestField.getTimestamp()));
                telemetryDataBuilder.setKeyValue(keyValueList);
                telemetryDataList.add(telemetryDataBuilder.build());
            }

            inputBuilder.setNodeId(nodeId);
            inputBuilder.setTelemetryData(telemetryDataList);
            ListenableFuture<RpcResult<DataStoreOutput>> future = datastorageService.dataStore(inputBuilder.build());
            //RPCFutures.logResult(future, "data-storage", LOG);
        }

        public StreamObserver<TelemetryStreamRequest> publish(StreamObserver<TelemetryStreamResponse>
                                                                      responseObserver) {
            return new StreamObserver<TelemetryStreamRequest>() {
                @Override
                public void onNext(TelemetryStreamRequest telemetryStreamRequest) {
                    dataStorage(telemetryStreamRequest);
                    TelemetryNotification.publish(telemetryStreamRequest);
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.error("Telemetry stream request error.");
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    LOG.info("Telemetry stream request completed.");
                    TelemetryStreamResponse response = TelemetryStreamResponse.newBuilder().build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
