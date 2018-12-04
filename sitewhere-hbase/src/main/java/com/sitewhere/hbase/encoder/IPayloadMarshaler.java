/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.hbase.encoder;

import com.sitewhere.rest.model.area.Area;
import com.sitewhere.rest.model.area.Zone;
import com.sitewhere.rest.model.batch.BatchElement;
import com.sitewhere.rest.model.batch.BatchOperation;
import com.sitewhere.rest.model.device.Device;
import com.sitewhere.rest.model.device.DeviceAssignment;
import com.sitewhere.rest.model.device.DeviceType;
import com.sitewhere.rest.model.device.command.DeviceCommand;
import com.sitewhere.rest.model.device.event.DeviceAlert;
import com.sitewhere.rest.model.device.event.DeviceCommandInvocation;
import com.sitewhere.rest.model.device.event.DeviceCommandResponse;
import com.sitewhere.rest.model.device.event.DeviceLocation;
import com.sitewhere.rest.model.device.event.DeviceMeasurement;
import com.sitewhere.rest.model.device.event.DeviceStateChange;
import com.sitewhere.rest.model.device.group.DeviceGroup;
import com.sitewhere.rest.model.device.group.DeviceGroupElement;
import com.sitewhere.rest.model.device.state.DeviceState;
import com.sitewhere.rest.model.device.streaming.DeviceStream;
import com.sitewhere.rest.model.device.streaming.DeviceStreamData;
import com.sitewhere.rest.model.tenant.Tenant;
import com.sitewhere.rest.model.user.GrantedAuthority;
import com.sitewhere.rest.model.user.User;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.area.IArea;
import com.sitewhere.spi.area.IZone;
import com.sitewhere.spi.batch.IBatchElement;
import com.sitewhere.spi.batch.IBatchOperation;
import com.sitewhere.spi.device.IDevice;
import com.sitewhere.spi.device.IDeviceAssignment;
import com.sitewhere.spi.device.IDeviceType;
import com.sitewhere.spi.device.command.IDeviceCommand;
import com.sitewhere.spi.device.event.IDeviceAlert;
import com.sitewhere.spi.device.event.IDeviceCommandInvocation;
import com.sitewhere.spi.device.event.IDeviceCommandResponse;
import com.sitewhere.spi.device.event.IDeviceLocation;
import com.sitewhere.spi.device.event.IDeviceMeasurement;
import com.sitewhere.spi.device.event.IDeviceStateChange;
import com.sitewhere.spi.device.group.IDeviceGroup;
import com.sitewhere.spi.device.group.IDeviceGroupElement;
import com.sitewhere.spi.device.state.IDeviceState;
import com.sitewhere.spi.device.streaming.IDeviceStream;
import com.sitewhere.spi.device.streaming.IDeviceStreamData;
import com.sitewhere.spi.tenant.ITenant;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IUser;

/**
 * Interface for classes that can encode SiteWhere objects into byte arrays.
 * 
 * @author Derek
 */
public interface IPayloadMarshaler {

    /**
     * Gets encoding type for the encoder.
     * 
     * @return
     * @throws SiteWhereException
     */
    public PayloadEncoding getEncoding() throws SiteWhereException;

    /**
     * Encode an object.
     * 
     * @param obj
     * @return
     * @throws SiteWhereException
     */
    public byte[] encode(Object obj) throws SiteWhereException;

    /**
     * Decode a payload into an object.
     * 
     * @param payload
     * @param type
     * @return
     * @throws SiteWhereException
     */
    public <T> T decode(byte[] payload, Class<T> type) throws SiteWhereException;

    /**
     * Encode an {@link IArea}.
     * 
     * @param site
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeArea(IArea area) throws SiteWhereException;

    /**
     * Decode an {@link Area} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public Area decodeArea(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IZone}.
     * 
     * @param zone
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeZone(IZone zone) throws SiteWhereException;

    /**
     * Decode a {@link Zone} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public Zone decodeZone(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceType}.
     * 
     * @param deviceType
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceType(IDeviceType deviceType) throws SiteWhereException;

    /**
     * Decode a {@link DeviceType} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceType decodeDeviceType(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDevice}.
     * 
     * @param device
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDevice(IDevice device) throws SiteWhereException;

    /**
     * Decodea {@link Device} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public Device decodeDevice(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceAssignment}.
     * 
     * @param assignment
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceAssignment(IDeviceAssignment assignment) throws SiteWhereException;

    /**
     * Decode a {@link DeviceAssignment} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceAssignment decodeDeviceAssignment(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceStream}.
     * 
     * @param stream
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceStream(IDeviceStream stream) throws SiteWhereException;

    /**
     * Decode a {@link DeviceStream} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceStream decodeDeviceStream(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceState}.
     * 
     * @param state
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceAssignmentState(IDeviceState state) throws SiteWhereException;

    /**
     * Decode a {@link DeviceState} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceState decodeDeviceAssignmentState(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceMeasurement}.
     * 
     * @param measurements
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceMeasurements(IDeviceMeasurement measurements) throws SiteWhereException;

    /**
     * Decode a {@link DeviceMeasurement} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceMeasurement decodeDeviceMeasurements(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceLocation}.
     * 
     * @param location
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceLocation(IDeviceLocation location) throws SiteWhereException;

    /**
     * Decode a {@link DeviceLocation} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceLocation decodeDeviceLocation(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceAlert}.
     * 
     * @param alert
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceAlert(IDeviceAlert alert) throws SiteWhereException;

    /**
     * Decode a {@link DeviceAlert} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceAlert decodeDeviceAlert(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceStreamData}.
     * 
     * @param streamData
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceStreamData(IDeviceStreamData streamData) throws SiteWhereException;

    /**
     * Decode a {@link DeviceStreamData} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceStreamData decodeDeviceStreamData(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceCommandInvocation}.
     * 
     * @param invocation
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceCommandInvocation(IDeviceCommandInvocation invocation) throws SiteWhereException;

    /**
     * Decode a {@link DeviceCommandInvocation} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceCommandInvocation decodeDeviceCommandInvocation(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceStateChange}.
     * 
     * @param change
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceStateChange(IDeviceStateChange change) throws SiteWhereException;

    /**
     * Decode a {@link DeviceStateChange} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceStateChange decodeDeviceStateChange(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceCommandResponse}.
     * 
     * @param response
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceCommandResponse(IDeviceCommandResponse response) throws SiteWhereException;

    /**
     * Decode a {@link DeviceCommandResponse} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceCommandResponse decodeDeviceCommandResponse(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IBatchOperation}.
     * 
     * @param operation
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeBatchOperation(IBatchOperation operation) throws SiteWhereException;

    /**
     * Decode a {@link BatchOperation} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public BatchOperation decodeBatchOperation(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IBatchElement}.
     * 
     * @param element
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeBatchElement(IBatchElement element) throws SiteWhereException;

    /**
     * Decode a {@link BatchElement} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public BatchElement decodeBatchElement(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceGroup}.
     * 
     * @param group
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceGroup(IDeviceGroup group) throws SiteWhereException;

    /**
     * Decode a {@link DeviceGroup} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceGroup decodeDeviceGroup(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceGroupElement}.
     * 
     * @param element
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceGroupElement(IDeviceGroupElement element) throws SiteWhereException;

    /**
     * Decode a {@link DeviceGroupElement} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceGroupElement decodeDeviceGroupElement(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IDeviceCommand}.
     * 
     * @param command
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeDeviceCommand(IDeviceCommand command) throws SiteWhereException;

    /**
     * Decode a {@link DeviceCommand} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public DeviceCommand decodeDeviceCommand(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IUser}.
     * 
     * @param user
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeUser(IUser user) throws SiteWhereException;

    /**
     * Decode a {@link User} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public User decodeUser(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link IGrantedAuthority}.
     * 
     * @param auth
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeGrantedAuthority(IGrantedAuthority auth) throws SiteWhereException;

    /**
     * Decode a {@link GrantedAuthority} from the binary payload.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public GrantedAuthority decodeGrantedAuthority(byte[] payload) throws SiteWhereException;

    /**
     * Encode an {@link ITenant}.
     * 
     * @param tenant
     * @return
     * @throws SiteWhereException
     */
    public byte[] encodeTenant(ITenant tenant) throws SiteWhereException;

    /**
     * Deocde a {@link Tenant}.
     * 
     * @param payload
     * @return
     * @throws SiteWhereException
     */
    public Tenant decodeTenant(byte[] payload) throws SiteWhereException;
}