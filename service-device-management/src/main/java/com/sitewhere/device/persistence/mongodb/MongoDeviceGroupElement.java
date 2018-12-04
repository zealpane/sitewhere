/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.device.persistence.mongodb;

import java.util.List;
import java.util.UUID;

import org.bson.Document;

import com.sitewhere.mongodb.MongoConverter;
import com.sitewhere.rest.model.device.group.DeviceGroupElement;
import com.sitewhere.spi.device.group.IDeviceGroupElement;

/**
 * Used to load or save device group element data to MongoDB.
 * 
 * @author Derek
 */
public class MongoDeviceGroupElement implements MongoConverter<IDeviceGroupElement> {

    /** Property for id */
    public static final String PROP_ID = "_id";

    /** Property for element group id */
    public static final String PROP_GROUP_ID = "grid";

    /** Property for element device id */
    public static final String PROP_DEVICE_ID = "dvid";

    /** Property for element nested group id */
    public static final String PROP_NESTED_GROUP_ID = "ngid";

    /** Property for list of roles */
    public static final String PROP_ROLES = "role";

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.MongoConverter#convert(java.lang.Object)
     */
    @Override
    public Document convert(IDeviceGroupElement source) {
	return MongoDeviceGroupElement.toDocument(source);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.MongoConverter#convert(org.bson.Document)
     */
    @Override
    public IDeviceGroupElement convert(Document source) {
	return MongoDeviceGroupElement.fromDocument(source);
    }

    /**
     * Copy information from SPI into Mongo {@link Document}.
     * 
     * @param source
     * @param target
     */
    public static void toDocument(IDeviceGroupElement source, Document target) {
	target.append(PROP_ID, source.getId());
	target.append(PROP_GROUP_ID, source.getGroupId());
	target.append(PROP_DEVICE_ID, source.getDeviceId());
	target.append(PROP_NESTED_GROUP_ID, source.getNestedGroupId());
	target.append(PROP_ROLES, source.getRoles());
    }

    /**
     * Copy information from Mongo {@link Document} to model object.
     * 
     * @param source
     * @param target
     */
    @SuppressWarnings("unchecked")
    public static void fromDocument(Document source, DeviceGroupElement target) {
	UUID id = (UUID) source.get(PROP_ID);
	UUID groupId = (UUID) source.get(PROP_GROUP_ID);
	UUID deviceId = (UUID) source.get(PROP_DEVICE_ID);
	UUID nestedGroupId = (UUID) source.get(PROP_NESTED_GROUP_ID);
	List<String> roles = (List<String>) source.get(PROP_ROLES);

	target.setId(id);
	target.setGroupId(groupId);
	target.setDeviceId(deviceId);
	target.setNestedGroupId(nestedGroupId);
	target.setRoles(roles);
    }

    /**
     * Convert SPI object to Mongo {@link Document}.
     * 
     * @param source
     * @return
     */
    public static Document toDocument(IDeviceGroupElement source) {
	Document result = new Document();
	MongoDeviceGroupElement.toDocument(source, result);
	return result;
    }

    /**
     * Convert a {@link Document} into the SPI equivalent.
     * 
     * @param source
     * @return
     */
    public static DeviceGroupElement fromDocument(Document source) {
	DeviceGroupElement result = new DeviceGroupElement();
	MongoDeviceGroupElement.fromDocument(source, result);
	return result;
    }
}