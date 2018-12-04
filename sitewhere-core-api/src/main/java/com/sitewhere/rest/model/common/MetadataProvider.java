/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.common.IMetadataProvider;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;

/**
 * Holds arbitrary metadata associated with an entity.
 * 
 * @author dadams
 */
public class MetadataProvider implements IMetadataProvider, Serializable {

    /** Serialization version identifier */
    private static final long serialVersionUID = -7708181397230364294L;

    /** Map of metadata entries */
    private Map<String, String> entries = new HashMap<String, String>();

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.IMetadataProvider#getMetadata()
     */
    public Map<String, String> getMetadata() {
	return entries;
    }

    public void setMetadata(Map<String, String> entries) {
	this.entries = entries;
    }

    /**
     * Copy contents of one metadata provider to another.
     * 
     * @param source
     * @param target
     */
    public static void copy(IMetadataProvider source, MetadataProvider target) throws SiteWhereException {
	if (source != null) {
	    copy(source.getMetadata(), target);
	}
    }

    /**
     * Copy values from a map into a {@link MetadataProvider}.
     * 
     * @param source
     * @param target
     * @throws SiteWhereException
     */
    public static void copy(Map<String, String> source, MetadataProvider target) throws SiteWhereException {
	if (source != null) {
	    for (String key : source.keySet()) {
		if (!key.matches("^[\\w-]+$")) {
		    throw new SiteWhereSystemException(ErrorCode.InvalidMetadataFieldName, ErrorLevel.ERROR);
		}
		target.getMetadata().put(key, source.get(key));
	    }
	}
    }
}