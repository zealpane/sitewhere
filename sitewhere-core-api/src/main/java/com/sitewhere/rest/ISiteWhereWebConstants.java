/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest;

/**
 * Interface for constants used in web operations.
 * 
 * @author dadams
 */
public interface ISiteWhereWebConstants {

    /** Header containing JWT for authentication */
    public static final String HEADER_JWT = "X-Sitewhere-JWT";

    /** Header that holds SiteWhere tenant id being accessed */
    public static final String HEADER_TENANT_ID = "X-SiteWhere-Tenant-Id";

    /** Header that holds SiteWhere tenant auth token */
    public static final String HEADER_TENANT_AUTH = "X-SiteWhere-Tenant-Auth";

    /** Header that holds SiteWhere error string on error response */
    public static final String HEADER_SITEWHERE_ERROR = "X-SiteWhere-Error";

    /** Header that holds SiteWhere error code on error response */
    public static final String HEADER_SITEWHERE_ERROR_CODE = "X-SiteWhere-Error-Code";

    /** Request parameter name for passing tenant authentication token */
    @Deprecated
    public static final String REQUEST_TENANT_TOKEN = "tenantAuthToken";
}