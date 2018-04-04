/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.simulator.dataclient.client;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.opendaylight.telemetry.proto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class DataClientImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DataClientImpl.class);
    private final ManagedChannel channel;
    private final TelemetryGrpc.TelemetryStub asyncStub;

    private Map<String, RandomValueGenerator> ifMetrics = new LinkedHashMap<String, RandomValueGenerator>() {
        {
            put("in-octets",            new RandomValueGenerator());
            put("in-unicast-pkts",      new RandomValueGenerator());
            put("in-broadcast",         new RandomValueGenerator());
            put("in-multicast-pkts",    new RandomValueGenerator());
            put("in-discards",          new RandomValueGenerator());
            put("in-errors",            new RandomValueGenerator());
            put("out-unicast-pkts",     new RandomValueGenerator());
            put("out-broadcast-pkts",   new RandomValueGenerator());
            put("out-muticast-pkts",    new RandomValueGenerator());
            put("out-errors",           new RandomValueGenerator());
            put("out-discards",         new RandomValueGenerator());
            put("carrier-transitions",  new RandomValueGenerator());
            put("last-clear",           new RandomValueGenerator());
        }
    };

    public DataClientImpl(String ip, int port) {
        this(ManagedChannelBuilder.forAddress(ip, port).usePlaintext(true));
    }

    public DataClientImpl(ManagedChannelBuilder<?> channelBuilder) {
        this.channel = channelBuilder.build();
        this.asyncStub = TelemetryGrpc.newStub(channel);
    }

    public void shutdown()  {
        channel.shutdownNow();
    }

    public ConnectivityState getConnectivityState() {
        return channel.getState(true);
    }

    private TelemetryStreamRequest getRequest() {
        TelemetryStreamRequest.Builder streamRequestBuilder = TelemetryStreamRequest.newBuilder();
        streamRequestBuilder.setNodeId("TELEMETRY");
        RequestField.Builder requestFieldBuilder = RequestField.newBuilder();

        ifMetrics.forEach((name, generator) -> {
            KeyValue.Builder keyValueBuilder = KeyValue.newBuilder();
            TypedValue.Builder typedValueBuilder = TypedValue.newBuilder();
            typedValueBuilder.setUint64Val(generator.generate());
            keyValueBuilder.setKey(name);
            keyValueBuilder.setValue(typedValueBuilder.build());
            requestFieldBuilder.addKv(keyValueBuilder.build());
        });

        requestFieldBuilder.setBasePath("interfaces/interface[name=eth0]/counters");
        requestFieldBuilder.setSampleInterval(5);
        requestFieldBuilder.setTimestamp(System.currentTimeMillis());
        streamRequestBuilder.addFields(requestFieldBuilder.build());
        return streamRequestBuilder.build();
    }

    public void publish () {
        StreamObserver<TelemetryStreamRequest> requestStreamObserver =
            asyncStub.publish(new StreamObserver<TelemetryStreamResponse>() {
                @Override
                public void onNext(TelemetryStreamResponse telemetryStreamResponse) {
                    LOG.info("Receive response from telemetry server.");
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.info("Telemetry response stream on error.");
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                    LOG.info("Telemetry response stream on completed.");
                }
            });

        try {
            requestStreamObserver.onNext(getRequest());
        } catch (StatusRuntimeException e) {
            requestStreamObserver.onError(e);
            throw e;
        }

        requestStreamObserver.onCompleted();
    }

    private class RandomValueGenerator {
        private Long value = (long)0;
        private IncrementalStrategy incrementalStrategy;

        private RandomValueGenerator() {
            this(() -> (long)(new Random().nextInt(100)));
        }

        private RandomValueGenerator(IncrementalStrategy incrementalStrategy) {
            this.incrementalStrategy = incrementalStrategy;
        }

        private Long generate() {
            return value + incrementalStrategy.increase();
        }
    }

    private interface IncrementalStrategy {
        Long increase();
    }

}
