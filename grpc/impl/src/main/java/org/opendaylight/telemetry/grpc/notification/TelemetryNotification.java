/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.grpc.notification;

import org.opendaylight.telemetry.channel.proto.TelemetryStreamRequest;

public final class TelemetryNotification {
    private static TelemetryNotificationImpl instance = TelemetryNotificationImpl.getInstance();

    public static void subscribe(StreamDataHandler handler) {
        instance.subscribe(handler);
    }

    public static void unsubscribe(StreamDataHandler handler) {
        instance.unsubscribe(handler);
    }

    public static void publish(TelemetryStreamRequest data) {
        if (data != null) {
            instance.publish(data);
        }
    }

    public static void shutdown() {
        instance.shutdown();
    }

    public static String getDropCount() {
        return instance.getDropCount();
    }

    public static String getPublishCount() {
        return instance.getPublishCount();
    }

    public static String getConsumeCount() {
        return instance.getConsumeCount();
    }
}
