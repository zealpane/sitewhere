/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.search.configuration;

import com.sitewhere.configuration.model.CommonConnectorProvider;
import com.sitewhere.configuration.model.ConfigurationModelProvider;
import com.sitewhere.configuration.parser.IEventSearchParser;
import com.sitewhere.rest.model.configuration.AttributeNode;
import com.sitewhere.rest.model.configuration.ElementNode;
import com.sitewhere.spi.microservice.configuration.model.AttributeType;
import com.sitewhere.spi.microservice.configuration.model.IConfigurationRoleProvider;

/**
 * Configuration model provider for event search microservice.
 * 
 * @author Derek
 */
public class EventSearchModelProvider extends ConfigurationModelProvider {

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#getDefaultXmlNamespace()
     */
    @Override
    public String getDefaultXmlNamespace() {
	return "http://sitewhere.io/schema/sitewhere/microservice/event-search";
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#getRootRole()
     */
    @Override
    public IConfigurationRoleProvider getRootRole() {
	return EventSearchRoles.EventSearch;
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#initializeElements()
     */
    @Override
    public void initializeElements() {
	addElement(createEventSearch());
	addElement(createSearchProviders());
	addElement(createSolrSearchProvider());
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#initializeRoles()
     */
    @Override
    public void initializeRoles() {
	for (EventSearchRoles role : EventSearchRoles.values()) {
	    getRolesById().put(role.getRole().getKey().getId(), role.getRole());
	}
    }

    /*
     * @see com.sitewhere.configuration.model.ConfigurationModelProvider#
     * initializeDependencies()
     */
    @Override
    public void initializeDependencies() {
	getDependencies().add(new CommonConnectorProvider());
    }

    /**
     * Create the container for asset management configuration.
     * 
     * @return
     */
    protected ElementNode createEventSearch() {
	ElementNode.Builder builder = new ElementNode.Builder("Event Search", IEventSearchParser.ROOT, "search",
		EventSearchRoleKeys.EventSearch, this);
	builder.description("Provides ability to query events via externally provided search interfaces.");
	return builder.build();
    }

    /**
     * Create the search providers configuration.
     * 
     * @return
     */
    protected ElementNode createSearchProviders() {
	ElementNode.Builder builder = new ElementNode.Builder("Search Providers",
		IEventSearchParser.Elements.SearchProviders.getLocalName(), "search",
		EventSearchRoleKeys.SearchProviders, this);
	builder.description("A list of search providers that are available to be searched.");
	return builder.build();
    }

    /**
     * Create element configuration for Solr search provider.
     * 
     * @return
     */
    protected ElementNode createSolrSearchProvider() {
	ElementNode.Builder builder = new ElementNode.Builder("Solr Search Provider",
		IEventSearchParser.SearchProvidersElements.SolrSearchProvider.getLocalName(), "search",
		EventSearchRoleKeys.SolrSearchProvider, this);

	builder.description("Provider that delegates search tasks to a linked Solr instance.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_GENERAL);

	builder.attribute((new AttributeNode.Builder("Id", "id", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).description("Unique id for search provider.")
			.defaultValue("solr").makeIndex().build()));
	builder.attribute((new AttributeNode.Builder("Name", "name", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).description("Name shown for search provider.")
			.defaultValue("Apache Solr").build()));

	return builder.build();
    }
}