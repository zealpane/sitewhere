/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.connectors.spi;

import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.event.IDeviceEvent;
import com.sitewhere.spi.device.event.IDeviceEventContext;
import com.sitewhere.spi.server.lifecycle.ITenantEngineLifecycleComponent;

/**
 * Simple interface to support event filtering.
 * 
 * @author Derek
 */
public interface IDeviceEventFilter extends ITenantEngineLifecycleComponent {

    /**
     * Indicates if an event should be filtered.
     * 
     * @param context
     * @param event
     * @return
     * @throws SiteWhereException
     */
    public boolean isFiltered(IDeviceEventContext context, IDeviceEvent event) throws SiteWhereException;
}