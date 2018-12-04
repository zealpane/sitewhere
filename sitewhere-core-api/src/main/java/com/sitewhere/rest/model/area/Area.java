/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.area;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sitewhere.rest.model.common.BrandedEntity;
import com.sitewhere.rest.model.common.Location;
import com.sitewhere.spi.area.IArea;

/**
 * Model object for area information.
 * 
 * @author dadams
 */
@JsonInclude(Include.NON_NULL)
public class Area extends BrandedEntity implements IArea {

    /** Serialization version identifier */
    private static final long serialVersionUID = -566693689485715028L;

    /** Area type id */
    private UUID areaTypeId;

    /** Parent area id */
    private UUID parentAreaId;

    /** Area name */
    private String name;

    /** Area description */
    private String description;

    /** Image URL */
    private String imageUrl;

    /** Area boundary coordinates */
    private List<Location> bounds = new ArrayList<Location>();

    /*
     * @see com.sitewhere.spi.area.IArea#getAreaTypeId()
     */
    @Override
    public UUID getAreaTypeId() {
	return areaTypeId;
    }

    public void setAreaTypeId(UUID areaTypeId) {
	this.areaTypeId = areaTypeId;
    }

    /*
     * @see com.sitewhere.spi.area.IArea#getParentAreaId()
     */
    @Override
    public UUID getParentAreaId() {
	return parentAreaId;
    }

    public void setParentAreaId(UUID parentAreaId) {
	this.parentAreaId = parentAreaId;
    }

    /*
     * @see com.sitewhere.spi.area.IArea#getName()
     */
    @Override
    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    /*
     * @see com.sitewhere.spi.area.IArea#getDescription()
     */
    @Override
    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    /*
     * @see com.sitewhere.spi.area.IArea#getImageUrl()
     */
    @Override
    public String getImageUrl() {
	return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
	this.imageUrl = imageUrl;
    }

    /*
     * @see com.sitewhere.spi.area.IBoundedEntity#getBounds()
     */
    @Override
    public List<Location> getBounds() {
	return bounds;
    }

    public void setBounds(List<Location> bounds) {
	this.bounds = bounds;
    }
}