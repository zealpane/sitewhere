/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.device.command;

import java.io.Serializable;

/**
 * Sends a chunk of device stream data to a device.
 * 
 * @author Derek
 */
public interface ISendDeviceStreamDataCommand extends ISystemCommand, Serializable {

    /**
     * Hardware id of device to receive data.
     * 
     * @return
     */
    public String getHardwareId();

    /**
     * Get id of stream data belongs to.
     * 
     * @return
     */
    public String getStreamId();

    /**
     * Get sequence number of data chunk.
     * 
     * @return
     */
    public long getSequenceNumber();

    /**
     * Get data.
     * 
     * @return
     */
    public byte[] getData();
}