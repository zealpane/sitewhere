/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.sources.coap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.MessageDeliverer;

import com.sitewhere.rest.model.device.communication.DeviceRequest.Type;
import com.sitewhere.sources.decoder.coap.CoapJsonDecoder;
import com.sitewhere.sources.spi.IInboundEventReceiver;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Take care of all SiteWhere message handling.
 * 
 * @author Derek
 */
public class CoapMessageDeliverer implements MessageDeliverer {

    /** Path segment for device interactions */
    private static final String PATH_DEVICES = "devices";

    /** Path segment for adding measurements */
    private static final String PATH_MEASUREMENTS = "measurements";

    /** Path segment for adding alerts */
    private static final String PATH_ALERTS = "alerts";

    /** Path segment for adding locations */
    private static final String PATH_LOCATIONS = "locations";

    /** Path segment for adding acks */
    private static final String PATH_ACKS = "acks";

    /** Receiver that handles incoming events */
    private IInboundEventReceiver<byte[]> eventReceiver;

    public CoapMessageDeliverer(IInboundEventReceiver<byte[]> eventReceiver) {
	this.eventReceiver = eventReceiver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.californium.core.server.MessageDeliverer#deliverRequest(org.
     * eclipse. californium.core.network.Exchange)
     */
    @Override
    public void deliverRequest(Exchange exchange) {
	OptionSet options = exchange.getRequest().getOptions();
	List<String> paths = options.getUriPath();
	handleRequest(getEventReceiver().getTenantEngine().getTenant(), paths, exchange);
    }

    /**
     * Handle a CoAP request.
     * 
     * @param tenant
     * @param paths
     * @param exchange
     */
    protected void handleRequest(ITenant tenant, List<String> paths, Exchange exchange) {
	String resourceType = paths.remove(0);
	if (PATH_DEVICES.equals(resourceType)) {
	    handleDeviceRequest(tenant, paths, exchange);
	} else {
	    createAndSendResponse(ResponseCode.BAD_REQUEST, "Unknown tenant resource type: " + resourceType, exchange);
	}
    }

    /**
     * Handle a request for device-specific functionality.
     * 
     * @param tenant
     * @param paths
     * @param exchange
     */
    protected void handleDeviceRequest(ITenant tenant, List<String> paths, Exchange exchange) {
	if (paths.size() == 0) {
	    createAndSendResponse(ResponseCode.BAD_REQUEST, "No device token specified.", exchange);
	} else {
	    String deviceToken = paths.remove(0);
	    switch (exchange.getRequest().getCode()) {
	    case POST: {
		handlePerDevicePostRequest(tenant, deviceToken, paths, exchange);
		break;
	    }
	    default: {
		createAndSendResponse(ResponseCode.BAD_REQUEST, "Operation not available for device.", exchange);
	    }
	    }
	}
    }

    /**
     * Handle POST request scoped to a specific device.
     * 
     * @param tenant
     * @param deviceToken
     * @param paths
     * @param exchange
     */
    protected void handlePerDevicePostRequest(ITenant tenant, String deviceToken, List<String> paths,
	    Exchange exchange) {
	if (paths.size() > 0) {
	    String operation = paths.remove(0);
	    if (PATH_MEASUREMENTS.equals(operation)) {
		handleAddDeviceMeasurement(tenant, deviceToken, paths, exchange);
	    } else if (PATH_ALERTS.equals(operation)) {
		handleAddDeviceAlert(tenant, deviceToken, paths, exchange);
	    } else if (PATH_LOCATIONS.equals(operation)) {
		handleAddDeviceLocation(tenant, deviceToken, paths, exchange);
	    } else if (PATH_ACKS.equals(operation)) {
		handleAddDeviceAck(tenant, deviceToken, paths, exchange);
	    }
	} else {
	    handleDeviceRegistration(tenant, deviceToken, exchange);
	}
    }

    /**
     * Handle device registration request.
     * 
     * @param tenant
     * @param deviceToken
     * @param exchange
     */
    protected void handleDeviceRegistration(ITenant tenant, String deviceToken, Exchange exchange) {
	Map<String, Object> metadata = new HashMap<String, Object>();
	metadata.put(CoapJsonDecoder.META_EVENT_TYPE, Type.RegisterDevice.name());
	metadata.put(CoapJsonDecoder.META_DEVICE_TOKEN, deviceToken);
	getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
	createAndSendResponse(ResponseCode.CONTENT, "Device registration submitted successfully.", exchange);
    }

    /**
     * Handle add device measurement.
     * 
     * @param tenant
     * @param deviceToken
     * @param paths
     * @param exchange
     */
    protected void handleAddDeviceMeasurement(ITenant tenant, String deviceToken, List<String> paths,
	    Exchange exchange) {
	Map<String, Object> metadata = new HashMap<String, Object>();
	metadata.put(CoapJsonDecoder.META_EVENT_TYPE, Type.DeviceMeasurement.name());
	metadata.put(CoapJsonDecoder.META_DEVICE_TOKEN, deviceToken);
	getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
	createAndSendResponse(ResponseCode.CONTENT, "Device measurement submitted successfully.", exchange);
    }

    /**
     * Handle add device alert.
     * 
     * @param tenant
     * @param deviceToken
     * @param paths
     * @param exchange
     */
    protected void handleAddDeviceAlert(ITenant tenant, String deviceToken, List<String> paths, Exchange exchange) {
	Map<String, Object> metadata = new HashMap<String, Object>();
	metadata.put(CoapJsonDecoder.META_EVENT_TYPE, Type.DeviceAlert.name());
	metadata.put(CoapJsonDecoder.META_DEVICE_TOKEN, deviceToken);
	getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
	createAndSendResponse(ResponseCode.CONTENT, "Device alert submitted successfully.", exchange);
    }

    /**
     * Handle add device location.
     * 
     * @param tenant
     * @param deviceToken
     * @param paths
     * @param exchange
     */
    protected void handleAddDeviceLocation(ITenant tenant, String deviceToken, List<String> paths, Exchange exchange) {
	Map<String, Object> metadata = new HashMap<String, Object>();
	metadata.put(CoapJsonDecoder.META_EVENT_TYPE, Type.DeviceLocation.name());
	metadata.put(CoapJsonDecoder.META_DEVICE_TOKEN, deviceToken);
	getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
	createAndSendResponse(ResponseCode.CONTENT, "Device location submitted successfully.", exchange);
    }

    /**
     * Handle add device acknowledgement.
     * 
     * @param tenant
     * @param deviceToken
     * @param paths
     * @param exchange
     */
    protected void handleAddDeviceAck(ITenant tenant, String deviceToken, List<String> paths, Exchange exchange) {
	Map<String, Object> metadata = new HashMap<String, Object>();
	metadata.put(CoapJsonDecoder.META_EVENT_TYPE, Type.Acknowledge.name());
	metadata.put(CoapJsonDecoder.META_DEVICE_TOKEN, deviceToken);
	getEventReceiver().onEventPayloadReceived(exchange.getRequest().getPayload(), metadata);
	createAndSendResponse(ResponseCode.CONTENT, "Device acknowledgement submitted successfully.", exchange);
    }

    /**
     * Send an error response on the given exchange.
     * 
     * @param code
     * @param message
     * @param exchange
     */
    protected void createAndSendResponse(ResponseCode code, String message, Exchange exchange) {
	Response response = new Response(code);
	response.setPayload(message);
	exchange.sendResponse(response);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.californium.core.server.MessageDeliverer#deliverResponse(org.
     * eclipse. californium.core.network.Exchange,
     * org.eclipse.californium.core.coap.Response)
     */
    @Override
    public void deliverResponse(Exchange exchange, Response response) {
	if (response == null) {
	    throw new RuntimeException("Response must not be null");
	} else if (exchange == null) {
	    throw new RuntimeException("Exchange must not be null");
	} else if (exchange.getRequest() == null) {
	    throw new IllegalArgumentException("Exchange does not contain request");
	} else {
	    exchange.getRequest().setResponse(response);
	}
    }

    protected IInboundEventReceiver<byte[]> getEventReceiver() {
	return eventReceiver;
    }

    protected void setEventReceiver(IInboundEventReceiver<byte[]> eventReceiver) {
	this.eventReceiver = eventReceiver;
    }
}