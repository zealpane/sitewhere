/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.media.microservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sitewhere.media.spi.microservice.IStreamingMediaMicroservice;
import com.sitewhere.microservice.instance.InstanceSettings;
import com.sitewhere.spi.microservice.instance.IInstanceSettings;

/**
 * Spring bean configuration for microservice.
 * 
 * @author Derek
 */
@Configuration
public class StreamingMediaMicroserviceConfiguration {

    @Bean
    public IStreamingMediaMicroservice eventSearchMicroservice() {
	return new StreamingMediaMicroservice();
    }

    @Bean
    public IInstanceSettings instanceSettings() {
	return new InstanceSettings();
    }
}