/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.grpc.client.devicestate;

import com.sitewhere.grpc.client.MultitenantApiDemux;
import com.sitewhere.grpc.client.spi.client.IDeviceStateApiChannel;
import com.sitewhere.grpc.client.spi.client.IDeviceStateApiDemux;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.MicroserviceIdentifier;

/**
 * Demultiplexes device state requests across one or more API channels.
 * 
 * @author Derek
 *
 * @param <IAssetManagementApiChannel>
 */
public class DeviceStateApiDemux extends MultitenantApiDemux<IDeviceStateApiChannel<?>>
	implements IDeviceStateApiDemux {

    /*
     * @see com.sitewhere.grpc.client.spi.IApiDemux#getTargetIdentifier()
     */
    @Override
    public String getTargetIdentifier() {
	return MicroserviceIdentifier.DeviceState.getPath();
    }

    /*
     * @see
     * com.sitewhere.grpc.model.spi.IApiDemux#createApiChannel(java.lang.String)
     */
    @Override
    public IDeviceStateApiChannel<?> createApiChannel(String host) throws SiteWhereException {
	return new DeviceStateApiChannel(this, host, getMicroservice().getInstanceSettings().getGrpcPort());
    }
}