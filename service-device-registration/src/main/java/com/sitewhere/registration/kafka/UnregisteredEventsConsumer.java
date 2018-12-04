/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.registration.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import com.sitewhere.common.MarshalUtils;
import com.sitewhere.grpc.client.event.EventModelConverter;
import com.sitewhere.grpc.client.event.EventModelMarshaler;
import com.sitewhere.grpc.model.DeviceEventModel.GInboundEventPayload;
import com.sitewhere.microservice.kafka.DirectKafkaConsumer;
import com.sitewhere.microservice.security.SystemUserRunnable;
import com.sitewhere.registration.spi.kafka.IUnregisteredEventsConsumer;
import com.sitewhere.registration.spi.microservice.IDeviceRegistrationTenantEngine;
import com.sitewhere.rest.model.microservice.kafka.payload.InboundEventPayload;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.multitenant.IMicroserviceTenantEngine;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;

/**
 * Listens on Kafka topic for decoded events, making them available for inbound
 * processing.
 * 
 * @author Derek
 */
public class UnregisteredEventsConsumer extends DirectKafkaConsumer implements IUnregisteredEventsConsumer {

    /** Consumer id */
    private static String CONSUMER_ID = UUID.randomUUID().toString();

    /** Suffix for group id */
    private static String GROUP_ID_SUFFIX = "unregistered-event-consumers";

    /** Number of threads processing unregistered events */
    private static final int CONCURRENT_EVENT_PROCESSING_THREADS = 10;

    /** Executor */
    private ExecutorService executor;

    /*
     * @see com.sitewhere.spi.microservice.kafka.IMicroserviceKafkaConsumer#
     * getConsumerId()
     */
    @Override
    public String getConsumerId() throws SiteWhereException {
	return CONSUMER_ID;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IMicroserviceKafkaConsumer#
     * getConsumerGroupId()
     */
    @Override
    public String getConsumerGroupId() throws SiteWhereException {
	return getMicroservice().getKafkaTopicNaming().getTenantPrefix(getTenantEngine().getTenant()) + GROUP_ID_SUFFIX;
    }

    /*
     * @see com.sitewhere.spi.microservice.kafka.IMicroserviceKafkaConsumer#
     * getSourceTopicNames()
     */
    @Override
    public List<String> getSourceTopicNames() throws SiteWhereException {
	List<String> topics = new ArrayList<String>();
	topics.add(getMicroservice().getKafkaTopicNaming()
		.getUnregisteredDeviceEventsTopic(getTenantEngine().getTenant()));
	return topics;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.kafka.MicroserviceKafkaConsumer#start(com.
     * sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.start(monitor);
	executor = Executors.newFixedThreadPool(CONCURRENT_EVENT_PROCESSING_THREADS,
		new UnregisteredEventProcessorThreadFactory());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.microservice.kafka.MicroserviceKafkaConsumer#stop(com.
     * sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void stop(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	super.stop(monitor);
	if (executor != null) {
	    executor.shutdown();
	    try {
		executor.awaitTermination(10, TimeUnit.SECONDS);
	    } catch (InterruptedException e) {
		getLogger().warn("Executor did not terminate within allotted time.");
	    }
	}
    }

    /*
     * @see
     * com.sitewhere.microservice.kafka.DirectKafkaConsumer#attemptToProcess(org.
     * apache.kafka.common.TopicPartition, java.util.List)
     */
    @Override
    public void attemptToProcess(TopicPartition topicPartition, List<ConsumerRecord<String, byte[]>> records)
	    throws SiteWhereException {
	for (ConsumerRecord<String, byte[]> record : records) {
	    received(record.key(), record.value());
	}
    }

    public void received(String key, byte[] message) throws SiteWhereException {
	executor.execute(new UnregisteredDeviceEventProcessor(getTenantEngine(), message));
    }

    /**
     * Processor that unmarshals a decoded event for an unregistered device and
     * hands it off to the registration manager.
     * 
     * @author Derek
     */
    protected class UnregisteredDeviceEventProcessor extends SystemUserRunnable {

	/** Encoded payload */
	private byte[] encoded;

	public UnregisteredDeviceEventProcessor(IMicroserviceTenantEngine tenantEngine, byte[] encoded) {
	    super(tenantEngine.getMicroservice(), tenantEngine.getTenant());
	    this.encoded = encoded;
	}

	/*
	 * @see com.sitewhere.microservice.security.SystemUserRunnable#
	 * runAsSystemUser()
	 */
	@Override
	public void runAsSystemUser() throws SiteWhereException {
	    try {
		GInboundEventPayload grpc = EventModelMarshaler.parseInboundEventPayloadMessage(encoded);
		InboundEventPayload eventPayload = EventModelConverter.asApiInboundEventPayload(grpc);
		if (getLogger().isDebugEnabled()) {
		    getLogger().debug("Received event for unregistered device:\n\n"
			    + MarshalUtils.marshalJsonAsPrettyString(eventPayload));
		}

		// Pass payload to registration manager.
		((IDeviceRegistrationTenantEngine) getTenantEngine()).getRegistrationManager()
			.handleUnregisteredDeviceEvent(eventPayload);
	    } catch (SiteWhereException e) {
		getLogger().error("Unable to parse unregistered device event payload.", e);
	    } catch (Throwable e) {
		getLogger().error("Unhandled exception parsing unregistered device event payload.", e);
	    }
	}
    }

    /** Used for naming inbound event processing threads */
    private class UnregisteredEventProcessorThreadFactory implements ThreadFactory {

	/** Counts threads */
	private AtomicInteger counter = new AtomicInteger();

	public Thread newThread(Runnable r) {
	    return new Thread(r, "Unregistered Device Events " + counter.incrementAndGet());
	}
    }
}