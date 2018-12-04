/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.web.rest.controllers;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sitewhere.rest.model.device.request.DeviceStatusCreateRequest;
import com.sitewhere.rest.model.search.device.DeviceStatusSearchCriteria;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.IDeviceStatus;
import com.sitewhere.spi.device.IDeviceType;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.user.SiteWhereRoles;
import com.sitewhere.web.annotation.SiteWhereCrossOrigin;
import com.sitewhere.web.rest.RestControllerBase;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Controller for device status operations.
 * 
 * @author Derek Adams
 */
@RestController
@SiteWhereCrossOrigin
@RequestMapping(value = "/statuses")
@Api(value = "statuses")
public class DeviceStatuses extends RestControllerBase {

    /**
     * Create a new status.
     * 
     * @param request
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Create device status.")
    @Secured({ SiteWhereRoles.REST })
    public IDeviceStatus createDeviceStatus(@RequestBody DeviceStatusCreateRequest request) throws SiteWhereException {
	return getDeviceManagement().createDeviceStatus(request);
    }

    /**
     * Get a device status by unique token.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}", method = RequestMethod.GET)
    @ApiOperation(value = "Get device status by unique token")
    @Secured({ SiteWhereRoles.REST })
    public IDeviceStatus getDeviceStatusByToken(@ApiParam(value = "Token", required = true) @PathVariable String token)
	    throws SiteWhereException {
	return assertDeviceStatusByToken(token);
    }

    /**
     * List statuses that match the given criteria.
     * 
     * @param request
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "List device statuses that match criteria.")
    @Secured({ SiteWhereRoles.REST })
    public ISearchResults<IDeviceStatus> listDeviceStatuses(
	    @ApiParam(value = "Device type token", required = false) @RequestParam(required = false) String deviceTypeToken,
	    @ApiParam(value = "Status code", required = false) @RequestParam(required = false) String code,
	    @ApiParam(value = "Page number", required = false) @RequestParam(required = false, defaultValue = "1") int page,
	    @ApiParam(value = "Page size", required = false) @RequestParam(required = false, defaultValue = "100") int pageSize)
	    throws SiteWhereException {
	DeviceStatusSearchCriteria criteria = new DeviceStatusSearchCriteria(page, pageSize);

	// Look up device type if specified.
	IDeviceType deviceType = null;
	if (deviceTypeToken != null) {
	    deviceType = getDeviceManagement().getDeviceTypeByToken(deviceTypeToken);
	    if (deviceType == null) {
		throw new SiteWhereSystemException(ErrorCode.InvalidDeviceTypeToken, ErrorLevel.ERROR);
	    }
	    criteria.setDeviceTypeId(deviceType.getId());
	}

	// Add code if specified.
	if (code != null) {
	    criteria.setCode(code);
	}

	return getDeviceManagement().listDeviceStatuses(criteria);
    }

    /**
     * Update an existing device status.
     * 
     * @param token
     * @param request
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}", method = RequestMethod.PUT)
    @ApiOperation(value = "Update an existing device status")
    @Secured({ SiteWhereRoles.REST })
    public IDeviceStatus updateDeviceStatus(@ApiParam(value = "Token", required = true) @PathVariable String token,
	    @RequestBody DeviceStatusCreateRequest request) throws SiteWhereException {
	IDeviceStatus status = assertDeviceStatusByToken(token);
	return getDeviceManagement().updateDeviceStatus(status.getId(), request);
    }

    /**
     * Delete an existing device status.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    @RequestMapping(value = "/{token}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete device status by unique token")
    @Secured({ SiteWhereRoles.REST })
    public IDeviceStatus deleteDeviceStatus(@ApiParam(value = "Token", required = true) @PathVariable String token)
	    throws SiteWhereException {
	IDeviceStatus command = assertDeviceStatusByToken(token);
	return getDeviceManagement().deleteDeviceStatus(command.getId());
    }

    /**
     * Gets a device status by token and throws an exception if not found.
     * 
     * @param token
     * @param servletRequest
     * @return
     * @throws SiteWhereException
     */
    protected IDeviceStatus assertDeviceStatusByToken(String token) throws SiteWhereException {
	IDeviceStatus result = getDeviceManagement().getDeviceStatusByToken(token);
	if (result == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceStatusToken, ErrorLevel.ERROR);
	}
	return result;
    }

    private IDeviceManagement getDeviceManagement() {
	return getMicroservice().getDeviceManagementApiDemux().getApiChannel();
    }
}
