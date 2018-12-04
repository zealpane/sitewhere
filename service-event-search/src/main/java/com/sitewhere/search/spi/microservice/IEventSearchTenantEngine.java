/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.search.spi.microservice;

import com.sitewhere.search.spi.ISearchProvidersManager;
import com.sitewhere.spi.microservice.multitenant.IMicroserviceTenantEngine;

/**
 * Extends {@link IMicroserviceTenantEngine} with features specific to event
 * search.
 * 
 * @author Derek
 */
public interface IEventSearchTenantEngine extends IMicroserviceTenantEngine {

    /**
     * Get the search providers manager.
     * 
     * @return
     */
    public ISearchProvidersManager getSearchProvidersManager();
}