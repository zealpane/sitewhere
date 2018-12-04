/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.spi.tenant;

import com.sitewhere.spi.SiteWhereException;

/**
 * Thrown when attempting to access a tenant that is defined in the system, but
 * is not started or is otherwise unavailable.
 * 
 * @author Derek
 */
public class TenantNotAvailableException extends SiteWhereException {

    /** Serial version UID */
    private static final long serialVersionUID = -9036156272921131169L;

    public TenantNotAvailableException() {
    }

    public TenantNotAvailableException(String message, Throwable cause) {
	super(message, cause);
    }

    public TenantNotAvailableException(String message) {
	super(message);
    }

    public TenantNotAvailableException(Throwable cause) {
	super(cause);
    }
}