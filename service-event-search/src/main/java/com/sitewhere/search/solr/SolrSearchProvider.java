/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.search.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitewhere.configuration.instance.solr.SolrConfiguration;
import com.sitewhere.server.lifecycle.LifecycleComponent;
import com.sitewhere.solr.SiteWhereSolrFactory;
import com.sitewhere.solr.SolrConnection;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.event.IDeviceEvent;
import com.sitewhere.spi.device.event.IDeviceLocation;
import com.sitewhere.spi.search.IDateRangeSearchCriteria;
import com.sitewhere.spi.search.IDeviceEventSearchProvider;
import com.sitewhere.spi.search.ISearchProvider;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;
import com.sitewhere.spi.server.lifecycle.LifecycleComponentType;

/**
 * Implementation of {@link ISearchProvider} that executes queries against a
 * Solr server.
 * 
 * @author Derek
 */
public class SolrSearchProvider extends LifecycleComponent implements IDeviceEventSearchProvider {

    /** Name returned for provider */
    private static final String DEFAULT_NAME = "Solr Search Provider";

    /** Provider id */
    private String id;

    /** Provider name */
    private String name = DEFAULT_NAME;

    /** For JSON marshaling */
    private static ObjectMapper MAPPER = new ObjectMapper();

    /** Injected Solr configuration */
    private SolrConfiguration solrConfiguration;

    /** Connection to Solr instance */
    private SolrConnection solrConnection;

    public SolrSearchProvider() {
	super(LifecycleComponentType.SearchProvider);
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
	if (getSolrConfiguration() == null) {
	    throw new SiteWhereException("No Solr configuration provided.");
	}
	try {
	    // Create and start Solr connection.
	    this.solrConnection = new SolrConnection(getSolrConfiguration());
	    getSolrConnection().start(monitor);

	    getLogger().info("Attempting to ping Solr server to verify availability...");
	    SolrPingResponse response = getSolrConnection().getSolrClient().ping();
	    int pingTime = response.getQTime();
	    getLogger().info("Solr server location verified. Ping responded in " + pingTime + " ms.");
	} catch (SolrServerException e) {
	    throw new SiteWhereException("Ping failed. Verify that Solr server is available.", e);
	} catch (IOException e) {
	    throw new SiteWhereException("Exception in ping. Verify that Solr server is available.", e);
	}
    }

    /**
     * Create Solr parameters from an arbitrary query string.
     * 
     * @param queryString
     * @return
     */
    protected SolrParams createParamsFromQueryString(String queryString) {
	MultiValueMap<String, String> parsed = UriComponentsBuilder.fromHttpUrl("http://localhost?" + queryString)
		.build().getQueryParams();
	Map<String, String[]> params = new HashMap<String, String[]>();
	for (String key : parsed.keySet()) {
	    params.put(key, parsed.get(key).toArray(new String[0]));
	}
	return new ModifiableSolrParams(params);
    }

    /*
     * @see
     * com.sitewhere.spi.search.IDeviceEventSearchProvider#executeQuery(java.lang.
     * String)
     */
    @Override
    public List<IDeviceEvent> executeQuery(String queryString) throws SiteWhereException {
	try {
	    getLogger().debug("About to execute Solr search with query string: " + queryString);
	    List<IDeviceEvent> results = new ArrayList<IDeviceEvent>();
	    SolrQuery solrQuery = new SolrQuery();
	    solrQuery.setQuery(queryString);
	    QueryResponse response = getSolrConnection().getSolrClient().query(solrQuery);
	    SolrDocumentList docs = response.getResults();
	    for (SolrDocument doc : docs) {
		results.add(SiteWhereSolrFactory.parseDocument(doc));
	    }
	    return results;
	} catch (SolrServerException e) {
	    throw new SiteWhereException("Unable to execute query.", e);
	} catch (IOException e) {
	    throw new SiteWhereException("Unable to execute query.", e);
	}
    }

    /*
     * @see com.sitewhere.spi.search.IDeviceEventSearchProvider#
     * executeQueryWithRawResponse(java.lang.String)
     */
    @Override
    public JsonNode executeQueryWithRawResponse(String queryString) throws SiteWhereException {
	try {
	    getLogger().debug("About to execute Solr search with query string: " + queryString);

	    NoOpResponseParser rawJsonResponseParser = new NoOpResponseParser();
	    rawJsonResponseParser.setWriterType("json");

	    SolrQuery query = new SolrQuery();
	    query.add(createParamsFromQueryString(queryString));
	    QueryRequest request = new QueryRequest(query);
	    request.setResponseParser(rawJsonResponseParser);
	    NamedList<?> results = getSolrConnection().getSolrClient().request(request);
	    return MAPPER.readTree((String) results.get("response"));
	} catch (SolrServerException e) {
	    throw new SiteWhereException("Unable to execute query.", e);
	} catch (IOException e) {
	    throw new SiteWhereException("Unable to execute query.", e);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.search.IDeviceEventSearchProvider#getLocationsNear(double,
     * double, double, com.sitewhere.spi.search.IDateRangeSearchCriteria)
     */
    @Override
    public List<IDeviceLocation> getLocationsNear(double latitude, double longitude, double distance,
	    IDateRangeSearchCriteria criteria) throws SiteWhereException {
	ModifiableSolrParams params = new ModifiableSolrParams();
	try {
	    QueryResponse response = getSolrConnection().getSolrClient().query(params);
	    SolrDocumentList docs = response.getResults();
	    while (docs.iterator().hasNext()) {
		SolrDocument doc = docs.iterator().next();
		doc.getFieldNames();
	    }
	    List<IDeviceLocation> results = new ArrayList<IDeviceLocation>();
	    return results;
	} catch (SolrServerException e) {
	    throw new SiteWhereException("Unable to execute 'getLocationsNear' query.", e);
	} catch (IOException e) {
	    throw new SiteWhereException("Unable to execute query.", e);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.search.external.ISearchProvider#getId()
     */
    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.search.external.ISearchProvider#getName()
     */
    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public SolrConfiguration getSolrConfiguration() {
	return solrConfiguration;
    }

    public void setSolrConfiguration(SolrConfiguration solrConfiguration) {
	this.solrConfiguration = solrConfiguration;
    }

    public SolrConnection getSolrConnection() {
	return solrConnection;
    }

    public void setSolrConnection(SolrConnection solrConnection) {
	this.solrConnection = solrConnection;
    }
}