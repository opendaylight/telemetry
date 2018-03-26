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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.concurrent.Future;


public final class RPCFutures {
    public static void logResult(Future<RpcResult<Void>> future, String rpc, Logger logger) {
        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(future), new FutureCallback<RpcResult<Void>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<Void> voidRpcResult) {
                logger.info("RPC {} success.", rpc);
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.error("RPC {} failed, message = {}.", throwable.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }
}
