/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.devicestate.configuration;

import com.sitewhere.configuration.ConfigurationRole;
import com.sitewhere.configuration.model.CommonDatastoreRoleKeys;
import com.sitewhere.spi.microservice.configuration.model.IConfigurationRole;
import com.sitewhere.spi.microservice.configuration.model.IConfigurationRoleProvider;
import com.sitewhere.spi.microservice.configuration.model.IRoleKey;

/**
 * Configuration roles available for presence management microservice.
 * 
 * @author Derek
 */
public enum DeviceStateRoles implements IConfigurationRoleProvider {

    /** Root device state role. */
    DeviceState(ConfigurationRole.build(
	    DeviceStateRoleKeys.DeviceState, "Device State", false, false, false, new IRoleKey[] {
		    CommonDatastoreRoleKeys.DeviceStateManagementDatastore, DeviceStateRoleKeys.PresenceManager },
	    new IRoleKey[0], true)),

    /** Presence manager. */
    PresenceManager(
	    ConfigurationRole.build(DeviceStateRoleKeys.PresenceManager, "Presence Manager", true, false, false));

    private ConfigurationRole role;

    private DeviceStateRoles(ConfigurationRole role) {
	this.role = role;
    }

    /*
     * @see
     * com.sitewhere.spi.microservice.configuration.model.IConfigurationRoleProvider
     * #getRole()
     */
    @Override
    public IConfigurationRole getRole() {
	return role;
    }
}