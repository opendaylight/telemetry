/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.grpc.notification;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.lmax.disruptor.EventHandler;

import java.math.BigInteger;
import java.util.Collection;

public class TelemetryEventConsumer implements EventHandler<TelemetryEvent> {
    private final String TELEMETRY_DATA = "TD";
    private volatile Multimap<String, StreamDataHandler> map = ArrayListMultimap.create();
    private BigInteger consumeCount = BigInteger.ZERO;

    @Override
    public void onEvent(TelemetryEvent event, long sequence, boolean endOfBatch) throws Exception {
        Collection<StreamDataHandler> subscribers = map.get(TELEMETRY_DATA);
        for(StreamDataHandler handler : subscribers) {
            handler.process(event.getValue());
        }
        consumeCount = consumeCount.add(BigInteger.valueOf(1));
    }

    public void addSubscriber(StreamDataHandler handler) {
        map.put(TELEMETRY_DATA, handler);
    }

    public void removeSubscriber(StreamDataHandler handler) {
        map.get(TELEMETRY_DATA).remove(handler);
    }

    public String getConsumeCount() {
        return consumeCount.toString();
    }
}
