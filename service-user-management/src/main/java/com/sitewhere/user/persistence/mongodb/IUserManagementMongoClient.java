/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.user.persistence.mongodb;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.sitewhere.spi.SiteWhereException;

/**
 * Mongo client that provides user management collections.
 * 
 * @author Derek
 */
public interface IUserManagementMongoClient {

    /** Default collection name for SiteWhere users */
    public static final String DEFAULT_USERS_COLLECTION_NAME = "users";

    /** Default collection name for SiteWhere granted authorities */
    public static final String DEFAULT_AUTHORITIES_COLLECTION_NAME = "authorities";

    /**
     * Get the collection for users.
     * 
     * @return
     * @throws SiteWhereException
     */
    public MongoCollection<Document> getUsersCollection() throws SiteWhereException;

    /**
     * Get the collection for authorities.
     * 
     * @return
     * @throws SiteWhereException
     */
    public MongoCollection<Document> getAuthoritiesCollection() throws SiteWhereException;
}