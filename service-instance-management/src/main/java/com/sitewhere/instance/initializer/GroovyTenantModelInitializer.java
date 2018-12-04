/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.instance.initializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sitewhere.groovy.IGroovyVariables;
import com.sitewhere.instance.spi.initializer.ITenantModelInitializer;
import com.sitewhere.microservice.groovy.GroovyConfiguration;
import com.sitewhere.rest.model.tenant.request.scripting.TenantManagementRequestBuilder;
import com.sitewhere.server.ModelInitializer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.tenant.ITenantManagement;

import groovy.lang.Binding;

/**
 * Implementation of {@link ITenantModelInitializer} that delegates creation
 * logic to a Groovy script.
 * 
 * @author Derek
 */
public class GroovyTenantModelInitializer extends ModelInitializer implements ITenantModelInitializer {

    /** Static logger instance */
    private static Log LOGGER = LogFactory.getLog(GroovyTenantModelInitializer.class);

    /** Groovy configuration */
    private GroovyConfiguration groovyConfiguration;

    /** Relative path to Groovy script */
    private String scriptPath;

    public GroovyTenantModelInitializer(GroovyConfiguration groovyConfiguration, String scriptPath) {
	this.groovyConfiguration = groovyConfiguration;
	this.scriptPath = scriptPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.server.tenant.ITenantModelInitializer#initialize(com.
     * sitewhere. spi.tenant.ITenantManagement)
     */
    @Override
    public void initialize(ITenantManagement tenantManagement) throws SiteWhereException {
	// Skip if not enabled.
	if (!isEnabled()) {
	    return;
	}

	Binding binding = new Binding();
	binding.setVariable(IGroovyVariables.VAR_LOGGER, LOGGER);
	binding.setVariable(IGroovyVariables.VAR_TENANT_MANAGEMENT_BUILDER,
		new TenantManagementRequestBuilder(tenantManagement));

	try {
	    getGroovyConfiguration().run(getScriptPath(), binding);
	} catch (SiteWhereException e) {
	    throw new SiteWhereException("Unable to run tenant model initializer.", e);
	}
    }

    public GroovyConfiguration getGroovyConfiguration() {
	return groovyConfiguration;
    }

    public void setGroovyConfiguration(GroovyConfiguration groovyConfiguration) {
	this.groovyConfiguration = groovyConfiguration;
    }

    public String getScriptPath() {
	return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
	this.scriptPath = scriptPath;
    }
}