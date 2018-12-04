/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.labels.grpc;

import com.sitewhere.labels.spi.grpc.ILabelGenerationGrpcServer;
import com.sitewhere.labels.spi.microservice.ILabelGenerationMicroservice;
import com.sitewhere.microservice.grpc.MultitenantGrpcServer;

/**
 * Hosts a GRPC server that handles label generation requests.
 * 
 * @author Derek
 */
public class LabelGenerationGrpcServer extends MultitenantGrpcServer implements ILabelGenerationGrpcServer {

    public LabelGenerationGrpcServer(ILabelGenerationMicroservice microservice) {
	super(new LabelGenerationRouter(microservice), microservice.getInstanceSettings().getGrpcPort());
    }
}
