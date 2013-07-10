/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.droidium.native_.selendroid.impl;

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.arquillian.droidium.native_.selendroid.exception.SelendroidConfigurationException;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

/**
 * Configuration of Selendroid parsed from arquillian.xml.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidConfiguration implements DroneConfiguration<SelendroidConfiguration> {

    private static final Logger logger = Logger.getLogger(SelendroidConfiguration.class.getName());

    private static final String CONFIGURATION_NAME = "selendroid";

    private String remoteAddress = "http://localhost:8080/wd/hub";

    @Override
    public SelendroidConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier) {

        SelendroidConfiguration configuration = ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
        try {
            SelendroidConfigurationValidator.validate(configuration);
        } catch (SelendroidConfigurationException ex) {
            logger.severe("Unable to get configuration of " + CONFIGURATION_NAME);
            return null;
        }

        return configuration;
    }

    @Override
    public String getConfigurationName() {
        return CONFIGURATION_NAME;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * Validator of Selendroid extension configuration from arquillian.xml.
     *
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
     *
     */
    public static final class SelendroidConfigurationValidator {

        /**
         * Validates {@code SelendroidConfiguration}.
         *
         * @param configuration configuration to validate
         * @throws SelendroidConfigurationException when configuration is invalid
         */
        public static void validate(SelendroidConfiguration configuration) throws SelendroidConfigurationException {
            // validate URL
            try {
                new URL(configuration.getRemoteAddress());
            } catch (MalformedURLException ex) {
                throw new SelendroidConfigurationException("unable to get valid URL address to hook SelendroidDriver to, got: "
                    + configuration.getRemoteAddress());
            }
        }
    }

}
