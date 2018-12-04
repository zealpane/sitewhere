/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.microservice;

/**
 * Provides an identifier for classifying a microservice.
 * 
 * @author Derek
 *
 * @param <T>
 */
public interface IMicroserviceClassification<T extends IFunctionIdentifier> {

    /**
     * Get identifier that classifies microservice.
     * 
     * @return
     */
    public T getIdentifier();
}
