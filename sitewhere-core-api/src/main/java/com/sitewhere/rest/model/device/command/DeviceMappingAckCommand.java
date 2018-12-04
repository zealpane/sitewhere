/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.device.command;

import com.sitewhere.spi.device.command.DeviceMappingResult;
import com.sitewhere.spi.device.command.IDeviceMappingAckCommand;
import com.sitewhere.spi.device.command.SystemCommandType;

/**
 * Default implementation of {@link IDeviceMappingAckCommand}.
 * 
 * @author Derek
 */
public class DeviceMappingAckCommand extends SystemCommand implements IDeviceMappingAckCommand {

    /** Serial version UID */
    private static final long serialVersionUID = 459571414041623952L;

    public DeviceMappingAckCommand() {
	super(SystemCommandType.DeviceMappingAck);
    }

    /** Device mapping result */
    private DeviceMappingResult result;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.spi.device.command.IDeviceMappingAckCommand#getResult()
     */
    public DeviceMappingResult getResult() {
	return result;
    }

    public void setResult(DeviceMappingResult result) {
	this.result = result;
    }
}