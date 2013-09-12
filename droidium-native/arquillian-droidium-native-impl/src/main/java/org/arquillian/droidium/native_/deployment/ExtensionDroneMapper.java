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
package org.arquillian.droidium.native_.deployment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.arquillian.droidium.native_.configuration.DroneConfigurationHolder;
import org.jboss.arquillian.core.spi.Validate;

/**
 * Holds the relationship between extension name (extension qualifier) for WebDriver extensions in arquillian.xml and its
 * underlying Drone configuration.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ExtensionDroneMapper {

    private static final Logger logger = Logger.getLogger(ExtensionDroneMapper.class.getName());

    private static final Map<String, DroneConfigurationHolder> map = new ConcurrentHashMap<String, DroneConfigurationHolder>();

    /**
     * Maps {@code extensionQualifier} to {@code configuration}.
     *
     * @param extensionQualifier
     * @param configuration
     * @throws IllegalArgumentException if either {@code extensionQualifier} or {@code configuration} is a null object or empty
     *         string respectively.
     */
    public void put(String extensionQualifier, DroneConfigurationHolder configuration) {
        Validate.notNullOrEmpty(extensionQualifier, "Extension qualifier you want to map to Drone configuration can not be a null object nor an empty string!");
        Validate.notNull(configuration, "Configuration you are trying to map with extension qualifier can not be a null object!");

        if (!isValid(configuration)) {
            logger.info("You are trying to put invalid mapping. Drone configuration validation process failed. You tried to put: " + configuration.toString());
        }

        if (map.containsKey(extensionQualifier)) {
            logger.fine("You are trying to put extensionQualifier-drone config pair into the mapper but there is already such extension qualifier stored.");
        }

        map.put(extensionQualifier, configuration);
    }

    /**
     *
     * @return the whole mapping
     */
    public Map<String, DroneConfigurationHolder> get() {
        return map;
    }

    /**
     *
     * @param extensionQualifier qualifier of an extension in arqullian.xml you wan to get its Drone configuration of
     * @return Drone configuration of the extension specified by its {@code extensionQualifier} in arquillian.xml, null if
     *         there is not such mapping for given {@code extensionQualifier}
     * @throws IllegalArgumentException if {@code extensionQualifier} is a null object or an empty string
     */
    public DroneConfigurationHolder getConfiguration(String extensionQualifier) {
        Validate.notNullOrEmpty(extensionQualifier, "Extension qualifier you are trying to get the configuration of can not "
            + "be a null object nor an empty string!");
        return map.get(extensionQualifier);
    }

    /**
     * Gets extension qualifier for a mapping entry for which its configuration is backed by specified port.
     *
     * @param port a port where some WebDriver-like extension is supposed to listen to, parsing its {@code remoteAddress}
     * @return name of extension as extension qualifier which {@code remoteAddress} listens to specified {@code port}.
     */
    public String getExtensionQualifierForPort(String port) {
        for (Map.Entry<String, DroneConfigurationHolder> entry : map.entrySet()) {
            if (entry.getValue().getPort().equals(port)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Gets extension qualifier for a mapping entry for which its configuration is backed by specified Drone type.
     *
     * @param droneQualifier Drone qualifier you want to get the extension qualifier of
     * @return extension qualifier for Drone instance of qualifier {@code droneQualifier}
     */
    public String getExtensionQualifierForDroneQualifer(String droneQualifier) {
        for (Map.Entry<String, DroneConfigurationHolder> entry : map.entrySet()) {
            if (entry.getValue().getQualifier().equals(droneQualifier)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private boolean isValid(DroneConfigurationHolder configuration) {
        if (configuration == null) {
            return false;
        }
        try {
            Validate.notNullOrEmpty(configuration.getDroneType(), "Type of Drone can not be a null object nor an empty sring!");
            Validate.notNullOrEmpty(configuration.getQualifier(), "Qualifier of Drone can not be a null object nor an empty string!");
            Validate.notNullOrEmpty(configuration.getPort(), "Port where Drone instance is supposed to be hooked can not be a null object nor an empty string!");
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }
}
