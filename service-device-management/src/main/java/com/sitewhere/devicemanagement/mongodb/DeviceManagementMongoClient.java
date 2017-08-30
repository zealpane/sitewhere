package com.sitewhere.devicemanagement.mongodb;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.sitewhere.mongodb.BaseMongoClient;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.tenant.ITenant;

/**
 * Mongo client for interacting with device managment object model.
 * 
 * @author Derek
 */
public class DeviceManagementMongoClient extends BaseMongoClient implements IDeviceManagementMongoClient {

    /** Injected name used for device specifications collection */
    private String deviceSpecificationsCollectionName = IDeviceManagementMongoClient.DEFAULT_DEVICE_SPECIFICATIONS_COLLECTION_NAME;

    /** Injected name used for device commands collection */
    private String deviceCommandsCollectionName = IDeviceManagementMongoClient.DEFAULT_DEVICE_COMMANDS_COLLECTION_NAME;

    /** Injected name used for device statuses collection */
    private String deviceStatusesCollectionName = IDeviceManagementMongoClient.DEFAULT_DEVICE_STATUSES_COLLECTION_NAME;

    /** Injected name used for devices collection */
    private String devicesCollectionName = IDeviceManagementMongoClient.DEFAULT_DEVICES_COLLECTION_NAME;

    /** Injected name used for device assignments collection */
    private String deviceAssignmentsCollectionName = IDeviceManagementMongoClient.DEFAULT_DEVICE_ASSIGNMENTS_COLLECTION_NAME;

    /** Injected name used for sites collection */
    private String sitesCollectionName = IDeviceManagementMongoClient.DEFAULT_SITES_COLLECTION_NAME;

    /** Injected name used for zones collection */
    private String zonesCollectionName = IDeviceManagementMongoClient.DEFAULT_ZONES_COLLECTION_NAME;

    /** Injected name used for device groups collection */
    private String deviceGroupsCollectionName = IDeviceManagementMongoClient.DEFAULT_DEVICE_GROUPS_COLLECTION_NAME;

    /** Injected name used for group elements collection */
    private String groupElementsCollectionName = IDeviceManagementMongoClient.DEFAULT_DEVICE_GROUP_ELEMENTS_COLLECTION_NAME;

    /** Injected name used for device streams collection */
    private String streamsCollectionName = IDeviceManagementMongoClient.DEFAULT_DEVICE_STREAMS_COLLECTION_NAME;

    /** Injected name used for device stream data collection */
    private String streamDataCollectionName = IDeviceManagementMongoClient.DEFAULT_DEVICE_STREAM_DATA_COLLECTION_NAME;

    /** Injected name used for batch operations collection */
    private String batchOperationsCollectionName = IDeviceManagementMongoClient.DEFAULT_BATCH_OPERATIONS_COLLECTION_NAME;

    /** Injected name used for batch operation elements collection */
    private String batchOperationElementsCollectionName = IDeviceManagementMongoClient.DEFAULT_BATCH_OPERATION_ELEMENTS_COLLECTION_NAME;

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.IDeviceManagementMongoClient#
     * getDeviceSpecificationsCollection(com.sitewhere.spi.tenant.ITenant)
     */
    public MongoCollection<Document> getDeviceSpecificationsCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getDeviceSpecificationsCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.IDeviceManagementMongoClient#
     * getDeviceCommandsCollection( com.sitewhere.spi.user.ITenant)
     */
    public MongoCollection<Document> getDeviceCommandsCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getDeviceCommandsCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.IDeviceManagementMongoClient#
     * getDeviceStatusesCollection(com.sitewhere.spi.tenant.ITenant)
     */
    @Override
    public MongoCollection<Document> getDeviceStatusesCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getDeviceStatusesCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.mongodb.IDeviceManagementMongoClient#getDevicesCollection(
     * com. sitewhere .spi.user.ITenant)
     */
    public MongoCollection<Document> getDevicesCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getDevicesCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.IDeviceManagementMongoClient#
     * getDeviceAssignmentsCollection (com.sitewhere.spi.user.ITenant)
     */
    public MongoCollection<Document> getDeviceAssignmentsCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getDeviceAssignmentsCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.mongodb.IDeviceManagementMongoClient#getSitesCollection(com
     * .sitewhere .spi.user.ITenant)
     */
    public MongoCollection<Document> getSitesCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getSitesCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.mongodb.IDeviceManagementMongoClient#getZonesCollection(com
     * .sitewhere .spi.user.ITenant)
     */
    public MongoCollection<Document> getZonesCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getZonesCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.IDeviceManagementMongoClient#
     * getDeviceGroupsCollection(com .sitewhere.spi.user.ITenant)
     */
    public MongoCollection<Document> getDeviceGroupsCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getDeviceGroupsCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.IDeviceManagementMongoClient#
     * getGroupElementsCollection(com .sitewhere.spi.user.ITenant)
     */
    public MongoCollection<Document> getGroupElementsCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getGroupElementsCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.mongodb.IDeviceManagementMongoClient#getStreamsCollection(
     * com. sitewhere .spi.user.ITenant)
     */
    public MongoCollection<Document> getStreamsCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getStreamsCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.IDeviceManagementMongoClient#
     * getStreamDataCollection(com. sitewhere.spi.user.ITenant)
     */
    public MongoCollection<Document> getStreamDataCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getStreamDataCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.IDeviceManagementMongoClient#
     * getBatchOperationsCollection (com.sitewhere.spi.user.ITenant)
     */
    public MongoCollection<Document> getBatchOperationsCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getBatchOperationsCollectionName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.IDeviceManagementMongoClient#
     * getBatchOperationElementsCollection (com.sitewhere.spi.user.ITenant)
     */
    public MongoCollection<Document> getBatchOperationElementsCollection(ITenant tenant) throws SiteWhereException {
	return getTenantDatabase(tenant).getCollection(getBatchOperationElementsCollectionName());
    }

    public String getDeviceSpecificationsCollectionName() {
	return deviceSpecificationsCollectionName;
    }

    public void setDeviceSpecificationsCollectionName(String deviceSpecificationsCollectionName) {
	this.deviceSpecificationsCollectionName = deviceSpecificationsCollectionName;
    }

    public String getDeviceCommandsCollectionName() {
	return deviceCommandsCollectionName;
    }

    public void setDeviceCommandsCollectionName(String deviceCommandsCollectionName) {
	this.deviceCommandsCollectionName = deviceCommandsCollectionName;
    }

    public String getDeviceStatusesCollectionName() {
	return deviceStatusesCollectionName;
    }

    public void setDeviceStatusesCollectionName(String deviceStatusesCollectionName) {
	this.deviceStatusesCollectionName = deviceStatusesCollectionName;
    }

    public String getDevicesCollectionName() {
	return devicesCollectionName;
    }

    public void setDevicesCollectionName(String devicesCollectionName) {
	this.devicesCollectionName = devicesCollectionName;
    }

    public String getDeviceGroupsCollectionName() {
	return deviceGroupsCollectionName;
    }

    public void setDeviceGroupsCollectionName(String deviceGroupsCollectionName) {
	this.deviceGroupsCollectionName = deviceGroupsCollectionName;
    }

    public String getGroupElementsCollectionName() {
	return groupElementsCollectionName;
    }

    public void setGroupElementsCollectionName(String groupElementsCollectionName) {
	this.groupElementsCollectionName = groupElementsCollectionName;
    }

    public String getDeviceAssignmentsCollectionName() {
	return deviceAssignmentsCollectionName;
    }

    public void setDeviceAssignmentsCollectionName(String deviceAssignmentsCollectionName) {
	this.deviceAssignmentsCollectionName = deviceAssignmentsCollectionName;
    }

    public String getSitesCollectionName() {
	return sitesCollectionName;
    }

    public void setSitesCollectionName(String sitesCollectionName) {
	this.sitesCollectionName = sitesCollectionName;
    }

    public String getZonesCollectionName() {
	return zonesCollectionName;
    }

    public void setZonesCollectionName(String zonesCollectionName) {
	this.zonesCollectionName = zonesCollectionName;
    }

    public String getStreamsCollectionName() {
	return streamsCollectionName;
    }

    public void setStreamsCollectionName(String streamsCollectionName) {
	this.streamsCollectionName = streamsCollectionName;
    }

    public String getStreamDataCollectionName() {
	return streamDataCollectionName;
    }

    public void setStreamDataCollectionName(String streamDataCollectionName) {
	this.streamDataCollectionName = streamDataCollectionName;
    }

    public String getBatchOperationsCollectionName() {
	return batchOperationsCollectionName;
    }

    public void setBatchOperationsCollectionName(String batchOperationsCollectionName) {
	this.batchOperationsCollectionName = batchOperationsCollectionName;
    }

    public String getBatchOperationElementsCollectionName() {
	return batchOperationElementsCollectionName;
    }

    public void setBatchOperationElementsCollectionName(String batchOperationElementsCollectionName) {
	this.batchOperationElementsCollectionName = batchOperationElementsCollectionName;
    }
}