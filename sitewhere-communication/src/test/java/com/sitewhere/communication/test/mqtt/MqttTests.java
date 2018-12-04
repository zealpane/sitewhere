/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.communication.test.mqtt;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sitewhere.rest.model.device.communication.DeviceRequest;
import com.sitewhere.rest.model.device.communication.DeviceRequest.Type;
import com.sitewhere.rest.model.device.event.request.DeviceAlertCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceLocationCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceMeasurementCreateRequest;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.event.AlertLevel;
import com.sitewhere.spi.device.event.AlertSource;

public class MqttTests {

    /** Nunber of threads for multithreaded tests */
    private static final int NUM_THREADS = 1;

    /** Nunber of calls performed per thread */
    private static final int NUM_CALLS_PER_THREAD = 100;

    @Test
    public void runMqttTest() throws Exception {
	ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
	CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executor);

	for (int i = 0; i < NUM_THREADS; i++) {
	    completionService.submit(new MqttTester(NUM_CALLS_PER_THREAD));
	}
	for (int i = 0; i < NUM_THREADS; ++i) {
	    completionService.take().get();
	}
    }

    public class MqttTester implements Callable<Void> {

	/** Message count */
	private int messageCount;

	/** MQTT settings */
	private MQTT mqtt;

	/** MQTT connection */
	private BlockingConnection connection;

	/** Global object mapper instance */
	private ObjectMapper MAPPER = new ObjectMapper();

	public MqttTester(int messageCount) {
	    this.messageCount = messageCount;
	}

	@Override
	public Void call() throws Exception {
	    try {
		this.mqtt = new MQTT();
		mqtt.setHost("192.168.171.129", 1883);
		this.connection = mqtt.blockingConnection();
		connection.connect();
		System.out.println("Connected to: " + mqtt.getHost());

		long start = System.currentTimeMillis();
		for (int i = 0; i < messageCount; i++) {
		    sendLocationOverMqtt();
		}
		System.out
			.println("Sent " + messageCount + " events in " + (System.currentTimeMillis() - start) + "ms.");

		connection.disconnect();
		return null;
	    } catch (URISyntaxException e) {
		throw new RuntimeException(e);
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}

	/**
	 * Send a location event request via JSON/MQTT.
	 * 
	 * @throws SiteWhereException
	 */
	public void sendLocationOverMqtt() throws SiteWhereException {
	    DeviceRequest request = new DeviceRequest();
	    request.setDeviceToken("25683-LAIPAC-S911-7539094");
	    request.setType(Type.DeviceLocation);
	    DeviceLocationCreateRequest location = new DeviceLocationCreateRequest();
	    location.setEventDate(new Date());
	    location.setLatitude(34.10469794977326);
	    location.setLongitude(-84.23966646194458);
	    location.setElevation(0.0);
	    Map<String, String> metadata = new HashMap<String, String>();
	    metadata.put("fromMQTT", "true");
	    location.setMetadata(metadata);
	    location.setUpdateState(true);
	    request.setRequest(location);
	    try {
		String payload = MAPPER.writeValueAsString(request);
		connection.publish("SiteWhere/default/input/json", payload.getBytes(), QoS.AT_MOST_ONCE, false);
	    } catch (JsonProcessingException e) {
		throw new SiteWhereException(e);
	    } catch (Exception e) {
		throw new SiteWhereException(e);
	    }
	}

	/**
	 * Send an alert event request via JSON/MQTT.
	 * 
	 * @throws SiteWhereException
	 */
	public void sendAlertOverMqtt() throws SiteWhereException {
	    DeviceRequest request = new DeviceRequest();
	    request.setDeviceToken("4545-UNO-9430527");
	    request.setType(Type.DeviceAlert);
	    DeviceAlertCreateRequest alert = new DeviceAlertCreateRequest();
	    alert.setSource(AlertSource.Device);
	    alert.setLevel(AlertLevel.Info);
	    alert.setType("engine.overheat");
	    alert.setMessage("eeeeyyyyyy!");
	    alert.setEventDate(new Date(System.currentTimeMillis() - (10 * 60 * 1000)));
	    Map<String, String> metadata = new HashMap<String, String>();
	    metadata.put("name1", "value1");
	    metadata.put("name2", "value2");
	    alert.setMetadata(metadata);
	    alert.setUpdateState(true);
	    request.setRequest(alert);
	    try {
		String payload = MAPPER.writeValueAsString(request);
		// System.out.println(payload);
		connection.publish("SiteWhere/default/input/json", payload.getBytes(), QoS.AT_MOST_ONCE, false);
	    } catch (JsonProcessingException e) {
		throw new SiteWhereException(e);
	    } catch (Exception e) {
		throw new SiteWhereException(e);
	    }
	}

	/**
	 * Send device measurements request with non-numeric measurement values.
	 * 
	 * @throws SiteWhereException
	 */
	public void sendNonStandardMeasurements() throws SiteWhereException {
	    DeviceRequest request = new DeviceRequest();
	    request.setDeviceToken("123-TEST-4567890");
	    request.setType(Type.DeviceMeasurement);
	    DeviceMeasurementCreateRequest mx = new DeviceMeasurementCreateRequest();
	    mx.setName("normal");
	    mx.setValue(1.234);
	    Map<String, String> metadata = new HashMap<String, String>();
	    metadata.put("fromMQTT", "true");
	    mx.setMetadata(metadata);
	    mx.setUpdateState(true);
	    request.setRequest(mx);
	    try {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = mapper.convertValue(request, JsonNode.class);
		ObjectNode mxsNode = (ObjectNode) json.get("request").get("measurements");
		mxsNode.put("stringTest", "value");
		mxsNode.put("booleanTest", true);
		String payload = mapper.writeValueAsString(json);

		System.out.println("Payload:\n\n" + payload);
		connection.publish("SiteWhere/default/input/json", payload.getBytes(), QoS.AT_LEAST_ONCE, false);
		System.out.println("Message sent successfully.");
	    } catch (JsonProcessingException e) {
		throw new SiteWhereException(e);
	    } catch (Exception e) {
		throw new SiteWhereException(e);
	    }
	}
    }
}
