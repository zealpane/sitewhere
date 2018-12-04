/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.schedule.spi.microservice;

import com.sitewhere.grpc.service.ScheduleManagementGrpc;
import com.sitewhere.spi.microservice.multitenant.IMicroserviceTenantEngine;
import com.sitewhere.spi.scheduling.IScheduleManagement;

/**
 * Extends {@link IMicroserviceTenantEngine} with features specific to schedule
 * management.
 * 
 * @author Derek
 */
public interface IScheduleManagementTenantEngine extends IMicroserviceTenantEngine {

    /**
     * Get associated schedule management implementation.
     * 
     * @return
     */
    public IScheduleManagement getScheduleManagement();

    /**
     * Get implementation class that wraps schedule management with GRPC
     * conversions.
     * 
     * @return
     */
    public ScheduleManagementGrpc.ScheduleManagementImplBase getScheduleManagementImpl();
}