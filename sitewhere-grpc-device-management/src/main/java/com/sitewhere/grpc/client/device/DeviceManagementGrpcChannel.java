/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.grpc.client.device;

import com.sitewhere.grpc.client.MultitenantGrpcChannel;
import com.sitewhere.grpc.service.DeviceManagementGrpc;
import com.sitewhere.grpc.service.DeviceManagementGrpc.DeviceManagementBlockingStub;
import com.sitewhere.grpc.service.DeviceManagementGrpc.DeviceManagementStub;
import com.sitewhere.spi.tracing.ITracerProvider;

/**
 * Channel that allows for communication with a remote device management GRPC
 * server.
 * 
 * @author Derek
 */
public class DeviceManagementGrpcChannel
	extends MultitenantGrpcChannel<DeviceManagementBlockingStub, DeviceManagementStub> {

    public DeviceManagementGrpcChannel(ITracerProvider tracerProvider, String host, int port) {
	super(tracerProvider, host, port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.grpc.model.client.GrpcChannel#createBlockingStub()
     */
    @Override
    public DeviceManagementBlockingStub createBlockingStub() {
	return DeviceManagementGrpc.newBlockingStub(getChannel());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.grpc.model.client.GrpcChannel#createAsyncStub()
     */
    @Override
    public DeviceManagementStub createAsyncStub() {
	return DeviceManagementGrpc.newStub(getChannel());
    }
}