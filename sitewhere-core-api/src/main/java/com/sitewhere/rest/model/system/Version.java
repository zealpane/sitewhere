/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.system;

import com.sitewhere.spi.system.IVersion;

/**
 * Model object used to read version information from REST call.
 * 
 * @author Derek
 */
public class Version implements IVersion {

    /** Serial version UID */
    private static final long serialVersionUID = -1056887925751094237L;

    /** Full edition */
    private String edition;

    /** Edition identifier */
    private String editionIdentifier;

    /** Version identifier */
    private String versionIdentifier;

    /** Build timestamp */
    private String buildTimestamp;

    /** Server implementation class */
    private Class<?> serverClass;

    public Version() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.system.IVersion#getEdition()
     */
    public String getEdition() {
	return edition;
    }

    public void setEdition(String edition) {
	this.edition = edition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.system.IVersion#getEditionIdentifier()
     */
    public String getEditionIdentifier() {
	return editionIdentifier;
    }

    public void setEditionIdentifier(String editionIdentifier) {
	this.editionIdentifier = editionIdentifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.system.IVersion#getVersionIdentifier()
     */
    public String getVersionIdentifier() {
	return versionIdentifier;
    }

    public void setVersionIdentifier(String versionIdentifier) {
	this.versionIdentifier = versionIdentifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.system.IVersion#getBuildTimestamp()
     */
    public String getBuildTimestamp() {
	return buildTimestamp;
    }

    public void setBuildTimestamp(String buildTimestamp) {
	this.buildTimestamp = buildTimestamp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.system.IVersion#getServerClass()
     */
    public Class<?> getServerClass() {
	return serverClass;
    }

    public void setServerClass(Class<?> serverClass) {
	this.serverClass = serverClass;
    }
}