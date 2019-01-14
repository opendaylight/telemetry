/*
 * Copyright © 2017 CTCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;


import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainClosedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author: li.jiansong
 **/
public class TransactionChainManager implements TransactionChainListener {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionChainManager.class);
    private static final String CANNOT_WRITE_INTO_TRANSACTION = "Cannot write into transaction.";
    private final Object txLock = new Object();
    private final DataBroker dataBroker;
    private final String nodeId;
    private static boolean initCommit = true;

    @GuardedBy("txLock")
    private BindingTransactionChain txChainFactory;
    @GuardedBy("txLock")
    private WriteTransaction wTx;

    TransactionChainManager(@Nonnull final DataBroker dataBroker,
                            @Nonnull final String nodeIp) {
        this.dataBroker = dataBroker;
        this.nodeId = nodeIp;
    }

    public void activateTransactionManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activateTransactionManager for node {}", this.nodeId);
        }
        synchronized (txLock) {
            createTxChain();
        }
    }
    <T extends DataObject> void addDeleteOperationTotTxChain(final LogicalDatastoreType store,
                                                             final InstanceIdentifier<T> path){
        synchronized (txLock) {
            if (wTx == null) {
                LOG.debug("WriteTx is null for node {}. Delete {} was not realized.", this.nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }

            wTx.delete(store, path);
        }
    }
    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
                                                   final InstanceIdentifier<T> path,
                                                   final T data,
                                                   final boolean createParents){
        synchronized (txLock) {
            if (wTx == null) {
                LOG.debug("WriteTx is null for node {}. Write data for {} was not realized.", this.nodeId, path);
                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
            }
            wTx.put(store, path, data, createParents);
        }
    }
    @Override
    public void onTransactionChainSuccessful(@Nonnull TransactionChain chain) {
        LOG.debug("transtion chain successful.");
        wTx = null;
    }

    @GuardedBy("txLock")
    private void createTxChain() {
        txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
    }

    @Override
    public void onTransactionChainFailed(TransactionChain<?, ?> chain, AsyncTransaction<?, ?> transaction, Throwable cause) {
        synchronized (txLock) {
            LOG.warn("Transaction chain failed, recreating chain due to ", cause);
            createTxChain();
            wTx = null;
        }
    }

    @GuardedBy("txLock")
    @Nullable
    private void ensureTransaction() {
        if (wTx == null && txChainFactory != null) {
            wTx = txChainFactory.newWriteOnlyTransaction();
        }
    }

    boolean submitWriteTransaction() {
        synchronized (txLock) {
            if (Objects.isNull(wTx)) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("nothing to commit - submit returns true");
                }
                return true;
            }

            final CheckedFuture<Void, TransactionCommitFailedException> submitFuture = wTx.submit();
            wTx = null;
            if (initCommit) {
                try {
                    submitFuture.get(5L, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    LOG.error("Exception during INITIAL transaction submitting. ", ex);
                    return false;
                }
                initCommit = false;
                return true;
            }

            Futures.addCallback(submitFuture, new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    //NOOP
                }
                @Override
                public void onFailure(final Throwable t) {
                    if (t instanceof TransactionCommitFailedException) {
                        LOG.error("Transaction commit failed. ", t);
                    } else {
                        if (t instanceof CancellationException) {
                            LOG.warn("Submit task was canceled");
                            LOG.trace("Submit exception: ", t);
                        } else {
                            LOG.error("Exception during transaction submitting. ", t);
                        }
                    }
                }
            });
        }
        return true;
    }

}
