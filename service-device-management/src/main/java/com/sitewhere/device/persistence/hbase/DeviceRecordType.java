/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.device.persistence.hbase;

/**
 * Indicates device record type.
 * 
 * @author Derek
 */
public enum DeviceRecordType {

    /** Device record */
    Device((byte) 0x00),

    /** Device specification record */
    DeviceSpecification((byte) 0x01),

    /** Device group record */
    DeviceGroup((byte) 0x02),

    /** Batch operation record */
    BatchOperation((byte) 0x03);

    /** Type indicator */
    private byte type;

    /**
     * Create a unique id type with the given byte value.
     * 
     * @param type
     */
    private DeviceRecordType(byte type) {
	this.type = type;
    }

    /**
     * Get the record type indicator.
     * 
     * @return
     */
    public byte getType() {
	return type;
    }
}