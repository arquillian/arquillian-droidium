/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.platform.impl;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.platform.event.DroidiumPlatformConfigured;
import org.arquillian.spacelift.process.event.ProcessExecutorCreated;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Configures Arquillian Droidium Platform extension observing {@link ProcessExecutorCreated} event and parsing configuration
 * from {@link ArquillianDescriptor}.<br>
 * <br>
 * Produces ApplicationScoped:
 * <ul>
 * <li>{@link DroidiumPlatformConfiguration}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link DroidiumPlatformConfigured}</li>
 * </ul>
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumPlatformConfigurator {

    private static final Logger logger = Logger.getLogger(DroidiumPlatformConfigurator.class.getName());

    /**
     * Extension qualifier for Arquillian Droidium native extension in arquillian.xml.
     */
    private static final String DROIDIUM_PLATFORM_EXTENSION_NAME = "droidium-platform";

    @Inject
    @ApplicationScoped
    private InstanceProducer<DroidiumPlatformConfiguration> configuration;

    @Inject
    private Event<DroidiumPlatformConfigured> platformConfigured;

    public void configureDroidiumPlatofrm(@Observes ProcessExecutorCreated event, ArquillianDescriptor descriptor) {

        logger.info("Configuring " + DROIDIUM_PLATFORM_EXTENSION_NAME);

        DroidiumPlatformConfiguration configuration = new DroidiumPlatformConfiguration();

        for (ExtensionDef extensionDef : descriptor.getExtensions()) {
            if (DROIDIUM_PLATFORM_EXTENSION_NAME.equals(extensionDef.getExtensionName())) {
                Map<String, String> properties = extensionDef.getExtensionProperties();
                configuration.setProperties(properties);
                break;
            }
        }

        configuration.validate();

        if (logger.isLoggable(Level.INFO)) {
            System.out.println("Configuration of Arquillian Droidium Platform");
            System.out.println(configuration);
        }

        this.configuration.set(configuration);
        platformConfigured.fire(new DroidiumPlatformConfigured());
    }
}
