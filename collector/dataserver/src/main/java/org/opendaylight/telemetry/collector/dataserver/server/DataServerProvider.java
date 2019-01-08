/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.server;

import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.TelemetryDatastorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DataServerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DataServerProvider.class);
    private final TelemetryDatastorageService datastorageService;
    private final int port = 50051;
    private DataServerImpl server;

    /**
     * No support config specific tcp port currently.
     * @param datastorageService data storage service
     */
    public DataServerProvider(final TelemetryDatastorageService datastorageService) {
        this.datastorageService = datastorageService;
    }

    public void init() {
        try {
            server = new DataServerImpl(port, datastorageService);
            server.start();
            LOG.info("Telemetry data server started, listening on port " + port);
        } catch (IOException e) {
            LOG.error("Telemetry data server start failed.");
            e.printStackTrace();
        }
    }

    public void close() {
        if (server != null) {
            server.stop();
        }
        LOG.info("Telemetry data server closed.");
    }

}
