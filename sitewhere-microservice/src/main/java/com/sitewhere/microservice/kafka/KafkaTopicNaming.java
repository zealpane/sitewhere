/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.microservice.kafka;

import org.springframework.beans.factory.annotation.Autowired;

import com.sitewhere.spi.microservice.instance.IInstanceSettings;
import com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Class for locating SiteWhere Kafka topics.
 * 
 * @author Derek
 */
public class KafkaTopicNaming implements IKafkaTopicNaming {

    /** Separator used to partition topic name */
    protected static final String SEPARATOR = ".";

    /** Global topic indicator */
    protected static final String GLOBAL_INDICATOR = "global";

    /** Tenant topic indicator */
    protected static final String TENANT_INDICATOR = "tenant";

    /** Topic suffix for microservice state updates */
    protected static final String MICROSERVICE_STATE_UPDATES_SUFFIX = "microservice-state-updates";

    /** Topic suffix for instance topology updates */
    protected static final String INSTANCE_TOPOLOGY_UPDATES_SUFFIX = "instance-topology-updates";

    /** Topic suffix for tenant model updates */
    protected static final String TENANT_MODEL_UPDATES_SUFFIX = "tenant-model-updates";

    /** Topic suffix for instance-wide logging */
    protected static final String INSTANCE_LOGGING_SUFFIX = "instance-logging";

    /** Topic suffix for events decoded by event sources for a tenant */
    protected static final String TENANT_TOPIC_EVENT_SOURCE_DECODED_EVENTS = "event-source-decoded-events";

    /** Topic suffix for events that could not be decoded for a tenant */
    protected static final String TENANT_TOPIC_EVENT_SOURCE_FAILED_DECODE_EVENTS = "event-source-failed-decode-events";

    /** Topic suffix for events that should be reprocessed */
    protected static final String TENANT_TOPIC_INBOUND_REPROCESS_EVENTS = "inbound-reprocess-events";

    /** Topic suffix for events that have been persisted */
    protected static final String TENANT_TOPIC_INBOUND_PERSISTED_EVENTS = "inbound-persisted-events";

    /** Topic suffix for device registration events from inbound event sources */
    protected static final String TENANT_TOPIC_INBOUND_DEVICE_REGISTRATION_EVENTS = "inbound-device-registration-events";

    /** Topic suffix for tenant events sent to unregistered devices */
    protected static final String TENANT_TOPIC_INBOUND_UNREGISTERED_DEVICE_EVENTS = "inbound-unregistered-device-events";

    /** Topic suffix for events that have been persisted and enriched */
    protected static final String TENANT_TOPIC_INBOUND_ENRICHED_EVENTS = "inbound-enriched-events";

    /** Topic suffix for persisted and enriched command invocations */
    protected static final String TENANT_TOPIC_INBOUND_ENRICHED_COMMAND_INVOCATIONS = "inbound-enriched-command-invocations";

    /** Topic suffix for undelivered command invocations */
    protected static final String TENANT_TOPIC_UNDELIVERED_COMMAND_INVOCATIONS = "undelivered-command-invocations";

    @Autowired
    private IInstanceSettings instanceSettings;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.kafka.IKafkaTopicNaming#getInstancePrefix( )
     */
    @Override
    public String getInstancePrefix() {
	return getInstanceSettings().getProductId() + SEPARATOR + getInstanceSettings().getInstanceId();
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#getGlobalPrefix()
     */
    @Override
    public String getGlobalPrefix() {
	return getInstancePrefix() + SEPARATOR + GLOBAL_INDICATOR + SEPARATOR;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#getTenantPrefix(
     * com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getTenantPrefix(ITenant tenant) {
	return getInstancePrefix() + SEPARATOR + TENANT_INDICATOR + SEPARATOR + tenant.getId() + SEPARATOR;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getMicroserviceStateUpdatesTopic()
     */
    @Override
    public String getMicroserviceStateUpdatesTopic() {
	return getGlobalPrefix() + MICROSERVICE_STATE_UPDATES_SUFFIX;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getInstanceTopologyUpdatesTopic()
     */
    @Override
    public String getInstanceTopologyUpdatesTopic() {
	return getGlobalPrefix() + INSTANCE_TOPOLOGY_UPDATES_SUFFIX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.spi.kafka.IKafkaTopicNaming#
     * getTenantUpdatesTopic()
     */
    @Override
    public String getTenantUpdatesTopic() {
	return getGlobalPrefix() + TENANT_MODEL_UPDATES_SUFFIX;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getInstanceLoggingTopic()
     */
    @Override
    public String getInstanceLoggingTopic() {
	return getGlobalPrefix() + INSTANCE_LOGGING_SUFFIX;
    }

    /*
     * @see com.sitewhere.microservice.spi.kafka.IKafkaTopicNaming#
     * getEventSourceDecodedEventsTopic(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getEventSourceDecodedEventsTopic(ITenant tenant) {
	return getTenantPrefix(tenant) + TENANT_TOPIC_EVENT_SOURCE_DECODED_EVENTS;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getEventSourceFailedDecodeTopic(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getEventSourceFailedDecodeTopic(ITenant tenant) {
	return getTenantPrefix(tenant) + TENANT_TOPIC_EVENT_SOURCE_FAILED_DECODE_EVENTS;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getInboundReprocessEventsTopic(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getInboundReprocessEventsTopic(ITenant tenant) {
	return getTenantPrefix(tenant) + TENANT_TOPIC_INBOUND_REPROCESS_EVENTS;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getInboundPersistedEventsTopic(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getInboundPersistedEventsTopic(ITenant tenant) {
	return getTenantPrefix(tenant) + TENANT_TOPIC_INBOUND_PERSISTED_EVENTS;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getDeviceRegistrationEventsTopic(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getDeviceRegistrationEventsTopic(ITenant tenant) {
	return getTenantPrefix(tenant) + TENANT_TOPIC_INBOUND_DEVICE_REGISTRATION_EVENTS;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getUnregisteredDeviceEventsTopic(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getUnregisteredDeviceEventsTopic(ITenant tenant) {
	return getTenantPrefix(tenant) + TENANT_TOPIC_INBOUND_UNREGISTERED_DEVICE_EVENTS;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getInboundEnrichedEventsTopic(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getInboundEnrichedEventsTopic(ITenant tenant) {
	return getTenantPrefix(tenant) + TENANT_TOPIC_INBOUND_ENRICHED_EVENTS;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getInboundEnrichedCommandInvocationsTopic(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getInboundEnrichedCommandInvocationsTopic(ITenant tenant) {
	return getTenantPrefix(tenant) + TENANT_TOPIC_INBOUND_ENRICHED_COMMAND_INVOCATIONS;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IKafkaTopicNaming#
     * getUndeliveredCommandInvocationsTopic(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public String getUndeliveredCommandInvocationsTopic(ITenant tenant) {
	return getTenantPrefix(tenant) + TENANT_TOPIC_UNDELIVERED_COMMAND_INVOCATIONS;
    }

    protected IInstanceSettings getInstanceSettings() {
	return instanceSettings;
    }

    protected void setInstanceSettings(IInstanceSettings instanceSettings) {
	this.instanceSettings = instanceSettings;
    }
}