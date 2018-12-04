/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.user.microservice;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import com.sitewhere.grpc.client.spi.client.ITenantManagementApiDemux;
import com.sitewhere.grpc.client.tenant.TenantManagementApiDemux;
import com.sitewhere.microservice.GlobalMicroservice;
import com.sitewhere.microservice.hazelcast.HazelcastManager;
import com.sitewhere.server.lifecycle.CompositeLifecycleStep;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.MicroserviceIdentifier;
import com.sitewhere.spi.microservice.configuration.model.IConfigurationModel;
import com.sitewhere.spi.microservice.hazelcast.IHazelcastManager;
import com.sitewhere.spi.microservice.spring.UserManagementBeans;
import com.sitewhere.spi.server.lifecycle.ICompositeLifecycleStep;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.user.IUserManagement;
import com.sitewhere.user.cache.CacheAwareUserManagement;
import com.sitewhere.user.configuration.UserManagementModelProvider;
import com.sitewhere.user.grpc.UserManagementGrpcServer;
import com.sitewhere.user.kafka.UserManagementKafkaTriggers;
import com.sitewhere.user.persistence.UserManagementAccessor;
import com.sitewhere.user.spi.grpc.IUserManagementGrpcServer;
import com.sitewhere.user.spi.microservice.IUserManagementMicroservice;

/**
 * Microservice that provides user management functionality.
 * 
 * @author Derek
 */
public class UserManagementMicroservice extends GlobalMicroservice<MicroserviceIdentifier>
	implements IUserManagementMicroservice<MicroserviceIdentifier> {

    /** Microservice name */
    private static final String NAME = "User Management";

    /** User management configuration file name */
    private static final String CONFIGURATION_PATH = MicroserviceIdentifier.UserManagement.getPath() + ".xml";

    /** Responds to user management GRPC requests */
    private IUserManagementGrpcServer userManagementGrpcServer;

    /** Hazelcast manager */
    private IHazelcastManager hazelcastManager;

    /** User management persistence API */
    private UserManagementAccessor userManagementAccessor = new UserManagementAccessor();

    /** User management implementation */
    private IUserManagement userManagement;

    /** Tenant management API demux */
    private ITenantManagementApiDemux tenantManagementApiDemux;

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.spi.IMicroservice#getName()
     */
    @Override
    public String getName() {
	return NAME;
    }

    /*
     * @see com.sitewhere.spi.microservice.IMicroservice#getIdentifier()
     */
    @Override
    public MicroserviceIdentifier getIdentifier() {
	return MicroserviceIdentifier.UserManagement;
    }

    /*
     * @see com.sitewhere.spi.microservice.IMicroservice#isGlobal()
     */
    @Override
    public boolean isGlobal() {
	return true;
    }

    /*
     * @see com.sitewhere.spi.microservice.IMicroservice#buildConfigurationModel()
     */
    @Override
    public IConfigurationModel buildConfigurationModel() {
	return new UserManagementModelProvider().buildModel();
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.IConfigurableMicroservice#
     * getConfigurationPath()
     */
    @Override
    public String getConfigurationPath() throws SiteWhereException {
	return CONFIGURATION_PATH;
    }

    /*
     * @see com.sitewhere.microservice.configuration.ConfigurableMicroservice#
     * configurationInitialize(org.springframework.context.ApplicationContext,
     * org.springframework.context.ApplicationContext,
     * com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void configurationInitialize(ApplicationContext global, ApplicationContext local,
	    ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.configurationInitialize(global, local, monitor);
	this.userManagement = initializeUserManagement(local);

	// Composite step for initializing microservice.
	ICompositeLifecycleStep init = new CompositeLifecycleStep("Initialize configuration for " + getName());

	// Initialize user management implementation.
	init.addInitializeStep(this, getUserManagement(), true);

	// Execute initialization steps.
	init.execute(monitor);
    }

    /**
     * Initialize user management implementation.
     * 
     * @param context
     * @return
     * @throws SiteWhereException
     */
    protected IUserManagement initializeUserManagement(ApplicationContext context) throws SiteWhereException {
	try {
	    IUserManagement bean = (IUserManagement) context.getBean(UserManagementBeans.BEAN_USER_MANAGEMENT);
	    IUserManagement cached = new CacheAwareUserManagement(bean, this);
	    return new UserManagementKafkaTriggers(cached);
	} catch (NoSuchBeanDefinitionException e) {
	    throw new SiteWhereException("User management bean not found.", e);
	}
    }

    /*
     * @see com.sitewhere.microservice.configuration.ConfigurableMicroservice#
     * configurationStart(org.springframework.context.ApplicationContext,
     * org.springframework.context.ApplicationContext,
     * com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void configurationStart(ApplicationContext global, ApplicationContext local,
	    ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.configurationStart(global, local, monitor);
	getUserManagementAccessor().setDelegate(getUserManagement());

	// Composite step for starting microservice.
	ICompositeLifecycleStep start = new CompositeLifecycleStep("Start " + getName());

	// Start user mangement persistence.
	start.addStartStep(this, getUserManagement(), true);

	// Execute initialization steps.
	start.execute(monitor);
    }

    /*
     * @see com.sitewhere.microservice.configuration.ConfigurableMicroservice#
     * configurationStop(org.springframework.context.ApplicationContext,
     * org.springframework.context.ApplicationContext,
     * com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void configurationStop(ApplicationContext global, ApplicationContext local,
	    ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.configurationStop(global, local, monitor);

	// Composite step for stopping microservice.
	ICompositeLifecycleStep stop = new CompositeLifecycleStep("Stop " + getName());

	// Stop user management persistence.
	stop.addStopStep(this, getUserManagement());

	// Execute shutdown steps.
	stop.execute(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.IGlobalMicroservice#microserviceInitialize
     * (com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void microserviceInitialize(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Create Hazelcast manager.
	this.hazelcastManager = new HazelcastManager();

	// Create GRPC components.
	createGrpcComponents();

	// Composite step for initializing microservice.
	ICompositeLifecycleStep init = new CompositeLifecycleStep("Initialize " + getName());

	// Initialize Hazelcast manager.
	init.addInitializeStep(this, getHazelcastManager(), true);

	// Initialize user management GRPC server.
	init.addInitializeStep(this, getUserManagementGrpcServer(), true);

	// Initialize tenant management API demux.
	init.addInitializeStep(this, getTenantManagementApiDemux(), true);

	// Execute initialization steps.
	init.execute(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.IGlobalMicroservice#microserviceStart(com.
     * sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void microserviceStart(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Composite step for starting microservice.
	ICompositeLifecycleStep start = new CompositeLifecycleStep("Start " + getName());

	// Start Hazelcast manager.
	start.addStartStep(this, getHazelcastManager(), true);

	// Start GRPC server.
	start.addStartStep(this, getUserManagementGrpcServer(), true);

	// Start tenant management API demux.
	start.addStartStep(this, getTenantManagementApiDemux(), true);

	// Execute initialization steps.
	start.execute(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.spi.IGlobalMicroservice#microserviceStop(com.
     * sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void microserviceStop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Composite step for stopping microservice.
	ICompositeLifecycleStep stop = new CompositeLifecycleStep("Stop " + getName());

	// Stop tenant management API demux.
	stop.addStopStep(this, getTenantManagementApiDemux());

	// Stop GRPC server.
	stop.addStopStep(this, getUserManagementGrpcServer());

	// Stop Hazelcast manager.
	stop.addStopStep(this, getHazelcastManager());

	// Execute shutdown steps.
	stop.execute(monitor);
    }

    /**
     * Create components that interact via GRPC.
     */
    protected void createGrpcComponents() {
	this.userManagementGrpcServer = new UserManagementGrpcServer(this, getUserManagementAccessor());
	this.tenantManagementApiDemux = new TenantManagementApiDemux();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.user.spi.microservice.IUserManagementMicroservice#
     * getUserManagementGrpcServer()
     */
    @Override
    public IUserManagementGrpcServer getUserManagementGrpcServer() {
	return userManagementGrpcServer;
    }

    public void setUserManagementGrpcServer(IUserManagementGrpcServer userManagementGrpcServer) {
	this.userManagementGrpcServer = userManagementGrpcServer;
    }

    /*
     * @see
     * com.sitewhere.spi.microservice.ICachingMicroservice#getHazelcastManager()
     */
    @Override
    public IHazelcastManager getHazelcastManager() {
	return hazelcastManager;
    }

    public void setHazelcastManager(IHazelcastManager hazelcastManager) {
	this.hazelcastManager = hazelcastManager;
    }

    /*
     * @see com.sitewhere.user.spi.microservice.IUserManagementMicroservice#
     * getUserManagement()
     */
    @Override
    public IUserManagement getUserManagement() {
	return userManagement;
    }

    public void setUserManagement(IUserManagement userManagement) {
	this.userManagement = userManagement;
    }

    /*
     * @see com.sitewhere.grpc.client.spi.provider.ITenantManagementDemuxProvider#
     * getTenantManagementApiDemux()
     */
    @Override
    public ITenantManagementApiDemux getTenantManagementApiDemux() {
	return tenantManagementApiDemux;
    }

    public void setTenantManagementApiDemux(ITenantManagementApiDemux tenantManagementApiDemux) {
	this.tenantManagementApiDemux = tenantManagementApiDemux;
    }

    public UserManagementAccessor getUserManagementAccessor() {
	return userManagementAccessor;
    }

    public void setUserManagementAccessor(UserManagementAccessor userManagementAccessor) {
	this.userManagementAccessor = userManagementAccessor;
    }
}