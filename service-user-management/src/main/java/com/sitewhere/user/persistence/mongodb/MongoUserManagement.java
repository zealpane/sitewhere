/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.user.persistence.mongodb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.springframework.stereotype.Component;

import com.mongodb.DBObject;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.sitewhere.grpc.client.spi.client.ITenantManagementApiChannel;
import com.sitewhere.mongodb.IMongoConverterLookup;
import com.sitewhere.mongodb.MongoPersistence;
import com.sitewhere.rest.model.user.GrantedAuthority;
import com.sitewhere.rest.model.user.GrantedAuthoritySearchCriteria;
import com.sitewhere.rest.model.user.User;
import com.sitewhere.server.lifecycle.LifecycleComponent;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.server.lifecycle.LifecycleComponentType;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IGrantedAuthoritySearchCriteria;
import com.sitewhere.spi.user.IUser;
import com.sitewhere.spi.user.IUserManagement;
import com.sitewhere.spi.user.IUserSearchCriteria;
import com.sitewhere.spi.user.request.IGrantedAuthorityCreateRequest;
import com.sitewhere.spi.user.request.IUserCreateRequest;
import com.sitewhere.user.persistence.UserManagementPersistence;
import com.sitewhere.user.spi.microservice.IUserManagementMicroservice;

/**
 * User management implementation that uses MongoDB for persistence.
 * 
 * @author dadams
 */
@Component
public class MongoUserManagement extends LifecycleComponent implements IUserManagement {

    /** Converter lookup */
    private static IMongoConverterLookup LOOKUP = new MongoConverters();

    /** Injected with global SiteWhere Mongo client */
    private IUserManagementMongoClient mongoClient;

    public MongoUserManagement() {
	super(LifecycleComponentType.DataStore);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#start(com.sitewhere.spi
     * .server.lifecycle.ILifecycleProgressMonitor)
     */
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	/** Ensure that expected indexes exist */
	ensureIndexes();
    }

    /**
     * Ensure that expected collection indexes exist.
     * 
     * @throws SiteWhereException
     */
    protected void ensureIndexes() throws SiteWhereException {
	getMongoClient().getUsersCollection().createIndex(new Document(MongoUser.PROP_USERNAME, 1),
		new IndexOptions().unique(true));
	getMongoClient().getAuthoritiesCollection().createIndex(new Document(MongoGrantedAuthority.PROP_AUTHORITY, 1),
		new IndexOptions().unique(true));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.spi.user.IUserManagement#createUser(com.sitewhere.spi.user.
     * request.IUserCreateRequest, java.lang.Boolean)
     */
    @Override
    public IUser createUser(IUserCreateRequest request, Boolean encodePassword) throws SiteWhereException {
	boolean encode = ((encodePassword != null) && (encodePassword == false)) ? false : true;
	User user = UserManagementPersistence.userCreateLogic(request, encode);

	MongoCollection<Document> users = getMongoClient().getUsersCollection();
	Document created = MongoUser.toDocument(user);
	MongoPersistence.insert(users, created, ErrorCode.DuplicateUser);
	return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.spi.user.IUserManagement#importUser(com.sitewhere.spi.user.
     * IUser, boolean)
     */
    @Override
    public IUser importUser(IUser imported, boolean overwrite) throws SiteWhereException {
	IUser existing = getUserByUsername(imported.getUsername());
	if (existing != null) {
	    if (!overwrite) {
		throw new SiteWhereSystemException(ErrorCode.DuplicateUser, ErrorLevel.ERROR);
	    }
	    deleteUser(imported.getUsername());
	}
	User user = User.copy(imported);

	MongoCollection<Document> users = getMongoClient().getUsersCollection();
	Document created = MongoUser.toDocument(user);
	MongoPersistence.insert(users, created, ErrorCode.DuplicateUser);
	return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IUserManagement#authenticate(java.lang.String,
     * java.lang.String, boolean)
     */
    @Override
    public IUser authenticate(String username, String password, boolean updateLastLogin) throws SiteWhereException {
	if (password == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidPassword, ErrorLevel.ERROR);
	}
	Document userObj = assertUser(username);
	User match = MongoUser.fromDocument(userObj);
	if (!UserManagementPersistence.passwordMatches(password, match.getHashedPassword())) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidPassword, ErrorLevel.ERROR);
	}

	// Update last login date if requested.
	if (updateLastLogin) {
	    match.setLastLogin(new Date());
	    Document updated = MongoUser.toDocument(match);
	    MongoCollection<Document> users = getMongoClient().getUsersCollection();
	    Document query = new Document(MongoUser.PROP_USERNAME, username);
	    MongoPersistence.update(users, query, updated);
	}

	return match;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IUserManagement#updateUser(java.lang.String,
     * com.sitewhere.spi.user.request.IUserCreateRequest, boolean)
     */
    @Override
    public IUser updateUser(String username, IUserCreateRequest request, boolean encodePassword)
	    throws SiteWhereException {
	Document existing = assertUser(username);

	// Copy any non-null fields.
	User updatedUser = MongoUser.fromDocument(existing);
	UserManagementPersistence.userUpdateLogic(request, updatedUser, encodePassword);

	Document updated = MongoUser.toDocument(updatedUser);

	MongoCollection<Document> users = getMongoClient().getUsersCollection();
	Document query = new Document(MongoUser.PROP_USERNAME, username);
	MongoPersistence.update(users, query, updated);
	return MongoUser.fromDocument(updated);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IUserManagement#getUserByUsername(java.lang.
     * String)
     */
    @Override
    public IUser getUserByUsername(String username) throws SiteWhereException {
	Document dbUser = getUserDocumentByUsername(username);
	if (dbUser != null) {
	    return MongoUser.fromDocument(dbUser);
	}
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IUserManagement#getGrantedAuthorities(java.lang.
     * String)
     */
    @Override
    public List<IGrantedAuthority> getGrantedAuthorities(String username) throws SiteWhereException {
	IUser user = getUserByUsername(username);
	List<String> userAuths = user.getAuthorities();
	ISearchResults<IGrantedAuthority> all = listGrantedAuthorities(new GrantedAuthoritySearchCriteria(1, 0));
	List<IGrantedAuthority> matched = new ArrayList<IGrantedAuthority>();
	for (IGrantedAuthority auth : all.getResults()) {
	    if (userAuths.contains(auth.getAuthority())) {
		matched.add(auth);
	    }
	}
	return matched;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IUserManagement#addGrantedAuthorities(java.lang.
     * String, java.util.List)
     */
    @Override
    public List<IGrantedAuthority> addGrantedAuthorities(String username, List<String> authorities)
	    throws SiteWhereException {
	throw new SiteWhereException("Not implemented.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.spi.user.IUserManagement#removeGrantedAuthorities(java.lang
     * .String, java.util.List)
     */
    @Override
    public List<IGrantedAuthority> removeGrantedAuthorities(String username, List<String> authorities)
	    throws SiteWhereException {
	throw new SiteWhereException("Not implemented.");
    }

    /*
     * @see com.sitewhere.spi.user.IUserManagement#listUsers(com.sitewhere.spi.user.
     * IUserSearchCriteria)
     */
    @Override
    public ISearchResults<IUser> listUsers(IUserSearchCriteria criteria) throws SiteWhereException {
	MongoCollection<Document> users = getMongoClient().getUsersCollection();
	Document dbCriteria = new Document();
	Document sort = new Document(MongoUser.PROP_USERNAME, 1);
	return MongoPersistence.search(IUser.class, users, dbCriteria, sort, criteria, LOOKUP);
    }

    /*
     * @see com.sitewhere.spi.user.IUserManagement#deleteUser(java.lang.String)
     */
    @Override
    public IUser deleteUser(String username) throws SiteWhereException {
	Document existing = assertUser(username);
	MongoCollection<Document> users = getMongoClient().getUsersCollection();
	MongoPersistence.delete(users, existing);
	UserManagementPersistence.userDeleteLogic(username, getTenantManagementApiDemux());
	return MongoUser.fromDocument(existing);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IUserManagement#createGrantedAuthority(com.
     * sitewhere.spi .user.request. IGrantedAuthorityCreateRequest)
     */
    @Override
    public IGrantedAuthority createGrantedAuthority(IGrantedAuthorityCreateRequest request) throws SiteWhereException {
	GrantedAuthority auth = UserManagementPersistence.grantedAuthorityCreateLogic(request);
	MongoCollection<Document> auths = getMongoClient().getAuthoritiesCollection();
	Document created = MongoGrantedAuthority.toDocument(auth);
	MongoPersistence.insert(auths, created, ErrorCode.DuplicateAuthority);
	return auth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IUserManagement#getGrantedAuthorityByName(java.
     * lang.String)
     */
    @Override
    public IGrantedAuthority getGrantedAuthorityByName(String name) throws SiteWhereException {
	Document dbAuth = getGrantedAuthorityDocumentByName(name);
	if (dbAuth != null) {
	    return MongoGrantedAuthority.fromDocument(dbAuth);
	}
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IUserManagement#updateGrantedAuthority(java.lang.
     * String, com.sitewhere.spi.user.request.IGrantedAuthorityCreateRequest)
     */
    @Override
    public IGrantedAuthority updateGrantedAuthority(String name, IGrantedAuthorityCreateRequest request)
	    throws SiteWhereException {
	throw new SiteWhereException("Not implemented.");
    }

    /*
     * @see
     * com.sitewhere.spi.user.IUserManagement#listGrantedAuthorities(com.sitewhere.
     * spi.user.IGrantedAuthoritySearchCriteria)
     */
    @Override
    public ISearchResults<IGrantedAuthority> listGrantedAuthorities(IGrantedAuthoritySearchCriteria criteria)
	    throws SiteWhereException {
	MongoCollection<Document> auths = getMongoClient().getAuthoritiesCollection();
	Document dbCriteria = new Document();
	Document sort = new Document(MongoGrantedAuthority.PROP_AUTHORITY, 1);
	return MongoPersistence.search(IGrantedAuthority.class, auths, dbCriteria, sort, criteria, LOOKUP);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IUserManagement#deleteGrantedAuthority(java.lang.
     * String)
     */
    @Override
    public void deleteGrantedAuthority(String authority) throws SiteWhereException {
	throw new SiteWhereException("Not implemented.");
    }

    /**
     * Get the {@link DBObject} for a User given username. Throw an exception if not
     * found.
     * 
     * @param username
     * @return
     * @throws SiteWhereException
     */
    protected Document assertUser(String username) throws SiteWhereException {
	Document match = getUserDocumentByUsername(username);
	if (match == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidUsername, ErrorLevel.ERROR);
	}
	return match;
    }

    /**
     * Get the {@link Document} for a User given unique username.
     * 
     * @param username
     * @return
     * @throws SiteWhereException
     */
    protected Document getUserDocumentByUsername(String username) throws SiteWhereException {
	try {
	    MongoCollection<Document> users = getMongoClient().getUsersCollection();
	    Document query = new Document(MongoUser.PROP_USERNAME, username);
	    return users.find(query).first();
	} catch (MongoTimeoutException e) {
	    throw new SiteWhereException("Connection to MongoDB lost.", e);
	}
    }

    /**
     * Get the {@link Document} for a GrantedAuthority given name. Throw an
     * exception if not found.
     * 
     * @param name
     * @return
     * @throws SiteWhereException
     */
    protected Document assertGrantedAuthority(String name) throws SiteWhereException {
	Document match = getGrantedAuthorityDocumentByName(name);
	if (match == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidAuthority, ErrorLevel.ERROR);
	}
	return match;
    }

    /**
     * Get the {@link Document} for a GrantedAuthority given unique name.
     * 
     * @param name
     * @return
     * @throws SiteWhereException
     */
    protected Document getGrantedAuthorityDocumentByName(String name) throws SiteWhereException {
	try {
	    MongoCollection<Document> auths = getMongoClient().getAuthoritiesCollection();
	    Document query = new Document(MongoGrantedAuthority.PROP_AUTHORITY, name);
	    return auths.find(query).first();
	} catch (MongoTimeoutException e) {
	    throw new SiteWhereException("Connection to MongoDB lost.", e);
	}
    }

    public IUserManagementMongoClient getMongoClient() {
	return mongoClient;
    }

    public void setMongoClient(IUserManagementMongoClient mongoClient) {
	this.mongoClient = mongoClient;
    }

    protected ITenantManagementApiChannel<?> getTenantManagementApiDemux() {
	return ((IUserManagementMicroservice<?>) getMicroservice()).getTenantManagementApiDemux().getApiChannel();
    }
}