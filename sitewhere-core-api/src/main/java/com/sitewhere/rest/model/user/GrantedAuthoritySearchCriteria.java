/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.user;

import com.sitewhere.rest.model.search.SearchCriteria;
import com.sitewhere.spi.user.IGrantedAuthoritySearchCriteria;

/**
 * Implementation of IGrantedAuthoritySearchCriteria.
 * 
 * @author Derek
 */
public class GrantedAuthoritySearchCriteria extends SearchCriteria implements IGrantedAuthoritySearchCriteria {

    /** Serial version UID */
    private static final long serialVersionUID = 3724218780869528660L;

    public GrantedAuthoritySearchCriteria() {
	super();
    }

    public GrantedAuthoritySearchCriteria(int pageNumber, int pageSize) {
	super(pageNumber, pageSize);
    }
}