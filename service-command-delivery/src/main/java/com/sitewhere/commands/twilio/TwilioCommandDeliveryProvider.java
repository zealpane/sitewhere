/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.commands.twilio;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.sitewhere.commands.destination.sms.SmsParameters;
import com.sitewhere.commands.spi.ICommandDeliveryProvider;
import com.sitewhere.server.lifecycle.TenantEngineLifecycleComponent;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceNestingContext;
import com.sitewhere.spi.device.command.IDeviceCommandExecution;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.server.lifecycle.LifecycleComponentType;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Account;

/**
 * Implementation of {@link ICommandDeliveryProvider} that sends an SMS message
 * via Twilio.
 * 
 * @author Derek
 */
public class TwilioCommandDeliveryProvider extends TenantEngineLifecycleComponent
	implements ICommandDeliveryProvider<String, SmsParameters> {

    /** Account SID */
    private String accountSid;

    /** Auth token */
    private String authToken;

    /** Phone number to send command from */
    private String fromPhoneNumber;

    /** Client for Twilio REST calls */
    private TwilioRestClient twilio;

    /** Twilio account */
    private Account account;

    public TwilioCommandDeliveryProvider() {
	super(LifecycleComponentType.CommandDeliveryProvider);
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
	if (getAccountSid() == null) {
	    throw new SiteWhereException("Twilio command delivery provider missing account SID.");
	}
	if (getAuthToken() == null) {
	    throw new SiteWhereException("Twilio command delivery provider missing auth token.");
	}
	this.twilio = new TwilioRestClient(getAccountSid(), getAuthToken());
	this.account = twilio.getAccount();
	getLogger().info("Twilio delivery provider started. Calls will originate from " + getFromPhoneNumber() + ".");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.communication.ICommandDeliveryProvider#deliver(
     * com.sitewhere .spi.device.IDeviceNestingContext,
     * com.sitewhere.spi.device.IDeviceAssignment,
     * com.sitewhere.spi.device.command.IDeviceCommandExecution, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public void deliver(IDeviceNestingContext nested, IDeviceAssignment assignment, IDeviceCommandExecution execution,
	    String encoded, SmsParameters params) throws SiteWhereException {
	getLogger().info("Delivering SMS command to " + params.getSmsPhoneNumber() + ".");
	sendSms(encoded, getFromPhoneNumber(), params.getSmsPhoneNumber());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.communication.ICommandDeliveryProvider#
     * deliverSystemCommand (com.sitewhere.spi.device.IDeviceNestingContext,
     * com.sitewhere.spi.device.IDeviceAssignment, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public void deliverSystemCommand(IDeviceNestingContext nested, IDeviceAssignment assignment, String encoded,
	    SmsParameters params) throws SiteWhereException {
	throw new UnsupportedOperationException();
    }

    /**
     * Send an SMS message.
     * 
     * @param message
     * @param from
     * @param to
     * @throws SiteWhereException
     */
    protected void sendSms(String message, String from, String to) throws SiteWhereException {
	MessageFactory messageFactory = account.getMessageFactory();
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("To", to));
	params.add(new BasicNameValuePair("From", from));
	params.add(new BasicNameValuePair("Body", message));
	try {
	    messageFactory.create(params);
	} catch (TwilioRestException e) {
	    throw new SiteWhereException("Unable to send Twilio SMS message.", e);
	}
    }

    public String getAccountSid() {
	return accountSid;
    }

    public void setAccountSid(String accountSid) {
	this.accountSid = accountSid;
    }

    public String getAuthToken() {
	return authToken;
    }

    public void setAuthToken(String authToken) {
	this.authToken = authToken;
    }

    public String getFromPhoneNumber() {
	return fromPhoneNumber;
    }

    public void setFromPhoneNumber(String fromPhoneNumber) {
	this.fromPhoneNumber = fromPhoneNumber;
    }
}