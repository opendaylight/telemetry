/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.server;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.opendaylight.telemetry.collector.dataserver.notification.TelemetryNotification;
import org.opendaylight.telemetry.collector.dataserver.utils.RPCFutures;
import org.opendaylight.telemetry.proto.*;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.DataCategory;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.tsdrrecord.RecordKeys;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.tsdrrecord.RecordKeysBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.TelemetryDatastorageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.data.store.input.MetricRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.data.store.input.MetricRecordBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
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
    private final TelemetryDatastorageService dataStorageService;
    private int port = 50051;
    private Server server;

    public DataServer(final TelemetryDatastorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
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

        /**
         * Write to TSDR
         */
        private void dataStorage(TelemetryStreamRequest telemetryStreamRequest) {
            DataStoreInputBuilder inputBuilder = new DataStoreInputBuilder();
            String systemId = telemetryStreamRequest.getSystemId();
            List<MetricRecord> metricRecordList = new ArrayList<>();

            for(Notification notification : telemetryStreamRequest.getNotificationList()) {
                long timestamp = notification.getTimestamp();
                for(KeyValue keyValue : notification.getKvList()) {
                    MetricRecordBuilder metricRecordBuilder = new MetricRecordBuilder();
                    List<RecordKeys> recordKeysList = new ArrayList<>();
                    RecordKeysBuilder recordKeysBuilder = new RecordKeysBuilder();
                    recordKeysBuilder.setKeyName("Telemetry");
                    recordKeysBuilder.setKeyValue(notification.getKeyPrefix() + "/" + keyValue.getKey());
                    recordKeysList.add(recordKeysBuilder.build());

                    metricRecordBuilder.setRecordKeys(recordKeysList);
                    metricRecordBuilder.setTSDRDataCategory(DataCategory.EXTERNAL);
                    metricRecordBuilder.setMetricName(keyValue.getKey());
                    metricRecordBuilder.setMetricValue(BigDecimal.valueOf(keyValue.getValue().getUint64Val()));
                    metricRecordBuilder.setTimeStamp(timestamp);
                    metricRecordBuilder.setNodeID(systemId);
                    metricRecordList.add(metricRecordBuilder.build());
                }
            }
            inputBuilder.setMetricRecord(metricRecordList);
            Future<RpcResult<Void>> future = dataStorageService.dataStore(inputBuilder.build());
            RPCFutures.logResult(future, "data-storage", LOG);
        }

        public StreamObserver<TelemetryStreamRequest> publish(StreamObserver<TelemetryStreamResponse> responseObserver) {
            return new StreamObserver<TelemetryStreamRequest>() {
                @Override
                public void onNext(TelemetryStreamRequest telemetryStreamRequest) {
                    dataStorage(telemetryStreamRequest);
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
