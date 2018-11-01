/*
 * Copyright © 2017 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.telemetry.configurator.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractConcurrentDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.paths.TelemetrySensorPaths;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.paths.TelemetrySensorPathsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.paths.TelemetrySensorPathsKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroup;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroupBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.telemetry.sensor.specification.TelemetrySensorGroupKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170824.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.AddTelemetryDestinationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.AddTelemetryDestinationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.AddTelemetryDestinationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.AddTelemetrySensorInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.AddTelemetrySensorInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.AddTelemetrySensorOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.ConfigureNodeTelemetrySubscriptionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.ConfigureNodeTelemetrySubscriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.ConfigureNodeTelemetrySubscriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteNodeTelemetrySubscriptionDestinationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteNodeTelemetrySubscriptionDestinationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteNodeTelemetrySubscriptionDestinationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteNodeTelemetrySubscriptionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteNodeTelemetrySubscriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteNodeTelemetrySubscriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteNodeTelemetrySubscriptionSensorInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteNodeTelemetrySubscriptionSensorInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteNodeTelemetrySubscriptionSensorOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteTelemetryDestinationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteTelemetryDestinationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteTelemetryDestinationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteTelemetrySensorInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteTelemetrySensorInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.DeleteTelemetrySensorOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.QueryNodeTelemetrySubscriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.QueryNodeTelemetrySubscriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.QueryTelemetryDestinationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.QueryTelemetryDestinationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.QueryTelemetrySensorInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.QueryTelemetrySensorOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.destination.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.destination.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.destination.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionDestinationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.input.telemetry.node.TelemetryNodeSubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.input.telemetry.node.TelemetryNodeSubscriptionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.input.telemetry.node.TelemetryNodeSubscriptionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.sensor.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionSensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.sensor.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionSensorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry.subscription.sensor.input.telemetry.node.telemetry.node.subscription.TelemetryNodeSubscriptionSensorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.Telemetry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.TelemetryDestinationGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfile;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfileBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.destination.specification.telemetry.destination.group.DestinationProfileKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.node.subscription.TelemetryNodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscriptionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.TelemetrySubscriptionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetryDestinationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.rev171120.telemetry.subscription.specification.telemetry.subscription.TelemetrySensorKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class ConfiguratorServiceImplTest extends AbstractConcurrentDataBrokerTest {

    @Mock
    private MountPointService mountPointService;
    private DataProcessor dataProcessor;
    private ConfigurationWriter configurationWriter;
    private ConfiguratorServiceImpl configuratorService;

    private static final String INPUT_NULL = "Input is null!";
    private static final String SENSOR_GROUP_NULL = "There is no sensor group provided by input!";
    private static final String SENSOR_PATHS = " sensor paths not provided by input!";
    private static final String SENSOR_GROUP_EXIST = "There are sensor groups Exist!";
    private static final String NO_SENSOR_GROUP = "No sensor group configured!";
    private static final String SENSOR_GROUP_ID_NULL = "There is no sensor group id provided by input!";

    private static final String DES_GROUP_NULL = "There is no destination group provided by input!";
    private static final String DES_FILE = " destination profile not provided by input!";
    private static final String DES_GROUP_EXIST = "There are destination groups Exist!";
    private static final String NO_DES_GROUP = "No destination group configured!";
    private static final String DES_GROUP_ID_NULL = "There is no destination group id provided by input!";
    private static final String NO_SUBSCR = "No node subscription configured!";

    private static final String NODE_NULL = "There is no node id provided by input!";
    private static final String SUBSCR_NULL = " no subscription provided by input!";
    private static final String NODE_SUBSCR_NULL = "Exist node not provide subscription!";
    private static final String NODE_SUBSCR_SENSOR_NULL = "There is no sensor provided in node subscription!";
    private static final String NODE_SUBSCR_DES_NULL = "There is no destination provided in node subscription!";
    private static final String SUBSCR_PARAS_NULL = " exist Param is null! ";
    private static final String SUBSCR_SENSOR_ABNORMAL = "Sensor empty in node subscription or exist Param in"
            + " sensor is null or exist sensor not configured!";
    private static final String SUBSCR_DES_ABNORMAL = "Destination empty in node subscription"
            + " or exist destination not configured!";

    @Before
    public void setUp() {
        dataProcessor = new DataProcessor(getDataBroker());
        configurationWriter = new ConfigurationWriter(mountPointService);
        configuratorService = new ConfiguratorServiceImpl(dataProcessor, configurationWriter);
        configuratorService.init();
    }

    @After
    public void tearDown() {
        configuratorService.close();
    }

    @Test
    public void testAddTelemetrySensor() throws Exception {
        Future<RpcResult<AddTelemetrySensorOutput>> result1 = configuratorService.addTelemetrySensor(null);
        Assert.assertTrue(result1.get().isSuccessful());
        Assert.assertFalse(result1.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(INPUT_NULL, result1.get().getResult().getConfigureResult().getErrorCause());

        AddTelemetrySensorInputBuilder builder = new AddTelemetrySensorInputBuilder();
        builder.setTelemetrySensorGroup(null);
        Future<RpcResult<AddTelemetrySensorOutput>> result2 = configuratorService.addTelemetrySensor(builder.build());
        Assert.assertTrue(result2.get().isSuccessful());
        Assert.assertFalse(result2.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(SENSOR_GROUP_NULL, result2.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<AddTelemetrySensorOutput>> result3 = configuratorService
                .addTelemetrySensor(constructAddTelemetrySensorInput("sensor1", null, "filter1",
                        "sensor2", "path2", "filter2"));
        Assert.assertTrue(result3.get().isSuccessful());
        Assert.assertFalse(result3.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals("sensor1" + SENSOR_PATHS, result3.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<AddTelemetrySensorOutput>> result4 = configuratorService
                .addTelemetrySensor(constructAddTelemetrySensorInput("sensor1", "path1", "filter1",
                        "sensor2", "path2", "filter2"));
        Assert.assertTrue(result4.get().isSuccessful());
        Assert.assertFalse(result4.get().getResult().getConfigureResult().getResult().equals(true));

        addSensorToDataStore("sensor1", "path1", "filter");
        Future<RpcResult<AddTelemetrySensorOutput>> result5 = configuratorService
                .addTelemetrySensor(constructAddTelemetrySensorInput("sensor1", "path1", "filter1",
                        "sensor2", "path2", "filter2"));
        Assert.assertTrue(result5.get().isSuccessful());
        Assert.assertFalse(result5.get().getResult().getConfigureResult().getResult().equals(true));
        Assert.assertEquals(SENSOR_GROUP_EXIST, result5.get().getResult().getConfigureResult().getErrorCause());
    }

    @Test
    public void testQueryTelemetrySensor() throws Exception {
        Future<RpcResult<QueryTelemetrySensorOutput>> result1 = configuratorService.queryTelemetrySensor(null);
        Assert.assertEquals(1, result1.get().getErrors().size());

        QueryTelemetrySensorInputBuilder builder = new QueryTelemetrySensorInputBuilder();
        Future<RpcResult<QueryTelemetrySensorOutput>> result2 = configuratorService
                .queryTelemetrySensor(builder.build());
        Assert.assertEquals(1, result2.get().getErrors().size());

        addSensorToDataStore("sensor1", "path1", "filter1");
        Future<RpcResult<QueryTelemetrySensorOutput>> result3 = configuratorService
                .queryTelemetrySensor(builder.build());
        Assert.assertTrue(result3.get().isSuccessful());
    }

    @Test
    public void testDeleteTelemetrySensor() throws Exception {
        Future<RpcResult<DeleteTelemetrySensorOutput>> result1 = configuratorService.deleteTelemetrySensor(null);
        Assert.assertTrue(result1.get().isSuccessful());
        Assert.assertFalse(result1.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(INPUT_NULL, result1.get().getResult().getConfigureResult().getErrorCause());

        DeleteTelemetrySensorInputBuilder builder = new DeleteTelemetrySensorInputBuilder();
        builder.setTelemetrySensorGroup(null);
        Future<RpcResult<DeleteTelemetrySensorOutput>> result2 = configuratorService
                .deleteTelemetrySensor(builder.build());
        Assert.assertTrue(result2.get().isSuccessful());
        Assert.assertFalse(result2.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(SENSOR_GROUP_ID_NULL, result2.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteTelemetrySensorOutput>> result3 = configuratorService
                .deleteTelemetrySensor(constructDelTelemetrySensorInput("sensor1"));
        Assert.assertTrue(result3.get().isSuccessful());
        Assert.assertFalse(result3.get().getResult().getConfigureResult().getResult().equals(true));
    }

    @Test
    public void testAddTelemetryDestination() throws Exception {
        Future<RpcResult<AddTelemetryDestinationOutput>> result1 = configuratorService.addTelemetryDestination(null);
        Assert.assertTrue(result1.get().isSuccessful());
        Assert.assertFalse(result1.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(INPUT_NULL, result1.get().getResult().getConfigureResult().getErrorCause());

        AddTelemetryDestinationInputBuilder builder = new AddTelemetryDestinationInputBuilder();
        builder.setTelemetryDestinationGroup(null);
        Future<RpcResult<AddTelemetryDestinationOutput>> result2 = configuratorService
                .addTelemetryDestination(builder.build());
        Assert.assertTrue(result2.get().isSuccessful());
        Assert.assertFalse(result2.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(DES_GROUP_NULL, result2.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<AddTelemetryDestinationOutput>> result3 = configuratorService
                .addTelemetryDestination(constructAddTelemetryDestinationInput("des1", null, null));
        Assert.assertTrue(result3.get().isSuccessful());
        Assert.assertFalse(result3.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals("des1" + DES_FILE, result3.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<AddTelemetryDestinationOutput>> result4 = configuratorService
                .addTelemetryDestination(constructAddTelemetryDestinationInput("des1", "10.42.89.15", 50051));
        Assert.assertTrue(result4.get().isSuccessful());
        Assert.assertFalse(result4.get().getResult().getConfigureResult().getResult().equals(true));

        addDesToDataStore("des1", "10.42.89.15", 50051);
        Future<RpcResult<AddTelemetryDestinationOutput>> result5 = configuratorService
                .addTelemetryDestination(constructAddTelemetryDestinationInput("des1", "10.42.89.15", 50051));
        Assert.assertTrue(result5.get().isSuccessful());
        Assert.assertFalse(result5.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(DES_GROUP_EXIST, result5.get().getResult().getConfigureResult().getErrorCause());
    }

    @Test
    public void testQueryTelemetryDestination() throws Exception {
        Future<RpcResult<QueryTelemetryDestinationOutput>> result1 = configuratorService
                .queryTelemetryDestination(null);
        Assert.assertEquals(1, result1.get().getErrors().size());

        QueryTelemetryDestinationInputBuilder builder = new QueryTelemetryDestinationInputBuilder();
        Future<RpcResult<QueryTelemetryDestinationOutput>> result2 = configuratorService
                .queryTelemetryDestination(builder.build());
        Assert.assertEquals(1, result2.get().getErrors().size());

        addDesToDataStore("des1", "10.42.89.15", 50051);
        Future<RpcResult<QueryTelemetryDestinationOutput>> result3 = configuratorService
                .queryTelemetryDestination(builder.build());
        Assert.assertTrue(result3.get().isSuccessful());
    }

    @Test
    public void testDeleteTelemetryDestination() throws Exception {
        Future<RpcResult<DeleteTelemetryDestinationOutput>> result1 = configuratorService
                .deleteTelemetryDestination(null);
        Assert.assertTrue(result1.get().isSuccessful());
        Assert.assertFalse(result1.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(INPUT_NULL, result1.get().getResult().getConfigureResult().getErrorCause());

        DeleteTelemetryDestinationInputBuilder builder = new DeleteTelemetryDestinationInputBuilder();
        builder.setTelemetryDestinationGroup(null);
        Future<RpcResult<DeleteTelemetryDestinationOutput>> result2 = configuratorService
                .deleteTelemetryDestination(builder.build());
        Assert.assertTrue(result2.get().isSuccessful());
        Assert.assertFalse(result2.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(DES_GROUP_ID_NULL, result2.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteTelemetryDestinationOutput>> result3 = configuratorService
                .deleteTelemetryDestination(constructDelTelemetryDesInput("des1"));
        Assert.assertTrue(result3.get().isSuccessful());
        Assert.assertFalse(result3.get().getResult().getConfigureResult().getResult().equals(true));
    }

    @Test
    public void testConfigureNodeTelemetrySubscription() throws Exception {
        Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> result1 = configuratorService
                .configureNodeTelemetrySubscription(null);
        Assert.assertTrue(result1.get().isSuccessful());
        Assert.assertFalse(result1.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(INPUT_NULL, result1.get().getResult().getConfigureResult().getErrorCause());

        ConfigureNodeTelemetrySubscriptionInputBuilder builder1 = new ConfigureNodeTelemetrySubscriptionInputBuilder();
        builder1.setTelemetryNode(null);
        Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> result2 = configuratorService
                .configureNodeTelemetrySubscription(builder1.build());
        Assert.assertTrue(result2.get().isSuccessful());
        Assert.assertFalse(result2.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(NODE_NULL, result2.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> result3 = configuratorService
                .configureNodeTelemetrySubscription(constructConfigureSubscriptionInput("node1",
                        null, null, null, null, null));
        Assert.assertTrue(result3.get().isSuccessful());
        Assert.assertFalse(result3.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals("node1" + SUBSCR_NULL, result3.get().getResult().getConfigureResult().getErrorCause());

        addDesToDataStore("des2", "10.22.22.22", 50051);
        Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> result4 = configuratorService
                .configureNodeTelemetrySubscription(constructConfigureSubscriptionInput("node1",
                        "sub1", "10.23.23.23", null, "des1", null));
        Assert.assertTrue(result4.get().isSuccessful());
        Assert.assertFalse(result4.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(SUBSCR_DES_ABNORMAL, result4.get().getResult().getConfigureResult().getErrorCause());

        addDesToDataStore("des1", "10.42.89.15", 50051);
        Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> result5 = configuratorService
                .configureNodeTelemetrySubscription(constructConfigureSubscriptionInput("node1",
                        "sub1", "10.23.23.23", null, "des1", null));
        Assert.assertTrue(result5.get().isSuccessful());


        addSensorToDataStore("sensor2", "path2", "filter2");
        Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> result6 = configuratorService
                .configureNodeTelemetrySubscription(constructConfigureSubscriptionInput("node1",
                        "sub1", "10.23.23.23", "sensor1", null, null));
        Assert.assertTrue(result6.get().isSuccessful());
        Assert.assertFalse(result6.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(SUBSCR_SENSOR_ABNORMAL, result6.get().getResult().getConfigureResult().getErrorCause());

        addSensorToDataStore("sensor1", "path1", "filter1");
        Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> result7 = configuratorService
                .configureNodeTelemetrySubscription(constructConfigureSubscriptionInput("node1",
                        "sub1", "10.23.23.23", "sensor1", null, new BigInteger("100")));
        Assert.assertTrue(result7.get().isSuccessful());

        Future<RpcResult<ConfigureNodeTelemetrySubscriptionOutput>> result8 = configuratorService
                .configureNodeTelemetrySubscription(constructConfigureSubscriptionInput("node1",
                        "sub1", "10.23.23.23", "sensor1", "des1", new BigInteger("100")));
        Assert.assertTrue(result8.get().isSuccessful());
    }

    @Test
    public void testQueryNodeTelemetrySubscription() throws Exception {
        Future<RpcResult<QueryNodeTelemetrySubscriptionOutput>> result1 = configuratorService
                .queryNodeTelemetrySubscription(null);
        Assert.assertEquals(1, result1.get().getErrors().size());

        QueryNodeTelemetrySubscriptionInputBuilder builder = new QueryNodeTelemetrySubscriptionInputBuilder();
        Future<RpcResult<QueryNodeTelemetrySubscriptionOutput>> result2 = configuratorService
                .queryNodeTelemetrySubscription(builder.build());
        Assert.assertEquals(1, result2.get().getErrors().size());

        addNodeSubToDataStore("node1", "sub1", "10.42.89.15", "sensor1", new BigInteger("100"), "des1");
        Future<RpcResult<QueryNodeTelemetrySubscriptionOutput>> result3 = configuratorService
                .queryNodeTelemetrySubscription(builder.build());
        Assert.assertTrue(result3.get().isSuccessful());
    }

    @Test
    public void testDeleteNodeTelemetrySubscription() throws Exception {
        Future<RpcResult<DeleteNodeTelemetrySubscriptionOutput>> result1 = configuratorService
                .deleteNodeTelemetrySubscription(null);
        Assert.assertTrue(result1.get().isSuccessful());
        Assert.assertFalse(result1.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(INPUT_NULL, result1.get().getResult().getConfigureResult().getErrorCause());

        DeleteNodeTelemetrySubscriptionInputBuilder builder = new DeleteNodeTelemetrySubscriptionInputBuilder();
        Future<RpcResult<DeleteNodeTelemetrySubscriptionOutput>> result2 = configuratorService
                .deleteNodeTelemetrySubscription(builder.setTelemetryNode(null).build());
        Assert.assertTrue(result2.get().isSuccessful());
        Assert.assertFalse(result2.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(NODE_NULL, result2.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteNodeTelemetrySubscriptionOutput>> result3 = configuratorService
                .deleteNodeTelemetrySubscription(constructDelSubInput("node1", null));
        Assert.assertTrue(result3.get().isSuccessful());
        Assert.assertFalse(result3.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals("node1" + SUBSCR_NULL, result3.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteNodeTelemetrySubscriptionOutput>> result4 = configuratorService
                .deleteNodeTelemetrySubscription(constructDelSubInput("node1", "sub1"));
        Assert.assertTrue(result4.get().isSuccessful());
        Assert.assertFalse(result4.get().getResult().getConfigureResult().getResult().equals(true));
    }

    @Test
    public void testDeleteNodeTelemetrySubscriptionSensor() throws Exception {
        Future<RpcResult<DeleteNodeTelemetrySubscriptionSensorOutput>> result1 = configuratorService
                .deleteNodeTelemetrySubscriptionSensor(null);
        Assert.assertTrue(result1.get().isSuccessful());
        Assert.assertFalse(result1.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(INPUT_NULL, result1.get().getResult().getConfigureResult().getErrorCause());

        DeleteNodeTelemetrySubscriptionSensorInputBuilder builder = new
                DeleteNodeTelemetrySubscriptionSensorInputBuilder();
        Future<RpcResult<DeleteNodeTelemetrySubscriptionSensorOutput>> result2 = configuratorService
                .deleteNodeTelemetrySubscriptionSensor(builder.setTelemetryNode(null).build());
        Assert.assertTrue(result2.get().isSuccessful());
        Assert.assertFalse(result2.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(NODE_NULL, result2.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteNodeTelemetrySubscriptionSensorOutput>> result3 = configuratorService
                .deleteNodeTelemetrySubscriptionSensor(constructDelSubSensorInput("node1", null, null));
        Assert.assertTrue(result3.get().isSuccessful());
        Assert.assertFalse(result3.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(NODE_SUBSCR_NULL, result3.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteNodeTelemetrySubscriptionSensorOutput>> result4 = configuratorService
                .deleteNodeTelemetrySubscriptionSensor(constructDelSubSensorInput("node1", "sub1", null));
        Assert.assertTrue(result4.get().isSuccessful());
        Assert.assertFalse(result4.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(NODE_SUBSCR_SENSOR_NULL, result4.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteNodeTelemetrySubscriptionSensorOutput>> result5 = configuratorService
                .deleteNodeTelemetrySubscriptionSensor(constructDelSubSensorInput("node1", "sub1", "sensor1"));
        Assert.assertTrue(result5.get().isSuccessful());
    }

    @Test
    public void testDeleteNodeTelemetrySubscriptionDestination() throws Exception {
        Future<RpcResult<DeleteNodeTelemetrySubscriptionDestinationOutput>> result1 = configuratorService
                .deleteNodeTelemetrySubscriptionDestination(null);
        Assert.assertTrue(result1.get().isSuccessful());
        Assert.assertFalse(result1.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(INPUT_NULL, result1.get().getResult().getConfigureResult().getErrorCause());

        DeleteNodeTelemetrySubscriptionDestinationInputBuilder builder = new
                DeleteNodeTelemetrySubscriptionDestinationInputBuilder();
        Future<RpcResult<DeleteNodeTelemetrySubscriptionDestinationOutput>> result2 = configuratorService
                .deleteNodeTelemetrySubscriptionDestination(builder.setTelemetryNode(null).build());
        Assert.assertTrue(result2.get().isSuccessful());
        Assert.assertFalse(result2.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(NODE_NULL, result2.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteNodeTelemetrySubscriptionDestinationOutput>> result3 = configuratorService
                .deleteNodeTelemetrySubscriptionDestination(constructDelSubDesInput("node1", null, null));
        Assert.assertTrue(result3.get().isSuccessful());
        Assert.assertFalse(result3.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(NODE_SUBSCR_NULL, result3.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteNodeTelemetrySubscriptionDestinationOutput>> result4 = configuratorService
                .deleteNodeTelemetrySubscriptionDestination(constructDelSubDesInput("node1", "sub1", null));
        Assert.assertTrue(result4.get().isSuccessful());
        Assert.assertFalse(result4.get().getResult().getConfigureResult().getResult().equals(false));
        Assert.assertEquals(NODE_SUBSCR_DES_NULL, result4.get().getResult().getConfigureResult().getErrorCause());

        Future<RpcResult<DeleteNodeTelemetrySubscriptionDestinationOutput>> result5 = configuratorService
                .deleteNodeTelemetrySubscriptionDestination(constructDelSubDesInput("node1", "sub1", "des1"));
        Assert.assertTrue(result5.get().isSuccessful());
    }

    private AddTelemetrySensorInput constructAddTelemetrySensorInput(String sensor1, String path1, String filter1,
                                                                     String sensor2, String path2, String filter2) {
        TelemetrySensorGroupBuilder sensorGroupBuilder1 = new TelemetrySensorGroupBuilder();
        sensorGroupBuilder1.withKey(new TelemetrySensorGroupKey(sensor1));
        sensorGroupBuilder1.setTelemetrySensorGroupId(sensor1);
        if (null == path1) {
            sensorGroupBuilder1.setTelemetrySensorPaths(null);
        } else {
            TelemetrySensorPathsBuilder sensorPathsBuilder1 = new TelemetrySensorPathsBuilder();
            sensorPathsBuilder1.withKey(new TelemetrySensorPathsKey(path1));
            sensorPathsBuilder1.setTelemetrySensorPath(path1);
            sensorPathsBuilder1.setSensorExcludeFilter(filter1);
            List<TelemetrySensorPaths> pathsList1 = new ArrayList<>();
            pathsList1.add(sensorPathsBuilder1.build());
            sensorGroupBuilder1.setTelemetrySensorPaths(pathsList1);

        }
        List<TelemetrySensorGroup> list = new ArrayList<>();
        list.add(sensorGroupBuilder1.build());

        TelemetrySensorGroupBuilder sensorGroupBuilder2 = new TelemetrySensorGroupBuilder();
        sensorGroupBuilder2.withKey(new TelemetrySensorGroupKey(sensor2));
        sensorGroupBuilder2.setTelemetrySensorGroupId(sensor2);
        if (null == path2) {
            sensorGroupBuilder2.setTelemetrySensorPaths(null);
        } else {
            TelemetrySensorPathsBuilder sensorPathsBuilder2 = new TelemetrySensorPathsBuilder();
            sensorPathsBuilder2.withKey(new TelemetrySensorPathsKey(path2));
            sensorPathsBuilder2.setTelemetrySensorPath(path2);
            sensorPathsBuilder2.setSensorExcludeFilter(filter2);
            List<TelemetrySensorPaths> pathsList2 = new ArrayList<>();
            pathsList2.add(sensorPathsBuilder2.build());
            sensorGroupBuilder2.setTelemetrySensorPaths(pathsList2);
        }
        list.add(sensorGroupBuilder2.build());

        AddTelemetrySensorInputBuilder builder = new AddTelemetrySensorInputBuilder();
        builder.setTelemetrySensorGroup(list);
        return builder.build();
    }

    private void addSensorToDataStore(String sensor, String path, String filter) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TelemetrySensorGroup> sensorGroupPath = InstanceIdentifier.create(Telemetry.class)
                .child(TelemetrySensorGroup.class, new TelemetrySensorGroupKey(sensor));
        TelemetrySensorGroupBuilder builder = new TelemetrySensorGroupBuilder();
        builder.withKey(new TelemetrySensorGroupKey(sensor));
        builder.setTelemetrySensorGroupId(sensor);
        TelemetrySensorPathsBuilder pathsBuilder = new TelemetrySensorPathsBuilder();
        pathsBuilder.withKey(new TelemetrySensorPathsKey(path));
        pathsBuilder.setTelemetrySensorPath(path);
        pathsBuilder.setSensorExcludeFilter(filter);
        List<TelemetrySensorPaths> list = new ArrayList<>();
        list.add(pathsBuilder.build());
        builder.setTelemetrySensorPaths(list);

        tx.put(LogicalDatastoreType.CONFIGURATION, sensorGroupPath, builder.build(), true);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private DeleteTelemetrySensorInput constructDelTelemetrySensorInput(String sensorId) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120
                .delete.telemetry.sensor.input.TelemetrySensorGroupBuilder sensorGroupBuilder = new org.opendaylight
                .yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.telemetry
                .sensor.input.TelemetrySensorGroupBuilder();
        sensorGroupBuilder.withKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang
                .configurator.api.rev171120.delete.telemetry.sensor.input.TelemetrySensorGroupKey(sensorId));
        sensorGroupBuilder.setSensorGroupId(sensorId);
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120
                .delete.telemetry.sensor.input.TelemetrySensorGroup> list = new ArrayList<>();
        list.add(sensorGroupBuilder.build());
        DeleteTelemetrySensorInputBuilder builder = new DeleteTelemetrySensorInputBuilder();
        builder.setTelemetrySensorGroup(list);
        return builder.build();
    }

    private AddTelemetryDestinationInput constructAddTelemetryDestinationInput(String desId, String ip, Integer port) {
        TelemetryDestinationGroupBuilder groupBuilder = new TelemetryDestinationGroupBuilder();
        groupBuilder.withKey(new TelemetryDestinationGroupKey(desId));
        groupBuilder.setDestinationGroupId(desId);
        DestinationProfileBuilder profileBuilder = new DestinationProfileBuilder();
        List<DestinationProfile> profileList = new ArrayList<>();
        if (null == ip || null == port) {
            groupBuilder.setDestinationProfile(null);
        } else {
            profileBuilder.setDestinationAddress(new Ipv4Address(ip));
            profileBuilder.setDestinationPort(port);
            profileList.add(profileBuilder.build());
            groupBuilder.setDestinationProfile(profileList);
        }

        List<TelemetryDestinationGroup> list = new ArrayList<>();
        list.add(groupBuilder.build());
        AddTelemetryDestinationInputBuilder builder = new AddTelemetryDestinationInputBuilder();
        return builder.setTelemetryDestinationGroup(list).build();
    }

    private void addDesToDataStore(String desId, String ip, Integer port) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TelemetryDestinationGroup> desGroupPath = InstanceIdentifier.create(Telemetry.class)
                .child(TelemetryDestinationGroup.class, new TelemetryDestinationGroupKey(desId));
        TelemetryDestinationGroupBuilder builder = new TelemetryDestinationGroupBuilder();
        builder.withKey(new TelemetryDestinationGroupKey(desId));
        builder.setDestinationGroupId(desId);
        DestinationProfileBuilder profileBuilder = new DestinationProfileBuilder();
        profileBuilder.withKey(new DestinationProfileKey(new Ipv4Address(ip), port));
        profileBuilder.setDestinationAddress(new Ipv4Address(ip));
        profileBuilder.setDestinationPort(port);
        List<DestinationProfile> list = new ArrayList<>();
        list.add(profileBuilder.build());
        builder.setDestinationProfile(list);

        tx.put(LogicalDatastoreType.CONFIGURATION, desGroupPath, builder.build(), true);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private DeleteTelemetryDestinationInput constructDelTelemetryDesInput(String desId) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete
                .telemetry.destination.input.TelemetryDestinationGroupBuilder groupBuilder = new org.opendaylight.yang
                .gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.telemetry
                .destination.input.TelemetryDestinationGroupBuilder();
        groupBuilder.withKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator
                .api.rev171120.delete.telemetry.destination.input.TelemetryDestinationGroupKey(desId));
        groupBuilder.setDestinationGroupId(desId);

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120
                .delete.telemetry.destination.input.TelemetryDestinationGroup> list = new ArrayList<>();
        list.add(groupBuilder.build());
        DeleteTelemetryDestinationInputBuilder builder = new DeleteTelemetryDestinationInputBuilder();
        builder.setTelemetryDestinationGroup(list);
        return builder.build();
    }

    private ConfigureNodeTelemetrySubscriptionInput constructConfigureSubscriptionInput(String nodeId, String subId,
                                                                                        String address, String sensorId,
                                                                                        String desId,
                                                                                        BigInteger sample) {
        TelemetryNodeBuilder nodeBuilder = new TelemetryNodeBuilder();
        nodeBuilder.withKey(new TelemetryNodeKey(nodeId));
        nodeBuilder.setNodeId(nodeId);
        if (null == subId) {
            nodeBuilder.setTelemetrySubscription(null);
        } else {
            TelemetrySubscriptionBuilder subscriptionBuilder = new TelemetrySubscriptionBuilder();
            subscriptionBuilder.withKey(new TelemetrySubscriptionKey(subId));
            subscriptionBuilder.setSubscriptionName(subId);
            subscriptionBuilder.setLocalSourceAddress(new Ipv4Address(address));
            if (null == sensorId) {
                subscriptionBuilder.setTelemetrySensor(null);
            } else {
                TelemetrySensorBuilder sensorBuilder = new TelemetrySensorBuilder();
                sensorBuilder.withKey(new TelemetrySensorKey(sensorId));
                sensorBuilder.setSensorGroupId(sensorId);
                sensorBuilder.setSampleInterval(sample);
                List<TelemetrySensor> sensorList = new ArrayList<>();
                sensorList.add(sensorBuilder.build());
                subscriptionBuilder.setTelemetrySensor(sensorList);
            }
            if (null == desId) {
                subscriptionBuilder.setTelemetryDestination(null);
            } else {
                TelemetryDestinationBuilder destinationBuilder = new TelemetryDestinationBuilder();
                destinationBuilder.withKey(new TelemetryDestinationKey(desId));
                destinationBuilder.setDestinationGroupId(desId);
                List<TelemetryDestination> destinationList = new ArrayList<>();
                destinationList.add(destinationBuilder.build());
                subscriptionBuilder.setTelemetryDestination(destinationList);
            }

            List<TelemetrySubscription> subscriptionList = new ArrayList<>();
            subscriptionList.add(subscriptionBuilder.build());
            nodeBuilder.setTelemetrySubscription(subscriptionList);
        }

        List<TelemetryNode> nodeList = new ArrayList<>();
        nodeList.add(nodeBuilder.build());
        ConfigureNodeTelemetrySubscriptionInputBuilder builder = new ConfigureNodeTelemetrySubscriptionInputBuilder();
        return builder.setTelemetryNode(nodeList).build();
    }

    private void addNodeSubToDataStore(String nodeId, String subId, String ip, String sensorId,
                                       BigInteger sample, String desId) {
        final ReadWriteTransaction tx = getDataBroker().newReadWriteTransaction();
        final InstanceIdentifier<TelemetrySubscription> sensorGroupPath = InstanceIdentifier.create(Telemetry.class)
                .child(TelemetryNode.class, new TelemetryNodeKey(nodeId)).child(TelemetrySubscription.class,
                        new TelemetrySubscriptionKey(subId));
        TelemetrySubscriptionBuilder builder = new TelemetrySubscriptionBuilder();
        builder.withKey(new TelemetrySubscriptionKey(subId));
        builder.setSubscriptionName(subId);
        builder.setLocalSourceAddress(new Ipv4Address(ip));
        TelemetrySensorBuilder sensorBuilder = new TelemetrySensorBuilder();
        sensorBuilder.withKey(new TelemetrySensorKey(sensorId));
        sensorBuilder.setSensorGroupId(sensorId);
        sensorBuilder.setSampleInterval(sample);
        List<TelemetrySensor> sensorList = new ArrayList<>();
        sensorList.add(sensorBuilder.build());
        builder.setTelemetrySensor(sensorList);

        TelemetryDestinationBuilder destinationBuilder = new TelemetryDestinationBuilder();
        destinationBuilder.withKey(new TelemetryDestinationKey(desId));
        destinationBuilder.setDestinationGroupId(desId);
        List<TelemetryDestination> destinationList = new ArrayList<>();
        destinationList.add(destinationBuilder.build());
        builder.setTelemetryDestination(destinationList);

        tx.put(LogicalDatastoreType.CONFIGURATION, sensorGroupPath, builder.build(), true);
        try {
            tx.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            return;
        }
    }

    private DeleteNodeTelemetrySubscriptionInput constructDelSubInput(String nodeId, String subId) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete
                .node.telemetry.subscription.input.TelemetryNodeBuilder nodeBuilder = new org.opendaylight.yang.gen
                .v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry
                .subscription.input.TelemetryNodeBuilder();
        nodeBuilder.withKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator
                .api.rev171120.delete.node.telemetry.subscription.input.TelemetryNodeKey(nodeId));
        nodeBuilder.setNodeId(nodeId);
        if (null == subId) {
            nodeBuilder.setTelemetryNodeSubscription(null);
        } else {
            TelemetryNodeSubscriptionBuilder subscriptionBuilder = new TelemetryNodeSubscriptionBuilder();
            subscriptionBuilder.withKey(new TelemetryNodeSubscriptionKey(subId));
            subscriptionBuilder.setSubscriptionName(subId);
            List<TelemetryNodeSubscription> subscriptionList = new ArrayList<>();
            subscriptionList.add(subscriptionBuilder.build());
            nodeBuilder.setTelemetryNodeSubscription(subscriptionList);
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120
                .delete.node.telemetry.subscription.input.TelemetryNode> nodeList = new ArrayList<>();
        nodeList.add(nodeBuilder.build());
        DeleteNodeTelemetrySubscriptionInputBuilder builder = new DeleteNodeTelemetrySubscriptionInputBuilder();
        return builder.setTelemetryNode(nodeList).build();
    }

    private DeleteNodeTelemetrySubscriptionSensorInput constructDelSubSensorInput(String nodeId, String subId,
                                                                                  String sensorId) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete
                .node.telemetry.subscription.sensor.input.TelemetryNodeBuilder nodeBuilder = new org.opendaylight.yang
                .gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node.telemetry
                .subscription.sensor.input.TelemetryNodeBuilder();
        nodeBuilder.withKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator
                .api.rev171120.delete.node.telemetry.subscription.sensor.input.TelemetryNodeKey(nodeId));
        nodeBuilder.setNodeId(nodeId);
        if (null == subId) {
            nodeBuilder.setTelemetryNodeSubscription(null);
        } else {
            org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120
                    .delete.node.telemetry.subscription.sensor.input.telemetry.node.TelemetryNodeSubscriptionBuilder
                    subscriptionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns
                    .yang.configurator.api.rev171120.delete.node.telemetry.subscription.sensor.input.telemetry.node
                    .TelemetryNodeSubscriptionBuilder();
            subscriptionBuilder.withKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns
                    .yang.configurator.api.rev171120.delete.node.telemetry.subscription.sensor.input.telemetry
                    .node.TelemetryNodeSubscriptionKey(subId));
            subscriptionBuilder.setSubscriptionName(subId);
            if (null == sensorId) {
                subscriptionBuilder.setTelemetryNodeSubscriptionSensor(null);
            } else {
                TelemetryNodeSubscriptionSensorBuilder sensorBuilder = new TelemetryNodeSubscriptionSensorBuilder();
                sensorBuilder.withKey(new TelemetryNodeSubscriptionSensorKey(sensorId));
                sensorBuilder.setSensorGroupId(sensorId);
                List<TelemetryNodeSubscriptionSensor> sensorList = new ArrayList<>();
                sensorList.add(sensorBuilder.build());
                subscriptionBuilder.setTelemetryNodeSubscriptionSensor(sensorList);
            }
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api
                    .rev171120.delete.node.telemetry.subscription.sensor.input.telemetry.node
                    .TelemetryNodeSubscription> subscriptionList = new ArrayList<>();
            subscriptionList.add(subscriptionBuilder.build());
            nodeBuilder.setTelemetryNodeSubscription(subscriptionList);
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120
                .delete.node.telemetry.subscription.sensor.input.TelemetryNode> nodeList = new ArrayList<>();
        nodeList.add(nodeBuilder.build());
        DeleteNodeTelemetrySubscriptionSensorInputBuilder builder = new
                DeleteNodeTelemetrySubscriptionSensorInputBuilder();
        return builder.setTelemetryNode(nodeList).build();
    }

    private DeleteNodeTelemetrySubscriptionDestinationInput constructDelSubDesInput(String nodeId, String subId,
                                                                                    String desId) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete
                .node.telemetry.subscription.destination.input.TelemetryNodeBuilder nodeBuilder = new org.opendaylight
                .yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120.delete.node
                .telemetry.subscription.destination.input.TelemetryNodeBuilder();
        nodeBuilder.withKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator
                .api.rev171120.delete.node.telemetry.subscription.destination.input.TelemetryNodeKey(nodeId));
        nodeBuilder.setNodeId(nodeId);
        if (null == subId) {
            nodeBuilder.setTelemetryNodeSubscription(null);
        } else {
            org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120
                    .delete.node.telemetry.subscription.destination.input.telemetry.node
                    .TelemetryNodeSubscriptionBuilder subscriptionBuilder = new org.opendaylight
                    .yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api
                    .rev171120.delete.node.telemetry.subscription.destination.input.telemetry.node
                    .TelemetryNodeSubscriptionBuilder();
            subscriptionBuilder.withKey(new org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang
                    .configurator.api.rev171120.delete.node.telemetry.subscription.destination.input.telemetry.node
                    .TelemetryNodeSubscriptionKey(subId));
            subscriptionBuilder.setSubscriptionName(subId);
            if (null == desId) {
                subscriptionBuilder.setTelemetryNodeSubscriptionDestination(null);
            } else {
                TelemetryNodeSubscriptionDestinationBuilder destinationBuilder = new
                        TelemetryNodeSubscriptionDestinationBuilder();
                destinationBuilder.withKey(new TelemetryNodeSubscriptionDestinationKey(desId));
                destinationBuilder.setDestinationGroupId(desId);
                List<TelemetryNodeSubscriptionDestination> destinationList = new ArrayList<>();
                destinationList.add(destinationBuilder.build());
                subscriptionBuilder.setTelemetryNodeSubscriptionDestination(destinationList);
            }
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api
                    .rev171120.delete.node.telemetry.subscription.destination.input.telemetry.node
                    .TelemetryNodeSubscription> subscriptionList = new ArrayList<>();
            subscriptionList.add(subscriptionBuilder.build());
            nodeBuilder.setTelemetryNodeSubscription(subscriptionList);
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.telemetry.params.xml.ns.yang.configurator.api.rev171120
                .delete.node.telemetry.subscription.destination.input.TelemetryNode> nodeList = new ArrayList<>();
        nodeList.add(nodeBuilder.build());
        DeleteNodeTelemetrySubscriptionDestinationInputBuilder builder = new
                DeleteNodeTelemetrySubscriptionDestinationInputBuilder();
        return builder.setTelemetryNode(nodeList).build();
    }

}
