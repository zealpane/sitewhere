/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.labels.configuration;

import com.sitewhere.configuration.model.ConfigurationModelProvider;
import com.sitewhere.configuration.old.IDeviceServicesParser;
import com.sitewhere.configuration.parser.ILabelGenerationParser;
import com.sitewhere.rest.model.configuration.AttributeNode;
import com.sitewhere.rest.model.configuration.ElementNode;
import com.sitewhere.spi.microservice.configuration.model.AttributeType;
import com.sitewhere.spi.microservice.configuration.model.IConfigurationRoleProvider;

/**
 * Configuration model provider for label generation microservice.
 * 
 * @author Derek
 */
public class LabelGenerationModelProvider extends ConfigurationModelProvider {

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#getDefaultXmlNamespace()
     */
    @Override
    public String getDefaultXmlNamespace() {
	return "http://sitewhere.io/schema/sitewhere/microservice/label-generation";
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#getRootRole()
     */
    @Override
    public IConfigurationRoleProvider getRootRole() {
	return LabelGenerationRoles.LabelGeneration;
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#initializeElements()
     */
    @Override
    public void initializeElements() {
	addElement(createLabelGenerationElement());

	addElement(createSymbolGeneratorManagerElement());
	addElement(createQRCodeSymbolGeneratorElement());
    }

    /*
     * @see com.sitewhere.spi.microservice.configuration.model.
     * IConfigurationModelProvider#initializeRoles()
     */
    @Override
    public void initializeRoles() {
	for (LabelGenerationRoles role : LabelGenerationRoles.values()) {
	    getRolesById().put(role.getRole().getKey().getId(), role.getRole());
	}
    }

    /**
     * Create label generation element.
     * 
     * @return
     */
    protected ElementNode createLabelGenerationElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Label Generation", ILabelGenerationParser.ROOT, "qrcode",
		LabelGenerationRoleKeys.LabelGeneration, this);

	builder.description("Handles generation of labels such as QR codes.");

	return builder.build();
    }

    /**
     * Create element configuration for device registration.
     * 
     * @return
     */
    protected ElementNode createSymbolGeneratorManagerElement() {
	ElementNode.Builder builder = new ElementNode.Builder("Symbol Generator Manager",
		IDeviceServicesParser.Elements.SymbolGeneratorManager.getLocalName(), "qrcode",
		LabelGenerationRoleKeys.SymbolGeneratorManager, this);

	builder.description("Manages how symbols such as QR-Codes are generated for devices "
		+ "and other SiteWhere entities. Generated symbol images are made available via "
		+ "the REST services.");
	return builder.build();
    }

    /**
     * Create element configuration for default registration manager.
     * 
     * @return
     */
    protected ElementNode createQRCodeSymbolGeneratorElement() {
	ElementNode.Builder builder = new ElementNode.Builder("QR-Code Symbol Generator",
		IDeviceServicesParser.SymbolGenerators.QRCodeSymbolGenerator.getLocalName(), "qrcode",
		LabelGenerationRoleKeys.SymbolGenerator, this);

	builder.description("Generates QR-Codes for devices and other SiteWhere entities. The generated "
		+ "images are available via the REST services.");
	builder.attributeGroup(ConfigurationModelProvider.ATTR_GROUP_GENERAL);

	builder.attribute((new AttributeNode.Builder("Unique generator id", "id", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL)
			.description("Each symbol generator must have a unique id").build()));
	builder.attribute((new AttributeNode.Builder("Generator name", "name", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).makeIndex()
			.description("Name shown in user interface for symbol generator.").build()));
	builder.attribute((new AttributeNode.Builder("QR-Code image width", "width", AttributeType.Integer,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).description("Width of QR-code image in pixels.")
			.defaultValue("200").build()));
	builder.attribute((new AttributeNode.Builder("QR-Code image height", "height", AttributeType.Integer,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL).description("Height of QR-code image in pixels.")
			.defaultValue("200").build()));
	builder.attribute((new AttributeNode.Builder("Background color", "backgroundColor", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL)
			.description("Background color of QR-Code image in AARRGGBB format.").defaultValue("FFFFFFFF")
			.build()));
	builder.attribute((new AttributeNode.Builder("Foreground color", "foregroundColor", AttributeType.String,
		ConfigurationModelProvider.ATTR_GROUP_GENERAL)
			.description("Foreground color of QR-Code image in AARRGGBB format.").defaultValue("FF333333")
			.build()));
	return builder.build();
    }
}