/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.batch.spring;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.sitewhere.batch.persistence.mongodb.BatchManagementMongoClient;
import com.sitewhere.batch.persistence.mongodb.MongoBatchManagement;
import com.sitewhere.configuration.datastore.DatastoreConfigurationChoice;
import com.sitewhere.configuration.datastore.DatastoreConfigurationParser;
import com.sitewhere.configuration.parser.IBatchOperationsParser.Elements;
import com.sitewhere.spi.microservice.spring.BatchManagementBeans;

/**
 * Parses elements related to batch operations.
 * 
 * @author Derek
 */
public class BatchOperationsParser extends AbstractBeanDefinitionParser {

    /** Static logger instance */
    @SuppressWarnings("unused")
    private static Log LOGGER = LogFactory.getLog(BatchOperationsParser.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.xml.AbstractBeanDefinitionParser#
     * parseInternal (org.w3c.dom.Element,
     * org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext context) {
	List<Element> dsChildren = DomUtils.getChildElements(element);
	for (Element child : dsChildren) {
	    Elements type = Elements.getByLocalName(child.getLocalName());
	    if (type == null) {
		throw new RuntimeException("Unknown batch operations element: " + child.getLocalName());
	    }
	    switch (type) {
	    case DeviceManagementDatastore: {
		parseDeviceManagementDatastore(child, context);
		break;
	    }
	    case BatchOperationManager: {
		break;
	    }
	    }
	}
	return null;
    }

    /**
     * Parse device management datastore element.
     * 
     * @param element
     * @param context
     */
    protected void parseDeviceManagementDatastore(Element element, ParserContext context) {
	DatastoreConfigurationChoice config = DatastoreConfigurationParser.parseDeviceManagementDatastoreChoice(element,
		context);
	switch (config.getType()) {
	case MongoDB: {
	    BeanDefinitionBuilder client = BeanDefinitionBuilder.rootBeanDefinition(BatchManagementMongoClient.class);
	    client.addConstructorArgValue(config.getConfiguration());
	    context.getRegistry().registerBeanDefinition(BatchManagementBeans.BEAN_MONGODB_CLIENT,
		    client.getBeanDefinition());
	    break;
	}
	case MongoDBReference: {
	    BeanDefinitionBuilder client = BeanDefinitionBuilder.rootBeanDefinition(BatchManagementMongoClient.class);
	    client.addConstructorArgReference((String) config.getConfiguration());
	    context.getRegistry().registerBeanDefinition(BatchManagementBeans.BEAN_MONGODB_CLIENT,
		    client.getBeanDefinition());
	    break;
	}
	default: {
	    throw new RuntimeException("Invalid datastore configured: " + config.getType());
	}
	}

	// Build batch management implementation.
	BeanDefinitionBuilder management = BeanDefinitionBuilder.rootBeanDefinition(MongoBatchManagement.class);
	management.addPropertyReference("mongoClient", BatchManagementBeans.BEAN_MONGODB_CLIENT);

	context.getRegistry().registerBeanDefinition(BatchManagementBeans.BEAN_BATCH_MANAGEMENT,
		management.getBeanDefinition());
    }
}