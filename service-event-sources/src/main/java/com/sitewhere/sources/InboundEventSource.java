/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.sources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.codahale.metrics.Meter;
import com.sitewhere.server.lifecycle.TenantEngineLifecycleComponent;
import com.sitewhere.sources.spi.EventDecodeException;
import com.sitewhere.sources.spi.IDecodedDeviceRequest;
import com.sitewhere.sources.spi.IDeviceEventDecoder;
import com.sitewhere.sources.spi.IDeviceEventDeduplicator;
import com.sitewhere.sources.spi.IEventSourcesManager;
import com.sitewhere.sources.spi.IInboundEventReceiver;
import com.sitewhere.sources.spi.IInboundEventSource;
import com.sitewhere.sources.spi.microservice.IEventSourcesTenantEngine;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.server.lifecycle.LifecycleComponentType;

/**
 * Default implementation of {@link IInboundEventSource}.
 * 
 * @author Derek
 * 
 * @param <T>
 */
public abstract class InboundEventSource<T> extends TenantEngineLifecycleComponent implements IInboundEventSource<T> {

    /** Unique id for referencing source */
    private String sourceId;

    /** Device event decoder */
    private IDeviceEventDecoder<T> deviceEventDecoder;

    /** Device event deduplicator (optional) */
    private IDeviceEventDeduplicator deviceEventDeduplicator;

    /** List of {@link IInboundEventReceiver} that supply this processor */
    private List<IInboundEventReceiver<T>> inboundEventReceivers = new ArrayList<IInboundEventReceiver<T>>();

    /** Meter for counting decoded events */
    private Meter decodedEvents;

    /** Meter for counting decode failures */
    private Meter decodeFailures;

    /** Meter for counting duplicate events */
    private Meter duplicates;

    public InboundEventSource() {
	super(LifecycleComponentType.InboundEventSource);
    }

    /*
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#initialize(com.sitewhere.
     * spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void initialize(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.initialize(monitor);

	if ((getInboundEventReceivers() == null) || (getInboundEventReceivers().size() == 0)) {
	    throw new SiteWhereException("No inbound event receivers registered for event source.");
	}
	if (getDeviceEventDecoder() == null) {
	    throw new SiteWhereException("No device event decoder assigned.");
	}

	// Set up metrics.
	this.decodedEvents = createMeterMetric(getMetricPrefix() + "decodedEvents");
	this.decodeFailures = createMeterMetric(getMetricPrefix() + "decodeFailures");
	this.duplicates = createMeterMetric(getMetricPrefix() + "duplicates");

	// Initialize device event decoder.
	initializeNestedComponent(getDeviceEventDecoder(), monitor, true);

	// Initialize device event deduplicator if provided.
	if (getDeviceEventDeduplicator() != null) {
	    initializeNestedComponent(getDeviceEventDeduplicator(), monitor, true);
	}

	// Initialize event receivers.
	for (IInboundEventReceiver<T> receiver : getInboundEventReceivers()) {
	    receiver.setEventSource(this);
	    initializeNestedComponent(receiver, monitor, true);
	}
    }

    /**
     * Get prefix appended to metrics.
     * 
     * @return
     */
    protected String getMetricPrefix() {
	return getSourceId() + ".";
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
	super.start(monitor);

	// Start device event decoder.
	startNestedComponent(getDeviceEventDecoder(), monitor, true);

	// Start device event deduplicator if provided.
	if (getDeviceEventDeduplicator() != null) {
	    startNestedComponent(getDeviceEventDeduplicator(), monitor, true);
	}

	// Start event receivers.
	for (IInboundEventReceiver<T> receiver : getInboundEventReceivers()) {
	    startNestedComponent(receiver, monitor, true);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#stop(com.sitewhere.spi.
     * server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void stop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.stop(monitor);

	// Stop all inbound event receivers.
	if (getInboundEventReceivers().size() > 0) {
	    for (IInboundEventReceiver<T> receiver : getInboundEventReceivers()) {
		stopNestedComponent(receiver, monitor);
	    }
	}

	// Stop device event decoder.
	stopNestedComponent(getDeviceEventDecoder(), monitor);

	// Stop device event deduplicator if present.
	if (getDeviceEventDeduplicator() != null) {
	    stopNestedComponent(getDeviceEventDeduplicator(), monitor);
	}
    }

    /*
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#terminate(com.sitewhere.spi
     * .server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void terminate(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.terminate(monitor);
	// Terminate all inbound event receivers.
	if (getInboundEventReceivers().size() > 0) {
	    for (IInboundEventReceiver<T> receiver : getInboundEventReceivers()) {
		receiver.lifecycleTerminate(monitor);
	    }
	}

	// Terminate device event decoder.
	getDeviceEventDecoder().lifecycleTerminate(monitor);

	// Terminate device event deduplicator if present.
	if (getDeviceEventDeduplicator() != null) {
	    getDeviceEventDeduplicator().lifecycleTerminate(monitor);
	}
    }

    /*
     * @see
     * com.sitewhere.sources.spi.IInboundEventSource#onEncodedEventReceived(com.
     * sitewhere.sources.spi.IInboundEventReceiver, java.lang.Object, java.util.Map)
     */
    @Override
    public void onEncodedEventReceived(IInboundEventReceiver<T> receiver, T encoded, Map<String, Object> metadata) {
	List<IDecodedDeviceRequest<?>> requests = decodeEvent(encoded, metadata);
	if (requests != null) {
	    for (IDecodedDeviceRequest<?> decoded : requests) {
		getDecodedEvents().mark();
		if (shouldProcess(decoded)) {
		    handleDecodedRequest(encoded, metadata, decoded);
		}
	    }
	}
    }

    /**
     * Decode an event into zero or more requests.
     * 
     * @param encoded
     * @param metadata
     * @return
     */
    protected List<IDecodedDeviceRequest<?>> decodeEvent(T encoded, Map<String, Object> metadata) {
	try {
	    return decodePayload(encoded, metadata);
	} catch (EventDecodeException e) {
	    onEventDecodeFailed(encoded, metadata, e);
	    return null;
	}
    }

    /**
     * Indicates if a decoded record should be processed. Returning false skips
     * processing for the request.
     * 
     * @param decoded
     * @return
     */
    protected boolean shouldProcess(IDecodedDeviceRequest<?> decoded) {
	try {
	    boolean isDuplicate = ((getDeviceEventDeduplicator() != null)
		    && (getDeviceEventDeduplicator().isDuplicate(decoded)));
	    if (isDuplicate) {
		getDuplicates().mark();
		getLogger().info("Event not processed due to duplicate detected.");
	    }
	    return !isDuplicate;
	} catch (SiteWhereException e) {
	    getLogger().error("Error determining whether request should be processed. Skipping.", e);
	    return false;
	}
    }

    /**
     * Pass decoded events to the {@link IEventSourcesManager} for further
     * processing.
     * 
     * @param encoded
     * @param metadata
     * @param decoded
     */
    protected void handleDecodedRequest(T encoded, Map<String, Object> metadata, IDecodedDeviceRequest<?> decoded) {
	try {
	    getEventSourcesManager().handleDecodedEvent(getSourceId(), getRawPayload(encoded), metadata, decoded);
	} catch (SiteWhereException e) {
	    getLogger().error("Unable to handle decoded event.", e);
	}
    }

    /**
     * Pass failed decoded to the {@link IEventSourcesManager} for further
     * processing.
     * 
     * @param encoded
     * @param metadata
     * @param t
     */
    protected void onEventDecodeFailed(T encoded, Map<String, Object> metadata, Throwable t) {
	try {
	    getDecodeFailures().mark();
	    getEventSourcesManager().handleFailedDecode(getSourceId(), getRawPayload(encoded), metadata, t);
	} catch (SiteWhereException e) {
	    getLogger().error("Unable to handle failed event decode.", e);
	}
    }

    /**
     * Decode a payload into individual events.
     * 
     * @param encodedPayload
     * @param metadata
     * @return
     * @throws EventDecodeException
     */
    protected List<IDecodedDeviceRequest<?>> decodePayload(T encodedPayload, Map<String, Object> metadata)
	    throws EventDecodeException {
	return getDeviceEventDecoder().decode(encodedPayload, metadata);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.communication.IInboundEventSource#getSourceId()
     */
    @Override
    public String getSourceId() {
	return sourceId;
    }

    public void setSourceId(String sourceId) {
	this.sourceId = sourceId;
    }

    /*
     * @see com.sitewhere.sources.spi.IInboundEventSource#getDeviceEventDecoder()
     */
    @Override
    public IDeviceEventDecoder<T> getDeviceEventDecoder() {
	return deviceEventDecoder;
    }

    public void setDeviceEventDecoder(IDeviceEventDecoder<T> deviceEventDecoder) {
	this.deviceEventDecoder = deviceEventDecoder;
    }

    /*
     * @see
     * com.sitewhere.sources.spi.IInboundEventSource#getDeviceEventDeduplicator()
     */
    @Override
    public IDeviceEventDeduplicator getDeviceEventDeduplicator() {
	return deviceEventDeduplicator;
    }

    public void setDeviceEventDeduplicator(IDeviceEventDeduplicator deviceEventDeduplicator) {
	this.deviceEventDeduplicator = deviceEventDeduplicator;
    }

    /*
     * @see com.sitewhere.sources.spi.IInboundEventSource#getInboundEventReceivers()
     */
    @Override
    public List<IInboundEventReceiver<T>> getInboundEventReceivers() {
	return inboundEventReceivers;
    }

    public void setInboundEventReceivers(List<IInboundEventReceiver<T>> inboundEventReceivers) {
	this.inboundEventReceivers = inboundEventReceivers;
    }

    protected Meter getDecodedEvents() {
	return decodedEvents;
    }

    protected Meter getDecodeFailures() {
	return decodeFailures;
    }

    protected Meter getDuplicates() {
	return duplicates;
    }

    protected IEventSourcesManager getEventSourcesManager() {
	return ((IEventSourcesTenantEngine) getTenantEngine()).getEventSourcesManager();
    }
}