/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.grpc.model.converter;

import java.util.ArrayList;
import java.util.List;

import com.sitewhere.grpc.kafka.model.KafkaModel.GLifecycleComponentState;
import com.sitewhere.grpc.kafka.model.KafkaModel.GLifecycleStatus;
import com.sitewhere.grpc.kafka.model.KafkaModel.GLogLevel;
import com.sitewhere.grpc.kafka.model.KafkaModel.GLoggedException;
import com.sitewhere.grpc.kafka.model.KafkaModel.GLoggedStackTraceElement;
import com.sitewhere.grpc.kafka.model.KafkaModel.GMicroserviceLogMessage;
import com.sitewhere.grpc.kafka.model.KafkaModel.GMicroserviceState;
import com.sitewhere.grpc.kafka.model.KafkaModel.GStateUpdate;
import com.sitewhere.grpc.kafka.model.KafkaModel.GTenantEngineState;
import com.sitewhere.rest.model.microservice.logging.LoggedException;
import com.sitewhere.rest.model.microservice.logging.LoggedStackTraceElement;
import com.sitewhere.rest.model.microservice.logging.MicroserviceLogMessage;
import com.sitewhere.rest.model.microservice.state.LifecycleComponentState;
import com.sitewhere.rest.model.microservice.state.MicroserviceState;
import com.sitewhere.rest.model.microservice.state.TenantEngineState;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.logging.ILoggedException;
import com.sitewhere.spi.microservice.logging.ILoggedStackTraceElement;
import com.sitewhere.spi.microservice.logging.IMicroserviceLogMessage;
import com.sitewhere.spi.microservice.logging.LogLevel;
import com.sitewhere.spi.microservice.state.ILifecycleComponentState;
import com.sitewhere.spi.microservice.state.IMicroserviceState;
import com.sitewhere.spi.microservice.state.ITenantEngineState;
import com.sitewhere.spi.server.lifecycle.LifecycleStatus;

/**
 * Convert model objects passed on Kafka topics.
 * 
 * @author Derek
 */
public class KafkaModelConverter {

    /**
     * Convert lifecycle status from API to GRPC.
     * 
     * @param grpc
     * @return
     * @throws SiteWhereException
     */
    public static LifecycleStatus asApiLifecycleStatus(GLifecycleStatus grpc) throws SiteWhereException {
	switch (grpc) {
	case LIFECYCLE_STATUS_INITIALIZING:
	    return LifecycleStatus.Initializing;
	case LIFECYCLE_STATUS_INITIALIZATION_ERROR:
	    return LifecycleStatus.InitializationError;
	case LIFECYCLE_STATUS_STOPPED:
	    return LifecycleStatus.Stopped;
	case LIFECYCLE_STATUS_STOPPED_WITH_ERRORS:
	    return LifecycleStatus.StoppedWithErrors;
	case LIFECYCLE_STATUS_STARTING:
	    return LifecycleStatus.Starting;
	case LIFECYCLE_STATUS_STARTED:
	    return LifecycleStatus.Started;
	case LIFECYCLE_STATUS_STARTED_WITH_ERRORS:
	    return LifecycleStatus.StartedWithErrors;
	case LIFECYCLE_STATUS_PAUSING:
	    return LifecycleStatus.Pausing;
	case LIFECYCLE_STATUS_PAUSED:
	    return LifecycleStatus.Paused;
	case LIFECYCLE_STATUS_STOPPING:
	    return LifecycleStatus.Stopping;
	case LIFECYCLE_STATUS_TERMINATING:
	    return LifecycleStatus.Terminating;
	case LIFECYCLE_STATUS_TERMINATED:
	    return LifecycleStatus.Terminated;
	case LIFECYCLE_STATUS_ERROR:
	    return LifecycleStatus.LifecycleError;
	case UNRECOGNIZED:
	    throw new SiteWhereException("Unknown lifecycle status: " + grpc.name());
	}
	return null;
    }

    /**
     * Convert lifecycle status from API to GRPC.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GLifecycleStatus asGrpcLifecycleStatus(LifecycleStatus api) throws SiteWhereException {
	switch (api) {
	case Initializing:
	    return GLifecycleStatus.LIFECYCLE_STATUS_INITIALIZING;
	case InitializationError:
	    return GLifecycleStatus.LIFECYCLE_STATUS_INITIALIZATION_ERROR;
	case Stopped:
	    return GLifecycleStatus.LIFECYCLE_STATUS_STOPPED;
	case StoppedWithErrors:
	    return GLifecycleStatus.LIFECYCLE_STATUS_STOPPED_WITH_ERRORS;
	case Starting:
	    return GLifecycleStatus.LIFECYCLE_STATUS_STARTING;
	case Started:
	    return GLifecycleStatus.LIFECYCLE_STATUS_STARTED;
	case StartedWithErrors:
	    return GLifecycleStatus.LIFECYCLE_STATUS_STARTED_WITH_ERRORS;
	case Pausing:
	    return GLifecycleStatus.LIFECYCLE_STATUS_PAUSING;
	case Paused:
	    return GLifecycleStatus.LIFECYCLE_STATUS_PAUSED;
	case Stopping:
	    return GLifecycleStatus.LIFECYCLE_STATUS_STOPPING;
	case Terminating:
	    return GLifecycleStatus.LIFECYCLE_STATUS_TERMINATING;
	case Terminated:
	    return GLifecycleStatus.LIFECYCLE_STATUS_TERMINATED;
	case LifecycleError:
	    return GLifecycleStatus.LIFECYCLE_STATUS_ERROR;
	}
	throw new SiteWhereException("Unknown lifecycle status: " + api.name());
    }

    /**
     * Convert lifecycle component state from GRPC to API.
     * 
     * @param grpc
     * @return
     * @throws SiteWhereException
     */
    public static LifecycleComponentState asApiLifecycleComponentState(GLifecycleComponentState grpc)
	    throws SiteWhereException {
	LifecycleComponentState api = new LifecycleComponentState();
	api.setComponentId(CommonModelConverter.asApiUuid(grpc.getId()));
	api.setComponentName(grpc.getName());
	api.setStatus(KafkaModelConverter.asApiLifecycleStatus(grpc.getStatus()));

	if (grpc.getErrorFramesCount() > 0) {
	    api.setErrorStack(grpc.getErrorFramesList());
	}

	if (grpc.getChildComponentStatesCount() > 0) {
	    api.setChildComponentStates(new ArrayList<>());
	    for (GLifecycleComponentState child : grpc.getChildComponentStatesList()) {
		api.getChildComponentStates().add(KafkaModelConverter.asApiLifecycleComponentState(child));
	    }
	}

	return api;
    }

    /**
     * Convert lifecycle component state from API to GRPC.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GLifecycleComponentState asGrpcLifecycleComponentState(ILifecycleComponentState api)
	    throws SiteWhereException {
	GLifecycleComponentState.Builder grpc = GLifecycleComponentState.newBuilder();
	grpc.setId(CommonModelConverter.asGrpcUuid(api.getComponentId()));
	grpc.setName(api.getComponentName());
	grpc.setStatus(KafkaModelConverter.asGrpcLifecycleStatus(api.getStatus()));
	if (api.getErrorStack() != null) {
	    grpc.addAllErrorFrames(api.getErrorStack());
	}
	if (api.getChildComponentStates() != null) {
	    for (ILifecycleComponentState child : api.getChildComponentStates()) {
		grpc.addChildComponentStates(KafkaModelConverter.asGrpcLifecycleComponentState(child));
	    }
	}
	return grpc.build();
    }

    /**
     * Convert microservice state from GRPC to API.
     * 
     * @param grpc
     * @return
     * @throws SiteWhereException
     */
    public static MicroserviceState asApiMicroserviceState(GMicroserviceState grpc) throws SiteWhereException {
	MicroserviceState api = new MicroserviceState();
	api.setMicroservice(MicroserviceModelConverter.asApiMicroserviceDetails(grpc.getMicroservice()));
	api.setLifecycleStatus(KafkaModelConverter.asApiLifecycleStatus(grpc.getStatus()));
	return api;
    }

    /**
     * Convert microservice state from API to GRPC.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GMicroserviceState asGrpcMicroserviceState(IMicroserviceState api) throws SiteWhereException {
	GMicroserviceState.Builder grpc = GMicroserviceState.newBuilder();
	grpc.setMicroservice(MicroserviceModelConverter.asGrpcMicroserviceDetails(api.getMicroservice()));
	grpc.setStatus(KafkaModelConverter.asGrpcLifecycleStatus(api.getLifecycleStatus()));
	return grpc.build();
    }

    /**
     * Convert tenant engine state from GRPC to API.
     * 
     * @param grpc
     * @return
     * @throws SiteWhereException
     */
    public static TenantEngineState asApiTenantEngineState(GTenantEngineState grpc) throws SiteWhereException {
	TenantEngineState api = new TenantEngineState();
	api.setMicroservice(MicroserviceModelConverter.asApiMicroserviceDetails(grpc.getMicroservice()));
	api.setTenantId(CommonModelConverter.asApiUuid(grpc.getTenantId()));
	api.setComponentState(KafkaModelConverter.asApiLifecycleComponentState(grpc.getState()));
	return api;
    }

    /**
     * Convert tenant engine state from API to GRPC.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GTenantEngineState asGrpcTenantEngineState(ITenantEngineState api) throws SiteWhereException {
	GTenantEngineState.Builder grpc = GTenantEngineState.newBuilder();
	grpc.setMicroservice(MicroserviceModelConverter.asGrpcMicroserviceDetails(api.getMicroservice()));
	grpc.setTenantId(CommonModelConverter.asGrpcUuid(api.getTenantId()));
	grpc.setState(KafkaModelConverter.asGrpcLifecycleComponentState(api.getComponentState()));
	return grpc.build();
    }

    /**
     * Convert microservice state to generic state update.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GStateUpdate asGrpcGenericStateUpdate(IMicroserviceState api) throws SiteWhereException {
	GStateUpdate.Builder grpc = GStateUpdate.newBuilder();
	grpc.setMicroserviceState(KafkaModelConverter.asGrpcMicroserviceState(api));
	return grpc.build();
    }

    /**
     * Convert tenant engine state to generic state update.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GStateUpdate asGrpcGenericStateUpdate(ITenantEngineState api) throws SiteWhereException {
	GStateUpdate.Builder grpc = GStateUpdate.newBuilder();
	grpc.setTenantEngineState(KafkaModelConverter.asGrpcTenantEngineState(api));
	return grpc.build();
    }

    /**
     * Convert log level from API to GRPC.
     * 
     * @param grpc
     * @return
     * @throws SiteWhereException
     */
    public static LogLevel asApiLogLevel(GLogLevel grpc) throws SiteWhereException {
	switch (grpc) {
	case LOG_LEVEL_DEBUG:
	    return LogLevel.Debug;
	case LOG_LEVEL_ERROR:
	    return LogLevel.Error;
	case LOG_LEVEL_INFO:
	    return LogLevel.Information;
	case LOG_LEVEL_TRACE:
	    return LogLevel.Trace;
	case LOG_LEVEL_WARN:
	    return LogLevel.Warning;
	case UNRECOGNIZED:
	    throw new SiteWhereException("Unknown log level: " + grpc.name());
	}
	return null;
    }

    /**
     * Convert log level from API to GRPC.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GLogLevel asGrpcLogLevel(LogLevel api) throws SiteWhereException {
	switch (api) {
	case Debug:
	    return GLogLevel.LOG_LEVEL_DEBUG;
	case Error:
	    return GLogLevel.LOG_LEVEL_ERROR;
	case Information:
	    return GLogLevel.LOG_LEVEL_INFO;
	case Trace:
	    return GLogLevel.LOG_LEVEL_TRACE;
	case Warning:
	    return GLogLevel.LOG_LEVEL_WARN;
	}
	throw new SiteWhereException("Unknown log level: " + api.name());
    }

    /**
     * Convert logged stack trace element from GRPC to API.
     * 
     * @param grpc
     * @return
     * @throws SiteWhereException
     */
    public static LoggedStackTraceElement asApiLoggedStackTraceElement(GLoggedStackTraceElement grpc)
	    throws SiteWhereException {
	LoggedStackTraceElement api = new LoggedStackTraceElement();
	api.setClassname(grpc.getClazz());
	api.setMethod(grpc.getMethod());
	api.setFile(grpc.getFile());
	api.setLineNumber(grpc.getLineNumber());
	return api;
    }

    /**
     * Convert logged stack trace elements from GRPC to API.
     * 
     * @param grpcs
     * @return
     * @throws SiteWhereException
     */
    public static List<ILoggedStackTraceElement> asApiLoggedStackTraceElements(List<GLoggedStackTraceElement> grpcs)
	    throws SiteWhereException {
	List<ILoggedStackTraceElement> api = new ArrayList<ILoggedStackTraceElement>();
	for (GLoggedStackTraceElement grpc : grpcs) {
	    api.add(KafkaModelConverter.asApiLoggedStackTraceElement(grpc));
	}
	return api;
    }

    /**
     * Convert logged stack trace element from API to GRPC.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GLoggedStackTraceElement asGrpcLoggedStackTraceElement(ILoggedStackTraceElement api)
	    throws SiteWhereException {
	GLoggedStackTraceElement.Builder grpc = GLoggedStackTraceElement.newBuilder();
	grpc.setClazz(api.getClassname());
	grpc.setMethod(api.getMethod());
	grpc.setFile(api.getFile());
	grpc.setLineNumber(api.getLineNumber());
	return grpc.build();
    }

    /**
     * Convert logged stack trace elements from API to GRPC.
     * 
     * @param apis
     * @return
     * @throws SiteWhereException
     */
    public static List<GLoggedStackTraceElement> asGrpcLoggedStackTraceElements(List<ILoggedStackTraceElement> apis)
	    throws SiteWhereException {
	List<GLoggedStackTraceElement> grpcs = new ArrayList<GLoggedStackTraceElement>();
	if (apis != null) {
	    for (ILoggedStackTraceElement api : apis) {
		grpcs.add(KafkaModelConverter.asGrpcLoggedStackTraceElement(api));
	    }
	}
	return grpcs;
    }

    /**
     * Convert logged exception from GRPC to API.
     * 
     * @param grpc
     * @return
     * @throws SiteWhereException
     */
    public static LoggedException asApiLoggedException(GLoggedException grpc) throws SiteWhereException {
	LoggedException api = new LoggedException();
	api.setMessageText(grpc.getMessageText());
	api.setStackTraceElements(KafkaModelConverter.asApiLoggedStackTraceElements(grpc.getElementsList()));
	return api;
    }

    /**
     * Convert logged exception from API to GRPC.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GLoggedException asGrpcLoggedException(ILoggedException api) throws SiteWhereException {
	GLoggedException.Builder grpc = GLoggedException.newBuilder();
	grpc.setMessageText(api.getMessageText());
	grpc.addAllElements(KafkaModelConverter.asGrpcLoggedStackTraceElements(api.getStackTraceElements()));
	return grpc.build();
    }

    /**
     * Convert microservice log message from GRPC to API.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static MicroserviceLogMessage asApiMicroserviceLogMessage(GMicroserviceLogMessage grpc)
	    throws SiteWhereException {
	MicroserviceLogMessage api = new MicroserviceLogMessage();
	api.setMicroserviceIdentifier(grpc.getMicroserviceIdentifier());
	api.setMicroserviceContainerId(grpc.getMicroserviceContainerId());
	if (grpc.hasTenantId()) {
	    api.setTenantId(CommonModelConverter.asApiUuid(grpc.getTenantId()));
	}
	api.setLogLevel(KafkaModelConverter.asApiLogLevel(grpc.getLevel()));
	api.setMessageText(grpc.getMessageText());
	if (grpc.hasException()) {
	    api.setException(KafkaModelConverter.asApiLoggedException(grpc.getException()));
	}
	return api;
    }

    /**
     * Convert microservice log message from API to GRPC.
     * 
     * @param api
     * @return
     * @throws SiteWhereException
     */
    public static GMicroserviceLogMessage asGrpcMicroserviceLogMessage(IMicroserviceLogMessage api)
	    throws SiteWhereException {
	GMicroserviceLogMessage.Builder grpc = GMicroserviceLogMessage.newBuilder();
	grpc.setMicroserviceIdentifier(api.getMicroserviceIdentifier());
	grpc.setMicroserviceContainerId(api.getMicroserviceContainerId());
	if (api.getTenantId() != null) {
	    grpc.setTenantId(CommonModelConverter.asGrpcUuid(api.getTenantId()));
	}
	grpc.setLevel(KafkaModelConverter.asGrpcLogLevel(api.getLogLevel()));
	grpc.setMessageText(api.getMessageText());
	if (api.getException() != null) {
	    grpc.setException(KafkaModelConverter.asGrpcLoggedException(api.getException()));
	}
	return grpc.build();
    }
}