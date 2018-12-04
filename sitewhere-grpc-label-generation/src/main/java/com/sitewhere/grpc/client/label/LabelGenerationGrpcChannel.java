/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.grpc.client.label;

import com.sitewhere.grpc.client.MultitenantGrpcChannel;
import com.sitewhere.grpc.service.LabelGenerationGrpc;
import com.sitewhere.grpc.service.LabelGenerationGrpc.LabelGenerationBlockingStub;
import com.sitewhere.grpc.service.LabelGenerationGrpc.LabelGenerationStub;
import com.sitewhere.spi.tracing.ITracerProvider;

/**
 * Channel that allows for communication with a remote label generation GRPC
 * server.
 * 
 * @author Derek
 */
public class LabelGenerationGrpcChannel
	extends MultitenantGrpcChannel<LabelGenerationBlockingStub, LabelGenerationStub> {

    public LabelGenerationGrpcChannel(ITracerProvider tracerProvider, String host, int port) {
	super(tracerProvider, host, port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.grpc.model.client.GrpcChannel#createBlockingStub()
     */
    @Override
    public LabelGenerationBlockingStub createBlockingStub() {
	return LabelGenerationGrpc.newBlockingStub(getChannel());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.grpc.model.client.GrpcChannel#createAsyncStub()
     */
    @Override
    public LabelGenerationStub createAsyncStub() {
	return LabelGenerationGrpc.newStub(getChannel());
    }
}