/*
 * Copyright © 2017 CTCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;


import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import javax.annotation.concurrent.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: li.jiansong
 **/
public class TransactionChainManager implements TransactionChainListener {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionChainManager.class);
    private static final String CANNOT_WRITE_INTO_TRANSACTION = "Cannot write into transaction.";
    private final Object txLock = new Object();
    private final DataBroker dataBroker;
    private final String nodeId;

    @GuardedBy("txLock")
    private BindingTransactionChain txChainFactory;
    @GuardedBy("txLock")
    private WriteTransaction wTx;

    TransactionChainManager(@Nonnull final DataBroker dataBroker,
                            @Nonnull final String nodeIp) {
        this.dataBroker = dataBroker;
        this.nodeId = nodeIp;
        synchronized (txLock) {
            createTxChain();
        }
    }

    void activateTransactionManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activateTransactionManager for node {}", this.nodeId);
        }
        synchronized (txLock) {
            createTxChain();
        }
    }

    @Override
    public void onTransactionChainSuccessful(@Nonnull TransactionChain chain) {
        LOG.debug("transtion chain successful.");
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
}
