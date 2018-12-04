/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.device.persistence.hbase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import com.sitewhere.hbase.IHBaseContext;
import com.sitewhere.hbase.ISiteWhereHBase;
import com.sitewhere.hbase.common.HBaseUtils;
import com.sitewhere.hbase.encoder.PayloadMarshalerResolver;
import com.sitewhere.rest.model.device.Device;
import com.sitewhere.rest.model.search.Pager;
import com.sitewhere.rest.model.search.SearchResults;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.SiteWhereSystemException;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.request.IDeviceCreateRequest;
import com.sitewhere.spi.error.ErrorCode;
import com.sitewhere.spi.error.ErrorLevel;
import com.sitewhere.spi.search.ISearchCriteria;
import com.sitewhere.spi.search.device.IDeviceSearchCriteria;

/**
 * HBase specifics for dealing with SiteWhere devices.
 * 
 * @author Derek
 */
public class HBaseDevice {

    /** Static logger instance */
    @SuppressWarnings("unused")
    private static Log LOGGER = LogFactory.getLog(HBaseDevice.class);

    /** Length of device identifier (subset of 8 byte long) */
    public static final int DEVICE_IDENTIFIER_LENGTH = 4;

    /** Byte that indicates an assignment history entry qualifier */
    public static final byte ASSIGNMENT_HISTORY_INDICATOR = (byte) 0x01;

    /** Column qualifier for current site */
    public static final byte[] CURRENT_SITE = "site".getBytes();

    /** Column qualifier for current device assignment */
    public static final byte[] CURRENT_ASSIGNMENT = "assn".getBytes();

    /**
     * Create a new device.
     * 
     * @param context
     * @param request
     * @return
     * @throws SiteWhereException
     */
    public static IDevice createDevice(IHBaseContext context, IDeviceCreateRequest request) throws SiteWhereException {
	// Long existing =
	// context.getDeviceIdManager().getDeviceKeys().getValue(request.getHardwareId());
	// if (existing != null) {
	// throw new SiteWhereSystemException(ErrorCode.DuplicateHardwareId,
	// ErrorLevel.ERROR);
	// }
	// Long value =
	// context.getDeviceIdManager().getDeviceKeys().getNextCounterValue();
	// Long inverse = Long.MAX_VALUE - value;
	// context.getDeviceIdManager().getDeviceKeys().create(request.getHardwareId(),
	// inverse);
	//
	// Device device = DeviceManagementPersistence.deviceCreateLogic(request);
	// return putDevicePayload(context, device);
	return null;
    }

    /**
     * Update an existing device.
     * 
     * @param context
     * @param hardwareId
     * @param request
     * @return
     * @throws SiteWhereException
     */
    public static IDevice updateDevice(IHBaseContext context, IDevice device, IDeviceCreateRequest request)
	    throws SiteWhereException {
	Device updated = getDeviceByToken(context, device.getToken());
	if (updated == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceId, ErrorLevel.ERROR);
	}
	// DeviceManagementPersistence.deviceUpdateLogic(request, updated);
	return putDevicePayload(context, updated);
    }

    /**
     * List devices that meet the given criteria.
     * 
     * @param context
     * @param includeDeleted
     * @param criteria
     * @return
     * @throws SiteWhereException
     */
    public static SearchResults<IDevice> listDevices(IHBaseContext context, boolean includeDeleted,
	    IDeviceSearchCriteria criteria) throws SiteWhereException {
	Pager<IDevice> matches = getFilteredDevices(context, includeDeleted, criteria);
	return new SearchResults<IDevice>(matches.getResults(), matches.getTotal());
    }

    /**
     * Get a list of devices filtered with certain criteria.
     * 
     * @param context
     * @param includeDeleted
     * @param criteria
     * @return
     * @throws SiteWhereException
     */
    protected static Pager<IDevice> getFilteredDevices(IHBaseContext context, boolean includeDeleted,
	    IDeviceSearchCriteria criteria) throws SiteWhereException {
	Table devices = null;
	ResultScanner scanner = null;

	try {
	    devices = getDeviceTableInterface(context);
	    Scan scan = new Scan();
	    scan.setStartRow(new byte[] { DeviceRecordType.Device.getType() });
	    scan.setStopRow(new byte[] { DeviceRecordType.DeviceSpecification.getType() });
	    scanner = devices.getScanner(scan);

	    Pager<IDevice> pager = new Pager<IDevice>(criteria);
	    for (Result result : scanner) {
		boolean shouldAdd = true;
		byte[] payload = result.getValue(ISiteWhereHBase.FAMILY_ID, ISiteWhereHBase.PAYLOAD);
		byte[] deleted = result.getValue(ISiteWhereHBase.FAMILY_ID, ISiteWhereHBase.DELETED);
		byte[] currAssn = result.getValue(ISiteWhereHBase.FAMILY_ID, CURRENT_ASSIGNMENT);

		if ((deleted != null) && (!includeDeleted)) {
		    shouldAdd = false;
		}
		if ((currAssn != null) && (criteria.isExcludeAssigned())) {
		    shouldAdd = false;
		}

		if ((shouldAdd) && (payload != null)) {
		    Device device = context.getPayloadMarshaler().decodeDevice(payload);

		    // Filter by device type.
		    if (criteria.getDeviceTypeToken() != null) {
			if (!criteria.getDeviceTypeToken().equals(device.getToken())) {
			    continue;
			}
		    }

		    pager.process(device);
		}
	    }
	    return pager;
	} catch (IOException e) {
	    throw new SiteWhereException("Error scanning device rows.", e);
	} finally {
	    if (scanner != null) {
		scanner.close();
	    }
	    HBaseUtils.closeCleanly(devices);
	}
    }

    /**
     * Save the payload for a device.
     * 
     * @param context
     * @param device
     * @return
     * @throws SiteWhereException
     */
    public static Device putDevicePayload(IHBaseContext context, Device device) throws SiteWhereException {
	Long value = context.getDeviceIdManager().getDeviceKeys().getValue(device.getToken());
	if (value == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceId, ErrorLevel.ERROR);
	}
	byte[] primary = getDeviceRowKey(value);
	byte[] payload = context.getPayloadMarshaler().encodeDevice(device);

	Table devices = null;
	try {
	    devices = getDeviceTableInterface(context);
	    Put put = new Put(primary);
	    HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
	    devices.put(put);
	} catch (IOException e) {
	    throw new SiteWhereException("Unable to put device data.", e);
	} finally {
	    HBaseUtils.closeCleanly(devices);
	}

	return device;
    }

    /**
     * Get a device by deviceToken.
     * 
     * @param context
     * @param deviceToken
     * @return
     * @throws SiteWhereException
     */
    public static Device getDeviceByToken(IHBaseContext context, String deviceToken) throws SiteWhereException {
	Long deviceId = context.getDeviceIdManager().getDeviceKeys().getValue(deviceToken);
	if (deviceId == null) {
	    return null;
	}

	// Find row key based on value associated with hardware id.
	byte[] primary = getDeviceRowKey(deviceId);

	Table devices = null;
	try {
	    devices = getDeviceTableInterface(context);
	    Get get = new Get(primary);
	    HBaseUtils.addPayloadFields(get);
	    Result result = devices.get(get);

	    byte[] type = result.getValue(ISiteWhereHBase.FAMILY_ID, ISiteWhereHBase.PAYLOAD_TYPE);
	    byte[] payload = result.getValue(ISiteWhereHBase.FAMILY_ID, ISiteWhereHBase.PAYLOAD);
	    if ((type == null) || (payload == null)) {
		return null;
	    }

	    return PayloadMarshalerResolver.getInstance().getMarshaler(type).decodeDevice(payload);
	} catch (IOException e) {
	    throw new SiteWhereException("Unable to load device by hardware id.", e);
	} finally {
	    HBaseUtils.closeCleanly(devices);
	}
    }

    /**
     * Delete a device based on hardware id. Depending on 'force' the record will be
     * physically deleted or a marker qualifier will be added to mark it as deleted.
     * Note: Physically deleting a device can leave orphaned references and should
     * not be done in a production system!
     * 
     * @param context
     * @param device
     * @param force
     * @return
     * @throws SiteWhereException
     */
    public static IDevice deleteDevice(IHBaseContext context, IDevice device, boolean force) throws SiteWhereException {
	Long deviceId = context.getDeviceIdManager().getDeviceKeys().getValue(device.getToken());
	if (deviceId == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceId, ErrorLevel.ERROR);
	}

	Device existing = getDeviceByToken(context, device.getToken());
	byte[] primary = getDeviceRowKey(deviceId);
	if (force) {
	    context.getDeviceIdManager().getDeviceKeys().delete(device.getToken());
	    Table devices = null;
	    try {
		Delete delete = new Delete(primary);
		devices = getDeviceTableInterface(context);
		devices.delete(delete);
	    } catch (IOException e) {
		throw new SiteWhereException("Unable to delete device.", e);
	    } finally {
		HBaseUtils.closeCleanly(devices);
	    }
	} else {
	    byte[] marker = { (byte) 0x01 };
	    byte[] updated = context.getPayloadMarshaler().encodeDevice(existing);

	    Table devices = null;
	    try {
		devices = getDeviceTableInterface(context);
		Put put = new Put(primary);
		put.addColumn(ISiteWhereHBase.FAMILY_ID, ISiteWhereHBase.PAYLOAD_TYPE,
			context.getPayloadMarshaler().getEncoding().getIndicator());
		put.addColumn(ISiteWhereHBase.FAMILY_ID, ISiteWhereHBase.PAYLOAD, updated);
		put.addColumn(ISiteWhereHBase.FAMILY_ID, ISiteWhereHBase.DELETED, marker);
		devices.put(put);
	    } catch (IOException e) {
		throw new SiteWhereException("Unable to set deleted flag for device.", e);
	    } finally {
		HBaseUtils.closeCleanly(devices);
	    }
	}
	return existing;
    }

    /**
     * Get the current device assignment id if assigned or null if not assigned.
     * 
     * @param context
     * @param device
     * @return
     * @throws SiteWhereException
     */
    public static String getCurrentAssignmentId(IHBaseContext context, IDevice device) throws SiteWhereException {
	Long deviceId = context.getDeviceIdManager().getDeviceKeys().getValue(device.getToken());
	if (deviceId == null) {
	    return null;
	}
	byte[] primary = getDeviceRowKey(deviceId);

	Table devices = null;
	try {
	    devices = getDeviceTableInterface(context);
	    Get get = new Get(primary);
	    get.addColumn(ISiteWhereHBase.FAMILY_ID, CURRENT_ASSIGNMENT);
	    Result result = devices.get(get);
	    if (result.isEmpty()) {
		return null;
	    } else if (result.size() == 1) {
		return new String(result.value());
	    } else {
		throw new SiteWhereException(
			"Expected one current assignment entry for device and found: " + result.size());
	    }
	} catch (IOException e) {
	    throw new SiteWhereException("Unable to load current device assignment value.", e);
	} finally {
	    HBaseUtils.closeCleanly(devices);
	}
    }

    /**
     * Set the current device assignment for a device.
     * 
     * @param context
     * @param device
     * @param assignmentToken
     * @throws SiteWhereException
     */
    public static void setDeviceAssignment(IHBaseContext context, IDevice device, String assignmentToken)
	    throws SiteWhereException {
	String existing = getCurrentAssignmentId(context, device);
	if (existing != null) {
	    throw new SiteWhereSystemException(ErrorCode.DeviceAlreadyAssigned, ErrorLevel.ERROR);
	}

	// Load object to update assignment token.
	Device updated = getDeviceByToken(context, device.getToken());
	// updated.setAssignmentToken(assignmentToken);
	byte[] payload = context.getPayloadMarshaler().encodeDevice(updated);

	Long deviceId = context.getDeviceIdManager().getDeviceKeys().getValue(device.getToken());
	if (deviceId == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceId, ErrorLevel.ERROR);
	}
	byte[] primary = getDeviceRowKey(deviceId);
	byte[] assnHistory = getNextDeviceAssignmentHistoryKey();

	Table devices = null;
	try {
	    devices = getDeviceTableInterface(context);
	    Put put = new Put(primary);
	    HBaseUtils.addPayloadFields(context.getPayloadMarshaler().getEncoding(), put, payload);
	    put.addColumn(ISiteWhereHBase.FAMILY_ID, CURRENT_ASSIGNMENT, assignmentToken.getBytes());
	    put.addColumn(ISiteWhereHBase.FAMILY_ID, assnHistory, assignmentToken.getBytes());
	    devices.put(put);
	} catch (IOException e) {
	    throw new SiteWhereException("Unable to set device assignment.", e);
	} finally {
	    HBaseUtils.closeCleanly(devices);
	}
    }

    /**
     * Removes the device assignment row if present.
     * 
     * @param context
     * @param hardwareId
     * @throws SiteWhereException
     */
    public static void removeDeviceAssignment(IHBaseContext context, String hardwareId) throws SiteWhereException {
	Long deviceId = context.getDeviceIdManager().getDeviceKeys().getValue(hardwareId);
	if (deviceId == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceId, ErrorLevel.ERROR);
	}
	byte[] primary = getDeviceRowKey(deviceId);

	Device updated = getDeviceByToken(context, hardwareId);
	updated.setDeviceAssignmentId(null);
	byte[] payload = context.getPayloadMarshaler().encodeDevice(updated);

	Table devices = null;
	try {
	    devices = getDeviceTableInterface(context);
	    Put put = new Put(primary);
	    put.addColumn(ISiteWhereHBase.FAMILY_ID, ISiteWhereHBase.PAYLOAD_TYPE,
		    context.getPayloadMarshaler().getEncoding().getIndicator());
	    put.addColumn(ISiteWhereHBase.FAMILY_ID, ISiteWhereHBase.PAYLOAD, payload);
	    devices.put(put);
	    Delete delete = new Delete(primary);
	    delete.addColumn(ISiteWhereHBase.FAMILY_ID, CURRENT_ASSIGNMENT);
	    devices.delete(delete);
	} catch (IOException e) {
	    throw new SiteWhereException("Unable to remove device assignment.", e);
	} finally {
	    HBaseUtils.closeCleanly(devices);
	}
    }

    /**
     * Get the assignment history for a device.
     * 
     * @param context
     * @param device
     * @param criteria
     * @return
     * @throws SiteWhereException
     */
    public static SearchResults<IDeviceAssignment> getDeviceAssignmentHistory(IHBaseContext context, IDevice device,
	    ISearchCriteria criteria) throws SiteWhereException {
	Long deviceId = context.getDeviceIdManager().getDeviceKeys().getValue(device.getToken());
	if (deviceId == null) {
	    throw new SiteWhereSystemException(ErrorCode.InvalidDeviceId, ErrorLevel.ERROR);
	}
	byte[] primary = getDeviceRowKey(deviceId);

	Table devices = null;
	try {
	    devices = getDeviceTableInterface(context);
	    Get get = new Get(primary);
	    Result result = devices.get(get);

	    Map<byte[], byte[]> map = result.getFamilyMap(ISiteWhereHBase.FAMILY_ID);
	    Pager<String> pager = new Pager<String>(criteria);
	    for (byte[] qualifier : map.keySet()) {
		if (qualifier[0] == ASSIGNMENT_HISTORY_INDICATOR) {
		    byte[] value = map.get(qualifier);
		    pager.process(new String(value));
		}
	    }
	    List<IDeviceAssignment> results = new ArrayList<IDeviceAssignment>();
	    // for (String token : pager.getResults()) {
	    // DeviceAssignment assn = HBaseDeviceAssignment.getDeviceAssignment(context,
	    // token);
	    // results.add(assn);
	    // }
	    return new SearchResults<IDeviceAssignment>(results, pager.getTotal());
	} catch (IOException e) {
	    throw new SiteWhereException("Unable to load current device assignment history.", e);
	} finally {
	    HBaseUtils.closeCleanly(devices);
	}
    }

    /**
     * Get the unique device identifier based on the long value associated with the
     * device UUID. This will be a subset of the full 8-bit long value.
     * 
     * @param value
     * @return
     */
    public static byte[] getTruncatedIdentifier(Long value) {
	byte[] bytes = Bytes.toBytes(value);
	byte[] result = new byte[DEVICE_IDENTIFIER_LENGTH];
	System.arraycopy(bytes, bytes.length - DEVICE_IDENTIFIER_LENGTH, result, 0, DEVICE_IDENTIFIER_LENGTH);
	return result;
    }

    /**
     * Get row key for a device with the given id.
     * 
     * @param deviceId
     * @return
     */
    public static byte[] getDeviceRowKey(Long deviceId) {
	ByteBuffer buffer = ByteBuffer.allocate(DEVICE_IDENTIFIER_LENGTH + 1);
	buffer.put(DeviceRecordType.Device.getType());
	buffer.put(getTruncatedIdentifier(deviceId));
	return buffer.array();
    }

    /**
     * Creates key with an indicator byte followed by the inverted timestamp to
     * order assignments in most recent to least recent order.
     * 
     * @return
     */
    public static byte[] getNextDeviceAssignmentHistoryKey() {
	long time = System.currentTimeMillis() / 1000;
	byte[] timeBytes = Bytes.toBytes(time);
	ByteBuffer buffer = ByteBuffer.allocate(5);
	buffer.put(ASSIGNMENT_HISTORY_INDICATOR);
	buffer.put((byte) ~timeBytes[4]);
	buffer.put((byte) ~timeBytes[5]);
	buffer.put((byte) ~timeBytes[6]);
	buffer.put((byte) ~timeBytes[7]);
	return buffer.array();
    }

    /**
     * Get device table based on context.
     * 
     * @param context
     * @return
     * @throws SiteWhereException
     */
    protected static Table getDeviceTableInterface(IHBaseContext context) throws SiteWhereException {
	return context.getClient().getTableInterface(context.getTenant(), ISiteWhereHBase.DEVICES_TABLE_NAME);
    }
}