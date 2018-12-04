/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.hbase;

import com.sitewhere.hbase.encoder.IPayloadMarshaler;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Supplies configuration information to HBase implementation classes.
 * 
 * @author Derek
 */
public interface IHBaseContext {

    /**
     * Get tenant information.
     * 
     * @return
     */
    public ITenant getTenant();

    /**
     * Get HBase client accessor.
     * 
     * @return
     */
    public ISiteWhereHBaseClient getClient();

    /**
     * Get configured payload marshaler.
     * 
     * @return
     */
    public IPayloadMarshaler getPayloadMarshaler();

    /**
     * Get buffer for storing device events.
     * 
     * @return
     */
    public IDeviceEventBuffer getDeviceEventBuffer();

    /**
     * Device id manager.
     * 
     * @return
     */
    public IDeviceIdManager getDeviceIdManager();

    /**
     * Asset id manager.
     * 
     * @return
     */
    public IAssetIdManager getAssetIdManager();

    /**
     * Schedule id manager.
     * 
     * @return
     */
    public IScheduleIdManager getScheduleIdManager();

    /**
     * User id manager.
     * 
     * @return
     */
    public IUserIdManager getUserIdManager();
}