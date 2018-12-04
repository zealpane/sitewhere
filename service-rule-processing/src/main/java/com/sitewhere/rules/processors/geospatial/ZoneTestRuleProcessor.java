/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rules.processors.geospatial;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sitewhere.geospatial.GeoUtils;
import com.sitewhere.rest.model.device.event.request.DeviceAlertCreateRequest;
import com.sitewhere.rules.spi.IRuleProcessor;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.area.IZone;
import com.sitewhere.spi.device.event.IDeviceEventContext;
import com.sitewhere.spi.device.event.IDeviceLocation;
import com.sitewhere.spi.geospatial.ZoneContainment;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implementation of {@link IRuleProcessor} that performs a series of tests for
 * whether a location is inside or outside of zones, firing alerts if the
 * criteria is met.
 * 
 * @author Derek
 */
public class ZoneTestRuleProcessor extends RuleProcessor {

    /** Map of polygons by zone token */
    private Map<String, Polygon> zoneMap = new HashMap<String, Polygon>();

    /** List of tests to perform */
    private List<ZoneTest> zoneTests = new ArrayList<ZoneTest>();

    /*
     * @see com.sitewhere.rules.processors.geospatial.RuleProcessor#onLocation(com.
     * sitewhere.spi.device.event.IDeviceEventContext,
     * com.sitewhere.spi.device.event.IDeviceLocation)
     */
    @Override
    public void onLocation(IDeviceEventContext context, IDeviceLocation location) throws SiteWhereException {
	for (ZoneTest test : zoneTests) {
	    Polygon poly = getZonePolygon(test.getZoneToken());
	    ZoneContainment containment = (poly.contains(GeoUtils.createPointForLocation(location)))
		    ? ZoneContainment.Inside
		    : ZoneContainment.Outside;
	    if (test.getCondition() == containment) {
		DeviceAlertCreateRequest alert = new DeviceAlertCreateRequest();
		alert.setType(test.getAlertType());
		alert.setLevel(test.getAlertLevel());
		alert.setMessage(test.getAlertMessage());
		alert.setUpdateState(false);
		alert.setEventDate(new Date());
		getDeviceEventManagement().addDeviceAlerts(location.getDeviceAssignmentId(), alert);
	    }
	}
    }

    /**
     * Get cached zone polygon or try to load from datastore.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    protected Polygon getZonePolygon(String token) throws SiteWhereException {
	Polygon poly = zoneMap.get(token);
	if (poly != null) {
	    return poly;
	}
	IZone zone = getDeviceManagement().getZoneByToken(token);
	if (zone != null) {
	    poly = GeoUtils.createPolygonForZone(zone);
	    zoneMap.put(token, poly);
	    return poly;
	}
	throw new SiteWhereException("Invalid zone token in " + ZoneTestRuleProcessor.class.getName() + ": " + token);
    }

    public List<ZoneTest> getZoneTests() {
	return zoneTests;
    }

    public void setZoneTests(List<ZoneTest> zoneTests) {
	this.zoneTests = zoneTests;
    }
}