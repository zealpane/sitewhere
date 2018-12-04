/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MarshalUtils {

    /** Singleton object mapper for JSON marshaling */
    public static ObjectMapper MAPPER = new ObjectMapper();

    /** Singleton mapper with pretty print turned on */
    public static ObjectMapper PRETTY_MAPPER = new ObjectMapper();

    // Enable pretty printing on the mapper.
    static {
	PRETTY_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Marshal an object to a byte array.
     * 
     * @param object
     * @return
     */
    public static byte[] marshalJson(Object object) {
	try {
	    return MAPPER.writeValueAsBytes(object);
	} catch (JsonProcessingException e) {
	    throw new RuntimeException("Could not marshal object as JSON: " + object.getClass().getName(), e);
	}
    }

    /**
     * Marshal an object to a JSON string.
     * 
     * @param object
     * @return
     */
    public static String marshalJsonAsString(Object object) {
	try {
	    return (object != null) ? MAPPER.writeValueAsString(object) : "NULL";
	} catch (JsonProcessingException e) {
	    throw new RuntimeException("Could not marshal object as JSON: " + object.getClass().getName(), e);
	}
    }

    /**
     * Marshal an object to a formatted JSON string.
     * 
     * @param object
     * @return
     */
    public static String marshalJsonAsPrettyString(Object object) {
	try {
	    return (object != null) ? PRETTY_MAPPER.writeValueAsString(object) : "NULL";
	} catch (JsonProcessingException e) {
	    throw new RuntimeException("Could not marshal object as JSON: " + object.getClass().getName(), e);
	}
    }

    /**
     * Unmarshal a JSON string to an object.
     * 
     * @param json
     * @param type
     * @return
     */
    public static <T> T unmarshalJson(byte[] json, Class<T> type) {
	try {
	    return MAPPER.readValue(json, type);
	} catch (Throwable e) {
	    throw new RuntimeException("Unable to parse JSON.", e);
	}
    }
}
