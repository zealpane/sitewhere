/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.microservice.multitenant;

import java.util.UUID;

import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.IFunctionIdentifier;
import com.sitewhere.spi.microservice.configuration.IConfigurableMicroservice;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Microservice that contains engines for multiple tenants.
 * 
 * @author Derek
 */
public interface IMultitenantMicroservice<I extends IFunctionIdentifier, T extends IMicroserviceTenantEngine>
	extends IConfigurableMicroservice<I> {

    /**
     * Create tenant engine specific to microservice function.
     * 
     * @param tenant
     * @return
     * @throws SiteWhereException
     */
    public T createTenantEngine(ITenant tenant) throws SiteWhereException;

    /**
     * Get tenant engine corresponding to the given id.
     * 
     * @param tenantId
     * @return
     * @throws SiteWhereException
     */
    public T getTenantEngineByTenantId(UUID tenantId) throws SiteWhereException;

    /**
     * Make sure the given tenant engine exists and is started.
     * 
     * @param tenantId
     * @return
     * @throws SiteWhereException
     */
    public T assureTenantEngineAvailable(UUID tenantId) throws TenantEngineNotAvailableException;

    /**
     * Shuts down and restarts the given tenant engine.
     * 
     * @param tenantId
     * @throws SiteWhereException
     */
    public void restartTenantEngine(UUID tenantId) throws SiteWhereException;

    /**
     * Shuts down and removes a tenant engine.
     * 
     * @param tenantId
     * @throws SiteWhereException
     */
    public void removeTenantEngine(UUID tenantId) throws SiteWhereException;

    /**
     * Get configuration for the given tenant.
     * 
     * @param tenantId
     * @return
     * @throws SiteWhereException
     */
    public byte[] getTenantConfiguration(UUID tenantId) throws SiteWhereException;

    /**
     * Update configuration for the given tenant.
     * 
     * @param tenantId
     * @param content
     * @throws SiteWhereException
     */
    public void updateTenantConfiguration(UUID tenantId, byte[] content) throws SiteWhereException;
}