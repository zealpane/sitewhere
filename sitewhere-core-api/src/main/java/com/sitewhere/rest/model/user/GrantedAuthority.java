/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sitewhere.spi.user.IGrantedAuthority;

/**
 * Model object for a granted authority.
 * 
 * @author Derek Adams
 */
@JsonInclude(Include.NON_NULL)
public class GrantedAuthority implements IGrantedAuthority {

    /** Serial version UID */
    private static final long serialVersionUID = 872647505610555428L;

    /** Authority */
    private String authority;

    /** Description */
    private String description;

    /** Parent authority */
    private String parent;

    /** Indicates whether authority is a group */
    private boolean group;

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IGrantedAuthority#getAuthority()
     */
    public String getAuthority() {
	return authority;
    }

    public void setAuthority(String authority) {
	this.authority = authority;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IGrantedAuthority#getDescription()
     */
    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IGrantedAuthority#getParent()
     */
    public String getParent() {
	return parent;
    }

    public void setParent(String parent) {
	this.parent = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.user.IGrantedAuthority#isGroup()
     */
    public boolean isGroup() {
	return group;
    }

    public void setGroup(boolean group) {
	this.group = group;
    }

    /**
     * Copy contents from the SPI class.
     * 
     * @param input
     * @return
     */
    public static GrantedAuthority copy(IGrantedAuthority input) {
	GrantedAuthority result = new GrantedAuthority();
	result.setAuthority(input.getAuthority());
	result.setDescription(input.getDescription());
	result.setParent(input.getParent());
	result.setGroup(input.isGroup());
	return result;
    }
}