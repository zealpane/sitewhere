/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.device;

import java.io.Serializable;

import com.sitewhere.spi.device.element.IDeviceElementSchema;

/**
 * Maps a location in an {@link IDeviceElementSchema} to an {@link IDevice}.
 * 
 * @author Derek
 */
public interface IDeviceElementMapping extends Serializable {

    /**
     * Get path in {@link IDeviceElementSchema} being mapped.
     * 
     * @return
     */
    public String getDeviceElementSchemaPath();

    /**
     * Get token of {@link IDevice} being mapped.
     * 
     * @return
     */
    public String getDeviceToken();
}