/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.devicestate.grpc;

import com.sitewhere.devicestate.spi.grpc.IDeviceStateGrpcServer;
import com.sitewhere.devicestate.spi.microservice.IDeviceStateMicroservice;
import com.sitewhere.microservice.grpc.MultitenantGrpcServer;

/**
 * Hosts a GRPC server that handles device state requests.
 * 
 * @author Derek
 */
public class DeviceStateGrpcServer extends MultitenantGrpcServer implements IDeviceStateGrpcServer {

    public DeviceStateGrpcServer(IDeviceStateMicroservice microservice) {
	super(new DeviceStateRouter(microservice), microservice.getInstanceSettings().getGrpcPort());
    }
}