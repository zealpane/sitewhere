/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.user.persistence.hbase;

/**
 * Indicates type of data is stored in a row in the user table.
 * 
 * @author Derek
 */
public enum UserRecordType {

    /** User record */
    User((byte) 0x01),

    /** Granted authority record */
    GrantedAuthority((byte) 0x02),

    /** Tenant record */
    Tenant((byte) 0x03),

    /** Tenant group record */
    TenantGroup((byte) 0x04),

    /** Marker for scans */
    End((byte) 0x05);

    /** Type indicator */
    private byte type;

    /**
     * Create a unique id type with the given byte value.
     * 
     * @param value
     */
    private UserRecordType(byte type) {
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