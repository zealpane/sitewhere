/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.grpc.client.batch;

import java.util.UUID;

import com.sitewhere.grpc.client.GrpcUtils;
import com.sitewhere.grpc.client.MultitenantApiChannel;
import com.sitewhere.grpc.client.spi.IApiDemux;
import com.sitewhere.grpc.client.spi.client.IBatchManagementApiChannel;
import com.sitewhere.grpc.model.batch.BatchModelConverter;
import com.sitewhere.grpc.model.converter.CommonModelConverter;
import com.sitewhere.grpc.service.BatchManagementGrpc;
import com.sitewhere.grpc.service.GCreateBatchCommandInvocationRequest;
import com.sitewhere.grpc.service.GCreateBatchCommandInvocationResponse;
import com.sitewhere.grpc.service.GCreateBatchOperationRequest;
import com.sitewhere.grpc.service.GCreateBatchOperationResponse;
import com.sitewhere.grpc.service.GDeleteBatchOperationRequest;
import com.sitewhere.grpc.service.GDeleteBatchOperationResponse;
import com.sitewhere.grpc.service.GGetBatchOperationByTokenRequest;
import com.sitewhere.grpc.service.GGetBatchOperationByTokenResponse;
import com.sitewhere.grpc.service.GGetBatchOperationRequest;
import com.sitewhere.grpc.service.GGetBatchOperationResponse;
import com.sitewhere.grpc.service.GListBatchOperationElementsRequest;
import com.sitewhere.grpc.service.GListBatchOperationElementsResponse;
import com.sitewhere.grpc.service.GListBatchOperationsRequest;
import com.sitewhere.grpc.service.GListBatchOperationsResponse;
import com.sitewhere.grpc.service.GUpdateBatchOperationElementRequest;
import com.sitewhere.grpc.service.GUpdateBatchOperationElementResponse;
import com.sitewhere.grpc.service.GUpdateBatchOperationRequest;
import com.sitewhere.grpc.service.GUpdateBatchOperationResponse;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.batch.IBatchElement;
import com.sitewhere.spi.batch.IBatchOperation;
import com.sitewhere.spi.batch.request.IBatchCommandInvocationRequest;
import com.sitewhere.spi.batch.request.IBatchElementUpdateRequest;
import com.sitewhere.spi.batch.request.IBatchOperationCreateRequest;
import com.sitewhere.spi.batch.request.IBatchOperationUpdateRequest;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.search.batch.IBatchOperationSearchCriteria;
import com.sitewhere.spi.search.device.IBatchElementSearchCriteria;
import com.sitewhere.spi.tracing.ITracerProvider;

/**
 * Supports SiteWhere batch management APIs on top of a
 * {@link BatchManagementGrpcChannel}.
 * 
 * @author Derek
 */
public class BatchManagementApiChannel extends MultitenantApiChannel<BatchManagementGrpcChannel>
	implements IBatchManagementApiChannel<BatchManagementGrpcChannel> {

    public BatchManagementApiChannel(IApiDemux<?> demux, String host, int port) {
	super(demux, host, port);
    }

    /*
     * @see
     * com.sitewhere.grpc.client.spi.IApiChannel#createGrpcChannel(com.sitewhere.spi
     * .tracing.ITracerProvider, java.lang.String, int)
     */
    @Override
    public BatchManagementGrpcChannel createGrpcChannel(ITracerProvider tracerProvider, String host, int port) {
	return new BatchManagementGrpcChannel(tracerProvider, host, port);
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#createBatchOperation(com.sitewhere.
     * spi.batch.request.IBatchOperationCreateRequest)
     */
    @Override
    public IBatchOperation createBatchOperation(IBatchOperationCreateRequest request) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, BatchManagementGrpc.getCreateBatchOperationMethod());
	    GCreateBatchOperationRequest.Builder grequest = GCreateBatchOperationRequest.newBuilder();
	    grequest.setRequest(BatchModelConverter.asGrpcBatchOperationCreateRequest(request));
	    GCreateBatchOperationResponse gresponse = getGrpcChannel().getBlockingStub()
		    .createBatchOperation(grequest.build());
	    IBatchOperation response = (gresponse.hasBatchOperation())
		    ? BatchModelConverter.asApiBatchOperation(gresponse.getBatchOperation())
		    : null;
	    GrpcUtils.logClientMethodResponse(BatchManagementGrpc.getCreateBatchOperationMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(BatchManagementGrpc.getCreateBatchOperationMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#createBatchCommandInvocation(com.
     * sitewhere.spi.batch.request.IBatchCommandInvocationRequest)
     */
    @Override
    public IBatchOperation createBatchCommandInvocation(IBatchCommandInvocationRequest request)
	    throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, BatchManagementGrpc.getCreateBatchCommandInvocationMethod());
	    GCreateBatchCommandInvocationRequest.Builder grequest = GCreateBatchCommandInvocationRequest.newBuilder();
	    grequest.setRequest(BatchModelConverter.asGrpcBatchCommandInvocationRequest(request));
	    GCreateBatchCommandInvocationResponse gresponse = getGrpcChannel().getBlockingStub()
		    .createBatchCommandInvocation(grequest.build());
	    IBatchOperation response = (gresponse.hasBatchOperation())
		    ? BatchModelConverter.asApiBatchOperation(gresponse.getBatchOperation())
		    : null;
	    GrpcUtils.logClientMethodResponse(BatchManagementGrpc.getCreateBatchCommandInvocationMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(BatchManagementGrpc.getCreateBatchCommandInvocationMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#updateBatchOperation(java.util.UUID,
     * com.sitewhere.spi.batch.request.IBatchOperationUpdateRequest)
     */
    @Override
    public IBatchOperation updateBatchOperation(UUID batchOperationId, IBatchOperationUpdateRequest request)
	    throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, BatchManagementGrpc.getUpdateBatchOperationMethod());
	    GUpdateBatchOperationRequest.Builder grequest = GUpdateBatchOperationRequest.newBuilder();
	    grequest.setBatchOperationId(CommonModelConverter.asGrpcUuid(batchOperationId));
	    grequest.setRequest(BatchModelConverter.asGrpcBatchOperationUpdateRequest(request));
	    GUpdateBatchOperationResponse gresponse = getGrpcChannel().getBlockingStub()
		    .updateBatchOperation(grequest.build());
	    IBatchOperation response = (gresponse.hasBatchOperation())
		    ? BatchModelConverter.asApiBatchOperation(gresponse.getBatchOperation())
		    : null;
	    GrpcUtils.logClientMethodResponse(BatchManagementGrpc.getUpdateBatchOperationMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(BatchManagementGrpc.getUpdateBatchOperationMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#getBatchOperation(java.util.UUID)
     */
    @Override
    public IBatchOperation getBatchOperation(UUID batchOperationId) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, BatchManagementGrpc.getGetBatchOperationMethod());
	    GGetBatchOperationRequest.Builder grequest = GGetBatchOperationRequest.newBuilder();
	    grequest.setBatchOperationId(CommonModelConverter.asGrpcUuid(batchOperationId));
	    GGetBatchOperationResponse gresponse = getGrpcChannel().getBlockingStub()
		    .getBatchOperation(grequest.build());
	    IBatchOperation response = (gresponse.hasBatchOperation())
		    ? BatchModelConverter.asApiBatchOperation(gresponse.getBatchOperation())
		    : null;
	    GrpcUtils.logClientMethodResponse(BatchManagementGrpc.getGetBatchOperationMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(BatchManagementGrpc.getGetBatchOperationMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#getBatchOperationByToken(java.lang.
     * String)
     */
    @Override
    public IBatchOperation getBatchOperationByToken(String token) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, BatchManagementGrpc.getGetBatchOperationByTokenMethod());
	    GGetBatchOperationByTokenRequest.Builder grequest = GGetBatchOperationByTokenRequest.newBuilder();
	    grequest.setToken(token);
	    GGetBatchOperationByTokenResponse gresponse = getGrpcChannel().getBlockingStub()
		    .getBatchOperationByToken(grequest.build());
	    IBatchOperation response = (gresponse.hasBatchOperation())
		    ? BatchModelConverter.asApiBatchOperation(gresponse.getBatchOperation())
		    : null;
	    GrpcUtils.logClientMethodResponse(BatchManagementGrpc.getGetBatchOperationByTokenMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(BatchManagementGrpc.getGetBatchOperationByTokenMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#listBatchOperations(com.sitewhere.
     * spi.search.batch.IBatchOperationSearchCriteria)
     */
    @Override
    public ISearchResults<IBatchOperation> listBatchOperations(IBatchOperationSearchCriteria criteria)
	    throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, BatchManagementGrpc.getListBatchOperationsMethod());
	    GListBatchOperationsRequest.Builder grequest = GListBatchOperationsRequest.newBuilder();
	    grequest.setCriteria(BatchModelConverter.asGrpcBatchOperationSearchCriteria(criteria));
	    GListBatchOperationsResponse gresponse = getGrpcChannel().getBlockingStub()
		    .listBatchOperations(grequest.build());
	    ISearchResults<IBatchOperation> results = BatchModelConverter
		    .asApiBatchOperationSearchResults(gresponse.getResults());
	    GrpcUtils.logClientMethodResponse(BatchManagementGrpc.getListBatchOperationsMethod(), results);
	    return results;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(BatchManagementGrpc.getListBatchOperationsMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#deleteBatchOperation(java.util.UUID)
     */
    @Override
    public IBatchOperation deleteBatchOperation(UUID batchOperationId) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, BatchManagementGrpc.getDeleteBatchOperationMethod());
	    GDeleteBatchOperationRequest.Builder grequest = GDeleteBatchOperationRequest.newBuilder();
	    grequest.setBatchOperationId(CommonModelConverter.asGrpcUuid(batchOperationId));
	    GDeleteBatchOperationResponse gresponse = getGrpcChannel().getBlockingStub()
		    .deleteBatchOperation(grequest.build());
	    IBatchOperation response = (gresponse.hasBatchOperation())
		    ? BatchModelConverter.asApiBatchOperation(gresponse.getBatchOperation())
		    : null;
	    GrpcUtils.logClientMethodResponse(BatchManagementGrpc.getDeleteBatchOperationMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(BatchManagementGrpc.getDeleteBatchOperationMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#listBatchElements(java.util.UUID,
     * com.sitewhere.spi.search.device.IBatchElementSearchCriteria)
     */
    @Override
    public ISearchResults<IBatchElement> listBatchElements(UUID batchOperationId, IBatchElementSearchCriteria criteria)
	    throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, BatchManagementGrpc.getListBatchOperationElementsMethod());
	    GListBatchOperationElementsRequest.Builder grequest = GListBatchOperationElementsRequest.newBuilder();
	    grequest.setBatchOperationId(CommonModelConverter.asGrpcUuid(batchOperationId));
	    grequest.setCriteria(BatchModelConverter.asGrpcBatchElementSearchCriteria(criteria));
	    GListBatchOperationElementsResponse gresponse = getGrpcChannel().getBlockingStub()
		    .listBatchOperationElements(grequest.build());
	    ISearchResults<IBatchElement> results = BatchModelConverter
		    .asApiBatchElementSearchResults(gresponse.getResults());
	    GrpcUtils.logClientMethodResponse(BatchManagementGrpc.getListBatchOperationElementsMethod(), results);
	    return results;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(BatchManagementGrpc.getListBatchOperationElementsMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.batch.IBatchManagement#updateBatchElement(java.util.UUID,
     * com.sitewhere.spi.batch.request.IBatchElementUpdateRequest)
     */
    @Override
    public IBatchElement updateBatchElement(UUID elementId, IBatchElementUpdateRequest request)
	    throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, BatchManagementGrpc.getUpdateBatchOperationElementMethod());
	    GUpdateBatchOperationElementRequest.Builder grequest = GUpdateBatchOperationElementRequest.newBuilder();
	    grequest.setElementId(CommonModelConverter.asGrpcUuid(elementId));
	    grequest.setRequest(BatchModelConverter.asGrpcBatchElementUpdateRequest(request));
	    GUpdateBatchOperationElementResponse gresponse = getGrpcChannel().getBlockingStub()
		    .updateBatchOperationElement(grequest.build());
	    IBatchElement response = (gresponse.hasElement())
		    ? BatchModelConverter.asApiBatchElement(gresponse.getElement())
		    : null;
	    GrpcUtils.logClientMethodResponse(BatchManagementGrpc.getUpdateBatchOperationElementMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(BatchManagementGrpc.getUpdateBatchOperationElementMethod(), t);
	}
    }
}