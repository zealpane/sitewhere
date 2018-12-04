/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.tenant.persistence.mongodb;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.server.lifecycle.ILifecycleComponent;

/**
 * Mongo client that provides tenant management collections.
 * 
 * @author Derek
 */
public interface ITenantManagementMongoClient extends ILifecycleComponent {

    /** Default collection name for SiteWhere tenants */
    public static final String DEFAULT_TENANTS_COLLECTION_NAME = "tenants";

    /**
     * Get the collection for tenants.
     * 
     * @return
     */
    public MongoCollection<Document> getTenantsCollection() throws SiteWhereException;
}