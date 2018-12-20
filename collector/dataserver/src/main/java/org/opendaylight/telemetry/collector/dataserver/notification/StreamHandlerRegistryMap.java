/*
 * Copyright © 2017 CTCC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.notification;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author: li.jiansong
 **/
public class StreamHandlerRegistryMap {
    private static volatile Multimap<String, StreamDataHandler> handlerMap = ArrayListMultimap.create();
    private static final String TELEMETRY_DATA = "TD";
    public static void registryHandlerMap(StreamDataHandler handler) {
        handlerMap.put(TELEMETRY_DATA,handler);
    }

    public static void unRegistryHandlerMap(StreamDataHandler handler) {
        handlerMap.get(TELEMETRY_DATA).remove(handler);
    }

    public static Multimap<String,StreamDataHandler> getHandlerMap() {
        return handlerMap;
    }

    public static void clearHandlerMap() {
        handlerMap.clear();
    }
}
