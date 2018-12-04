/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.event.persistence.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;
import com.sitewhere.rest.model.device.event.DeviceCommandResponse;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.event.DeviceEventType;
import com.sitewhere.spi.device.event.IDeviceCommandResponse;

/**
 * Handles mapping of device command response event fields to Cassandra records.
 * 
 * @author Derek
 */
public class CassandraDeviceCommandResponse implements ICassandraEventBinder<IDeviceCommandResponse> {

    /** Static instance */
    public static final ICassandraEventBinder<IDeviceCommandResponse> INSTANCE = new CassandraDeviceCommandResponse();

    // Response field.
    public static final String FIELD_RESPONSE = "response";

    // Originating event id field.
    public static final String FIELD_ORIGINATING_EVENT_ID = "orig_event_id";

    // Response event id field.
    public static final String FIELD_RESPONSE_EVENT_ID = "resp_event_id";

    // Response field.
    public static final String FIELD_RESPONSE_CONTENT = "response";

    /*
     * @see
     * com.sitewhere.event.persistence.cassandra.ICassandraEventBinder#bind(com.
     * sitewhere.event.persistence.cassandra.CassandraEventManagementClient,
     * com.datastax.driver.core.BoundStatement,
     * com.sitewhere.spi.device.event.IDeviceEvent)
     */
    @Override
    public void bind(CassandraEventManagementClient client, BoundStatement bound, IDeviceCommandResponse event)
	    throws SiteWhereException {
	CassandraDeviceCommandResponse.bindFields(client, bound, event);
    }

    /*
     * @see
     * com.sitewhere.event.persistence.cassandra.ICassandraEventBinder#load(com.
     * sitewhere.event.persistence.cassandra.CassandraEventManagementClient,
     * com.datastax.driver.core.Row)
     */
    @Override
    public IDeviceCommandResponse load(CassandraEventManagementClient client, Row row) throws SiteWhereException {
	DeviceCommandResponse event = new DeviceCommandResponse();
	CassandraDeviceCommandResponse.loadFields(client, event, row);
	return event;
    }

    /*
     * @see
     * com.sitewhere.event.persistence.cassandra.ICassandraEventBinder#getEventType(
     * )
     */
    @Override
    public DeviceEventType getEventType() {
	return DeviceEventType.CommandResponse;
    }

    /**
     * Bind fields from a device alert to an existing {@link BoundStatement}.
     * 
     * @param client
     * @param bound
     * @param response
     * @throws SiteWhereException
     */
    public static void bindFields(CassandraEventManagementClient client, BoundStatement bound,
	    IDeviceCommandResponse response) throws SiteWhereException {
	CassandraDeviceEvent.bindEventFields(bound, response);

	UDTValue udt = client.getResponseType().newValue();
	udt.setUUID(FIELD_ORIGINATING_EVENT_ID, response.getOriginatingEventId());
	udt.setUUID(FIELD_RESPONSE_EVENT_ID, response.getResponseEventId());
	udt.setString(FIELD_RESPONSE_CONTENT, response.getResponse());
	bound.setUDTValue(FIELD_RESPONSE, udt);
    }

    /**
     * Load fields from a row into a device command response.
     * 
     * @param client
     * @param response
     * @param row
     * @throws SiteWhereException
     */
    public static void loadFields(CassandraEventManagementClient client, DeviceCommandResponse response, Row row)
	    throws SiteWhereException {
	CassandraDeviceEvent.loadEventFields(response, row);

	UDTValue udt = row.getUDTValue(FIELD_RESPONSE);
	response.setOriginatingEventId(udt.getUUID(FIELD_ORIGINATING_EVENT_ID));
	response.setResponseEventId(udt.getUUID(FIELD_RESPONSE_EVENT_ID));
	response.setResponse(udt.getString(FIELD_RESPONSE_CONTENT));
    }
}
