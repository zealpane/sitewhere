/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.device.persistence.hbase;

import java.util.List;
import java.util.UUID;

import org.apache.hadoop.hbase.regionserver.BloomType;

import com.sitewhere.device.persistence.DeviceManagementPersistence;
import com.sitewhere.hbase.DeviceIdManager;
import com.sitewhere.hbase.HBaseContext;
import com.sitewhere.hbase.ISiteWhereHBase;
import com.sitewhere.hbase.ISiteWhereHBaseClient;
import com.sitewhere.hbase.common.SiteWhereTables;
import com.sitewhere.hbase.encoder.IPayloadMarshaler;
import com.sitewhere.rest.model.area.Area;
import com.sitewhere.rest.model.search.SearchResults;
import com.sitewhere.server.lifecycle.TenantEngineLifecycleComponent;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.area.IArea;
import com.sitewhere.spi.area.IAreaType;
import com.sitewhere.spi.area.IZone;
import com.sitewhere.spi.area.request.IAreaCreateRequest;
import com.sitewhere.spi.area.request.IAreaTypeCreateRequest;
import com.sitewhere.spi.area.request.IZoneCreateRequest;
import com.sitewhere.spi.customer.ICustomer;
import com.sitewhere.spi.customer.ICustomerType;
import com.sitewhere.spi.customer.request.ICustomerCreateRequest;
import com.sitewhere.spi.customer.request.ICustomerTypeCreateRequest;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAlarm;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceElementMapping;
import com.sitewhere.spi.device.IDeviceManagement;
import com.sitewhere.spi.device.IDeviceStatus;
import com.sitewhere.spi.device.IDeviceType;
import com.sitewhere.spi.device.command.IDeviceCommand;
import com.sitewhere.spi.device.event.request.IDeviceStreamCreateRequest;
import com.sitewhere.spi.device.group.IDeviceGroup;
import com.sitewhere.spi.device.group.IDeviceGroupElement;
import com.sitewhere.spi.device.request.IDeviceAlarmCreateRequest;
import com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest;
import com.sitewhere.spi.device.request.IDeviceCommandCreateRequest;
import com.sitewhere.spi.device.request.IDeviceCreateRequest;
import com.sitewhere.spi.device.request.IDeviceGroupCreateRequest;
import com.sitewhere.spi.device.request.IDeviceGroupElementCreateRequest;
import com.sitewhere.spi.device.request.IDeviceStatusCreateRequest;
import com.sitewhere.spi.device.request.IDeviceTypeCreateRequest;
import com.sitewhere.spi.device.streaming.IDeviceStream;
import com.sitewhere.spi.search.ISearchCriteria;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.search.area.IAreaSearchCriteria;
import com.sitewhere.spi.search.customer.ICustomerSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceAlarmSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceAssignmentSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceCommandSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceSearchCriteria;
import com.sitewhere.spi.search.device.IDeviceStatusSearchCriteria;
import com.sitewhere.spi.search.device.IZoneSearchCriteria;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.server.lifecycle.LifecycleComponentType;

/**
 * HBase implementation of SiteWhere device management.
 * 
 * The Hbase device management implementation will be removed for SiteWhere 2.0.
 * HBase will still be supported for event management, but device management is
 * not a good use of the technology.
 * 
 * @author Derek
 */
@Deprecated
public class HBaseDeviceManagement extends TenantEngineLifecycleComponent implements IDeviceManagement {

    /** Used to communicate with HBase */
    private ISiteWhereHBaseClient client;

    /** Injected payload encoder */
    private IPayloadMarshaler payloadMarshaler;

    /** Supplies context to implementation methods */
    private HBaseContext context;

    /** Device id manager */
    private DeviceIdManager deviceIdManager;

    public HBaseDeviceManagement() {
	super(LifecycleComponentType.DataStore);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.server.lifecycle.LifecycleComponent#start(com.sitewhere.spi
     * .server.lifecycle.ILifecycleProgressMonitor)
     */
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Create context from configured options.
	this.context = new HBaseContext();
	context.setTenant(getTenantEngine().getTenant());
	context.setClient(getClient());
	context.setPayloadMarshaler(getPayloadMarshaler());

	ensureTablesExist();

	// Create device id manager instance.
	deviceIdManager = new DeviceIdManager();
	deviceIdManager.load(context);
	context.setDeviceIdManager(deviceIdManager);
    }

    /**
     * Make sure that all SiteWhere tables exist, creating them if necessary.
     * 
     * @throws SiteWhereException
     */
    protected void ensureTablesExist() throws SiteWhereException {
	SiteWhereTables.assureTenantTable(context, ISiteWhereHBase.UID_TABLE_NAME, BloomType.ROW);
	SiteWhereTables.assureTenantTable(context, ISiteWhereHBase.SITES_TABLE_NAME, BloomType.ROW);
	SiteWhereTables.assureTenantTable(context, ISiteWhereHBase.DEVICES_TABLE_NAME, BloomType.ROW);
	SiteWhereTables.assureTenantTable(context, ISiteWhereHBase.STREAMS_TABLE_NAME, BloomType.ROW);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceType(com.sitewhere.spi
     * .device.request.IDeviceTypeCreateRequest)
     */
    @Override
    public IDeviceType createDeviceType(IDeviceTypeCreateRequest request) throws SiteWhereException {
	return HBaseDeviceType.createDeviceType(context, request);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceType(java.util.UUID)
     */
    @Override
    public IDeviceType getDeviceType(UUID id) throws SiteWhereException {
	return null;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceTypeByToken(java.lang.
     * String)
     */
    @Override
    public IDeviceType getDeviceTypeByToken(String token) throws SiteWhereException {
	return HBaseDeviceType.getDeviceTypeByToken(context, token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceType(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceTypeCreateRequest)
     */
    @Override
    public IDeviceType updateDeviceType(UUID id, IDeviceTypeCreateRequest request) throws SiteWhereException {
	IDeviceType deviceType = getDeviceType(id);
	return HBaseDeviceType.updateDeviceType(context, deviceType, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceTypes(com.sitewhere.spi.
     * search.ISearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceType> listDeviceTypes(ISearchCriteria criteria) throws SiteWhereException {
	return HBaseDeviceType.listDeviceTypes(context, false, criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceType(java.util.UUID)
     */
    @Override
    public IDeviceType deleteDeviceType(UUID id) throws SiteWhereException {
	IDeviceType deviceType = getDeviceType(id);
	return HBaseDeviceType.deleteDeviceType(context, deviceType, false);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceCommand(com.sitewhere.
     * spi.device.request.IDeviceCommandCreateRequest)
     */
    @Override
    public IDeviceCommand createDeviceCommand(IDeviceCommandCreateRequest request) throws SiteWhereException {
	return HBaseDeviceCommand.createDeviceCommand(context, null, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceCommand(java.util.UUID)
     */
    @Override
    public IDeviceCommand getDeviceCommand(UUID id) throws SiteWhereException {
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceCommandByToken(java.
     * lang.String )
     */
    @Override
    public IDeviceCommand getDeviceCommandByToken(String token) throws SiteWhereException {
	return HBaseDeviceCommand.getDeviceCommandByToken(context, token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceCommand(java.util.
     * UUID, com.sitewhere.spi.device.request.IDeviceCommandCreateRequest)
     */
    @Override
    public IDeviceCommand updateDeviceCommand(UUID id, IDeviceCommandCreateRequest request) throws SiteWhereException {
	IDeviceCommand command = getDeviceCommand(id);
	IDeviceType deviceType = getDeviceType(command.getDeviceTypeId());
	return HBaseDeviceCommand.updateDeviceCommand(context, deviceType, command, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceCommands(com.sitewhere.
     * spi.search.device.IDeviceCommandSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceCommand> listDeviceCommands(IDeviceCommandSearchCriteria criteria)
	    throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceCommand(java.util.
     * UUID)
     */
    @Override
    public IDeviceCommand deleteDeviceCommand(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceStatus(com.sitewhere.
     * spi.device.request.IDeviceStatusCreateRequest)
     */
    @Override
    public IDeviceStatus createDeviceStatus(IDeviceStatusCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceStatus(java.util.UUID)
     */
    @Override
    public IDeviceStatus getDeviceStatus(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceStatusByToken(java.lang.
     * String)
     */
    @Override
    public IDeviceStatus getDeviceStatusByToken(String token) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceStatus(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceStatusCreateRequest)
     */
    @Override
    public IDeviceStatus updateDeviceStatus(UUID id, IDeviceStatusCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceStatuses(com.sitewhere.
     * spi.search.device.IDeviceStatusSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceStatus> listDeviceStatuses(IDeviceStatusSearchCriteria criteria)
	    throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceStatus(java.util.UUID)
     */
    @Override
    public IDeviceStatus deleteDeviceStatus(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDevice(com.sitewhere.spi
     * .device .request.IDeviceCreateRequest)
     */
    @Override
    public IDevice createDevice(IDeviceCreateRequest device) throws SiteWhereException {
	return HBaseDevice.createDevice(context, device);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getDevice(java.util.UUID)
     */
    @Override
    public IDevice getDevice(UUID deviceId) throws SiteWhereException {
	return null;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceByToken(java.lang.String)
     */
    @Override
    public IDevice getDeviceByToken(String deviceToken) throws SiteWhereException {
	return HBaseDevice.getDeviceByToken(context, deviceToken);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#updateDevice(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceCreateRequest)
     */
    @Override
    public IDevice updateDevice(UUID id, IDeviceCreateRequest request) throws SiteWhereException {
	IDevice device = getDevice(id);
	return HBaseDevice.updateDevice(context, device, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getCurrentDeviceAssignment(java.
     * util.UUID)
     */
    @Override
    public IDeviceAssignment getCurrentDeviceAssignment(UUID deviceId) throws SiteWhereException {
	IDevice device = getDevice(deviceId);
	String token = HBaseDevice.getCurrentAssignmentId(context, device);
	if (token == null) {
	    return null;
	}
	// return HBaseDeviceAssignment.getDeviceAssignment(context, token);
	return null;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDevices(com.sitewhere.spi.
     * search.device.IDeviceSearchCriteria)
     */
    @Override
    public SearchResults<IDevice> listDevices(IDeviceSearchCriteria criteria) throws SiteWhereException {
	return HBaseDevice.listDevices(context, false, criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceElementMapping(java.
     * util.UUID, com.sitewhere.spi.device.IDeviceElementMapping)
     */
    @Override
    public IDevice createDeviceElementMapping(UUID deviceId, IDeviceElementMapping mapping) throws SiteWhereException {
	IDevice device = getDevice(deviceId);
	return DeviceManagementPersistence.deviceElementMappingCreateLogic(this, device, mapping);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceElementMapping(java.
     * util.UUID, java.lang.String)
     */
    @Override
    public IDevice deleteDeviceElementMapping(UUID deviceId, String path) throws SiteWhereException {
	IDevice device = getDevice(deviceId);
	return DeviceManagementPersistence.deviceElementMappingDeleteLogic(this, device, path);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#deleteDevice(java.util.UUID)
     */
    @Override
    public IDevice deleteDevice(UUID id) throws SiteWhereException {
	IDevice device = getDevice(id);
	return HBaseDevice.deleteDevice(context, device, false);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#createDeviceAssignment(com.
     * sitewhere.spi.device.request.IDeviceAssignmentCreateRequest)
     */
    @Override
    public IDeviceAssignment createDeviceAssignment(IDeviceAssignmentCreateRequest request) throws SiteWhereException {
	return HBaseDeviceAssignment.createDeviceAssignment(context, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignment(java.util.
     * UUID)
     */
    @Override
    public IDeviceAssignment getDeviceAssignment(UUID id) throws SiteWhereException {
	return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.IDeviceManagement#getDeviceAssignmentByToken(
     * java.lang .String)
     */
    @Override
    public IDeviceAssignment getDeviceAssignmentByToken(String token) throws SiteWhereException {
	// return HBaseDeviceAssignment.getDeviceAssignment(context, token);
	return null;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceAssignment(java.util.
     * UUID)
     */
    @Override
    public IDeviceAssignment deleteDeviceAssignment(UUID id) throws SiteWhereException {
	IDeviceAssignment assn = getDeviceAssignment(id);
	return HBaseDeviceAssignment.deleteDeviceAssignment(context, assn, false);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceAlarm(com.sitewhere.
     * spi.device.request.IDeviceAlarmCreateRequest)
     */
    @Override
    public IDeviceAlarm createDeviceAlarm(IDeviceAlarmCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceAlarm(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceAlarmCreateRequest)
     */
    @Override
    public IDeviceAlarm updateDeviceAlarm(UUID id, IDeviceAlarmCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceAlarm(java.util.UUID)
     */
    @Override
    public IDeviceAlarm getDeviceAlarm(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#searchDeviceAlarms(com.sitewhere.
     * spi.search.device.IDeviceAlarmSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceAlarm> searchDeviceAlarms(IDeviceAlarmSearchCriteria criteria)
	    throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceAlarm(java.util.UUID)
     */
    @Override
    public IDeviceAlarm deleteDeviceAlarm(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceAssignment(java.util.
     * UUID, com.sitewhere.spi.device.request.IDeviceAssignmentCreateRequest)
     */
    @Override
    public IDeviceAssignment updateDeviceAssignment(UUID id, IDeviceAssignmentCreateRequest request)
	    throws SiteWhereException {
	IDeviceAssignment assn = getDeviceAssignment(id);
	return HBaseDeviceAssignment.updateDeviceAssignmentMetadata(context, assn, null);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#listDeviceAssignments(com.
     * sitewhere.spi.search.device.IDeviceAssignmentSearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceAssignment> listDeviceAssignments(IDeviceAssignmentSearchCriteria criteria)
	    throws SiteWhereException {
	return null;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#endDeviceAssignment(java.util.
     * UUID)
     */
    @Override
    public IDeviceAssignment endDeviceAssignment(UUID id) throws SiteWhereException {
	IDeviceAssignment assn = getDeviceAssignment(id);
	return HBaseDeviceAssignment.endDeviceAssignment(context, assn);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createDeviceStream(java.util.UUID,
     * com.sitewhere.spi.device.event.request.IDeviceStreamCreateRequest)
     */
    @Override
    public IDeviceStream createDeviceStream(UUID assignmentId, IDeviceStreamCreateRequest request)
	    throws SiteWhereException {
	IDeviceAssignment assn = getDeviceAssignment(assignmentId);
	return HBaseDeviceStream.createDeviceStream(context, assn, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceStream(java.util.UUID,
     * java.lang.String)
     */
    @Override
    public IDeviceStream getDeviceStream(UUID assignmentId, String streamId) throws SiteWhereException {
	IDeviceAssignment assn = getDeviceAssignment(assignmentId);
	return HBaseDeviceStream.getDeviceStream(context, assn, streamId);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceStreams(java.util.UUID,
     * com.sitewhere.spi.search.ISearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceStream> listDeviceStreams(UUID assignmentId, ISearchCriteria criteria)
	    throws SiteWhereException {
	IDeviceAssignment assn = getDeviceAssignment(assignmentId);
	return HBaseDeviceStream.listDeviceStreams(context, assn, criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createCustomerType(com.sitewhere.
     * spi.customer.request.ICustomerTypeCreateRequest)
     */
    @Override
    public ICustomerType createCustomerType(ICustomerTypeCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getCustomerType(java.util.UUID)
     */
    @Override
    public ICustomerType getCustomerType(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getCustomerTypeByToken(java.lang.
     * String)
     */
    @Override
    public ICustomerType getCustomerTypeByToken(String token) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateCustomerType(java.util.UUID,
     * com.sitewhere.spi.customer.request.ICustomerTypeCreateRequest)
     */
    @Override
    public ICustomerType updateCustomerType(UUID id, ICustomerTypeCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listCustomerTypes(com.sitewhere.
     * spi.search.ISearchCriteria)
     */
    @Override
    public ISearchResults<ICustomerType> listCustomerTypes(ISearchCriteria criteria) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteCustomerType(java.util.UUID)
     */
    @Override
    public ICustomerType deleteCustomerType(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createCustomer(com.sitewhere.spi.
     * customer.request.ICustomerCreateRequest)
     */
    @Override
    public ICustomer createCustomer(ICustomerCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getCustomer(java.util.UUID)
     */
    @Override
    public ICustomer getCustomer(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getCustomerByToken(java.lang.
     * String)
     */
    @Override
    public ICustomer getCustomerByToken(String token) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getCustomerChildren(java.lang.
     * String)
     */
    @Override
    public List<ICustomer> getCustomerChildren(String token) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateCustomer(java.util.UUID,
     * com.sitewhere.spi.customer.request.ICustomerCreateRequest)
     */
    @Override
    public ICustomer updateCustomer(UUID id, ICustomerCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listCustomers(com.sitewhere.spi.
     * search.customer.ICustomerSearchCriteria)
     */
    @Override
    public ISearchResults<ICustomer> listCustomers(ICustomerSearchCriteria criteria) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteCustomer(java.util.UUID)
     */
    @Override
    public ICustomer deleteCustomer(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createAreaType(com.sitewhere.spi.
     * area.request.IAreaTypeCreateRequest)
     */
    @Override
    public IAreaType createAreaType(IAreaTypeCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getAreaType(java.util.UUID)
     */
    @Override
    public IAreaType getAreaType(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getAreaTypeByToken(java.lang.
     * String)
     */
    @Override
    public IAreaType getAreaTypeByToken(String token) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateAreaType(java.util.UUID,
     * com.sitewhere.spi.area.request.IAreaTypeCreateRequest)
     */
    @Override
    public IAreaType updateAreaType(UUID id, IAreaTypeCreateRequest request) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listAreaTypes(com.sitewhere.spi.
     * search.ISearchCriteria)
     */
    @Override
    public ISearchResults<IAreaType> listAreaTypes(ISearchCriteria criteria) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteAreaType(java.util.UUID)
     */
    @Override
    public IAreaType deleteAreaType(UUID id) throws SiteWhereException {
	throw new SiteWhereException("Not implemented yet for HBase device managment.");
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createArea(com.sitewhere.spi.area.
     * request.IAreaCreateRequest)
     */
    @Override
    public IArea createArea(IAreaCreateRequest request) throws SiteWhereException {
	return HBaseArea.createArea(context, request);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#deleteArea(java.util.UUID)
     */
    @Override
    public IArea deleteArea(UUID id) throws SiteWhereException {
	IArea area = getArea(id);
	return HBaseArea.deleteArea(context, (Area) area, false);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#updateArea(java.util.UUID,
     * com.sitewhere.spi.area.request.IAreaCreateRequest)
     */
    @Override
    public IArea updateArea(UUID id, IAreaCreateRequest request) throws SiteWhereException {
	IArea area = getArea(id);
	return HBaseArea.updateArea(context, (Area) area, request);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getArea(java.util.UUID)
     */
    @Override
    public IArea getArea(UUID id) throws SiteWhereException {
	return null;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getAreaByToken(java.lang.String)
     */
    @Override
    public IArea getAreaByToken(String token) throws SiteWhereException {
	return HBaseArea.getAreaByToken(context, token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getAreaChildren(java.lang.String)
     */
    @Override
    public List<IArea> getAreaChildren(String token) throws SiteWhereException {
	return null;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listAreas(com.sitewhere.spi.search
     * .area.IAreaSearchCriteria)
     */
    @Override
    public SearchResults<IArea> listAreas(IAreaSearchCriteria criteria) throws SiteWhereException {
	return HBaseArea.listAreas(context, criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#createZone(com.sitewhere.spi.area.
     * request.IZoneCreateRequest)
     */
    @Override
    public IZone createZone(IZoneCreateRequest request) throws SiteWhereException {
	return null;
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#updateZone(java.util.UUID,
     * com.sitewhere.spi.device.request.IZoneCreateRequest)
     */
    @Override
    public IZone updateZone(UUID id, IZoneCreateRequest request) throws SiteWhereException {
	IZone zone = getZone(id);
	return HBaseZone.updateZone(context, zone, request);
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#getZone(java.util.UUID)
     */
    @Override
    public IZone getZone(UUID id) throws SiteWhereException {
	return null;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getZoneByToken(java.lang.String)
     */
    @Override
    public IZone getZoneByToken(String zoneToken) throws SiteWhereException {
	return HBaseZone.getZone(context, zoneToken);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listZones(com.sitewhere.spi.search
     * .device.IZoneSearchCriteria)
     */
    @Override
    public SearchResults<IZone> listZones(IZoneSearchCriteria criteria) throws SiteWhereException {
	return null;
    }

    /*
     * @see com.sitewhere.spi.device.IDeviceManagement#deleteZone(java.util.UUID)
     */
    @Override
    public IZone deleteZone(UUID id) throws SiteWhereException {
	IZone zone = getZone(id);
	return HBaseZone.deleteZone(context, zone, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.device.IDeviceManagement#createDeviceGroup(com.
     * sitewhere.spi. device.request.IDeviceGroupCreateRequest)
     */
    @Override
    public IDeviceGroup createDeviceGroup(IDeviceGroupCreateRequest request) throws SiteWhereException {
	return HBaseDeviceGroup.createDeviceGroup(context, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#updateDeviceGroup(java.util.UUID,
     * com.sitewhere.spi.device.request.IDeviceGroupCreateRequest)
     */
    @Override
    public IDeviceGroup updateDeviceGroup(UUID id, IDeviceGroupCreateRequest request) throws SiteWhereException {
	IDeviceGroup group = getDeviceGroup(id);
	return HBaseDeviceGroup.updateDeviceGroup(context, group, request);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceGroup(java.util.UUID)
     */
    @Override
    public IDeviceGroup getDeviceGroup(UUID id) throws SiteWhereException {
	return null;
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#getDeviceGroupByToken(java.lang.
     * String)
     */
    @Override
    public IDeviceGroup getDeviceGroupByToken(String token) throws SiteWhereException {
	return HBaseDeviceGroup.getDeviceGroupByToken(context, token);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceGroups(com.sitewhere.spi
     * .search.ISearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceGroup> listDeviceGroups(ISearchCriteria criteria) throws SiteWhereException {
	return HBaseDeviceGroup.listDeviceGroups(context, false, criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceGroupsWithRole(java.lang
     * .String, com.sitewhere.spi.search.ISearchCriteria)
     */
    @Override
    public ISearchResults<IDeviceGroup> listDeviceGroupsWithRole(String role, ISearchCriteria criteria)
	    throws SiteWhereException {
	return HBaseDeviceGroup.listDeviceGroupsWithRole(context, role, false, criteria);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#deleteDeviceGroup(java.util.UUID)
     */
    @Override
    public IDeviceGroup deleteDeviceGroup(UUID id) throws SiteWhereException {
	IDeviceGroup group = getDeviceGroup(id);
	return HBaseDeviceGroup.deleteDeviceGroup(context, group, false);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#addDeviceGroupElements(java.util.
     * UUID, java.util.List, boolean)
     */
    @Override
    public List<IDeviceGroupElement> addDeviceGroupElements(UUID groupId,
	    List<IDeviceGroupElementCreateRequest> elements, boolean ignoreDuplicates) throws SiteWhereException {
	IDeviceGroup group = getDeviceGroup(groupId);
	return HBaseDeviceGroupElement.createDeviceGroupElements(context, group, elements, ignoreDuplicates);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#removeDeviceGroupElements(java.
     * util.List)
     */
    @Override
    public List<IDeviceGroupElement> removeDeviceGroupElements(List<UUID> elements) throws SiteWhereException {
	return HBaseDeviceGroupElement.removeDeviceGroupElements(context, null, null);
    }

    /*
     * @see
     * com.sitewhere.spi.device.IDeviceManagement#listDeviceGroupElements(java.util.
     * UUID, com.sitewhere.spi.search.ISearchCriteria)
     */
    @Override
    public SearchResults<IDeviceGroupElement> listDeviceGroupElements(UUID groupId, ISearchCriteria criteria)
	    throws SiteWhereException {
	IDeviceGroup group = getDeviceGroup(groupId);
	return HBaseDeviceGroupElement.listDeviceGroupElements(context, group, criteria);
    }

    /**
     * Verify that the given assignment exists.
     * 
     * @param token
     * @return
     * @throws SiteWhereException
     */
    protected IDeviceAssignment assertDeviceAssignment(String token) throws SiteWhereException {
	// IDeviceAssignment result = HBaseDeviceAssignment.getDeviceAssignment(context,
	// token);
	// if (result == null) {
	// throw new SiteWhereSystemException(ErrorCode.InvalidDeviceAssignmentToken,
	// ErrorLevel.ERROR);
	// }
	// return result;
	return null;
    }

    public ISiteWhereHBaseClient getClient() {
	return client;
    }

    public void setClient(ISiteWhereHBaseClient client) {
	this.client = client;
    }

    public IPayloadMarshaler getPayloadMarshaler() {
	return payloadMarshaler;
    }

    public void setPayloadMarshaler(IPayloadMarshaler payloadMarshaler) {
	this.payloadMarshaler = payloadMarshaler;
    }
}