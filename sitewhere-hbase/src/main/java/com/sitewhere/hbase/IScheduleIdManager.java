/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.hbase;

import com.sitewhere.hbase.uid.UniqueIdCounterMap;

/**
 * Interface for schedule id management.
 * 
 * @author Derek
 */
public interface IScheduleIdManager {

    public UniqueIdCounterMap getScheduleKeys();

    public UniqueIdCounterMap getScheduledJobKeys();
}