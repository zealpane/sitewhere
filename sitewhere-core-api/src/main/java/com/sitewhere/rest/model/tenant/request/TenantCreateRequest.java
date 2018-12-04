/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.rest.model.tenant.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sitewhere.rest.model.common.request.BrandedEntityCreateRequest;
import com.sitewhere.spi.tenant.ITenant;
import com.sitewhere.spi.tenant.request.ITenantCreateRequest;

/**
 * Default implementation of {@link ITenantCreateRequest} for use in REST
 * services.
 * 
 * @author Derek
 */
@JsonInclude(Include.NON_NULL)
public class TenantCreateRequest extends BrandedEntityCreateRequest implements ITenantCreateRequest {

    /** Serial version UID */
    private static final long serialVersionUID = -5706275554835627264L;

    /** Tenant name */
    private String name;

    /** Device authentication token */
    private String authenticationToken;

    /** List of users authorized for access */
    private List<String> authorizedUserIds;

    /** Tenant template id */
    private String tenantTemplateId;

    /** Dataset template id */
    private String datasetTemplateId;

    /*
     * @see com.sitewhere.spi.tenant.request.ITenantCreateRequest#getName()
     */
    @Override
    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    /*
     * @see
     * com.sitewhere.spi.tenant.request.ITenantCreateRequest#getAuthenticationToken(
     * )
     */
    @Override
    public String getAuthenticationToken() {
	return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
	this.authenticationToken = authenticationToken;
    }

    /*
     * @see
     * com.sitewhere.spi.tenant.request.ITenantCreateRequest#getAuthorizedUserIds()
     */
    @Override
    public List<String> getAuthorizedUserIds() {
	return authorizedUserIds;
    }

    public void setAuthorizedUserIds(List<String> authorizedUserIds) {
	this.authorizedUserIds = authorizedUserIds;
    }

    /*
     * @see
     * com.sitewhere.spi.tenant.request.ITenantCreateRequest#getTenantTemplateId()
     */
    @Override
    public String getTenantTemplateId() {
	return tenantTemplateId;
    }

    public void setTenantTemplateId(String tenantTemplateId) {
	this.tenantTemplateId = tenantTemplateId;
    }

    /*
     * @see
     * com.sitewhere.spi.tenant.request.ITenantCreateRequest#getDatasetTemplateId()
     */
    @Override
    public String getDatasetTemplateId() {
	return datasetTemplateId;
    }

    public void setDatasetTemplateId(String datasetTemplateId) {
	this.datasetTemplateId = datasetTemplateId;
    }

    public static class Builder {

	/** Request being built */
	private TenantCreateRequest request = new TenantCreateRequest();

	public Builder(String token, String name, String authenticationToken, String logoUrl, String tenantTemplateId,
		String datasetTemplateId) {
	    request.setToken(token);
	    request.setName(name);
	    request.setAuthenticationToken(authenticationToken);
	    request.setImageUrl(logoUrl);
	    request.setTenantTemplateId(tenantTemplateId);
	    request.setDatasetTemplateId(datasetTemplateId);
	}

	public Builder(ITenant existing) {
	    request.setToken(existing.getToken());
	    request.setName(existing.getName());
	    request.setImageUrl(existing.getImageUrl());
	    request.setAuthenticationToken(existing.getAuthenticationToken());
	    request.setAuthorizedUserIds(existing.getAuthorizedUserIds());
	    request.setTenantTemplateId(existing.getTenantTemplateId());
	    request.setDatasetTemplateId(existing.getDatasetTemplateId());
	    request.setMetadata(existing.getMetadata());
	}

	public Builder withAuthorizedUserId(String userId) {
	    if (request.getAuthorizedUserIds() == null) {
		request.setAuthorizedUserIds(new ArrayList<String>());
	    }
	    request.getAuthorizedUserIds().add(userId);
	    return this;
	}

	public Builder metadata(String name, String value) {
	    if (request.getMetadata() == null) {
		request.setMetadata(new HashMap<String, String>());
	    }
	    request.getMetadata().put(name, value);
	    return this;
	}

	public TenantCreateRequest build() {
	    return request;
	}
    }
}