/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.sources.activemq;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

import com.sitewhere.sources.InboundEventReceiver;
import com.sitewhere.sources.spi.IInboundEventReceiver;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;

/**
 * Implementation of {@link IInboundEventReceiver} that uses an ActiveMQ broker
 * to listen on a transport for messages.
 * 
 * @author Derek
 */
public class ActiveMQBrokerEventReceiver extends InboundEventReceiver<byte[]> {

    /** Number of consumers reading messages from the queue */
    private static final int DEFAULT_NUM_CONSUMERS = 3;

    /** ActiveMQ broker service */
    private BrokerService brokerService;

    /** Unique name of ActiveMQ broker */
    private String brokerName;

    /** URI for configuring transport */
    private String transportUri;

    /** Queue name used for inbound event data */
    private String queueName;

    /** Number of consumers used to read messages from the queue */
    private int numConsumers = DEFAULT_NUM_CONSUMERS;

    /** List of consumers reading messages */
    private List<Consumer> consumers = new ArrayList<Consumer>();

    /** Thread pool for consumer processing */
    private ExecutorService consumersPool;

    /*
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#initialize(com.sitewhere.
     * spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void initialize(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	if (getBrokerName() == null) {
	    throw new SiteWhereException("Broker name must be configured.");
	}
	if (getTransportUri() == null) {
	    throw new SiteWhereException("Transport URI must be configured.");
	}
	if (getQueueName() == null) {
	    throw new SiteWhereException("Queue name must be configured.");
	}
	this.brokerService = new BrokerService();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#start(com.sitewhere.spi
     * .server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	try {
	    getBrokerService().setBrokerName(getBrokerName());
	    TransportConnector connector = new TransportConnector();
	    connector.setUri(new URI(getTransportUri()));
	    getBrokerService().addConnector(connector);
	    getBrokerService().setUseShutdownHook(false);
	    getBrokerService().setUseJmx(false);
	    getBrokerService().start();
	    startConsumers();
	} catch (Exception e) {
	    throw new SiteWhereException("Error starting ActiveMQ inbound event receiver.", e);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.communication.IInboundEventReceiver#
     * getDisplayName()
     */
    @Override
    public String getDisplayName() {
	return getTransportUri();
    }

    /**
     * Starts consumers for reading messages into SiteWhere.
     * 
     * @throws SiteWhereException
     */
    protected void startConsumers() throws SiteWhereException {
	getConsumers().clear();
	this.consumersPool = Executors.newFixedThreadPool(getNumConsumers(), new ConsumersThreadFactory());
	for (int i = 0; i < getNumConsumers(); i++) {
	    Consumer consumer = new Consumer();
	    consumer.start();
	    getConsumersPool().execute(consumer);
	    getConsumers().add(consumer);
	}
	getLogger().info("Created " + consumers.size() + " consumers for processing ActiveMQ messages.");
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
	if (getBrokerService() != null) {
	    try {
		getBrokerService().stop();
	    } catch (Exception e) {
		throw new SiteWhereException("Error stopping ActiveMQ broker.", e);
	    }
	}
	stopConsumers();
    }

    /**
     * Stops all consumers.
     * 
     * @throws SiteWhereException
     */
    protected void stopConsumers() throws SiteWhereException {
	getConsumersPool().shutdownNow();
	for (Consumer consumer : consumers) {
	    consumer.stop();
	}
    }

    /** Used for naming consumer threads */
    private class ConsumersThreadFactory implements ThreadFactory {

	/** Counts threads */
	private AtomicInteger counter = new AtomicInteger();

	public Thread newThread(Runnable r) {
	    return new Thread(r, "SiteWhere ActiveMQ(" + getBrokerName() + ") Consumer " + counter.incrementAndGet());
	}
    }

    /**
     * Reads messages from the ActiveMQ queue and puts the binary content on a queue
     * for SiteWhere to use.
     * 
     * @author Derek
     */
    private class Consumer implements Runnable, ExceptionListener {

	/** Connection to ActiveMQ */
	private Connection connection;

	/** JMS session */
	private Session session;

	/** Consumer for reading data */
	private MessageConsumer consumer;

	public void start() throws SiteWhereException {
	    try {
		// Create a VM connection to the broker.
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://" + getBrokerName());
		this.connection = connectionFactory.createConnection();
		getConnection().setExceptionListener(this);
		getConnection().start();

		// Create a Session
		this.session = getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);

		Destination destination = getSession().createQueue(getQueueName());
		this.consumer = getSession().createConsumer(destination);
	    } catch (Exception e) {
		throw new SiteWhereException("Error starting ActiveMQ consumer.", e);
	    }
	}

	public void stop() throws SiteWhereException {
	    try {
		getConsumer().close();
		getSession().close();
		getConnection().close();
	    } catch (Exception e) {
		throw new SiteWhereException("Error shutting down ActiveMQ consumer.", e);
	    }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	    while (true) {
		try {
		    Message message = consumer.receive();
		    if (message == null) {
			break;
		    }
		    if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			onEventPayloadReceived(textMessage.getText().getBytes(), null);
		    } else if (message instanceof BytesMessage) {
			BytesMessage bytesMessage = (BytesMessage) message;
			byte[] buffer = new byte[(int) bytesMessage.getBodyLength()];
			bytesMessage.readBytes(buffer);
			onEventPayloadReceived(buffer, null);
		    } else {
			getLogger().warn("Ignoring unknown JMS message type: " + message.getClass().getName());
		    }
		} catch (Throwable e) {
		    getLogger().error("Error in ActiveMQ message processing.", e);
		    return;
		}
	    }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.ExceptionListener#onException(javax.jms.JMSException)
	 */
	@Override
	public void onException(JMSException e) {
	    try {
		stop();
	    } catch (SiteWhereException e1) {
	    }
	}

	protected Connection getConnection() {
	    return connection;
	}

	protected Session getSession() {
	    return session;
	}

	protected MessageConsumer getConsumer() {
	    return consumer;
	}
    }

    protected BrokerService getBrokerService() {
	return brokerService;
    }

    protected ExecutorService getConsumersPool() {
	return consumersPool;
    }

    protected List<Consumer> getConsumers() {
	return consumers;
    }

    public String getBrokerName() {
	return brokerName;
    }

    public void setBrokerName(String brokerName) {
	this.brokerName = brokerName;
    }

    public String getTransportUri() {
	return transportUri;
    }

    public void setTransportUri(String transportUri) {
	this.transportUri = transportUri;
    }

    public String getQueueName() {
	return queueName;
    }

    public void setQueueName(String queueName) {
	this.queueName = queueName;
    }

    public int getNumConsumers() {
	return numConsumers;
    }

    public void setNumConsumers(int numConsumers) {
	this.numConsumers = numConsumers;
    }
}