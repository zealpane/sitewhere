/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.device.persistence.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;

import com.sitewhere.mongodb.MongoConverter;
import com.sitewhere.mongodb.common.MongoPersistentEntity;
import com.sitewhere.rest.model.device.command.CommandParameter;
import com.sitewhere.rest.model.device.command.DeviceCommand;
import com.sitewhere.spi.device.command.ICommandParameter;
import com.sitewhere.spi.device.command.IDeviceCommand;
import com.sitewhere.spi.device.command.ParameterType;

/**
 * Used to load or save device command data to MongoDB.
 * 
 * @author dadams
 */
public class MongoDeviceCommand implements MongoConverter<IDeviceCommand> {

    /** Property for device type id */
    public static final String PROP_DEVICE_TYPE_ID = "dtid";

    /** Property for command namespace */
    public static final String PROP_NAMESPACE = "nmsp";

    /** Property for command name */
    public static final String PROP_NAME = "name";

    /** Property for command description */
    public static final String PROP_DESCRIPTION = "desc";

    /** Property for command parameters list */
    public static final String PROP_PARAMETERS = "parm";

    /** Property for command parameter name */
    public static final String PROP_PARAM_NAME = "pnam";

    /** Property for command parameter type */
    public static final String PROP_PARAM_TYPE = "ptyp";

    /** Property for command parameter required indicator */
    public static final String PROP_PARAM_REQUIRED = "preq";

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.MongoConverter#convert(java.lang.Object)
     */
    @Override
    public Document convert(IDeviceCommand source) {
	return MongoDeviceCommand.toDocument(source);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.MongoConverter#convert(org.bson.Document)
     */
    @Override
    public IDeviceCommand convert(Document source) {
	return MongoDeviceCommand.fromDocument(source);
    }

    /**
     * Copy information from SPI into Mongo {@link Document}.
     * 
     * @param source
     * @param target
     */
    public static void toDocument(IDeviceCommand source, Document target) {
	target.append(PROP_DEVICE_TYPE_ID, source.getDeviceTypeId());
	target.append(PROP_NAMESPACE, source.getNamespace());
	target.append(PROP_NAME, source.getName());
	target.append(PROP_DESCRIPTION, source.getDescription());

	// Create parameters list.
	List<Document> params = new ArrayList<Document>();
	for (ICommandParameter parameter : source.getParameters()) {
	    Document dbparam = new Document();
	    dbparam.append(PROP_PARAM_NAME, parameter.getName());
	    dbparam.append(PROP_PARAM_TYPE, parameter.getType().name());
	    dbparam.append(PROP_PARAM_REQUIRED, parameter.isRequired());
	    params.add(dbparam);
	}
	target.append(PROP_PARAMETERS, params);

	MongoPersistentEntity.toDocument(source, target);
    }

    /**
     * Copy information from Mongo {@link Document} to model object.
     * 
     * @param source
     * @param target
     */
    @SuppressWarnings("unchecked")
    public static void fromDocument(Document source, DeviceCommand target) {
	UUID deviceTypeId = (UUID) source.get(PROP_DEVICE_TYPE_ID);
	String namespace = (String) source.get(PROP_NAMESPACE);
	String name = (String) source.get(PROP_NAME);
	String desc = (String) source.get(PROP_DESCRIPTION);

	target.setDeviceTypeId(deviceTypeId);
	target.setNamespace(namespace);
	target.setName(name);
	target.setDescription(desc);

	List<Document> params = (List<Document>) source.get(PROP_PARAMETERS);
	if (params != null) {
	    for (Document param : params) {
		String pname = (String) param.get(PROP_PARAM_NAME);
		String ptype = (String) param.get(PROP_PARAM_TYPE);
		Boolean prequired = (Boolean) param.get(PROP_PARAM_REQUIRED);
		CommandParameter parameter = new CommandParameter();
		parameter.setName(pname);
		if (ptype != null) {
		    parameter.setType(ParameterType.valueOf(ptype));
		}
		if (prequired != null) {
		    parameter.setRequired(prequired);
		}
		target.getParameters().add(parameter);
	    }
	}

	MongoPersistentEntity.fromDocument(source, target);
    }

    /**
     * Convert SPI object to Mongo DBObject.
     * 
     * @param source
     * @return
     */
    public static Document toDocument(IDeviceCommand source) {
	Document result = new Document();
	MongoDeviceCommand.toDocument(source, result);
	return result;
    }

    /**
     * Convert a DBObject into the SPI equivalent.
     * 
     * @param source
     * @return
     */
    public static DeviceCommand fromDocument(Document source) {
	DeviceCommand result = new DeviceCommand();
	MongoDeviceCommand.fromDocument(source, result);
	return result;
    }
}