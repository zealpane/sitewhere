/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.device.streaming;

import java.util.UUID;

import com.sitewhere.rest.model.common.PersistentEntity;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.streaming.IDeviceStream;

/**
 * Model object for a stream of binary data received from a device.
 * 
 * @author Derek
 */
public class DeviceStream extends PersistentEntity implements IDeviceStream {

    /** Serial version UID */
    private static final long serialVersionUID = -5721420122887571143L;

    /** Parent assignment id */
    private UUID assignmentId;

    /** Alias for stream id */
    private String streamId;

    /** Stream content type */
    private String contentType;

    /*
     * @see com.sitewhere.spi.device.streaming.IDeviceStream#getAssignmentId()
     */
    @Override
    public UUID getAssignmentId() {
	return assignmentId;
    }

    public void setAssignmentId(UUID assignmentId) {
	this.assignmentId = assignmentId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.streaming.IDeviceStream#getStreamId()
     */
    @Override
    public String getStreamId() {
	return streamId;
    }

    public void setStreamId(String streamId) {
	this.streamId = streamId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.streaming.IDeviceStream#getContentType()
     */
    @Override
    public String getContentType() {
	return contentType;
    }

    public void setContentType(String contentType) {
	this.contentType = contentType;
    }

    public static DeviceStream copy(IDeviceStream input) throws SiteWhereException {
	DeviceStream result = new DeviceStream();
	result.setId(input.getId());
	result.setAssignmentId(input.getAssignmentId());
	result.setStreamId(input.getStreamId());
	result.setContentType(input.getContentType());

	PersistentEntity.copy(input, result);
	return result;
    }
}