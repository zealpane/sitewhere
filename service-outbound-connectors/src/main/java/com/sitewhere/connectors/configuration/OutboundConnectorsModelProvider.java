/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.connectors.configuration;

import com.sitewhere.configuration.CommonConnectorModel;
import com.sitewhere.configuration.model.CommonConnectorProvider;
import com.sitewhere.configuration.model.ConfigurationModelProvider;
import com.sitewhere.configuration.parser.IOutboundConnectorsParser;
import com.sitewhere.rest.model.configuration.AttributeNode;
import com.sitewhere.rest.model.configuration.ElementNode;
import com.sitewhere.spi.microservice.configuration.model.AttributeType;
import com.sitewhere.spi.microservice.configuration.model.IAttributeGroup;
import com.sitewhere.spi.microservice.configuration.model.IConfigurationRoleProvider;

/**
 * Configuration model provider for outbound connectors microservice.
 * 
 * @author Derek
 */
public class OutboundConnectorsModelProvider extends ConfigurationModelProvider {

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#getDefaultXmlNamespace()
     */
    @Override
    public String getDefaultXmlNamespace() {
	return "http://sitewhere.io/schema/sitewhere/microservice/outbound-connectors";
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#getRootRole()
     */
    @Override
    public IConfigurationRoleProvider getRootRole() {
	return OutboundConnectorsRoles.OutboundConnectors;
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#initializeElements()
     */
    @Override
    public void initializeElements() {
	addElement(createOutboundConnectorsElement());

	// Outbound connectors.
	addElement(createSolrConnectorElement());
	addElement(createAzureEventHubConnectorElement());
	addElement(createAmazonSqsConnectorElement());
	addElement(createInitialStateConnectorElement());
	addElement(createDweetConnectorElement());
	addElement(createGroovyConnectorElement());

	// MQTT connector elements.
	addElement(createGroovyRouteBuilderElement());
	addElement(createMqttConnectorElement());
	addElement(createRabbitMqConnectorElement());

	// Outbound connector filters.
	addElement(createFilterCriteriaElement());
	addElement(createAreaFilterElement());
	addElement(createSpecificationFilterElement());
	addElement(createGroovyFilterElement());
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#initializeRoles()
     */
    @Override
    public void initializeRoles() {
	for (OutboundConnectorsRoles role : OutboundConnectorsRoles.values()) {
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
     * Create outbound processing element.
     * 
     * @return
     */
    protected ElementNode createOutboundConnectorsElement() {
	ElementNode.Builder builder = new ElementNode.Builder(
		OutboundConnectorsRoles.OutboundConnectors.getRole().getName(), IOutboundConnectorsParser.ROOT,
		"sign-out-alt", OutboundConnectorsRoleKeys.OutboundConnectors, this);

	builder.description("Manages a list of connectors used to forward events to external systems.");

	return builder.build();
    }

    /**
     * Create an MQTT connector.
     * 
     * @return
     */
    protected ElementNode createMqttConnectorElement() {
	ElementNode.Builder builder = new ElementNode.Builder("MQTT Connector",
		IOutboundConnectorsParser.Elements.MqttConnector.getLocalName(), "sign-out-alt",
		OutboundConnectorsRoleKeys.MqttConnector, this);
	builder.description("Allows events to be forwarded to any number of MQTT topics based on configuration "
		+ "of filters and (optionally) a route builder. If no route builder is specified, the MQTT topic "
		+ "field determines where events are delivered.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_AUTHENTICATION);

	addCommonConnectorAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);
	CommonConnectorModel.addMqttCommonAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY,
		ConfigurationModelProvider.ATTR_GROUP_AUTHENTICATION);
	builder.attribute((new AttributeNode.Builder("MQTT topic", "topic", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY)
			.description("MQTT topic used if no route builder is specified.").build()));
	return builder.build();
    }

    /**
     * Create a RabbitMQ connector.
     * 
     * @return
     */
    protected ElementNode createRabbitMqConnectorElement() {
	ElementNode.Builder builder = new ElementNode.Builder("RabbitMQ Connector",
		IOutboundConnectorsParser.Elements.RabbitMqConnector.getLocalName(), "sign-out-alt",
		OutboundConnectorsRoleKeys.RabbitMqConnector, this);
	builder.description("Allows events to be forwarded to any number of RabbitMQ exchanges based on configuration "
		+ "of filters and (optionally) a route builder. If no route builder is specified, the exchange "
		+ "field determines where events are delivered.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);

	addCommonConnectorAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);
	builder.attribute((new AttributeNode.Builder("Connection URI", "connectionUri", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY).defaultValue("amqp://localhost")
			.description("URI that provides information about the RabbitMQ instance to connect to.")
			.build()));
	builder.attribute((new AttributeNode.Builder("Topic", "topic", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY).defaultValue("sitewhere.output")
			.description("Topic used if no route builder is specified.").build()));
	return builder.build();
    }

    /**
     * Create a Solr connector.
     * 
     * @return
     */
    protected ElementNode createSolrConnectorElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Apache Solr Connector",
		IOutboundConnectorsParser.Elements.SolrConnector.getLocalName(), "sign-out-alt",
		OutboundConnectorsRoleKeys.SolrConnector, this);
	builder.description("Forwards outbound events to Apache Solr for indexing in the search engine.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);

	addCommonConnectorAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);
	return builder.build();
    }

    /**
     * Create a Azure event hub connector.
     * 
     * @return
     */
    protected ElementNode createAzureEventHubConnectorElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Azure EventHub Connector",
		IOutboundConnectorsParser.Elements.AzureEventHubConnector.getLocalName(), "cloud",
		OutboundConnectorsRoleKeys.FilteredConnector, this);
	builder.description("Forwards outbound events to a Microsoft Azure EventHub for further processing.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);

	addCommonConnectorAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);
	builder.attribute((new AttributeNode.Builder("SAS Name", "sasName", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY)
			.description("Sets the identity used for SAS authentication.").makeRequired().build()));
	builder.attribute((new AttributeNode.Builder("SAS Key", "sasKey", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY)
			.description("Sets the key used for SAS authentication.").makeRequired().build()));
	builder.attribute((new AttributeNode.Builder("Service bus name", "serviceBusName", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY)
			.description("Set the service bus to connect to (e.g. xxx.servicebus.windows.net).")
			.makeRequired().build()));
	builder.attribute((new AttributeNode.Builder("Event hub name", "eventHubName", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY).description("Name of EventHub to connect to.")
			.makeRequired().build()));
	return builder.build();
    }

    /**
     * Create an Amazon SQS queue connector.
     * 
     * @return
     */
    protected ElementNode createAmazonSqsConnectorElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Amazon SQS Connector",
		IOutboundConnectorsParser.Elements.AmazonSqsConnector.getLocalName(), "cloud",
		OutboundConnectorsRoleKeys.FilteredConnector, this);
	builder.description("Forwards outbound events to an Amazon SQS queue for further processing.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);

	addCommonConnectorAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);
	builder.attribute((new AttributeNode.Builder("Access key", "accessKey", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY)
			.description("Amazon AWS access key for account owning SQS queue.").makeRequired().build()));
	builder.attribute((new AttributeNode.Builder("Secret key", "secretKey", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY)
			.description("Amazon AWS secret key for account owning SQS queue.").makeRequired().build()));
	builder.attribute((new AttributeNode.Builder("SQS queue URL", "queueUrl", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY).description("Unique URL for SQS queue.")
			.makeRequired().build()));
	return builder.build();
    }

    /**
     * Create a InitialState connector.
     * 
     * @return
     */
    protected ElementNode createInitialStateConnectorElement() {
	ElementNode.Builder builder = new ElementNode.Builder("InitialState Connector",
		IOutboundConnectorsParser.Elements.InitialStateConnector.getLocalName(), "cloud",
		OutboundConnectorsRoleKeys.FilteredConnector, this);
	builder.description("Forwards outbound events to InitialState.com for advanced visualization.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);

	addCommonConnectorAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);
	builder.attribute((new AttributeNode.Builder("Streaming access key", "streamingAccessKey", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY).description(
			"Access key obtained from the InitialState.com website that specifies the account that the events will be associated with.")
			.makeRequired().build()));
	return builder.build();
    }

    /**
     * Create a Dweet.io connector.
     * 
     * @return
     */
    protected ElementNode createDweetConnectorElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Dweet.io Connector",
		IOutboundConnectorsParser.Elements.DweetIoConnector.getLocalName(), "cloud",
		OutboundConnectorsRoleKeys.FilteredConnector, this);
	builder.description(
		"Sends events to the Dweet.io cloud service where they can be viewed and integrated with other services. "
			+ "The unique 'thing' name will be the unique token for the device assignment the event is associated with.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);

	addCommonConnectorAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_CONNECTIVITY);
	return builder.build();
    }

    /**
     * Create a Groovy connector.
     * 
     * @return
     */
    protected ElementNode createGroovyConnectorElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Groovy Connector",
		IOutboundConnectorsParser.Elements.GroovyConnector.getLocalName(), "cogs",
		OutboundConnectorsRoleKeys.FilteredConnector, this);
	builder.description("Delegates event processing to a Groovy script which can execute "
		+ "conditional logic, create new events, or carry out other tasks.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_GENERAL);

	addCommonConnectorAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_GENERAL);
	builder.attribute((new AttributeNode.Builder("Script path", "scriptPath", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL)
			.description("Script path relative to Groovy script root.").makeRequired().build()));
	return builder.build();
    }

    /**
     * Add common connector attributes.
     * 
     * @param builder
     * @param group
     */
    public static void addCommonConnectorAttributes(ElementNode.Builder builder, IAttributeGroup group) {
	builder.attribute((new AttributeNode.Builder("Connector id", "connectorId", AttributeType.String, group)
		.description("Unique id used for referencing this connector.").makeIndex().makeRequired().build()));
	builder.attribute((new AttributeNode.Builder("Number of processing threads", "numProcessingThreads",
		AttributeType.Integer, group)
			.description("Number of threads used to load inbound events into connector.").makeRequired()
			.build()));
    }

    /**
     * Create a Groovy route builder.
     * 
     * @return
     */
    protected ElementNode createGroovyRouteBuilderElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Groovy Route Builder",
		IOutboundConnectorsParser.RouteBuilders.GroovyRouteBuilder.getLocalName(), "sign-out-alt",
		OutboundConnectorsRoleKeys.GroovyRouteBuilder, this);
	builder.description(
		"Route builder which executes a Groovy script to choose routes where events will be delivered.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_GENERAL);

	addCommonConnectorAttributes(builder, ConfigurationModelProvider.ATTR_GROUP_GENERAL);
	builder.attribute((new AttributeNode.Builder("Script path", "scriptPath", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).description("Relative path to Groovy script.").build()));
	return builder.build();
    }

    /**
     * Create filter criteria element.
     * 
     * @return
     */
    protected ElementNode createFilterCriteriaElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Filter Criteria", "filters", "filter",
		OutboundConnectorsRoleKeys.Filters, this);
	builder.description("Adds filter criteria to control which events are sent to processor. "
		+ "Each filter is applied in the order below. Any events that have not been filtered "
		+ "will be passed to the outbound processor implementation.");
	return builder.build();
    }

    /**
     * Create connector area filter.
     * 
     * @return
     */
    protected ElementNode createAreaFilterElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Area Filter",
		IOutboundConnectorsParser.Filters.AreaFilter.getLocalName(), "filter",
		OutboundConnectorsRoleKeys.OutboundFilter, this);
	builder.description("Allows events from a given area to be included or excluded for an outbound processor.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_GENERAL);

	builder.attribute((new AttributeNode.Builder("Area", "area", AttributeType.AreaReference,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).description("Area filter applies to.").makeIndex()
			.build()));
	builder.attribute((new AttributeNode.Builder("Include/Exclude", "operation", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).description(
			"Indicates whether events from the area should be included or excluded from processing.")
			.choice("Include", "include").choice("Exclude", "exclude").defaultValue("include").build()));
	return builder.build();
    }

    /**
     * Create connector specification filter.
     * 
     * @return
     */
    // TODO: Update to device type.
    protected ElementNode createSpecificationFilterElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Specification Filter",
		IOutboundConnectorsParser.Filters.SpecificationFilter.getLocalName(), "filter",
		OutboundConnectorsRoleKeys.OutboundFilter, this);
	builder.description(
		"Allows events for devices of a given type to be included or excluded for an outbound processor.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_GENERAL);

	builder.attribute((new AttributeNode.Builder("Device type", "deviceTypeId", AttributeType.DeviceTypeReference,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).description("Device type filter applies to.").makeIndex()
			.makeRequired().build()));
	builder.attribute((new AttributeNode.Builder("Include/Exclude", "operation", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).description(
			"Indicates whether events from the specification should be included or excluded from processing.")
			.choice("Include", "include").choice("Exclude", "exclude").defaultValue("include").build()));
	return builder.build();
    }

    /**
     * Create connector Groovy filter.
     * 
     * @return
     */
    protected ElementNode createGroovyFilterElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Groovy Filter",
		IOutboundConnectorsParser.Filters.GroovyFilter.getLocalName(), "filter",
		OutboundConnectorsRoleKeys.OutboundFilter, this);
	builder.description("Allows events to be filtered based on the return value of a Groovy script. "
		+ "If the script returns false, the event is filtered. See the SiteWhere documentation for "
		+ "a description of the variable bindings provided by the system.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_GENERAL);

	builder.attribute((new AttributeNode.Builder("Script path", "scriptPath", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL)
			.description("Script path relative to Groovy script root.").makeRequired().build()));
	return builder.build();
    }
}