/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.batch.grpc;

import com.sitewhere.batch.spi.microservice.IBatchOperationsMicroservice;
import com.sitewhere.grpc.client.GrpcUtils;
import com.sitewhere.grpc.client.spi.server.IGrpcApiImplementation;
import com.sitewhere.grpc.model.BatchModel.GBatchOperationElementSearchResults;
import com.sitewhere.grpc.model.BatchModel.GBatchOperationSearchResults;
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
import com.sitewhere.grpc.service.GListBatchOperationElementsRequest;
import com.sitewhere.grpc.service.GListBatchOperationElementsResponse;
import com.sitewhere.grpc.service.GListBatchOperationsRequest;
import com.sitewhere.grpc.service.GListBatchOperationsResponse;
import com.sitewhere.grpc.service.GUpdateBatchOperationElementRequest;
import com.sitewhere.grpc.service.GUpdateBatchOperationElementResponse;
import com.sitewhere.grpc.service.GUpdateBatchOperationRequest;
import com.sitewhere.grpc.service.GUpdateBatchOperationResponse;
import com.sitewhere.spi.batch.IBatchElement;
import com.sitewhere.spi.batch.IBatchManagement;
import com.sitewhere.spi.batch.IBatchOperation;
import com.sitewhere.spi.batch.request.IBatchCommandInvocationRequest;
import com.sitewhere.spi.batch.request.IBatchElementUpdateRequest;
import com.sitewhere.spi.batch.request.IBatchOperationCreateRequest;
import com.sitewhere.spi.batch.request.IBatchOperationUpdateRequest;
import com.sitewhere.spi.microservice.IMicroservice;
import com.sitewhere.spi.search.ISearchResults;

import io.grpc.stub.StreamObserver;

/**
 * Implements server logic for batch management GRPC requests.
 * 
 * @author Derek
 */
public class BatchManagementImpl extends BatchManagementGrpc.BatchManagementImplBase implements IGrpcApiImplementation {

    /** Parent microservice */
    private IBatchOperationsMicroservice microservice;

    /** Batch management persistence */
    private IBatchManagement batchManagement;

    public BatchManagementImpl(IBatchOperationsMicroservice microservice, IBatchManagement batchManagement) {
	this.microservice = microservice;
	this.batchManagement = batchManagement;
    }

    /*
     * @see com.sitewhere.grpc.service.BatchManagementGrpc.BatchManagementImplBase#
     * createBatchOperation(com.sitewhere.grpc.service.GCreateBatchOperationRequest,
     * io.grpc.stub.StreamObserver)
     */
    @Override
    public void createBatchOperation(GCreateBatchOperationRequest request,
	    StreamObserver<GCreateBatchOperationResponse> responseObserver) {
	try {
	    GrpcUtils.handleServerMethodEntry(this, BatchManagementGrpc.getCreateBatchOperationMethod());
	    IBatchOperationCreateRequest apiRequest = BatchModelConverter
		    .asApiBatchOperationCreateRequest(request.getRequest());
	    IBatchOperation apiResult = getBatchManagement().createBatchOperation(apiRequest);
	    GCreateBatchOperationResponse.Builder response = GCreateBatchOperationResponse.newBuilder();
	    response.setBatchOperation(BatchModelConverter.asGrpcBatchOperation(apiResult));
	    responseObserver.onNext(response.build());
	    responseObserver.onCompleted();
	} catch (Throwable e) {
	    GrpcUtils.handleServerMethodException(BatchManagementGrpc.getCreateBatchOperationMethod(), e,
		    responseObserver);
	} finally {
	    GrpcUtils.handleServerMethodExit(BatchManagementGrpc.getCreateBatchOperationMethod());
	}
    }

    /*
     * @see com.sitewhere.grpc.service.BatchManagementGrpc.BatchManagementImplBase#
     * createBatchCommandInvocation(com.sitewhere.grpc.service.
     * GCreateBatchCommandInvocationRequest, io.grpc.stub.StreamObserver)
     */
    @Override
    public void createBatchCommandInvocation(GCreateBatchCommandInvocationRequest request,
	    StreamObserver<GCreateBatchCommandInvocationResponse> responseObserver) {
	try {
	    GrpcUtils.handleServerMethodEntry(this, BatchManagementGrpc.getCreateBatchCommandInvocationMethod());
	    IBatchCommandInvocationRequest apiRequest = BatchModelConverter
		    .asApiBatchCommandInvocationRequest(request.getRequest());
	    IBatchOperation apiResult = getBatchManagement().createBatchCommandInvocation(apiRequest);
	    GCreateBatchCommandInvocationResponse.Builder response = GCreateBatchCommandInvocationResponse.newBuilder();
	    response.setBatchOperation(BatchModelConverter.asGrpcBatchOperation(apiResult));
	    responseObserver.onNext(response.build());
	    responseObserver.onCompleted();
	} catch (Throwable e) {
	    GrpcUtils.handleServerMethodException(BatchManagementGrpc.getCreateBatchCommandInvocationMethod(), e,
		    responseObserver);
	} finally {
	    GrpcUtils.handleServerMethodExit(BatchManagementGrpc.getCreateBatchCommandInvocationMethod());
	}
    }

    /*
     * @see com.sitewhere.grpc.service.BatchManagementGrpc.BatchManagementImplBase#
     * updateBatchOperation(com.sitewhere.grpc.service.GUpdateBatchOperationRequest,
     * io.grpc.stub.StreamObserver)
     */
    @Override
    public void updateBatchOperation(GUpdateBatchOperationRequest request,
	    StreamObserver<GUpdateBatchOperationResponse> responseObserver) {
	try {
	    GrpcUtils.handleServerMethodEntry(this, BatchManagementGrpc.getUpdateBatchOperationMethod());
	    IBatchOperationUpdateRequest apiRequest = BatchModelConverter
		    .asApiBatchOperationUpdateRequest(request.getRequest());
	    IBatchOperation apiResult = getBatchManagement()
		    .updateBatchOperation(CommonModelConverter.asApiUuid(request.getBatchOperationId()), apiRequest);
	    GUpdateBatchOperationResponse.Builder response = GUpdateBatchOperationResponse.newBuilder();
	    response.setBatchOperation(BatchModelConverter.asGrpcBatchOperation(apiResult));
	    responseObserver.onNext(response.build());
	    responseObserver.onCompleted();
	} catch (Throwable e) {
	    GrpcUtils.handleServerMethodException(BatchManagementGrpc.getUpdateBatchOperationMethod(), e,
		    responseObserver);
	} finally {
	    GrpcUtils.handleServerMethodExit(BatchManagementGrpc.getUpdateBatchOperationMethod());
	}
    }

    /*
     * @see com.sitewhere.grpc.service.BatchManagementGrpc.BatchManagementImplBase#
     * getBatchOperationByToken(com.sitewhere.grpc.service.
     * GGetBatchOperationByTokenRequest, io.grpc.stub.StreamObserver)
     */
    @Override
    public void getBatchOperationByToken(GGetBatchOperationByTokenRequest request,
	    StreamObserver<GGetBatchOperationByTokenResponse> responseObserver) {
	try {
	    GrpcUtils.handleServerMethodEntry(this, BatchManagementGrpc.getGetBatchOperationByTokenMethod());
	    IBatchOperation apiResult = getBatchManagement().getBatchOperationByToken(request.getToken());
	    GGetBatchOperationByTokenResponse.Builder response = GGetBatchOperationByTokenResponse.newBuilder();
	    if (apiResult != null) {
		response.setBatchOperation(BatchModelConverter.asGrpcBatchOperation(apiResult));
	    }
	    responseObserver.onNext(response.build());
	    responseObserver.onCompleted();
	} catch (Throwable e) {
	    GrpcUtils.handleServerMethodException(BatchManagementGrpc.getGetBatchOperationByTokenMethod(), e,
		    responseObserver);
	} finally {
	    GrpcUtils.handleServerMethodExit(BatchManagementGrpc.getGetBatchOperationByTokenMethod());
	}
    }

    /*
     * @see com.sitewhere.grpc.service.BatchManagementGrpc.BatchManagementImplBase#
     * listBatchOperations(com.sitewhere.grpc.service.GListBatchOperationsRequest,
     * io.grpc.stub.StreamObserver)
     */
    @Override
    public void listBatchOperations(GListBatchOperationsRequest request,
	    StreamObserver<GListBatchOperationsResponse> responseObserver) {
	try {
	    GrpcUtils.handleServerMethodEntry(this, BatchManagementGrpc.getListBatchOperationsMethod());
	    ISearchResults<IBatchOperation> apiResult = getBatchManagement()
		    .listBatchOperations(BatchModelConverter.asApiBatchOperationSearchCriteria(request.getCriteria()));
	    GListBatchOperationsResponse.Builder response = GListBatchOperationsResponse.newBuilder();
	    GBatchOperationSearchResults.Builder results = GBatchOperationSearchResults.newBuilder();
	    for (IBatchOperation api : apiResult.getResults()) {
		results.addBatchOperations(BatchModelConverter.asGrpcBatchOperation(api));
	    }
	    results.setCount(apiResult.getNumResults());
	    response.setResults(results.build());
	    responseObserver.onNext(response.build());
	    responseObserver.onCompleted();
	} catch (Throwable e) {
	    GrpcUtils.handleServerMethodException(BatchManagementGrpc.getListBatchOperationsMethod(), e,
		    responseObserver);
	} finally {
	    GrpcUtils.handleServerMethodExit(BatchManagementGrpc.getListBatchOperationsMethod());
	}
    }

    /*
     * @see com.sitewhere.grpc.service.BatchManagementGrpc.BatchManagementImplBase#
     * deleteBatchOperation(com.sitewhere.grpc.service.GDeleteBatchOperationRequest,
     * io.grpc.stub.StreamObserver)
     */
    @Override
    public void deleteBatchOperation(GDeleteBatchOperationRequest request,
	    StreamObserver<GDeleteBatchOperationResponse> responseObserver) {
	try {
	    GrpcUtils.handleServerMethodEntry(this, BatchManagementGrpc.getDeleteBatchOperationMethod());
	    IBatchOperation apiResult = getBatchManagement()
		    .deleteBatchOperation(CommonModelConverter.asApiUuid(request.getBatchOperationId()));
	    GDeleteBatchOperationResponse.Builder response = GDeleteBatchOperationResponse.newBuilder();
	    response.setBatchOperation(BatchModelConverter.asGrpcBatchOperation(apiResult));
	    responseObserver.onNext(response.build());
	    responseObserver.onCompleted();
	} catch (Throwable e) {
	    GrpcUtils.handleServerMethodException(BatchManagementGrpc.getDeleteBatchOperationMethod(), e,
		    responseObserver);
	} finally {
	    GrpcUtils.handleServerMethodExit(BatchManagementGrpc.getDeleteBatchOperationMethod());
	}
    }

    /*
     * @see com.sitewhere.grpc.service.BatchManagementGrpc.BatchManagementImplBase#
     * listBatchOperationElements(com.sitewhere.grpc.service.
     * GListBatchOperationElementsRequest, io.grpc.stub.StreamObserver)
     */
    @Override
    public void listBatchOperationElements(GListBatchOperationElementsRequest request,
	    StreamObserver<GListBatchOperationElementsResponse> responseObserver) {
	try {
	    GrpcUtils.handleServerMethodEntry(this, BatchManagementGrpc.getListBatchOperationElementsMethod());
	    ISearchResults<IBatchElement> apiResult = getBatchManagement().listBatchElements(
		    CommonModelConverter.asApiUuid(request.getBatchOperationId()),
		    BatchModelConverter.asApiBatchElementSearchCriteria(request.getCriteria()));
	    GListBatchOperationElementsResponse.Builder response = GListBatchOperationElementsResponse.newBuilder();
	    GBatchOperationElementSearchResults.Builder results = GBatchOperationElementSearchResults.newBuilder();
	    for (IBatchElement api : apiResult.getResults()) {
		results.addBatchOperationElements(BatchModelConverter.asGrpcBatchElement(api));
	    }
	    results.setCount(apiResult.getNumResults());
	    response.setResults(results.build());
	    responseObserver.onNext(response.build());
	    responseObserver.onCompleted();
	} catch (Throwable e) {
	    GrpcUtils.handleServerMethodException(BatchManagementGrpc.getListBatchOperationElementsMethod(), e,
		    responseObserver);
	} finally {
	    GrpcUtils.handleServerMethodExit(BatchManagementGrpc.getListBatchOperationElementsMethod());
	}
    }

    /*
     * @see com.sitewhere.grpc.service.BatchManagementGrpc.BatchManagementImplBase#
     * updateBatchOperationElement(com.sitewhere.grpc.service.
     * GUpdateBatchOperationElementRequest, io.grpc.stub.StreamObserver)
     */
    @Override
    public void updateBatchOperationElement(GUpdateBatchOperationElementRequest request,
	    StreamObserver<GUpdateBatchOperationElementResponse> responseObserver) {
	try {
	    GrpcUtils.handleServerMethodEntry(this, BatchManagementGrpc.getUpdateBatchOperationElementMethod());
	    IBatchElementUpdateRequest apiRequest = BatchModelConverter
		    .asApiBatchElementUpdateRequest(request.getRequest());
	    IBatchElement apiResult = getBatchManagement()
		    .updateBatchElement(CommonModelConverter.asApiUuid(request.getElementId()), apiRequest);
	    GUpdateBatchOperationElementResponse.Builder response = GUpdateBatchOperationElementResponse.newBuilder();
	    response.setElement(BatchModelConverter.asGrpcBatchElement(apiResult));
	    responseObserver.onNext(response.build());
	    responseObserver.onCompleted();
	} catch (Throwable e) {
	    GrpcUtils.handleServerMethodException(BatchManagementGrpc.getUpdateBatchOperationElementMethod(), e,
		    responseObserver);
	} finally {
	    GrpcUtils.handleServerMethodExit(BatchManagementGrpc.getUpdateBatchOperationElementMethod());
	}
    }

    /*
     * @see
     * com.sitewhere.grpc.client.spi.server.IGrpcApiImplementation#getMicroservice()
     */
    @Override
    public IMicroservice<?> getMicroservice() {
	return microservice;
    }

    protected IBatchManagement getBatchManagement() {
	return batchManagement;
    }
}