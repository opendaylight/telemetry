/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.notification;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class TelemetryEventConsumer implements EventHandler<TelemetryEvent>,WorkHandler<TelemetryEvent> {
    private String TELEMETRY_DATA;
    private AtomicInteger consumeCount = new AtomicInteger(0);

    @Override
    public void onEvent(TelemetryEvent event, long sequence, boolean endOfBatch) throws Exception {
        Collection<StreamDataHandler> subscribers = StreamHandlerRegistryMap.getHandlerMap()
                .get(TELEMETRY_DATA);
        for(StreamDataHandler handler : subscribers) {
            handler.process(event.getValue());
        }
        consumeCount = incrementAndGet(consumeCount);
    }

    public String getConsumeCount() {
        return consumeCount.toString();
    }
    public void setComsumeHandlerKey(String key) {
        this.TELEMETRY_DATA = key;
    }
    @Override
    public void onEvent(TelemetryEvent telemetryEvent) throws Exception {
        Collection<StreamDataHandler> subscribers = StreamHandlerRegistryMap.getHandlerMap()
                .get(TELEMETRY_DATA);
        for(StreamDataHandler handler : subscribers) {
            handler.process(telemetryEvent.getValue());
        }
        consumeCount = incrementAndGet(consumeCount);
    }
    public final AtomicInteger incrementAndGet(final AtomicInteger value) {
        int current;
        int next;
        do {
            current = value.get();
            next = current >= Integer.MAX_VALUE ? 0 : current + 1;
        }while(!value.compareAndSet(current,next));

        return new AtomicInteger(next);

    }
}
