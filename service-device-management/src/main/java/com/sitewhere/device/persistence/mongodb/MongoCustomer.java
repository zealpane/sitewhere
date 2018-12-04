/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.device.persistence.mongodb;

import java.util.UUID;

import org.bson.Document;

import com.sitewhere.mongodb.MongoConverter;
import com.sitewhere.mongodb.common.MongoBrandedEntity;
import com.sitewhere.rest.model.customer.Customer;
import com.sitewhere.spi.customer.ICustomer;

/**
 * Used to load or save customer data to MongoDB.
 * 
 * @author dadams
 */
public class MongoCustomer implements MongoConverter<ICustomer> {

    /** Property for customer type id */
    public static final String PROP_CUSTOMER_TYPE_ID = "ctid";

    /** Property for parent customer id */
    public static final String PROP_PARENT_CUSTOMER_ID = "pcid";

    /** Property for name */
    public static final String PROP_NAME = "name";

    /** Property for description */
    public static final String PROP_DESCRIPTION = "desc";

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.dao.mongodb.MongoConverter#convert(java.lang.Object)
     */
    @Override
    public Document convert(ICustomer source) {
	return MongoCustomer.toDocument(source);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.MongoConverter#convert(org.bson.Document)
     */
    @Override
    public ICustomer convert(Document source) {
	return MongoCustomer.fromDocument(source);
    }

    /**
     * Copy information from SPI into Mongo {@link Document}.
     * 
     * @param source
     * @param target
     */
    public static void toDocument(ICustomer source, Document target) {
	target.append(PROP_CUSTOMER_TYPE_ID, source.getCustomerTypeId());
	target.append(PROP_PARENT_CUSTOMER_ID, source.getParentCustomerId());
	target.append(PROP_NAME, source.getName());
	target.append(PROP_DESCRIPTION, source.getDescription());

	MongoBrandedEntity.toDocument(source, target);
    }

    /**
     * Copy information from Mongo {@link Document} to model object.
     * 
     * @param source
     * @param target
     */
    public static void fromDocument(Document source, Customer target) {
	UUID customerTypeId = (UUID) source.get(PROP_CUSTOMER_TYPE_ID);
	UUID parentCustomerId = (UUID) source.get(PROP_PARENT_CUSTOMER_ID);
	String name = (String) source.get(PROP_NAME);
	String description = (String) source.get(PROP_DESCRIPTION);

	target.setCustomerTypeId(customerTypeId);
	target.setParentCustomerId(parentCustomerId);
	target.setName(name);
	target.setDescription(description);

	MongoBrandedEntity.fromDocument(source, target);
    }

    /**
     * Convert SPI object to Mongo {@link Document}.
     * 
     * @param source
     * @return
     */
    public static Document toDocument(ICustomer source) {
	Document result = new Document();
	MongoCustomer.toDocument(source, result);
	return result;
    }

    /**
     * Convert a {@link Document} into the SPI equivalent.
     * 
     * @param source
     * @return
     */
    public static Customer fromDocument(Document source) {
	Customer result = new Customer();
	MongoCustomer.fromDocument(source, result);
	return result;
    }
}
