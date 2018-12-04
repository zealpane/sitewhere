/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.user.grpc;

import com.sitewhere.microservice.grpc.GrpcServer;
import com.sitewhere.spi.user.IUserManagement;
import com.sitewhere.user.spi.grpc.IUserManagementGrpcServer;
import com.sitewhere.user.spi.microservice.IUserManagementMicroservice;

/**
 * Hosts a GRPC server that handles user management requests.
 * 
 * @author Derek
 */
public class UserManagementGrpcServer extends GrpcServer implements IUserManagementGrpcServer {

    public UserManagementGrpcServer(IUserManagementMicroservice<?> microservice, IUserManagement userManagement) {
	super(new UserManagementImpl(microservice, userManagement), microservice.getInstanceSettings().getGrpcPort());
    }
}