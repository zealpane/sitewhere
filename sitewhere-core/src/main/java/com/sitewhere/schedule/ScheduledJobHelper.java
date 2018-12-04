/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.schedule;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sitewhere.rest.model.batch.request.BatchCommandForCriteriaRequest;
import com.sitewhere.rest.model.datatype.JsonDateSerializer;
import com.sitewhere.rest.model.scheduling.request.ScheduledJobCreateRequest;
import com.sitewhere.spi.scheduling.JobConstants;
import com.sitewhere.spi.scheduling.ScheduledJobState;
import com.sitewhere.spi.scheduling.ScheduledJobType;
import com.sitewhere.spi.scheduling.request.IScheduledJobCreateRequest;

/**
 * Helper class for building {@link IScheduledJobCreateRequest} instances based
 * on job types.
 * 
 * @author Derek
 */
public class ScheduledJobHelper {

    /**
     * Create job that will invoke a command on an assignment.
     * 
     * @param token
     * @param assignmentToken
     * @param commandToken
     * @param parameters
     * @param scheduleToken
     * @return
     */
    public static IScheduledJobCreateRequest createCommandInvocationJob(String token, String assignmentToken,
	    String commandToken, Map<String, String> parameters, String scheduleToken) {
	ScheduledJobCreateRequest job = new ScheduledJobCreateRequest();

	job.setToken(token);
	job.setJobType(ScheduledJobType.CommandInvocation);

	Map<String, String> config = new HashMap<String, String>();
	config.put(JobConstants.CommandInvocation.ASSIGNMENT_TOKEN, assignmentToken);
	config.put(JobConstants.CommandInvocation.COMMAND_TOKEN, commandToken);
	for (String key : parameters.keySet()) {
	    String value = parameters.get(key);
	    config.put(JobConstants.CommandInvocation.PARAMETER_PREFIX + key, value);
	}
	job.setJobConfiguration(config);
	job.setScheduleToken(scheduleToken);

	return job;
    }

    /**
     * Create request for a job that uses criteria to choose a list of devices on
     * which a command will be invoked.
     * 
     * @param token
     * @param request
     * @param scheduleToken
     * @return
     */
    public static IScheduledJobCreateRequest createBatchCommandInvocationJobByCriteria(String token,
	    BatchCommandForCriteriaRequest request, String scheduleToken) {
	ScheduledJobCreateRequest job = new ScheduledJobCreateRequest();
	job.setToken(token);
	job.setJobType(ScheduledJobType.BatchCommandInvocation);
	job.setJobState(ScheduledJobState.Unsubmitted);

	Map<String, String> config = new HashMap<String, String>();

	// Store command information.
	config.put(JobConstants.CommandInvocation.COMMAND_TOKEN, request.getCommandToken());
	for (String key : request.getParameterValues().keySet()) {
	    String value = request.getParameterValues().get(key);
	    config.put(JobConstants.CommandInvocation.PARAMETER_PREFIX + key, value);
	}

	// Store criteria information.
	config.put(JobConstants.BatchCommandInvocation.DEVICE_TYPE_TOKEN, request.getDeviceTypeToken());
	if (!StringUtils.isEmpty(request.getAreaToken())) {
	    config.put(JobConstants.BatchCommandInvocation.AREA_TOKEN, request.getAreaToken());
	}
	if (!StringUtils.isEmpty(request.getGroupToken())) {
	    config.put(JobConstants.BatchCommandInvocation.GROUP_TOKEN, request.getGroupToken());
	}
	if (!StringUtils.isEmpty(request.getGroupsWithRole())) {
	    config.put(JobConstants.BatchCommandInvocation.GROUP_ROLE, request.getGroupsWithRole());
	}
	if (request.getStartDate() != null) {
	    config.put(JobConstants.BatchCommandInvocation.START_DATE,
		    JsonDateSerializer.serialize(request.getStartDate()));
	}
	if (request.getEndDate() != null) {
	    config.put(JobConstants.BatchCommandInvocation.END_DATE,
		    JsonDateSerializer.serialize(request.getEndDate()));
	}
	job.setJobConfiguration(config);
	job.setScheduleToken(scheduleToken);

	return job;
    }
}