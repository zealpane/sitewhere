/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.microservice.kafka;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.sitewhere.microservice.MicroserviceApplication;
import com.sitewhere.server.lifecycle.TenantEngineLifecycleComponent;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.kafka.IMicroserviceKafkaProducer;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;

/**
 * Base class for components that produce messages that are forwarded to a Kafka
 * topic.
 * 
 * @author Derek
 */
public abstract class MicroserviceKafkaProducer extends TenantEngineLifecycleComponent
	implements IMicroserviceKafkaProducer {

    /** Static logger instance for callback */
    private static Log CALLBACK_LOGGER = LogFactory.getLog(MicroserviceApplication.class);

    /** Create single callback */
    private static Callback CALLBACK = new Callback() {
	public void onCompletion(RecordMetadata metadata, Exception e) {
	    if (e != null) {
		CALLBACK_LOGGER.error("Unable to complete delivery of Kafka message.", e);
	    }
	}
    };

    /** Producer */
    private KafkaProducer<String, byte[]> producer;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#start(com.sitewhere.spi
     * .server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	getLogger().debug(
		"Producer connecting to Kafka: " + getMicroservice().getInstanceSettings().getKafkaBootstrapServers());
	getLogger().debug("Will be producing messages for: " + getTargetTopicName());
	this.producer = new KafkaProducer<String, byte[]>(buildConfiguration());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#stop(com.sitewhere.spi.
     * server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void stop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	if (getProducer() != null) {
	    getProducer().close();
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.microservice.spi.kafka.IMicroserviceProducer#send(java.lang
     * .String, byte[])
     */
    @Override
    public void send(String key, byte[] message) throws SiteWhereException {
	ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(getTargetTopicName(), key, message);
	getProducer().send(record, CALLBACK);
    }

    /**
     * Build configuration settings used by producer.
     * 
     * @return
     * @throws SiteWhereException
     */
    protected Properties buildConfiguration() throws SiteWhereException {
	Properties config = new Properties();
	config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
		getMicroservice().getInstanceSettings().getKafkaBootstrapServers());
	config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
	config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
	return config;
    }

    public KafkaProducer<String, byte[]> getProducer() {
	return producer;
    }

    public void setProducer(KafkaProducer<String, byte[]> producer) {
	this.producer = producer;
    }
}