/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.microservice;

/**
 * Used to differentiate between functions provided by microservices.
 * 
 * @author Derek
 */
public interface IFunctionIdentifier {

    /**
     * Get path associated with identifier.
     * 
     * @return
     */
    public String getPath();

    /**
     * Get the short name used in events and other references.
     * 
     * @return
     */
    public String getShortName();
}
