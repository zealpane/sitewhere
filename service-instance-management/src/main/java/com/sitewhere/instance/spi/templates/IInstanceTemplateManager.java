/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.instance.spi.templates;

import java.util.Map;

import org.apache.curator.framework.CuratorFramework;

import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.server.lifecycle.ILifecycleComponent;

/**
 * Manages a list of templates used to create SiteWhere instances.
 * 
 * @author Derek
 */
public interface IInstanceTemplateManager extends ILifecycleComponent {

    /**
     * Get map of instance templates indexed by template id.
     * 
     * @return
     * @throws SiteWhereException
     */
    public Map<String, IInstanceTemplate> getInstanceTemplates() throws SiteWhereException;

    /**
     * Copies contents of an instnace template into the Zookeeper instance
     * folder.
     * 
     * @param templateId
     * @param curator
     * @param instancePath
     * @throws SiteWhereException
     */
    public void copyTemplateContentsToZk(String templateId, CuratorFramework curator, String instancePath)
	    throws SiteWhereException;
}