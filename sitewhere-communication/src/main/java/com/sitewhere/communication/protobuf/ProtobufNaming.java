/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.communication.protobuf;

import com.sitewhere.spi.device.IDeviceType;
import com.sitewhere.spi.device.command.IDeviceCommand;

/**
 * Common methods for naming scheme used in internal Google Protocol Buffer
 * representation.
 * 
 * @author Derek
 */
public class ProtobufNaming {

    /** Name of enum for message types */
    public static final String COMMAND_TYPES_ENUM = "Command";

    /** Name of command field in header */
    public static final String HEADER_COMMAND_FIELD_NAME = "command";

    /** Name of originator field in header */
    public static final String HEADER_ORIGINATOR_FIELD_NAME = "originator";

    /** Name of nested path field in header */
    public static final String HEADER_NESTED_PATH_FIELD_NAME = "nestedPath";

    /** Name of nested type field in header */
    public static final String HEADER_NESTED_TYPE_FIELD_NAME = "nestedType";

    /** Name associated with uuid message */
    public static final String UUID_MSG_NAME = "Uuid";

    /** Name associated with header message */
    public static final String HEADER_MSG_NAME = "_Header";

    /**
     * Get the device type identifier.
     * 
     * @param deviceType
     * @return
     */
    protected static String getDeviceTypeIdentifier(IDeviceType deviceType) {
	return "Spec_" + deviceType.getToken();
    }

    /**
     * Get the command enum entry name.
     * 
     * @param command
     * @return
     */
    protected static String getCommandEnumName(IDeviceCommand command) {
	return command.getName().toUpperCase();
    }
}