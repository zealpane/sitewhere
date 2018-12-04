/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.event.persistence.influxdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.influxdb.dto.Point;

import com.sitewhere.event.persistence.DeviceEventManagementPersistence;
import com.sitewhere.event.spi.microservice.IEventManagementMicroservice;
import com.sitewhere.influxdb.InfluxDbClient;
import com.sitewhere.rest.model.device.event.DeviceAlert;
import com.sitewhere.rest.model.device.event.DeviceCommandInvocation;
import com.sitewhere.rest.model.device.event.DeviceCommandResponse;
import com.sitewhere.rest.model.device.event.DeviceLocation;
import com.sitewhere.rest.model.device.event.DeviceMeasurement;
import com.sitewhere.rest.model.device.event.DeviceStateChange;
import com.sitewhere.server.lifecycle.TenantEngineLifecycleComponent;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.event.DeviceEventIndex;
import com.sitewhere.spi.device.event.DeviceEventType;
import com.sitewhere.spi.device.event.IDeviceAlert;
import com.sitewhere.spi.device.event.IDeviceCommandInvocation;
import com.sitewhere.spi.device.event.IDeviceCommandResponse;
import com.sitewhere.spi.device.event.IDeviceEvent;
import com.sitewhere.spi.device.event.IDeviceEventBatch;
import com.sitewhere.spi.device.event.IDeviceEventBatchResponse;
import com.sitewhere.spi.device.event.IDeviceEventManagement;
import com.sitewhere.spi.device.event.IDeviceLocation;
import com.sitewhere.spi.device.event.IDeviceMeasurement;
import com.sitewhere.spi.device.event.IDeviceStateChange;
import com.sitewhere.spi.device.event.request.IDeviceAlertCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceCommandInvocationCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceCommandResponseCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceLocationCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceMeasurementCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceStateChangeCreateRequest;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;
import com.sitewhere.spi.search.IDateRangeSearchCriteria;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.server.lifecycle.LifecycleComponentType;

/**
 * Implementation of {@link IDeviceEventManagement} that stores events in
 * InfluxDB.
 * 
 * @author Derek
 */
public class InfluxDbDeviceEventManagement extends TenantEngineLifecycleComponent implements IDeviceEventManagement {

    /** Client */
    private InfluxDbClient client;

    /**
     * Prefix to compare against when adding user defined tags from assignment meta
     * data
     */
    private final String ASSIGNMENT_META_DATA_TAG_PREFIX = "INFLUX_TAG_";

    /** Assignment meta data tag to check for user defined retention policy */
    private final String ASSIGNMENT_META_DATA_RETENTION_POLICY = "INFLUX_RETENTION_POLICY";

    public InfluxDbDeviceEventManagement() {
	super(LifecycleComponentType.DataStore);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#start(com.sitewhere.spi
     * .server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	if (getClient() == null) {
	    throw new SiteWhereException("No InfluxDB client configured.");
	}
	getClient().start(monitor);
    }

    /*
     * @see
     * com.sitewhere.spi.device.event.IDeviceEventManagement#addDeviceEventBatch(
     * java.util.UUID, com.sitewhere.spi.device.event.IDeviceEventBatch)
     */
    @Override
    public IDeviceEventBatchResponse addDeviceEventBatch(UUID deviceAssignmentId, IDeviceEventBatch batch)
	    throws SiteWhereException {
	IDeviceAssignment assignment = assertDeviceAssignmentById(deviceAssignmentId);
	return DeviceEventManagementPersistence.deviceEventBatchLogic(assignment, batch, this);
    }

    /**
     * Add any user defined tags from assignment metadata. A tag should be prefixed
     * with ASSIGNMENT_META_DATA_TAG_PREFIX i.e INFLUX_TAG_displayName. The prefix
     * will be removed and a new tag created using the remaining characters as the
     * tag name with value metadata.key assigned to it.
     *
     * @param assignment
     * @param builder
     */
    protected void addUserDefinedTags(IDeviceAssignment assignment, Point.Builder builder) {
	Map<String, String> assignmentMetaData = assignment.getMetadata();

	if (assignmentMetaData != null) {
	    for (Map.Entry<String, String> metaData : assignmentMetaData.entrySet()) {
		String metaDataKey = metaData.getKey().trim();
		if (metaDataKey.length() == 0) {
		    continue;
		}

		String metaDataValue = metaData.getValue();
		if (metaDataValue == null) {
		    continue;
		}

		metaDataValue = metaDataValue.trim();
		if (metaDataValue.length() == 0) {
		    continue;
		}

		if (metaDataKey.startsWith(ASSIGNMENT_META_DATA_TAG_PREFIX)
			&& metaDataKey.length() > ASSIGNMENT_META_DATA_TAG_PREFIX.length()) {
		    InfluxDbDeviceEvent.addUserDefinedTag(metaDataKey.replaceFirst(ASSIGNMENT_META_DATA_TAG_PREFIX, ""),
			    metaDataValue, builder);
		}
	    }
	}
    }

    /*
     * @see
     * com.sitewhere.spi.device.event.IDeviceEventManagement#getDeviceEventById(java
     * .util.UUID)
     */
    @Override
    public IDeviceEvent getDeviceEventById(UUID eventId) throws SiteWhereException {
	return InfluxDbDeviceEvent.getEventById(eventId, getClient());
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * getDeviceEventByAlternateId(java.lang.String)
     */
    @Override
    public IDeviceEvent getDeviceEventByAlternateId(String alternateId) throws SiteWhereException {
	return InfluxDbDeviceEvent.getEventByAlternateId(alternateId, getClient());
    }

    /*
     * Check if the user has specific a retention policy in the assignment meta-data
     * If so, override the default one.
     */
    private String getAssignmentSpecificRetentionPolicy(IDeviceAssignment assignment) {
	String policy = assignment.getMetadata().get(ASSIGNMENT_META_DATA_RETENTION_POLICY);

	if (policy == null) {
	    return getClient().getConfiguration().getRetention();
	}
	return policy;
    }

    /*
     * @see
     * com.sitewhere.spi.device.event.IDeviceEventManagement#addDeviceMeasurements(
     * java.util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceMeasurementCreateRequest[])
     */
    @Override
    public List<IDeviceMeasurement> addDeviceMeasurements(UUID deviceAssignmentId,
	    IDeviceMeasurementCreateRequest... requests) throws SiteWhereException {
	List<IDeviceMeasurement> result = new ArrayList<>();
	IDeviceAssignment assignment = assertDeviceAssignmentById(deviceAssignmentId);
	for (IDeviceMeasurementCreateRequest request : requests) {
	    DeviceMeasurement mxs = DeviceEventManagementPersistence.deviceMeasurementCreateLogic(request, assignment);
	    Point.Builder builder = InfluxDbDeviceEvent.createBuilder();
	    InfluxDbDeviceMeasurements.saveToBuilder(mxs, builder);
	    addUserDefinedTags(assignment, builder);
	    getClient().getInflux().write(getClient().getDatabase().getValue(),
		    getAssignmentSpecificRetentionPolicy(assignment), builder.build());
	    result.add(mxs);
	}
	return result;
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * listDeviceMeasurementsForIndex(com.sitewhere.spi.device.event.
     * DeviceEventIndex, java.util.List,
     * com.sitewhere.spi.search.IDateRangeSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceMeasurement> listDeviceMeasurementsForIndex(DeviceEventIndex index,
	    List<UUID> entityIds, IDateRangeSearchCriteria criteria) throws SiteWhereException {
	return InfluxDbDeviceEvent.searchByIndex(index, entityIds, DeviceEventType.Measurement, criteria, getClient(),
		IDeviceMeasurement.class);
    }

    /*
     * @see
     * com.sitewhere.spi.device.event.IDeviceEventManagement#addDeviceLocations(java
     * .util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceLocationCreateRequest[])
     */
    @Override
    public List<IDeviceLocation> addDeviceLocations(UUID deviceAssignmentId, IDeviceLocationCreateRequest... requests)
	    throws SiteWhereException {
	List<IDeviceLocation> result = new ArrayList<>();
	IDeviceAssignment assignment = assertDeviceAssignmentById(deviceAssignmentId);
	for (IDeviceLocationCreateRequest request : requests) {
	    DeviceLocation location = DeviceEventManagementPersistence.deviceLocationCreateLogic(assignment, request);
	    Point.Builder builder = InfluxDbDeviceEvent.createBuilder();
	    InfluxDbDeviceLocation.saveToBuilder(location, builder);
	    addUserDefinedTags(assignment, builder);
	    getClient().getInflux().write(getClient().getDatabase().getValue(),
		    getAssignmentSpecificRetentionPolicy(assignment), builder.build());
	    result.add(location);
	}
	return result;
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * listDeviceLocationsForIndex(com.sitewhere.spi.device.event.DeviceEventIndex,
     * java.util.List, com.sitewhere.spi.search.IDateRangeSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceLocation> listDeviceLocationsForIndex(DeviceEventIndex index, List<UUID> entityIds,
	    IDateRangeSearchCriteria criteria) throws SiteWhereException {
	return InfluxDbDeviceEvent.searchByIndex(index, entityIds, DeviceEventType.Location, criteria, getClient(),
		IDeviceLocation.class);
    }

    /*
     * @see
     * com.sitewhere.spi.device.event.IDeviceEventManagement#addDeviceAlerts(java.
     * util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceAlertCreateRequest[])
     */
    @Override
    public List<IDeviceAlert> addDeviceAlerts(UUID deviceAssignmentId, IDeviceAlertCreateRequest... requests)
	    throws SiteWhereException {
	List<IDeviceAlert> result = new ArrayList<>();
	IDeviceAssignment assignment = assertDeviceAssignmentById(deviceAssignmentId);
	for (IDeviceAlertCreateRequest request : requests) {
	    DeviceAlert alert = DeviceEventManagementPersistence.deviceAlertCreateLogic(assignment, request);
	    Point.Builder builder = InfluxDbDeviceEvent.createBuilder();
	    InfluxDbDeviceAlert.saveToBuilder(alert, builder);
	    addUserDefinedTags(assignment, builder);
	    getClient().getInflux().write(getClient().getDatabase().getValue(),
		    getAssignmentSpecificRetentionPolicy(assignment), builder.build());
	    result.add(alert);
	}
	return result;
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * listDeviceAlertsForIndex(com.sitewhere.spi.device.event.DeviceEventIndex,
     * java.util.List, com.sitewhere.spi.search.IDateRangeSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceAlert> listDeviceAlertsForIndex(DeviceEventIndex index, List<UUID> entityIds,
	    IDateRangeSearchCriteria criteria) throws SiteWhereException {
	return InfluxDbDeviceEvent.searchByIndex(index, entityIds, DeviceEventType.Alert, criteria, getClient(),
		IDeviceAlert.class);
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * addDeviceCommandInvocations(java.util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceCommandInvocationCreateRequest[
     * ])
     */
    @Override
    public List<IDeviceCommandInvocation> addDeviceCommandInvocations(UUID deviceAssignmentId,
	    IDeviceCommandInvocationCreateRequest... requests) throws SiteWhereException {
	List<IDeviceCommandInvocation> result = new ArrayList<>();
	IDeviceAssignment assignment = assertDeviceAssignmentById(deviceAssignmentId);
	for (IDeviceCommandInvocationCreateRequest request : requests) {
	    DeviceCommandInvocation ci = DeviceEventManagementPersistence.deviceCommandInvocationCreateLogic(assignment,
		    request);
	    Point.Builder builder = InfluxDbDeviceEvent.createBuilder();
	    InfluxDbDeviceCommandInvocation.saveToBuilder(ci, builder);
	    addUserDefinedTags(assignment, builder);
	    getClient().getInflux().write(getClient().getDatabase().getValue(),
		    getAssignmentSpecificRetentionPolicy(assignment), builder.build());
	    result.add(ci);
	}
	return result;
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * listDeviceCommandInvocationsForIndex(com.sitewhere.spi.device.event.
     * DeviceEventIndex, java.util.List,
     * com.sitewhere.spi.search.IDateRangeSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceCommandInvocation> listDeviceCommandInvocationsForIndex(DeviceEventIndex index,
	    List<UUID> entityIds, IDateRangeSearchCriteria criteria) throws SiteWhereException {
	return InfluxDbDeviceEvent.searchByIndex(index, entityIds, DeviceEventType.CommandInvocation, criteria,
		getClient(), IDeviceCommandInvocation.class);
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * listDeviceCommandInvocationResponses(java.util.UUID)
     */
    @Override
    public ISearchResults<IDeviceCommandResponse> listDeviceCommandInvocationResponses(UUID invocationId)
	    throws SiteWhereException {
	return InfluxDbDeviceCommandResponse.getResponsesForInvocation(invocationId, getClient().getInflux(),
		getClient().getDatabase().getValue());
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * addDeviceCommandResponses(java.util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceCommandResponseCreateRequest[])
     */
    @Override
    public List<IDeviceCommandResponse> addDeviceCommandResponses(UUID deviceAssignmentId,
	    IDeviceCommandResponseCreateRequest... requests) throws SiteWhereException {
	List<IDeviceCommandResponse> result = new ArrayList<>();
	IDeviceAssignment assignment = assertDeviceAssignmentById(deviceAssignmentId);
	for (IDeviceCommandResponseCreateRequest request : requests) {
	    DeviceCommandResponse cr = DeviceEventManagementPersistence.deviceCommandResponseCreateLogic(assignment,
		    request);
	    Point.Builder builder = InfluxDbDeviceEvent.createBuilder();
	    InfluxDbDeviceCommandResponse.saveToBuilder(cr, builder);
	    addUserDefinedTags(assignment, builder);
	    getClient().getInflux().write(getClient().getDatabase().getValue(),
		    getAssignmentSpecificRetentionPolicy(assignment), builder.build());
	    result.add(cr);
	}
	return result;
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * listDeviceCommandResponsesForIndex(com.sitewhere.spi.device.event.
     * DeviceEventIndex, java.util.List,
     * com.sitewhere.spi.search.IDateRangeSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceCommandResponse> listDeviceCommandResponsesForIndex(DeviceEventIndex index,
	    List<UUID> entityIds, IDateRangeSearchCriteria criteria) throws SiteWhereException {
	return InfluxDbDeviceEvent.searchByIndex(index, entityIds, DeviceEventType.CommandResponse, criteria,
		getClient(), IDeviceCommandResponse.class);
    }

    /*
     * @see
     * com.sitewhere.spi.device.event.IDeviceEventManagement#addDeviceStateChanges(
     * java.util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceStateChangeCreateRequest[])
     */
    @Override
    public List<IDeviceStateChange> addDeviceStateChanges(UUID deviceAssignmentId,
	    IDeviceStateChangeCreateRequest... requests) throws SiteWhereException {
	List<IDeviceStateChange> result = new ArrayList<>();
	IDeviceAssignment assignment = assertDeviceAssignmentById(deviceAssignmentId);
	for (IDeviceStateChangeCreateRequest request : requests) {
	    DeviceStateChange sc = DeviceEventManagementPersistence.deviceStateChangeCreateLogic(assignment, request);
	    Point.Builder builder = InfluxDbDeviceEvent.createBuilder();
	    InfluxDbDeviceStateChange.saveToBuilder(sc, builder);
	    addUserDefinedTags(assignment, builder);
	    getClient().getInflux().write(getClient().getDatabase().getValue(),
		    getAssignmentSpecificRetentionPolicy(assignment), builder.build());
	    result.add(sc);
	}
	return result;
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * listDeviceStateChangesForIndex(com.sitewhere.spi.device.event.
     * DeviceEventIndex, java.util.List,
     * com.sitewhere.spi.search.IDateRangeSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceStateChange> listDeviceStateChangesForIndex(DeviceEventIndex index,
	    List<UUID> entityIds, IDateRangeSearchCriteria criteria) throws SiteWhereException {
	return InfluxDbDeviceEvent.searchByIndex(index, entityIds, DeviceEventType.StateChange, criteria, getClient(),
		IDeviceStateChange.class);
    }

    /**
     * Assert that a device assignment exists and throw an exception if not.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    protected IDeviceAssignment assertDeviceAssignmentById(UUID id) throws SiteWhereException {
	IDeviceAssignment assignment = getDeviceManagement().getDeviceAssignment(id);
	if (assignment == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentId, ErrorLevel.ERROR);
	}
	return assignment;
    }

    protected IDeviceManagement getDeviceManagement() {
	return ((IEventManagementMicroservice) getTenantEngine().getMicroservice()).getDeviceManagementApiDemux()
		.getApiChannel();
    }

    public InfluxDbClient getClient() {
	return client;
    }

    public void setClient(InfluxDbClient client) {
	this.client = client;
    }
}