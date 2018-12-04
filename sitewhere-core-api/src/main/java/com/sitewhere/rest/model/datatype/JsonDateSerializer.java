/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.datatype;

import java.io.IOException;
import java.util.Date;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Used for serializing Calendar objects for JSON output.
 * 
 * @author dadams
 */
public class JsonDateSerializer extends JsonSerializer<Date> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object,
     * com.fasterxml.jackson.core.JsonGenerator,
     * com.fasterxml.jackson.databind.SerializerProvider)
     */
    public void serialize(Date value, JsonGenerator gen, SerializerProvider arg2)
	    throws IOException, JsonProcessingException {
	gen.writeString(JsonDateSerializer.serialize(value));
    }

    /**
     * Serialize a date in ISO 8601 format.
     * 
     * @param value
     * @return
     */
    public static final String serialize(Date value) {
	DateTimeFormatter formatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
	return formatter.print(value.getTime());
    }
}