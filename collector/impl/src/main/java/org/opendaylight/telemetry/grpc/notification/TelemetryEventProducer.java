/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.grpc.notification;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import org.opendaylight.telemetry.channel.proto.TelemetryStreamRequest;

import java.math.BigInteger;

public class TelemetryEventProducer {
    private RingBuffer<TelemetryEvent> ringBuffer;
    private BigInteger dropCount = BigInteger.ZERO;
    private BigInteger publishCount = BigInteger.ZERO;
    public TelemetryEventProducer(RingBuffer<TelemetryEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * Producer put data into ring buffer. This method is not thread safe,
     * if multi-threads call this method, it will broken the disruptor.
     * @param data publish data.
     */
    public void onData(TelemetryStreamRequest data) {
        try {
            long sequence = ringBuffer.tryNext();
            TelemetryEvent event = ringBuffer.get(sequence);
            event.setValue(data);
            ringBuffer.publish(sequence);
            publishCount = publishCount.add(BigInteger.valueOf(1));
        } catch (InsufficientCapacityException e) {
            dropCount = dropCount.add(BigInteger.valueOf(1));
        }
    }

    public String getDropount() {
        return dropCount.toString();
    }

    public String getPublishCount() {
        return publishCount.toString();
    }
}
