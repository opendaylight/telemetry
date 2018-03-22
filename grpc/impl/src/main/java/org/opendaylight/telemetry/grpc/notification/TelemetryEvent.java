/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.grpc.notification;

import com.lmax.disruptor.EventFactory;
import org.opendaylight.telemetry.grpc.proto.TelemetryStreamRequest;

public class TelemetryEvent {
    public static TelemetryEventFactory FACTORY = new TelemetryEventFactory();
    private TelemetryStreamRequest value;
    public void setValue(TelemetryStreamRequest value) {
        this.value = value;
    }

    public TelemetryStreamRequest getValue() {
        return value;
    }

    private static class TelemetryEventFactory implements EventFactory<TelemetryEvent> {
        @Override
        public TelemetryEvent newInstance() {
            return new TelemetryEvent();
        }
    }
}
