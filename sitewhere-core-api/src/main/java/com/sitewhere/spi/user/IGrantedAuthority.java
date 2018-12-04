/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.user;

import java.io.Serializable;

/**
 * Interface for granted authority information.
 * 
 * @author Derek
 */
public interface IGrantedAuthority extends Serializable {

    /**
     * Get the authority name.
     * 
     * @return
     */
    public String getAuthority();

    /**
     * Get the description.
     * 
     * @return
     */
    public String getDescription();

    /**
     * Get parent authority.
     * 
     * @return
     */
    public String getParent();

    /**
     * Indicates if the authority is a group.
     * 
     * @return
     */
    public boolean isGroup();
}