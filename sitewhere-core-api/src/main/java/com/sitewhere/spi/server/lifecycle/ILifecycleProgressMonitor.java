/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.server.lifecycle;

import java.util.Deque;

import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.IMicroservice;
import com.sitewhere.spi.monitoring.IProgressReporter;

import io.opentracing.ActiveSpan;

/**
 * Allows progress to be monitored on long-running lifecycle tasks.
 * 
 * @author Derek
 */
public interface ILifecycleProgressMonitor extends IProgressReporter {

    /**
     * Get current list of nested contexts.
     * 
     * @return
     */
    public Deque<ILifecycleProgressContext> getContextStack();

    /**
     * Push a new nested context onto the stack.
     * 
     * @param context
     * @throws SiteWhereException
     */
    public void pushContext(ILifecycleProgressContext context) throws SiteWhereException;

    /**
     * Start a span for the current context.
     * 
     * @param name
     * @return
     * @throws SiteWhereException
     */
    public ActiveSpan createTracerSpan(String name) throws SiteWhereException;

    /**
     * Handle an error encountered while in a tracer span.
     * 
     * @param span
     * @param t
     */
    public void handleErrorInTracerSpan(ActiveSpan span, Throwable t);

    /**
     * Finish the given tracer span.
     * 
     * @param span
     */
    public void finishTracerSpan(ActiveSpan span);

    /**
     * Start progress on a new operation within the current nesting context.
     * 
     * @param operation
     * @throws SiteWhereException
     */
    public void startProgress(String operation) throws SiteWhereException;

    /**
     * Finish progress for the current operation. This results in reporting of
     * progress message.
     * 
     * @throws SiteWhereException
     */
    public void finishProgress() throws SiteWhereException;

    /**
     * Pop last context from the stack.
     * 
     * @return
     * @throws SiteWhereException
     */
    public ILifecycleProgressContext popContext() throws SiteWhereException;

    /**
     * Get microservice associated with component.
     * 
     * @return
     */
    public IMicroservice<?> getMicroservice();
}