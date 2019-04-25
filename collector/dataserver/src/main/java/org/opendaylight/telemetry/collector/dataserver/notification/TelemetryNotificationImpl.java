/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.notification;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.telemetry.proto.TelemetryStreamRequest;
import java.util.concurrent.ThreadFactory;

import static org.opendaylight.telemetry.collector.dataserver.notification.TelemetryEvent.FACTORY;

/***
 * Telemetry notification implementation, initialize a disruptor, single producer and single
 * consumer
 */
public class TelemetryNotificationImpl {
    private static TelemetryNotificationImpl instance = new TelemetryNotificationImpl();
    private Disruptor<TelemetryEvent> disruptor;
    private TelemetryEventProducer producer;
    private TelemetryEventConsumer consumer;
    private int consumerSize = 3;
    private static final String TELEMETRY_DATA = "TD";
    private NotificationPublishService notificationProvider;

    private TelemetryNotificationImpl() {
        init();
    }

    public static TelemetryNotificationImpl getInstance() {
        return instance;
    }

    private void init() {
        initConsumer();
        initDisruptor();
        initProducer();
    }

    private void initDisruptor() {
        ThreadFactory threadFactory = Thread::new;
        WaitStrategy waitStrategy = new YieldingWaitStrategy();
        Integer BUFFER_SIZE = 2048;
        disruptor = new Disruptor<>(FACTORY, BUFFER_SIZE, threadFactory, ProducerType.SINGLE, waitStrategy);
        TelemetryEventConsumer[] consumers = new TelemetryEventConsumer[3];
        for (int i = 0; i < consumerSize; i++) {
            TelemetryEventConsumer consumer = new TelemetryEventConsumer();
            consumer.setComsumeHandlerKey(TELEMETRY_DATA);
            consumers[i] = new TelemetryEventConsumer();
        }
        disruptor.handleEventsWithWorkerPool(consumers);
        disruptor.start();
    }

    private void initProducer() {
        RingBuffer<TelemetryEvent> ringBuffer = disruptor.getRingBuffer();
        producer = new TelemetryEventProducer(ringBuffer);
    }

    private void initConsumer() {
        consumer = new TelemetryEventConsumer();
    }

    public void publish(TelemetryStreamRequest data) {
        producer.onData(data);
    }

    public void subscribe(StreamDataHandler handler) {
        StreamHandlerRegistryMap.registryHandlerMap(handler);
    }

    public void unsubscribe(StreamDataHandler handler) {
        StreamHandlerRegistryMap.unRegistryHandlerMap(handler);
    }

    public void shutdown() {
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }

    public String getDropCount() {
        return producer.getDropount();
    }

    public String getPublishCount() {
        return producer.getPublishCount();
    }

    public String getConsumeCount() {
        return consumer.getConsumeCount();
    }

    public NotificationPublishService getNotificationProvider() {
        return notificationProvider;
    }

    public void setNotificationProvider(NotificationPublishService notificationProvider) {
        this.notificationProvider = notificationProvider;
    }

}
