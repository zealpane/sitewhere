/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.microservice.multitenant.operations;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import com.sitewhere.spi.microservice.multitenant.IMicroserviceTenantEngine;

/**
 * Common base class for tenant operations that use {@link CompletableFuture} to
 * chain calls.
 * 
 * @author Derek
 *
 * @param <T>
 */
public abstract class CompletableTenantEngineOperation<T extends IMicroserviceTenantEngine> implements Callable<T> {

    /** Completable future that tracks progress */
    private CompletableFuture<T> completableFuture;

    public CompletableTenantEngineOperation(CompletableFuture<T> completableFuture) {
	this.completableFuture = completableFuture;
    }

    public CompletableFuture<T> getCompletableFuture() {
	return completableFuture;
    }

    public void setCompletableFuture(CompletableFuture<T> completableFuture) {
	this.completableFuture = completableFuture;
    }
}