/*
 * Copyright © 2017 CTCC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.notification;

import org.opendaylight.telemetry.proto.TelemetryStreamRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: li.jiansong
 **/
public class StreamDataHandlerImpl implements StreamDataHandler{
    private static final Logger LOG = LoggerFactory.getLogger(StreamDataHandlerImpl.class);
    @Override
    public void process(TelemetryStreamRequest data) {
        LOG.trace("consume request of stream:{}",data);

    }
}
