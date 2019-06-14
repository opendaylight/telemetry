/*
 * Copyright © 2017 CTCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;


import com.google.errorprone.annotations.concurrent.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
//import org.opendaylight.mdsal.binding.api.BindingTransactionChain;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainClosedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
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
    private static boolean initCommit = true;

    @GuardedBy("txLock")
    private TransactionChain txChainFactory;
    @GuardedBy("txLock")
    private WriteTransaction wTx;

    TransactionChainManager(@NonNull final DataBroker dataBroker,
                            @NonNull final String nodeIp) {
        this.dataBroker = dataBroker;
        this.nodeId = nodeIp;
    }

//    public void activateTransactionManager() {
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("activateTransactionManager for node {}", this.nodeId);
//        }
//        synchronized (txLock) {
//            createTxChain();
//        }
//    }
//    <T extends DataObject> void addDeleteOperationTotTxChain(final LogicalDatastoreType store,
//                                                             final InstanceIdentifier<T> path){
//        synchronized (txLock) {
//            if (wTx == null) {
//                LOG.debug("WriteTx is null for node {}. Delete {} was not realized.", this.nodeId, path);
//                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
//            }
//
//            wTx.delete(store, path);
//        }
//    }
//    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store,
//                                                   final InstanceIdentifier<T> path,
//                                                   final T data,
//                                                   final boolean createParents){
//        synchronized (txLock) {
//            if (wTx == null) {
//                LOG.debug("WriteTx is null for node {}. Write data for {} was not realized.", this.nodeId, path);
//                throw new TransactionChainClosedException(CANNOT_WRITE_INTO_TRANSACTION);
//            }
//            wTx.mergeParentStructureMerge(store, path, data);
//        }
//    }
    @Override
    public void onTransactionChainSuccessful(@NonNull TransactionChain chain) {
        LOG.debug("transtion chain successful.");
        wTx = null;
    }

//    @GuardedBy("txLock")
//    private void createTxChain() {
//        if (txChainFactory == null) {
//            txChainFactory = dataBroker.createTransactionChain(TransactionChainManager.this);
//        }
//    }

    @Override
    public void onTransactionChainFailed(TransactionChain<?, ?> chain, AsyncTransaction<?, ?> transaction, Throwable cause) {
        synchronized (txLock) {
            LOG.warn("Transaction chain failed, recreating chain due to ", cause);
            //createTxChain();
            wTx = null;
        }
    }
//
//    @GuardedBy("txLock")
//    private void ensureTransaction() {
//        if (wTx == null && txChainFactory != null) {
//            wTx = txChainFactory.newWriteOnlyTransaction();
//        }
//    }

}
