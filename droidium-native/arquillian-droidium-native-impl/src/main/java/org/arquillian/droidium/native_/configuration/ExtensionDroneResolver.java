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
package org.arquillian.droidium.native_.configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.native_.deployment.ExtensionDroneMapper;
import org.arquillian.droidium.native_.spi.event.AfterExtensionDroneMapping;
import org.arquillian.droidium.native_.spi.event.BeforeExtensionDroneMapping;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
import org.jboss.arquillian.drone.spi.event.AfterDroneConfigured;

/**
 * After some Drone is configured, it scans arquillian.xml and tries to map that just configured Drone instance with underlying
 * extension. <br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AfterDroneConfigured}</li>
 * </ul>
 * Fires:<br>
 * <ul>
 * <li>{@link BeforeExtensionDroneMapping}</li>
 * <li>{@link AfterExtensionDroneMapping}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ExtensionDroneResolver {

    private static final Logger logger = Logger.getLogger(ExtensionDroneResolver.class.getName());

    private static final String DEFAULT_REMOTE_URL = "http://localhost:14444/wd/hub";

    private static final String DEFAULT_PORT = "14444";

    @Inject
    private Instance<ExtensionDroneMapper> extensionDroneMapper;

    @Inject
    private Event<BeforeExtensionDroneMapping> beforeExtensionDroneMappingEvent;

    @Inject
    private Event<AfterExtensionDroneMapping> afterExtensionDroneMappingEvent;

    public void onAfterDroneConfigured(@Observes AfterDroneConfigured event, ArquillianDescriptor descriptor)
        throws URISyntaxException {

        beforeExtensionDroneMappingEvent.fire(new BeforeExtensionDroneMapping());

        logger.log(Level.FINE, "Drone type: {0}\nDrone qualifier: {1}\nDrone configuration name: {2}\n", new Object[] {
            event.getDroneType(),
            event.getQualifier().getSimpleName().toLowerCase(),
            event.getConfiguration().asInstance(DroneConfiguration.class).getConfigurationName().toLowerCase()});

        String configurationName = event.getConfiguration().asInstance(DroneConfiguration.class).getConfigurationName().toLowerCase();

        // skip global config
        if (configurationName.equals("drone")) {
            return;
        }

        String qualifier = event.getQualifier().getSimpleName().toLowerCase();
        String configurationNameWithQualifier = configurationName + "-" + qualifier;
        String extensionQualifier = resolveExtensionQualifier(descriptor, configurationName, configurationNameWithQualifier);

        ExtensionDef extension = getExtension(descriptor, extensionQualifier);

        String browser = getBrowser(extension.getExtensionProperties());

        if (browser == null || !browser.equals("android")) {
            logger.info("No \"android\" browser was used. The mapping process to arquillian extension for this Drone is skipped.");
            return;
        }

        String remoteUrl = parseRemoteAddress(extension.getExtensionProperties());
        String port = parsePort(remoteUrl);

        logger.log(Level.FINE, "Extension name: {0}\nExtension qualifier: {1}\nDrone qualifier: {2}\nPort: {3}",
            new Object[] {
                extension.getExtensionName(),
                extensionQualifier,
                qualifier,
                port
        });

        extensionDroneMapper.get().put(extension.getExtensionName(), new DroneConfigurationHolder(extensionQualifier, qualifier, port));

        afterExtensionDroneMappingEvent.fire(new AfterExtensionDroneMapping(extension.getExtensionName(), port));
    }

    private String resolveExtensionQualifier(ArquillianDescriptor descriptor, String configurationName,
        String configurationNameWithQualifier) throws IllegalStateException {
        if (configurationName.equals("webdriver")) {
            for (ExtensionDef extension : descriptor.getExtensions()) {
                if (extension.getExtensionName().equalsIgnoreCase(configurationNameWithQualifier)) {
                    return configurationNameWithQualifier;
                }
            }
            for (ExtensionDef extension : descriptor.getExtensions()) {
                if (extension.getExtensionName().equalsIgnoreCase(configurationName)) {
                    return configurationName;
                }
            }
            throw new IllegalStateException("Unable to resolve extension qualifier for Drone.");
        } else {
            throw new IllegalStateException("Please use WebDriver-based Drone and try again.");
        }
    }

    private ExtensionDef getExtension(ArquillianDescriptor descriptor, String extensionQualifier) {
        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (extension.getExtensionName().equals(extensionQualifier)) {
                return extension;
            }
        }
        return null;
    }

    private String getBrowser(Map<String, String> extensionProperties) {
        String browser = extensionProperties.get("browser");
        if (browser == null) {
            browser = extensionProperties.get("browserCapabilities");
        }

        return browser;
    }

    private String parsePort(String remoteUrl) throws URISyntaxException {
        URI uri = new URI(remoteUrl);

        int port = uri.getPort();

        if (port == -1) {
            return DEFAULT_PORT;
        }

        return Integer.toString(port);
    }

    private String parseRemoteAddress(Map<String, String> extensionProperties) {
        String remoteAddress = extensionProperties.get("remoteAddress");

        if (remoteAddress == null) {
            return DEFAULT_REMOTE_URL;
        }

        return remoteAddress;
    }

}
