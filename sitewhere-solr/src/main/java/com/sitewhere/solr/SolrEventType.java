/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.solr;

/**
 * Enumerates event types that can be stored in Solr.
 * 
 * @author Derek
 */
public enum SolrEventType {

    /** Event represents IDeviceMeasurement */
    Measurement,

    /** Event represents IDeviceLocation */
    Location,

    /** Event represents IDeviceAlert */
    Alert;
}