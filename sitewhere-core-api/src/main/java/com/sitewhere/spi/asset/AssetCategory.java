/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.asset;

/**
 * Indicates the general type of an asset.
 * 
 * @author dadams
 */
public enum AssetCategory {

    /** Device asset (hardware used for monitoring) */
    Device,

    /** Asset represents a person */
    Person,

    /** Asset represents a piece of hardware */
    Hardware;
}