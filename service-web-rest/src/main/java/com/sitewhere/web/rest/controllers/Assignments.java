/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.web.rest.controllers;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sitewhere.core.DataUtils;
import com.sitewhere.device.charting.ChartBuilder;
import com.sitewhere.device.marshaling.DeviceAssignmentMarshalHelper;
import com.sitewhere.device.marshaling.DeviceCommandInvocationMarshalHelper;
import com.sitewhere.grpc.client.event.BlockingDeviceEventManagement;
import com.sitewhere.grpc.client.spi.client.IDeviceEventManagementApiChannel;
import com.sitewhere.rest.model.device.DeviceAssignment;
import com.sitewhere.rest.model.device.event.DeviceCommandResponse;
import com.sitewhere.rest.model.device.event.request.DeviceAlertCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceCommandInvocationCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceCommandResponseCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceLocationCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceMeasurementCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceStateChangeCreateRequest;
import com.sitewhere.rest.model.device.request.DeviceAssignmentCreateRequest;
import com.sitewhere.rest.model.device.request.DeviceStreamCreateRequest;
import com.sitewhere.rest.model.device.streaming.DeviceStream;
import com.sitewhere.rest.model.search.DateRangeSearchCriteria;
import com.sitewhere.rest.model.search.SearchResults;
import com.sitewhere.rest.model.search.device.DeviceAssignmentSearchCriteria;
import com.sitewhere.schedule.ScheduledJobHelper;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.asset.IAsset;
import com.sitewhere.spi.asset.IAssetManagement;
import com.sitewhere.spi.device.DeviceAssignmentStatus;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.charting.IChartSeries;
import com.sitewhere.spi.device.command.IDeviceCommand;
import com.sitewhere.spi.device.event.DeviceEventIndex;
import com.sitewhere.spi.device.event.IDeviceAlert;
import com.sitewhere.spi.device.event.IDeviceCommandInvocation;
import com.sitewhere.spi.device.event.IDeviceCommandResponse;
import com.sitewhere.spi.device.event.IDeviceLocation;
import com.sitewhere.spi.device.event.IDeviceMeasurement;
import com.sitewhere.spi.device.event.IDeviceStateChange;
import com.sitewhere.spi.device.streaming.IDeviceStream;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;
import com.sitewhere.spi.label.ILabel;
import com.sitewhere.spi.label.ILabelGeneration;
import com.sitewhere.spi.scheduling.IScheduleManagement;
import com.sitewhere.spi.scheduling.IScheduledJob;
import com.sitewhere.spi.scheduling.request.IScheduledJobCreateRequest;
import com.sitewhere.spi.search.IDateRangeSearchCriteria;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.user.SiteWhereRoles;
import com.sitewhere.web.annotation.SiteWhereCrossOrigin;
import com.sitewhere.web.rest.RestControllerBase;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/*
 * Controller for assignment operations.
 * 
 * @author Derek Adams
 */
@RestController
@SiteWhereCrossOrigin
@RequestMapping(value = "/assignments")
@Api(value = "assignments")
public class Assignments extends RestControllerBase {

    /** Static logger instance */
    private static Log LOGGER = LogFactory.getLog(Assignments.class);

    /** Default date range for event queries */
    private static final long DEFAULT_EVENT_QUERY_DATE_RANGE = 24 * 60 * 60 * 1000;

    /**
     * Used by AJAX calls to create a device assignment.
     * 
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Create a new device assignment")
    @Secured({ SiteWhereRoles.REST })
    public DeviceAssignment createDeviceAssignment(@RequestBody DeviceAssignmentCreateRequest request,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceManagement management = getDeviceManagement();
	IDeviceAssignment created = management.createDeviceAssignment(request);
	DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper(getDeviceManagement());
	helper.setIncludeAsset(true);
	helper.setIncludeDevice(true);
	helper.setIncludeArea(true);
	return helper.convert(created, getAssetManagement());
    }

    /**
     * Get device assignment by token.
     * 
     * @param token
     * @param servletRequest
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token:.+}", method = RequestMethod.GET)
    @ApiOperation(value = "Get device assignment by token")
    @Secured({ SiteWhereRoles.REST })
    public DeviceAssignment getDeviceAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceAssignment assignment = assureAssignment(token, servletRequest);
	DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper(getDeviceManagement());
	helper.setIncludeAsset(true);
	helper.setIncludeDevice(true);
	helper.setIncludeArea(true);
	helper.setIncludeDeviceType(true);
	return helper.convert(assignment, getAssetManagement());
    }

    /**
     * Delete an existing device assignment.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token:.+}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete an existing device assignment")
    @Secured({ SiteWhereRoles.REST })
    public DeviceAssignment deleteDeviceAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token)
	    throws SiteWhereException {
	IDeviceAssignment existing = assertDeviceAssignment(token);
	IDeviceAssignment assignment = getDeviceManagement().deleteDeviceAssignment(existing.getId());
	DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper(getDeviceManagement());
	helper.setIncludeAsset(true);
	helper.setIncludeDevice(true);
	helper.setIncludeArea(true);
	return helper.convert(assignment, getAssetManagement());
    }

    /**
     * Update an existing device assignment.
     * 
     * @param token
     * @param request
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token:.+}", method = RequestMethod.PUT)
    @ApiOperation(value = "Update an existing device assignment")
    @Secured({ SiteWhereRoles.REST })
    public DeviceAssignment updateDeviceAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @RequestBody DeviceAssignmentCreateRequest request) throws SiteWhereException {
	IDeviceAssignment existing = assertDeviceAssignment(token);
	IDeviceAssignment result = getDeviceManagement().updateDeviceAssignment(existing.getId(), request);
	DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper(getDeviceManagement());
	helper.setIncludeAsset(true);
	helper.setIncludeDevice(true);
	helper.setIncludeArea(true);
	return helper.convert(result, getAssetManagement());
    }

    /**
     * Get label for assignment based on a specific generator.
     * 
     * @param token
     * @param generatorId
     * @param servletRequest
     * @param response
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/label/{generatorId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get label for area")
    public ResponseEntity<byte[]> getAssignmentLabel(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Generator id", required = true) @PathVariable String generatorId,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	IDeviceAssignment existing = assertDeviceAssignment(token);
	ILabel label = getLabelGeneration().getDeviceAssignmentLabel(generatorId, existing.getId());
	if (label == null) {
	    return ResponseEntity.notFound().build();
	}
	final HttpHeaders headers = new HttpHeaders();
	headers.setContentType(MediaType.IMAGE_PNG);
	return new ResponseEntity<byte[]>(label.getContent(), headers, HttpStatus.OK);
    }

    /**
     * List assignments matching criteria.
     * 
     * @param deviceToken
     * @param areaToken
     * @param assetToken
     * @param includeDevice
     * @param includeArea
     * @param includeAsset
     * @param page
     * @param pageSize
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "List assignments matching criteria")
    @Secured({ SiteWhereRoles.REST })
    public ISearchResults<IDeviceAssignment> listAssignments(
	    @ApiParam(value = "Limit by device token", required = false) @RequestParam(required = false) String deviceToken,
	    @ApiParam(value = "Limit by customer token", required = false) @RequestParam(required = false) String customerToken,
	    @ApiParam(value = "Limit by area token", required = false) @RequestParam(required = false) String areaToken,
	    @ApiParam(value = "Limit by asset token", required = false) @RequestParam(required = false) String assetToken,
	    @ApiParam(value = "Include device information", required = false) @RequestParam(defaultValue = "false") boolean includeDevice,
	    @ApiParam(value = "Include customer information", required = false) @RequestParam(defaultValue = "false") boolean includeCustomer,
	    @ApiParam(value = "Include area information", required = false) @RequestParam(defaultValue = "false") boolean includeArea,
	    @ApiParam(value = "Include asset information", required = false) @RequestParam(defaultValue = "false") boolean includeAsset,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize)
	    throws SiteWhereException {
	// Build criteria.
	DeviceAssignmentSearchCriteria criteria = new DeviceAssignmentSearchCriteria(page, pageSize);
	if (deviceToken != null) {
	    IDevice device = getDeviceManagement().getDeviceByToken(deviceToken);
	    if (device == null) {
		throw new SiteWhereSystemException(ErrorCode.InvalidDeviceToken, ErrorLevel.ERROR);
	    }
	    criteria.setDeviceId(device.getId());
	}

	// If limiting by customer, look up customer and contained customers.
	if (customerToken != null) {
	    List<UUID> customerIds = Customers.resolveCustomerIds(customerToken, true, getDeviceManagement());
	    criteria.setCustomerIds(customerIds);
	}

	// If limiting by area, look up area and contained areas.
	if (areaToken != null) {
	    List<UUID> areaIds = Areas.resolveAreaIds(areaToken, true, getDeviceManagement());
	    criteria.setAreaIds(areaIds);
	}

	// If limiting by asset, look up asset.
	if (assetToken != null) {
	    IAsset asset = getAssetManagement().getAssetByToken(assetToken);
	    if (asset == null) {
		throw new SiteWhereSystemException(ErrorCode.InvalidAssetToken, ErrorLevel.ERROR);
	    }
	    List<UUID> assetIds = new ArrayList<>();
	    assetIds.add(asset.getId());
	    criteria.setAssetIds(assetIds);
	}

	// Perform search.
	ISearchResults<IDeviceAssignment> matches = getDeviceManagement().listDeviceAssignments(criteria);
	DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper(getDeviceManagement());
	helper.setIncludeDevice(includeDevice);
	helper.setIncludeCustomer(includeCustomer);
	helper.setIncludeArea(includeArea);
	helper.setIncludeAsset(includeAsset);

	List<IDeviceAssignment> results = new ArrayList<>();
	for (IDeviceAssignment assn : matches.getResults()) {
	    results.add(helper.convert(assn, getAssetManagement()));
	}
	return new SearchResults<IDeviceAssignment>(results, matches.getNumResults());
    }

    /**
     * List all device measurements for a given assignment.
     * 
     * @param assignmentToken
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/measurements", method = RequestMethod.GET)
    @ApiOperation(value = "List measurement events for device assignment")
    @Secured({ SiteWhereRoles.REST })
    public ISearchResults<IDeviceMeasurement> listMeasurementsForAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
	    @ApiParam(value = "Start date", required = false) @RequestParam(required = false) String startDate,
	    @ApiParam(value = "End date", required = false) @RequestParam(required = false) String endDate,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	IDateRangeSearchCriteria criteria = createDateRangeSearchCriteria(page, pageSize, startDate, endDate, response);
	return new BlockingDeviceEventManagement(getDeviceEventManagement()).listDeviceMeasurementsForIndex(
		DeviceEventIndex.Assignment, Collections.singletonList(assignment.getId()), criteria);
    }

    /**
     * List device measurements for a given assignment.
     * 
     * @param assignmentToken
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/measurements/series", method = RequestMethod.GET)
    @ApiOperation(value = "List assignment measurements as chart series")
    @Secured({ SiteWhereRoles.REST })
    public List<IChartSeries<Double>> listMeasurementsForAssignmentAsChartSeries(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
	    @ApiParam(value = "Start date", required = false) @RequestParam(required = false) String startDate,
	    @ApiParam(value = "End date", required = false) @RequestParam(required = false) String endDate,
	    @ApiParam(value = "Measurement Ids", required = false) @RequestParam(required = false) String[] measurementIds,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	IDateRangeSearchCriteria criteria = createDateRangeSearchCriteria(page, pageSize, startDate, endDate, response);
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	ISearchResults<IDeviceMeasurement> measurements = new BlockingDeviceEventManagement(getDeviceEventManagement())
		.listDeviceMeasurementsForIndex(DeviceEventIndex.Assignment,
			Collections.singletonList(assignment.getId()), criteria);
	ChartBuilder builder = new ChartBuilder();
	return builder.process(measurements.getResults(), measurementIds);
    }

    /**
     * Create measurements to be associated with a device assignment.
     * 
     * @param input
     * @param token
     * @param updateState
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/measurements", method = RequestMethod.POST)
    @ApiOperation(value = "Create measurements event for device assignment")
    @Secured({ SiteWhereRoles.REST })
    public IDeviceMeasurement createMeasurements(@RequestBody DeviceMeasurementCreateRequest input,
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	return new BlockingDeviceEventManagement(getDeviceEventManagement())
		.addDeviceMeasurements(assignment.getId(), input).get(0);
    }

    /**
     * List device locations for a given assignment.
     * 
     * @param assignmentToken
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/locations", method = RequestMethod.GET)
    @ApiOperation(value = "List location events for device assignment")
    @Secured({ SiteWhereRoles.REST })
    public ISearchResults<IDeviceLocation> listLocationsForAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
	    @ApiParam(value = "Start date", required = false) @RequestParam(required = false) String startDate,
	    @ApiParam(value = "End date", required = false) @RequestParam(required = false) String endDate,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	IDateRangeSearchCriteria criteria = createDateRangeSearchCriteria(page, pageSize, startDate, endDate, response);
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	return new BlockingDeviceEventManagement(getDeviceEventManagement()).listDeviceLocationsForIndex(
		DeviceEventIndex.Assignment, Collections.singletonList(assignment.getId()), criteria);
    }

    /**
     * Create location to be associated with a device assignment.
     * 
     * @param input
     * @param token
     * @param updateState
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/locations", method = RequestMethod.POST)
    @ApiOperation(value = "Create location event for device assignment")
    @Secured({ SiteWhereRoles.REST })
    public IDeviceLocation createLocation(@RequestBody DeviceLocationCreateRequest input,
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token)
	    throws SiteWhereException {
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	return new BlockingDeviceEventManagement(getDeviceEventManagement())
		.addDeviceLocations(assignment.getId(), input).get(0);
    }

    /**
     * List device alerts for a given assignment.
     * 
     * @param assignmentToken
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/alerts", method = RequestMethod.GET)
    @ApiOperation(value = "List alert events for device assignment")
    @Secured({ SiteWhereRoles.REST })
    public ISearchResults<IDeviceAlert> listAlertsForAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
	    @ApiParam(value = "Start date", required = false) @RequestParam(required = false) String startDate,
	    @ApiParam(value = "End date", required = false) @RequestParam(required = false) String endDate,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	IDateRangeSearchCriteria criteria = createDateRangeSearchCriteria(page, pageSize, startDate, endDate, response);
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	return new BlockingDeviceEventManagement(getDeviceEventManagement()).listDeviceAlertsForIndex(
		DeviceEventIndex.Assignment, Collections.singletonList(assignment.getId()), criteria);
    }

    /**
     * Create alert to be associated with a device assignment.
     * 
     * @param input
     * @param token
     * @param updateState
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/alerts", method = RequestMethod.POST)
    @ApiOperation(value = "Create alert event for device assignment")
    @Secured({ SiteWhereRoles.REST })
    public IDeviceAlert createAlert(@RequestBody DeviceAlertCreateRequest input,
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	return new BlockingDeviceEventManagement(getDeviceEventManagement()).addDeviceAlerts(assignment.getId(), input)
		.get(0);
    }

    /**
     * Create a stream to be associated with a device assignment.
     * 
     * @param input
     * @param token
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/streams", method = RequestMethod.POST)
    @ApiOperation(value = "Create data stream for a device assignment")
    @Secured({ SiteWhereRoles.REST })
    public DeviceStream createDeviceStream(@RequestBody DeviceStreamCreateRequest request,
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceAssignment existing = assertDeviceAssignment(token);
	IDeviceStream result = getDeviceManagement().createDeviceStream(existing.getId(), request);
	return DeviceStream.copy(result);
    }

    /**
     * List device streams associated with an assignment.
     * 
     * @param token
     * @param page
     * @param pageSize
     * @param startDate
     * @param endDate
     * @param servletRequest
     * @param response
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/streams", method = RequestMethod.GET)
    @ApiOperation(value = "List data streams for device assignment")
    @Secured({ SiteWhereRoles.REST })
    public ISearchResults<IDeviceStream> listDeviceStreamsForAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
	    @ApiParam(value = "Start date", required = false) @RequestParam(required = false) String startDate,
	    @ApiParam(value = "End date", required = false) @RequestParam(required = false) String endDate,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	IDateRangeSearchCriteria criteria = createDateRangeSearchCriteria(page, pageSize, startDate, endDate, response);
	IDeviceAssignment existing = assertDeviceAssignment(token);
	ISearchResults<IDeviceStream> matches = getDeviceManagement().listDeviceStreams(existing.getId(), criteria);
	List<IDeviceStream> converted = new ArrayList<IDeviceStream>();
	for (IDeviceStream stream : matches.getResults()) {
	    converted.add(DeviceStream.copy(stream));
	}
	return new SearchResults<IDeviceStream>(converted);
    }

    /**
     * Get an existing device stream associated with an assignment.
     * 
     * @param token
     * @param streamId
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/streams/{streamId:.+}", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "Get device assignment data stream by id")
    @Secured({ SiteWhereRoles.REST })
    public DeviceStream getDeviceStream(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Stream Id", required = true) @PathVariable String streamId,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceAssignment existing = assertDeviceAssignment(token);
	IDeviceStream result = getDeviceManagement().getDeviceStream(existing.getId(), streamId);
	if (result == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidStreamId, ErrorLevel.ERROR,
		    HttpServletResponse.SC_NOT_FOUND);
	}
	return DeviceStream.copy(result);
    }

    /**
     * Create command invocation to be associated with a device assignment.
     * 
     * @param input
     * @param token
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/invocations", method = RequestMethod.POST)
    @ApiOperation(value = "Create command invocation event for assignment")
    @Secured({ SiteWhereRoles.REST })
    public IDeviceCommandInvocation createCommandInvocation(@RequestBody DeviceCommandInvocationCreateRequest request,
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	IDeviceCommandInvocation result = new BlockingDeviceEventManagement(getDeviceEventManagement())
		.addDeviceCommandInvocations(assignment.getId(), request).get(0);
	DeviceCommandInvocationMarshalHelper helper = new DeviceCommandInvocationMarshalHelper(getDeviceManagement());
	return helper.convert(result);
    }

    @RequestMapping(value = "/{token}/invocations/schedules/{scheduleToken}", method = RequestMethod.POST)
    @ApiOperation(value = "Schedule command invocation")
    @Secured({ SiteWhereRoles.REST })
    public IScheduledJob scheduleCommandInvocation(@RequestBody DeviceCommandInvocationCreateRequest request,
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Schedule token", required = true) @PathVariable String scheduleToken)
	    throws SiteWhereException {
	assureDeviceCommand(request.getCommandToken());
	IScheduledJobCreateRequest job = ScheduledJobHelper.createCommandInvocationJob(UUID.randomUUID().toString(),
		token, request.getCommandToken(), request.getParameterValues(), scheduleToken);
	return getScheduleManagement().createScheduledJob(job);
    }

    /**
     * List device command invocations for a given assignment.
     * 
     * @param assignmentToken
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/invocations", method = RequestMethod.GET)
    @ApiOperation(value = "List command invocation events for assignment")
    @Secured({ SiteWhereRoles.REST })
    public ISearchResults<IDeviceCommandInvocation> listCommandInvocationsForAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Include command information", required = false) @RequestParam(defaultValue = "true") boolean includeCommand,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
	    @ApiParam(value = "Start date", required = false) @RequestParam(required = false) String startDate,
	    @ApiParam(value = "End date", required = false) @RequestParam(required = false) String endDate,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	IDateRangeSearchCriteria criteria = createDateRangeSearchCriteria(page, pageSize, startDate, endDate, response);
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	ISearchResults<IDeviceCommandInvocation> matches = new BlockingDeviceEventManagement(getDeviceEventManagement())
		.listDeviceCommandInvocationsForIndex(DeviceEventIndex.Assignment,
			Collections.singletonList(assignment.getId()), criteria);
	DeviceCommandInvocationMarshalHelper helper = new DeviceCommandInvocationMarshalHelper(getDeviceManagement());
	helper.setIncludeCommand(includeCommand);
	List<IDeviceCommandInvocation> converted = new ArrayList<IDeviceCommandInvocation>();
	for (IDeviceCommandInvocation invocation : matches.getResults()) {
	    converted.add(helper.convert(invocation));
	}
	return new SearchResults<IDeviceCommandInvocation>(converted);
    }

    /**
     * Create state change to be associated with a device assignment.
     * 
     * @param input
     * @param token
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/statechanges", method = RequestMethod.POST)
    @ApiOperation(value = "Create an state change event for a device assignment")
    @Secured({ SiteWhereRoles.REST })
    public IDeviceStateChange createStateChange(@RequestBody DeviceStateChangeCreateRequest input,
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	return new BlockingDeviceEventManagement(getDeviceEventManagement())
		.addDeviceStateChanges(assignment.getId(), input).get(0);
    }

    /**
     * List device state changes for a given assignment.
     * 
     * @param assignmentToken
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/statechanges", method = RequestMethod.GET)
    @ApiOperation(value = "List state change events for a device assignment")
    @Secured({ SiteWhereRoles.REST })
    public ISearchResults<IDeviceStateChange> listStateChangesForAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
	    @ApiParam(value = "Start date", required = false) @RequestParam(required = false) String startDate,
	    @ApiParam(value = "End date", required = false) @RequestParam(required = false) String endDate,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	IDateRangeSearchCriteria criteria = createDateRangeSearchCriteria(page, pageSize, startDate, endDate, response);
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	return new BlockingDeviceEventManagement(getDeviceEventManagement()).listDeviceStateChangesForIndex(
		DeviceEventIndex.Assignment, Collections.singletonList(assignment.getId()), criteria);
    }

    /**
     * Create command response to be associated with a device assignment.
     * 
     * @param input
     * @param token
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/responses", method = RequestMethod.POST)
    @ApiOperation(value = "Create command response event for assignment")
    @Secured({ SiteWhereRoles.REST })
    public DeviceCommandResponse createCommandResponse(@RequestBody DeviceCommandResponseCreateRequest input,
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	IDeviceCommandResponse result = new BlockingDeviceEventManagement(getDeviceEventManagement())
		.addDeviceCommandResponses(assignment.getId(), input).get(0);
	return DeviceCommandResponse.copy(result);
    }

    /**
     * List device command responses for a given assignment.
     * 
     * @param assignmentToken
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/responses", method = RequestMethod.GET)
    @ApiOperation(value = "List command response events for assignment")
    @Secured({ SiteWhereRoles.REST })
    public ISearchResults<IDeviceCommandResponse> listCommandResponsesForAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize,
	    @ApiParam(value = "Start date", required = false) @RequestParam(required = false) String startDate,
	    @ApiParam(value = "End date", required = false) @RequestParam(required = false) String endDate,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	IDateRangeSearchCriteria criteria = createDateRangeSearchCriteria(page, pageSize, startDate, endDate, response);
	IDeviceAssignment assignment = assertDeviceAssignment(token);
	return new BlockingDeviceEventManagement(getDeviceEventManagement()).listDeviceCommandResponsesForIndex(
		DeviceEventIndex.Assignment, Collections.singletonList(assignment.getId()), criteria);
    }

    /**
     * Get the default symbol for a device assignment.
     * 
     * @param token
     * @param servletRequest
     * @param response
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/symbol", method = RequestMethod.GET)
    @ApiOperation(value = "Get default symbol for assignment")
    public ResponseEntity<byte[]> getDeviceAssignmentSymbol(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest, HttpServletResponse response) throws SiteWhereException {
	// IDeviceAssignment assignment = assureAssignmentWithoutUserValidation(token,
	// servletRequest);
	// IEntityUriProvider provider = DefaultEntityUriProvider.getInstance();
	// ISymbolGeneratorManager symbols =
	// getDeviceCommunication().getSymbolGeneratorManager();
	// ISymbolGenerator generator = symbols.getDefaultSymbolGenerator();
	// if (generator != null) {
	// byte[] image = generator.getDeviceAssigmentSymbol(assignment, provider);
	//
	// final HttpHeaders headers = new HttpHeaders();
	// headers.setContentType(MediaType.IMAGE_PNG);
	// return new ResponseEntity<byte[]>(image, headers, HttpStatus.CREATED);
	// } else {
	// return null;
	// }
	return null;
    }

    /**
     * End an existing device assignment.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/end", method = RequestMethod.POST)
    @ApiOperation(value = "Release an active device assignment")
    @Secured({ SiteWhereRoles.REST })
    public DeviceAssignment endDeviceAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceManagement management = getDeviceManagement();
	IDeviceAssignment existing = assertDeviceAssignment(token);
	IDeviceAssignment updated = management.endDeviceAssignment(existing.getId());
	DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper(getDeviceManagement());
	helper.setIncludeAsset(true);
	helper.setIncludeDevice(true);
	helper.setIncludeArea(true);
	return helper.convert(updated, getAssetManagement());
    }

    /**
     * Mark a device assignment as missing.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}/missing", method = RequestMethod.POST)
    @ApiOperation(value = "Mark device assignment as missing")
    @Secured({ SiteWhereRoles.REST })
    public DeviceAssignment missingDeviceAssignment(
	    @ApiParam(value = "Assignment token", required = true) @PathVariable String token,
	    HttpServletRequest servletRequest) throws SiteWhereException {
	IDeviceManagement management = getDeviceManagement();
	IDeviceAssignment existing = assertDeviceAssignment(token);

	// Update status field.
	DeviceAssignmentCreateRequest request = new DeviceAssignmentCreateRequest();
	request.setStatus(DeviceAssignmentStatus.Missing);

	IDeviceAssignment updated = management.updateDeviceAssignment(existing.getId(), request);
	DeviceAssignmentMarshalHelper helper = new DeviceAssignmentMarshalHelper(getDeviceManagement());
	helper.setIncludeAsset(true);
	helper.setIncludeDevice(true);
	helper.setIncludeArea(true);
	return helper.convert(updated, getAssetManagement());
    }

    /**
     * Get an assignment by unique token. Throw an exception if not found.
     * 
     * @param token
     * @param servletRequest
     * @return
     * @throws SiteWhereException
     */
    protected IDeviceAssignment assureAssignment(String token, HttpServletRequest servletRequest)
	    throws SiteWhereException {
	IDeviceAssignment assignment = getDeviceManagement().getDeviceAssignmentByToken(token);
	if (assignment == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
	}
	return assignment;
    }

    /**
     * Gets an assignment given its unique token. This implementation skips the
     * validation which ensures the authenticated user has access to the tenant. It
     * should *only* be used to access resources that are not protected.
     * 
     * @param token
     * @param servletRequest
     * @return
     * @throws SiteWhereException
     */
    protected IDeviceAssignment assureAssignmentWithoutUserValidation(String token, HttpServletRequest servletRequest)
	    throws SiteWhereException {
	IDeviceAssignment assignment = getDeviceManagement().getDeviceAssignmentByToken(token);
	if (assignment == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
	}
	return assignment;
    }

    /**
     * Get a device command by token. Throw an exception if not found.
     * 
     * @param commandToken
     * @return
     * @throws SiteWhereException
     */
    protected IDeviceCommand assureDeviceCommand(String commandToken) throws SiteWhereException {
	IDeviceCommand command = getDeviceManagement().getDeviceCommandByToken(commandToken);
	if (command == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceCommandId, ErrorLevel.ERROR);
	}
	return command;
    }

    /**
     * Assert that a device assignment exists and throw an exception if not.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    protected IDeviceAssignment assertDeviceAssignment(String token) throws SiteWhereException {
	IDeviceAssignment assignment = getDeviceManagement().getDeviceAssignmentByToken(token);
	if (assignment == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentToken, ErrorLevel.ERROR);
	}
	return assignment;
    }

    /**
     * Assert that a device stream exists and throw an exception if not.
     * 
     * @param assignmentId
     * @param id
     * @return
     * @throws SiteWhereException
     */
    protected IDeviceStream assertDeviceStream(UUID assignmentId, String id) throws SiteWhereException {
	IDeviceStream stream = getDeviceManagement().getDeviceStream(assignmentId, id);
	if (stream == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidStreamId, ErrorLevel.ERROR);
	}
	return stream;
    }

    protected static IDateRangeSearchCriteria createDateRangeSearchCriteria(int page, int pageSize, String startDate,
	    String endDate, HttpServletResponse response) {
	Date parsedStartDate = parseDateOrSendBadResponse(startDate, response);
	Date parsedEndDate = parseDateOrSendBadResponse(endDate, response);

	if (parsedEndDate == null) {
	    parsedEndDate = new Date();
	}

	if (parsedStartDate == null) {
	    parsedStartDate = new Date(java.lang.System.currentTimeMillis() - DEFAULT_EVENT_QUERY_DATE_RANGE);
	}

	return new DateRangeSearchCriteria(page, pageSize, parsedStartDate, parsedEndDate);
    }

    /**
     * Parse a date argument from a string and send a "bad request" code if date can
     * not be parsed.
     * 
     * @param dateString
     * @param response
     * @return
     */
    protected static Date parseDateOrSendBadResponse(String dateString, HttpServletResponse response) {
	try {
	    if (StringUtils.isBlank(dateString)) {
		return null;
	    }
	    ZonedDateTime zdt = DataUtils.parseDateInMutipleFormats(dateString);
	    return Date.from(zdt.toInstant());
	} catch (DateTimeParseException e) {
	    try {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	    } catch (IOException e1) {
		LOGGER.error(e);
	    }
	}
	return null;
    }

    private IDeviceManagement getDeviceManagement() {
	return getMicroservice().getDeviceManagementApiDemux().getApiChannel();
    }

    private IDeviceEventManagementApiChannel<?> getDeviceEventManagement() {
	return getMicroservice().getDeviceEventManagementApiDemux().getApiChannel();
    }

    private IAssetManagement getAssetManagement() {
	return getMicroservice().getAssetManagementApiDemux().getApiChannel();
    }

    private IScheduleManagement getScheduleManagement() {
	return null;
    }

    private ILabelGeneration getLabelGeneration() {
	return getMicroservice().getLabelGenerationApiDemux().getApiChannel();
    }
}