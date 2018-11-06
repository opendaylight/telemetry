/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.collector.datastorage;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.DataCategory;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.tsdrrecord.RecordKeys;
import org.opendaylight.yang.gen.v1.opendaylight.tsdr.rev150219.tsdrrecord.RecordKeysBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.InsertTSDRMetricRecordInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.InsertTSDRMetricRecordOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.TsdrCollectorSpiService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.inserttsdrmetricrecord.input.TSDRMetricRecord;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.tsdr.collector.spi.rev150915.inserttsdrmetricrecord.input.TSDRMetricRecordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.DataStoreOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.datastorage.rev180326.TelemetryDatastorageService;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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
        LOG.error("Write data to TSDR failed, " + builder.toString());
    }

    private Callable<ListenableFuture<RpcResult<InsertTSDRMetricRecordOutput>>> writeDataToTSDR(DataStoreInput input) {
        return () -> {
            InsertTSDRMetricRecordInputBuilder builder = new InsertTSDRMetricRecordInputBuilder();
            builder.setCollectorCodeName("Telemetry");
            List<TSDRMetricRecord> tsdrMetricRecordList = new ArrayList<>();
            String nodeId = input.getNodeId();
            input.getTelemetryData().forEach(telemetryData -> {
                telemetryData.getKeyValue().forEach(keyValue -> {
                    TSDRMetricRecordBuilder tsdrMetricRecordBuilder = new TSDRMetricRecordBuilder();
                    List<RecordKeys> recordKeysList = new ArrayList<>();
                    RecordKeysBuilder recordKeysBuilder = new RecordKeysBuilder();
                    recordKeysBuilder.setKeyName("OpenConfig-Path");
                    recordKeysBuilder.setKeyValue(telemetryData.getBasePath() + "/" + keyValue.getKey());
                    recordKeysList.add(recordKeysBuilder.build());
                    tsdrMetricRecordBuilder.setMetricName(keyValue.getKey());
                    tsdrMetricRecordBuilder.setMetricValue(BigDecimal.valueOf(keyValue.getValue().getInt64()));
                    tsdrMetricRecordBuilder.setNodeID(nodeId);
                    tsdrMetricRecordBuilder.setTimeStamp(telemetryData.getTimestamp().longValue());
                    tsdrMetricRecordBuilder.setRecordKeys(recordKeysList);
                    tsdrMetricRecordBuilder.setTSDRDataCategory(DataCategory.EXTERNAL);
                    tsdrMetricRecordList.add(tsdrMetricRecordBuilder.build());
                });
            });
            builder.setTSDRMetricRecord(tsdrMetricRecordList);
            return tsdrCollectorSpiService.insertTSDRMetricRecord(builder.build());
        };
    }

    @Override
    public ListenableFuture<RpcResult<DataStoreOutput>> dataStore(DataStoreInput input) {
        ListenableFuture<RpcResult<InsertTSDRMetricRecordOutput>> rpcResult;
        try {
            rpcResult = executorService.submit(writeDataToTSDR(input)).get();
            if (!rpcResult.get().isSuccessful()) {
                recordRpcErrors(rpcResult.get().getErrors());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Insert TSDR metric record exception, reason = {}.", e.getMessage());
            return null;
        }
        return Futures.transform(rpcResult, insertTSDRMetricRecordOutputRpcResult -> {
            return null;});

    }
}