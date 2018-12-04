/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.sources.spi;

import java.io.Serializable;

/**
 * Contains information decoded by an {@link IDeviceEventDecoder} including
 * hardware id and originator of the event.
 * 
 * @author Derek
 */
public interface IDecodedDeviceRequest<T> extends Serializable {

    /**
     * Get device token the request pertains to.
     * 
     * @return
     */
    public String getDeviceToken();

    /**
     * Get event originator if available.
     * 
     * @return
     */
    public String getOriginator();

    /**
     * Get event create request.
     * 
     * @return
     */
    public T getRequest();
}