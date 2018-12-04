/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.commands.destination.coap;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import com.sitewhere.commands.spi.ICommandDeliveryProvider;
import com.sitewhere.server.lifecycle.TenantEngineLifecycleComponent;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceNestingContext;
import com.sitewhere.spi.device.command.IDeviceCommandExecution;
import com.sitewhere.spi.server.lifecycle.LifecycleComponentType;

/**
 * Delivers commands via CoAP by creating a client request to send command data
 * to the remote device.
 * 
 * @author Derek
 */
public class CoapCommandDeliveryProvider extends TenantEngineLifecycleComponent
	implements ICommandDeliveryProvider<byte[], CoapParameters> {

    public CoapCommandDeliveryProvider() {
	super(LifecycleComponentType.CommandDeliveryProvider);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.communication.ICommandDeliveryProvider#deliver(
     * com. sitewhere.spi.device.IDeviceNestingContext,
     * com.sitewhere.spi.device.IDeviceAssignment,
     * com.sitewhere.spi.device.command.IDeviceCommandExecution, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public void deliver(IDeviceNestingContext nested, IDeviceAssignment assignment, IDeviceCommandExecution execution,
	    byte[] encoded, CoapParameters parameters) throws SiteWhereException {
	CoapClient client = createCoapClient(parameters);
	CoapResponse response = null;
	getLogger().debug(parameters.toString());
	getLogger().debug("Delivering command as " + parameters.getMethod().toUpperCase() + " to " + client.getURI());
	if ("put".equalsIgnoreCase(parameters.getMethod())) {
	    response = client.put(encoded, MediaTypeRegistry.APPLICATION_JSON);
	} else {
	    response = client.post(encoded, MediaTypeRegistry.APPLICATION_JSON);
	}
	if (response != null) {
	    getLogger().info("Response from delivering command: " + response.getResponseText());
	} else {
	    getLogger().info("No response from delivering command.");
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.communication.ICommandDeliveryProvider#
     * deliverSystemCommand(com.sitewhere.spi.device.IDeviceNestingContext,
     * com.sitewhere.spi.device.IDeviceAssignment, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public void deliverSystemCommand(IDeviceNestingContext nested, IDeviceAssignment assignment, byte[] encoded,
	    CoapParameters parameters) throws SiteWhereException {
	CoapClient client = createCoapClient(parameters);
	CoapResponse response = null;
	getLogger().debug(parameters.toString());
	getLogger().debug(
		"Delivering system command as " + parameters.getMethod().toUpperCase() + " to " + client.getURI());
	if ("put".equalsIgnoreCase(parameters.getMethod())) {
	    response = client.put(encoded, MediaTypeRegistry.APPLICATION_JSON);
	} else {
	    response = client.post(encoded, MediaTypeRegistry.APPLICATION_JSON);
	}
	if (response != null) {
	    getLogger().info("Response from delivering system command: " + response.getResponseText());
	} else {
	    getLogger().info("No response from delivering system command.");
	}
    }

    /**
     * Create a CoAP client based on extracted parameters.
     * 
     * @param parameters
     * @return
     */
    protected CoapClient createCoapClient(CoapParameters parameters) {
	return new CoapClient(
		"coap://" + parameters.getHostname() + ":" + parameters.getPort() + "/" + parameters.getUrl());
    }
}