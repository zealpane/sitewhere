/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.microservice.security;

import org.springframework.security.core.Authentication;

import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Support access to a global "superuser" for authorizing calls between
 * microservices.
 * 
 * @author Derek
 */
public interface ISystemUser {

    /**
     * Get authentication information for superuser.
     * 
     * @return
     * @throws SiteWhereException
     */
    public Authentication getAuthentication() throws SiteWhereException;

    /**
     * Get authentication for superuser in context of a given tenant.
     * 
     * @param tenant
     * @return
     * @throws SiteWhereException
     */
    public Authentication getAuthenticationForTenant(ITenant tenant) throws SiteWhereException;
}