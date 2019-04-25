/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.dataserver.utils;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.MoreExecutors;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

import java.util.concurrent.Future;


public final class RPCFutures {
    public static void logResult(Future<RpcResult<DataStoreOutput>> future, String rpc, Logger logger) {
        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(future), new FutureCallback<RpcResult<DataStoreOutput>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<DataStoreOutput> voidRpcResult) {
                logger.info("RPC {} success.", rpc);
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.error("RPC {} failed, message = {}.", throwable.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }
}
