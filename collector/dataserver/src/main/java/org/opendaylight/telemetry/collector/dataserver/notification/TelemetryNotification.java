/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.notification;

import com.google.common.collect.Lists;
import org.opendaylight.telemetry.proto.TelemetryStreamRequest;

import java.util.List;

public final class TelemetryNotification {
    private static List<StreamDataHandler> handlers = Lists.newArrayList();
    private static TelemetryNotificationImpl instance = TelemetryNotificationImpl.getInstance();
    //register default processer of stream handler to stream handler map.
    static {
        handlers.add(new StreamDataHandlerImpl());
        handlers.forEach(streamDataHandler -> TelemetryNotification.subscribe(streamDataHandler));
    }
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
        handlers.forEach(streamDataHandler -> TelemetryNotification.unsubscribe(streamDataHandler));
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
