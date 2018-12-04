/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.event.persistence.cassandra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.sitewhere.cassandra.CassandraClient;
import com.sitewhere.event.persistence.DeviceEventManagementPersistence;
import com.sitewhere.event.spi.microservice.IEventManagementMicroservice;
import com.sitewhere.rest.model.device.event.DeviceAlert;
import com.sitewhere.rest.model.device.event.DeviceCommandInvocation;
import com.sitewhere.rest.model.device.event.DeviceCommandResponse;
import com.sitewhere.rest.model.device.event.DeviceLocation;
import com.sitewhere.rest.model.device.event.DeviceMeasurement;
import com.sitewhere.rest.model.device.event.DeviceStateChange;
import com.sitewhere.rest.model.search.Pager;
import com.sitewhere.rest.model.search.SearchResults;
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
 * Implementation of {@link IDeviceEventManagement} that stores events in Apache
 * Cassandra.
 * 
 * @author Derek
 */
public class CassandraDeviceEventManagement extends TenantEngineLifecycleComponent implements IDeviceEventManagement {

    /** Configured Cassandra client */
    private CassandraClient client;

    /** Cassandra event management client */
    private CassandraEventManagementClient cassandraEventManagementClient;

    public CassandraDeviceEventManagement() {
	super(LifecycleComponentType.DataStore);
    }

    /*
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#initialize(com.sitewhere.
     * spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void initialize(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	if (getClient() == null) {
	    throw new SiteWhereException("No Cassandra client configured.");
	}
	getClient().initialize(monitor);

	// Initialize event management client wrapper.
	this.cassandraEventManagementClient = new CassandraEventManagementClient(getClient());
	getCassandraEventManagementClient().initialize(monitor);
    }

    /*
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#start(com.sitewhere.spi.
     * server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	getClient().start(monitor);
	getCassandraEventManagementClient().start(monitor);
    }

    /*
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#stop(com.sitewhere.spi.
     * server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void stop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	getCassandraEventManagementClient().stop(monitor);
	getClient().stop(monitor);
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

    /*
     * @see
     * com.sitewhere.spi.device.event.IDeviceEventManagement#getDeviceEventById(java
     * .util.UUID)
     */
    @Override
    public IDeviceEvent getDeviceEventById(UUID eventId) throws SiteWhereException {
	throw new SiteWhereException("Not implemented.");
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * getDeviceEventByAlternateId(java.lang.String)
     */
    @Override
    public IDeviceEvent getDeviceEventByAlternateId(String alternateId) throws SiteWhereException {
	throw new SiteWhereException("Not implemented.");
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
	    DeviceMeasurement mx = DeviceEventManagementPersistence.deviceMeasurementCreateLogic(request, assignment);
	    storeDeviceEvent(assignment, mx, CassandraDeviceMeasurement.INSTANCE);
	    result.add(mx);
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
	return searchEventsByIndex(index, entityIds, criteria, CassandraDeviceMeasurement.INSTANCE);
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
	    storeDeviceEvent(assignment, location, CassandraDeviceLocation.INSTANCE);
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
	return searchEventsByIndex(index, entityIds, criteria, CassandraDeviceLocation.INSTANCE);
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
	    storeDeviceEvent(assignment, alert, CassandraDeviceAlert.INSTANCE);
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
	return searchEventsByIndex(index, entityIds, criteria, CassandraDeviceAlert.INSTANCE);
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
	    DeviceCommandInvocation invocation = DeviceEventManagementPersistence
		    .deviceCommandInvocationCreateLogic(assignment, request);
	    storeDeviceEvent(assignment, invocation, CassandraDeviceCommandInvocation.INSTANCE);
	    result.add(invocation);
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
	return searchEventsByIndex(index, entityIds, criteria, CassandraDeviceCommandInvocation.INSTANCE);
    }

    /*
     * @see com.sitewhere.spi.device.event.IDeviceEventManagement#
     * listDeviceCommandInvocationResponses(java.util.UUID)
     */
    @Override
    public ISearchResults<IDeviceCommandResponse> listDeviceCommandInvocationResponses(UUID invocationId)
	    throws SiteWhereException {
	throw new SiteWhereException("Not implemented.");
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
	    DeviceCommandResponse response = DeviceEventManagementPersistence
		    .deviceCommandResponseCreateLogic(assignment, request);
	    storeDeviceEvent(assignment, response, CassandraDeviceCommandResponse.INSTANCE);
	    result.add(response);
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
	return searchEventsByIndex(index, entityIds, criteria, CassandraDeviceCommandResponse.INSTANCE);
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
	    DeviceStateChange state = DeviceEventManagementPersistence.deviceStateChangeCreateLogic(assignment,
		    request);
	    storeDeviceEvent(assignment, state, CassandraDeviceStateChange.INSTANCE);
	    result.add(state);
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
	return searchEventsByIndex(index, entityIds, criteria, CassandraDeviceStateChange.INSTANCE);
    }

    /**
     * Store a device event using various indexing strategies.
     * 
     * @param assignment
     * @param event
     * @param binder
     * @throws SiteWhereException
     */
    protected <I extends IDeviceEvent> void storeDeviceEvent(IDeviceAssignment assignment, I event,
	    ICassandraEventBinder<I> binder) throws SiteWhereException {
	// Build insert for event by id.
	BoundStatement eventById = getCassandraEventManagementClient().getInsertDeviceEventById().bind();
	binder.bind(getCassandraEventManagementClient(), eventById, event);
	process(eventById, event);

	// Build insert for event by alternate id.
	if (event.getAlternateId() != null) {
	    BoundStatement eventByAltId = getCassandraEventManagementClient().getInsertDeviceEventByAltId().bind();
	    binder.bind(getCassandraEventManagementClient(), eventByAltId, event);
	    process(eventByAltId, event);
	}

	// Build insert for event by assignment.
	BoundStatement eventByAssn = getCassandraEventManagementClient().getInsertDeviceEventByAssignment().bind();
	binder.bind(getCassandraEventManagementClient(), eventByAssn, event);
	eventByAssn.setInt("bucket", getClient().getBucketValue(event.getEventDate().getTime()));
	process(eventByAssn, event);

	// Build insert for event by customer.
	if (assignment.getCustomerId() != null) {
	    BoundStatement eventByCustomer = getCassandraEventManagementClient().getInsertDeviceEventByCustomer()
		    .bind();
	    binder.bind(getCassandraEventManagementClient(), eventByCustomer, event);
	    eventByCustomer.setInt("bucket", getClient().getBucketValue(event.getEventDate().getTime()));
	    process(eventByCustomer, event);
	}

	// Build insert for event by area.
	if (assignment.getAreaId() != null) {
	    BoundStatement eventByArea = getCassandraEventManagementClient().getInsertDeviceEventByArea().bind();
	    binder.bind(getCassandraEventManagementClient(), eventByArea, event);
	    eventByArea.setInt("bucket", getClient().getBucketValue(event.getEventDate().getTime()));
	    process(eventByArea, event);
	}

	// Build insert for event by asset.
	if (assignment.getAssetId() != null) {
	    BoundStatement eventByAsset = getCassandraEventManagementClient().getInsertDeviceEventByAsset().bind();
	    binder.bind(getCassandraEventManagementClient(), eventByAsset, event);
	    eventByAsset.setInt("bucket", getClient().getBucketValue(event.getEventDate().getTime()));
	    process(eventByAsset, event);
	}
    }

    /**
     * Search events by a given index.
     * 
     * @param index
     * @param entityIds
     * @param eventType
     * @param criteria
     * @param binder
     * @return
     * @throws SiteWhereException
     */
    protected <I extends IDeviceEvent> ISearchResults<I> searchEventsByIndex(DeviceEventIndex index,
	    List<UUID> entityIds, IDateRangeSearchCriteria criteria, ICassandraEventBinder<I> binder)
	    throws SiteWhereException {
	PreparedStatement query = getQueryForIndex(index);
	Pager<I> pager = new Pager<>(criteria);
	List<Integer> buckets = getBucketsForDateRange(criteria);
	for (int bucket : buckets) {
	    List<ResultSet> perBucket = listResultsForBucket(query, entityIds, criteria, binder.getEventType(), bucket);
	    List<I> bucketEvents = new ArrayList<>();
	    for (ResultSet perKey : perBucket) {
		for (Row row : perKey) {
		    bucketEvents.add(binder.load(getCassandraEventManagementClient(), row));
		}
	    }
	    addSortedEventsToPager(pager, bucketEvents, bucket);
	}
	return new SearchResults<I>(pager.getResults(), pager.getTotal());
    }

    /**
     * Process a Cassandra query and handle failures.
     * 
     * @param statement
     * @param event
     * @throws SiteWhereException
     */
    protected void process(BoundStatement statement, IDeviceEvent event) throws SiteWhereException {
	ResultSetFuture future = getClient().getSession().executeAsync(statement);
	Futures.addCallback(future, new FutureCallback<ResultSet>() {
	    /*
	     * @see
	     * com.google.common.util.concurrent.FutureCallback#onSuccess(java.lang.Object)
	     */
	    @Override
	    public void onSuccess(ResultSet result) {
	    }

	    /*
	     * @see com.google.common.util.concurrent.FutureCallback#onFailure(java.lang.
	     * Throwable)
	     */
	    @Override
	    public void onFailure(Throwable t) {
		getLogger().error("Failed to persist Cassandra event: " + event.getId(), t);
	    }
	}, MoreExecutors.directExecutor());
    }

    /**
     * Get query that corresponds to the given event index.
     * 
     * @param index
     * @return
     * @throws SiteWhereException
     */
    protected PreparedStatement getQueryForIndex(DeviceEventIndex index) throws SiteWhereException {
	switch (index) {
	case Assignment: {
	    return getCassandraEventManagementClient().getSelectEventsByAssignmentForType();
	}
	case Customer: {
	    return getCassandraEventManagementClient().getSelectEventsByCustomerForType();
	}
	case Area: {
	    return getCassandraEventManagementClient().getSelectEventsByAreaForType();
	}
	case Asset: {
	    return getCassandraEventManagementClient().getSelectEventsByAssetForType();
	}
	}
	throw new SiteWhereException("Index type not implemented: " + index.name());
    }

    /**
     * Find the list of buckets required to cover a given date range.
     * 
     * @param client
     * @param criteria
     * @return
     */
    protected List<Integer> getBucketsForDateRange(IDateRangeSearchCriteria criteria) {
	long bucket = getClient().getBucketLengthInMs();
	long current = criteria.getEndDate() != null ? criteria.getEndDate().getTime() : System.currentTimeMillis();
	long start = criteria.getStartDate() != null ? criteria.getStartDate().getTime() : current - 1;
	List<Integer> buckets = new ArrayList<>();
	while (current >= start) {
	    buckets.add(getClient().getBucketValue(current));
	    current -= bucket;
	}
	getLogger().debug("Processing " + buckets.size() + " buckets.");
	return buckets;
    }

    /**
     * Perform parallel queries to get results for a single bucket.
     * 
     * @param keyField
     * @param keys
     * @param criteria
     * @param eventType
     * @param bucket
     * @return
     * @throws SiteWhereException
     */
    protected List<ResultSet> listResultsForBucket(PreparedStatement statement, List<UUID> keys,
	    IDateRangeSearchCriteria criteria, DeviceEventType eventType, int bucket) throws SiteWhereException {
	List<ResultSetFuture> futures = new ArrayList<>();
	for (UUID key : keys) {
	    BoundStatement query = statement.bind();
	    query.setUUID(0, key);
	    query.setByte(1, CassandraDeviceEvent.getIndicatorForEventType(eventType));
	    query.setInt(2, bucket);
	    query.setTimestamp(3, criteria.getStartDate());
	    query.setTimestamp(4, criteria.getEndDate());
	    ResultSetFuture resultSetFuture = getClient().getSession().executeAsync(query);
	    futures.add(resultSetFuture);
	}
	List<ResultSet> results = new ArrayList<>();
	for (ResultSetFuture future : futures) {
	    results.add(future.getUninterruptibly());
	}
	return results;
    }

    /**
     * Sort the list of device events, then add them to the pager.
     * 
     * @param pager
     * @param events
     */
    protected <T extends IDeviceEvent> void addSortedEventsToPager(Pager<T> pager, List<T> events, int bucket) {
	Collections.sort(events, new Comparator<T>() {

	    @Override
	    public int compare(IDeviceEvent o1, IDeviceEvent o2) {
		return 1 - (o1.getEventDate().compareTo(o2.getEventDate()));
	    }
	});
	for (T event : events) {
	    pager.process(event);
	}
	getLogger().debug("Processed " + events.size() + " events for bucket " + bucket + ".");
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

    public CassandraClient getClient() {
	return client;
    }

    public void setClient(CassandraClient client) {
	this.client = client;
    }

    protected CassandraEventManagementClient getCassandraEventManagementClient() {
	return cassandraEventManagementClient;
    }

    protected void setCassandraEventManagementClient(CassandraEventManagementClient cassandraEventManagementClient) {
	this.cassandraEventManagementClient = cassandraEventManagementClient;
    }
}