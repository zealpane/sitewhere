/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.schedule.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.sitewhere.batch.BatchUtils;
import com.sitewhere.rest.model.batch.request.BatchCommandForCriteriaRequest;
import com.sitewhere.rest.model.batch.request.BatchCommandInvocationRequest;
import com.sitewhere.schedule.BatchCommandInvocationJobParser;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.asset.IAssetManagement;
import com.sitewhere.spi.batch.IBatchManagement;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.tenant.ITenant;
import com.sitewhere.spi.tenant.ITenantManagement;

/**
 * Creates a batch command invocation as the result of a Quartz schedule.
 * 
 * @author Derek
 */
public class BatchCommandInvocationJob implements Job {

    /** Static logger instance */
    private static Log LOGGER = LogFactory.getLog(BatchCommandInvocationJob.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
	Map<String, String> data = new HashMap<String, String>();
	JobDataMap jobData = context.getJobDetail().getJobDataMap();
	for (String key : jobData.keySet()) {
	    String value = jobData.getString(key);
	    data.put(key, value);
	}

	BatchCommandForCriteriaRequest criteria = BatchCommandInvocationJobParser.parse(data);
	if (criteria.getDeviceTypeToken() == null) {
	    throw new JobExecutionException("Device type token not provided.");
	}
	if (criteria.getCommandToken() == null) {
	    throw new JobExecutionException("Command token not provided.");
	}
	try {
	    ITenant tenant = getTenantManagement().getTenantByToken(context.getScheduler().getSchedulerName());

	    // Resolve tokens for devices matching criteria.
	    List<String> deviceTokens = BatchUtils.resolveDeviceTokensForCriteria(criteria, getDeviceManagement(),
		    getAssetManagement());

	    // Create batch command invocation.
	    BatchCommandInvocationRequest invoke = new BatchCommandInvocationRequest();
	    invoke.setCommandToken(criteria.getCommandToken());
	    invoke.setParameterValues(criteria.getParameterValues());
	    invoke.setDeviceTokens(deviceTokens);

	    getBatchManagement(tenant).createBatchCommandInvocation(invoke);

	    LOGGER.info("Executed batch command invocation job.");
	} catch (SiteWhereException e) {
	    throw new JobExecutionException("Unable to create batch command invocation.", e);
	} catch (SchedulerException e) {
	    throw new JobExecutionException("Unable to get scheduler name.", e);
	}
    }

    private IDeviceManagement getDeviceManagement() {
	return null;
    }

    private IAssetManagement getAssetManagement() {
	return null;
    }

    private ITenantManagement getTenantManagement() {
	return null;
    }

    private IBatchManagement getBatchManagement(ITenant tenant) {
	return null;
    }
}