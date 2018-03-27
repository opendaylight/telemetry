/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.datastorage;
 
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.InsertTSDRMetricRecordInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.InsertTSDRMetricRecordInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.TsdrCollectorSpiService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.inserttsdrmetricrecord.input.TSDRMetricRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.inserttsdrmetricrecord.input.TSDRMetricRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.TelemetryDatastorageService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.data.store.input.MetricRecord;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class DataStorageServiceImpl implements TelemetryDatastorageService {
    private static final Logger LOG = LoggerFactory.getLogger(DataStorageServiceImpl.class);
    private final DataBroker dataBroker;
    private final TsdrCollectorSpiService tsdrCollectorSpiService;
    private ExecutorService executorService;
    
    public DataStorageServiceImpl(final DataBroker dataBroker,
                                  final TsdrCollectorSpiService tsdrCollectorSpiService) {
        this.dataBroker = dataBroker;
        this.tsdrCollectorSpiService = tsdrCollectorSpiService;
    }

    public void init() {
        executorService = Executors.newFixedThreadPool(2);
    }

    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void recordRpcErrors(Collection<RpcError> errors) {
        StringBuilder builder = new StringBuilder();
        for(RpcError e : errors) {
            builder.append("type = ").append(e.getErrorType())
                    .append("message = ").append(e.getMessage())
                    .append(";");
        }
        LOG.error("Write data TSDR failed, " + builder.toString());
    }

    private Callable<RpcResult<Void>> writeTsdr(DataStoreInput input) {
        return () -> {
            InsertTSDRMetricRecordInputBuilder builder = new InsertTSDRMetricRecordInputBuilder();
            builder.setCollectorCodeName("Telemetry");

            List<MetricRecord> metricRecordList = input.getMetricRecord();
            List<TSDRMetricRecord> tsdrMetricRecordList = new ArrayList<>();

            for (MetricRecord metricRecord : metricRecordList) {
                TSDRMetricRecordBuilder tsdrMetricRecordBuilder = new TSDRMetricRecordBuilder();
                tsdrMetricRecordBuilder.setMetricName(metricRecord.getMetricName());
                tsdrMetricRecordBuilder.setMetricValue(metricRecord.getMetricValue());
                tsdrMetricRecordBuilder.setNodeID(metricRecord.getNodeID());
                tsdrMetricRecordBuilder.setTimeStamp(metricRecord.getTimeStamp());
                tsdrMetricRecordBuilder.setRecordKeys(metricRecord.getRecordKeys());
                tsdrMetricRecordBuilder.setTSDRDataCategory(metricRecord.getTSDRDataCategory());
                tsdrMetricRecordList.add(tsdrMetricRecordBuilder.build());
            }

            builder.setTSDRMetricRecord(tsdrMetricRecordList);
            Future<RpcResult<Void>> future = tsdrCollectorSpiService.insertTSDRMetricRecord(builder.build());

            try {
                RpcResult<Void> rpcResult = future.get();
                if (!rpcResult.isSuccessful()) {
                    recordRpcErrors(rpcResult.getErrors());
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Insert TSDR metric record exception, reason = {}.", e.getMessage());
            }

            return RpcResultBuilder.success(((Void)null)).build();
        };
    }

    @Override
    public Future<RpcResult<Void>> dataStore(DataStoreInput input) {
        return executorService.submit(writeTsdr(input));
    }
}