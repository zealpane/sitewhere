/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.sources.spi;

import java.util.Map;

import com.sitewhere.spi.server.lifecycle.ITenantEngineLifecycleComponent;

/**
 * Handles receipt of device event information from an underlying transport.
 * 
 * @author Derek
 */
public interface IInboundEventReceiver<T> extends ITenantEngineLifecycleComponent {

    /**
     * Get name shown in user interfaces when referencing receiver.
     * 
     * @return
     */
    public String getDisplayName();

    /**
     * Called when an event payload is received.
     * 
     * @param payload
     * @param metadata
     */
    public void onEventPayloadReceived(T payload, Map<String, Object> metadata);

    /**
     * Set the parent event source that will process events.
     * 
     * @param source
     */
    public void setEventSource(IInboundEventSource<T> source);

    /**
     * Get the parent event source.
     * 
     * @return
     */
    public IInboundEventSource<T> getEventSource();
}