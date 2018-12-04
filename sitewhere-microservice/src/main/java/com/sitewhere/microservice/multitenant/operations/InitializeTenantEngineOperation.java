/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.microservice.multitenant.operations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;

import com.sitewhere.microservice.multitenant.MultitenantMicroservice;
import com.sitewhere.server.lifecycle.LifecycleProgressContext;
import com.sitewhere.server.lifecycle.LifecycleProgressMonitor;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.IFunctionIdentifier;
import com.sitewhere.spi.microservice.multitenant.IMicroserviceTenantEngine;
import com.sitewhere.spi.microservice.multitenant.IMultitenantMicroservice;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Operation that adds a new tenant engine to an
 * {@link IMultitenantMicroservice} and initializes it.
 * 
 * @author Derek
 *
 * @param <T>
 */
public class InitializeTenantEngineOperation<I extends IFunctionIdentifier, T extends IMicroserviceTenantEngine>
	extends CompletableTenantEngineOperation<T> {

    /** Static logger instance */
    private static Log LOGGER = LogFactory.getLog(InitializeTenantEngineOperation.class);

    /** Max time to wait for tenant to be bootstrapped from template */
    private static final long MAX_WAIT_FOR_TENANT_BOOTSTRAPPED = 60 * 1000;

    /** Parent microservice */
    private MultitenantMicroservice<I, T> microservice;

    /** Tenant information */
    private ITenant tenant;

    public InitializeTenantEngineOperation(MultitenantMicroservice<I, T> microservice, ITenant tenant,
	    CompletableFuture<T> completableFuture) {
	super(completableFuture);
	this.microservice = microservice;
	this.tenant = tenant;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public T call() throws Exception {
	T created = null;
	try {
	    LOGGER.info("Creating tenant engine for '" + getTenant().getName() + "'...");
	    created = getMicroservice().createTenantEngine(getTenant());
	    created.setTenantEngine(created); // Required for nested components.

	    // Configuration files must be present before initialization.
	    LOGGER.info("Verifying tenant '" + getTenant().getName() + "' configuration bootstrapped.");
	    waitForTenantConfigurationBootstrapped();

	    // Initialize new engine.
	    LOGGER.info("Intializing tenant engine for '" + getTenant().getName() + "'.");
	    ILifecycleProgressMonitor monitor = new LifecycleProgressMonitor(
		    new LifecycleProgressContext(1, "Initialize tenant engine."), getMicroservice());
	    long start = System.currentTimeMillis();
	    getMicroservice().initializeNestedComponent(created, monitor, true);

	    // Mark tenant engine as initialized and remove failed engine if present.
	    getMicroservice().getInitializedTenantEngines().put(getTenant().getId(), created);
	    getMicroservice().getFailedTenantEngines().remove(getTenant().getId());

	    LOGGER.info("Tenant engine for '" + getTenant().getName() + "' initialized in "
		    + (System.currentTimeMillis() - start) + "ms.");
	    getCompletableFuture().complete(created);
	    return created;
	} catch (Throwable t) {
	    // Keep map of failed tenant engines.
	    if (created != null) {
		getMicroservice().getFailedTenantEngines().put(getTenant().getId(), created);
	    }

	    LOGGER.error("Unable to initialize tenant engine for '" + getTenant().getName() + "'.", t);
	    getCompletableFuture().completeExceptionally(t);
	    throw t;
	} finally {
	    // Make sure that tenant is cleared from the pending map.
	    getMicroservice().getInitializingTenantEngines().remove(getTenant().getId());
	}
    }

    /**
     * Wait until tenant configuration has been bootstrapped before starting
     * initialization.
     * 
     * @throws SiteWhereException
     */
    protected void waitForTenantConfigurationBootstrapped() throws SiteWhereException {
	CuratorFramework curator = getMicroservice().getZookeeperManager().getCurator();
	try {
	    long deadline = System.currentTimeMillis() + MAX_WAIT_FOR_TENANT_BOOTSTRAPPED;
	    while ((deadline - System.currentTimeMillis()) > 0) {
		if (curator.checkExists().forPath(
			getMicroservice().getInstanceTenantBootstrappedIndicatorPath(getTenant().getId())) != null) {
		    return;
		}
		Thread.sleep(1000);
	    }
	    throw new SiteWhereException("Tenant not bootstrapped within time limit. Aborting");
	} catch (Throwable t) {
	    throw new SiteWhereException("Unable to wait for tenant configuration bootstrap.", t);
	}
    }

    public MultitenantMicroservice<I, T> getMicroservice() {
	return microservice;
    }

    public void setMicroservice(MultitenantMicroservice<I, T> microservice) {
	this.microservice = microservice;
    }

    public ITenant getTenant() {
	return tenant;
    }

    public void setTenant(ITenant tenant) {
	this.tenant = tenant;
    }

    public static <I extends IFunctionIdentifier, T extends IMicroserviceTenantEngine> CompletableFuture<T> createCompletableFuture(
	    MultitenantMicroservice<I, T> microservice, ITenant tenant, ExecutorService executor) {
	CompletableFuture<T> completableFuture = new CompletableFuture<T>();
	executor.submit(new InitializeTenantEngineOperation<I, T>(microservice, tenant, completableFuture));
	return completableFuture;
    }
}