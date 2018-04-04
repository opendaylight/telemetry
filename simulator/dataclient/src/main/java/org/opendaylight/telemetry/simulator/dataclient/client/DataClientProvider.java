/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.simulator.dataclient.client;

import io.grpc.ConnectivityState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataClientProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DataClientProvider.class);
    private DataClientImpl client;
    private ExecutorService executorService;
    private int sample_interval;
    private int port;

    public DataClientProvider() {
        this.sample_interval = 5 * 1000;
        this.port = 50051;
    }

    public void init() {
        this.client = new DataClientImpl("localhost", port);
        this.executorService = Executors.newFixedThreadPool(1);
        waitServerStart();
        executorService.submit(task());
    }

    public void close() {
        if (client != null) {
            client.shutdown();
        }

        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private Runnable task() {
        return () -> {
            while (true) {
                await(sample_interval);
                client.publish();
            }
        };
    }

    private void waitServerStart() {
        await(60 * 1000);
    }

    private void await(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            //ignore
        }
    }
}
