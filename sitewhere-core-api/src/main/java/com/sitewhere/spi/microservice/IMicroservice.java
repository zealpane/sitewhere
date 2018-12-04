/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.microservice;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.configuration.IZookeeperManager;
import com.sitewhere.spi.microservice.configuration.model.IConfigurationModel;
import com.sitewhere.spi.microservice.grpc.IMicroserviceManagementGrpcServer;
import com.sitewhere.spi.microservice.instance.IInstanceSettings;
import com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming;
import com.sitewhere.spi.microservice.logging.IMicroserviceLogProducer;
import com.sitewhere.spi.microservice.scripting.IScriptTemplateManager;
import com.sitewhere.spi.microservice.security.ISystemUser;
import com.sitewhere.spi.microservice.security.ITokenManagement;
import com.sitewhere.spi.microservice.state.IMicroserviceDetails;
import com.sitewhere.spi.microservice.state.IMicroserviceState;
import com.sitewhere.spi.microservice.state.IMicroserviceStateUpdatesKafkaProducer;
import com.sitewhere.spi.microservice.state.ITenantEngineState;
import com.sitewhere.spi.microservice.state.ITopologyStateAggregator;
import com.sitewhere.spi.server.lifecycle.ILifecycleComponent;
import com.sitewhere.spi.system.IVersion;
import com.sitewhere.spi.tracing.ITracerProvider;

/**
 * Functionality common to all SiteWhere microservices.
 * 
 * @author Derek
 */
public interface IMicroservice<T extends IFunctionIdentifier>
	extends ILifecycleComponent, ITracerProvider, IMicroserviceClassification<T> {

    /**
     * Get name shown for microservice.
     * 
     * @return
     */
    public String getName();

    /**
     * Get version information.
     * 
     * @return
     */
    public IVersion getVersion();

    /**
     * Get unique microservice identifier.
     * 
     * @return
     */
    public T getIdentifier();

    /**
     * Get assigned hostname.
     * 
     * @return
     */
    public String getHostname();

    /**
     * Indicates whether the microservice is global in scope.
     * 
     * @return
     */
    public boolean isGlobal();

    /**
     * Get properties that should be passed into Spring context.
     * 
     * @return
     */
    public Map<String, Object> getSpringProperties();

    /**
     * Build configuration model.
     * 
     * @return
     */
    public IConfigurationModel buildConfigurationModel();

    /**
     * Get configuration model.
     * 
     * @return
     */
    public IConfigurationModel getConfigurationModel();

    /**
     * Get details that identify and describe the microservice.
     * 
     * @return
     * @throws SiteWhereException
     */
    public IMicroserviceDetails getMicroserviceDetails();

    /**
     * Get current state for microservice.
     * 
     * @return
     * @throws SiteWhereException
     */
    public IMicroserviceState getCurrentState() throws SiteWhereException;

    /**
     * Called when state of managed tenant engine is updated.
     * 
     * @param state
     */
    public void onTenantEngineStateChanged(ITenantEngineState state);

    /**
     * Get settings for SiteWhere instance.
     * 
     * @return
     */
    public IInstanceSettings getInstanceSettings();

    /**
     * Get token management interface.
     * 
     * @return
     */
    public ITokenManagement getTokenManagement();

    /**
     * Get system superuser.
     * 
     * @return
     */
    public ISystemUser getSystemUser();

    /**
     * Get Kafka topic naming helper.
     * 
     * @return
     */
    public IKafkaTopicNaming getKafkaTopicNaming();

    /**
     * Get microservice management GRPC server.
     * 
     * @return
     */
    public IMicroserviceManagementGrpcServer getMicroserviceManagementGrpcServer();

    /**
     * Get Kafka producer for reporting state updates in microservice and managed
     * tenant engines.
     * 
     * @return
     */
    public IMicroserviceStateUpdatesKafkaProducer getStateUpdatesKafkaProducer();

    /**
     * Get Kafka consumer that aggregates state updates for microservices/tenant
     * engines to build an instance topology dynamically.
     * 
     * @return
     */
    public ITopologyStateAggregator getTopologyStateAggregator();

    /**
     * Get Kafka producer that allows log messages to be processed in a centralized
     * fashion.
     * 
     * @return
     */
    public IMicroserviceLogProducer getMicroserviceLogProducer();

    /**
     * Get manager for script templates which provide examples of
     * microservice-specific scripting funcionality.
     * 
     * @return
     */
    public IScriptTemplateManager getScriptTemplateManager();

    /**
     * Get analytics processor.
     * 
     * @return
     */
    public IMicroserviceAnalytics getMicroserviceAnalytics();

    /**
     * Code executed after microservice has been started.
     */
    public void afterMicroserviceStarted();

    /**
     * Get Zookeeper node path for instance.
     * 
     * @return
     */
    public String getInstanceZkPath();

    /**
     * Get Zookeeper path for instance configuration.
     * 
     * @return
     */
    public String getInstanceConfigurationPath();

    /**
     * Get Zookeeper path for instance state.
     * 
     * @return
     */
    public String getInstanceStatePath();

    /**
     * Get path for marker used to indicate instance is bootstrapped.
     * 
     * @return
     * @throws SiteWhereException
     */
    public String getInstanceBootstrappedMarker() throws SiteWhereException;

    /**
     * Get root folder on local filesystem where script templates may be found.
     * 
     * @return
     */
    public File getScriptTemplatesRoot();

    /**
     * Get Zookeeper manager.
     * 
     * @return
     */
    public IZookeeperManager getZookeeperManager();

    /**
     * Get metric registry.
     * 
     * @return
     */
    public MetricRegistry getMetricRegistry();

    /**
     * Get metrics reporter.
     * 
     * @return
     */
    public ScheduledReporter getMetricsReporter();

    /**
     * Wait for SiteWhere instance configuration metadata to become initialized
     * before proceeding.
     * 
     * @throws SiteWhereException
     */
    public void waitForInstanceInitialization() throws SiteWhereException;

    /**
     * Get executor service that handles long-running microservice operations.
     * 
     * @return
     */
    public ExecutorService getMicroserviceOperationsService();

    /**
     * Get executor service that handle heartbeat messages.
     * 
     * @return
     */
    public ExecutorService getMicroserviceHeartbeatService();
}