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

import java.util.Map;
import java.util.logging.Logger;

import org.arquillian.droidium.native_.spi.event.DroidiumNativeConfigured;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * Configures Arquillian Droidium Native extension.
 *
 * Observes:
 * <ul>
 * <li>{@link BeforeSuite}</li>
 * </ul>
 *
 * Creates:
 * <ul>
 * <li>{@link DroidiumNativeConfiguration}</li>
 * </ul>
 *
 * Fires:
 * <ul>
 * <li>{@link DroidiumNativeConfigured}</li>
 * </ul>
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeConfigurator {

    private static final Logger logger = Logger.getLogger(DroidiumNativeConfigurator.class.getName());

    /**
     * Extension qualifier for Arquillian Droidium native extension in arquillian.xml.
     */
    public static final String DROIDIUM_NATIVE_EXTENSION_NAME = "droidium-native";

    @Inject
    @SuiteScoped
    private InstanceProducer<DroidiumNativeConfiguration> droidiumNativeConfiguration;

    @Inject
    private Event<DroidiumNativeConfigured> droidiumNativeConfigured;

    // precendence is important here, registration of extensions goes first in every case
    public void configureDroidiumNative(@Observes(precedence = 10) BeforeSuite event, ArquillianDescriptor descriptor) {

        logger.info("Configuring " + DROIDIUM_NATIVE_EXTENSION_NAME);

        DroidiumNativeConfiguration configuration = new DroidiumNativeConfiguration();

        for (ExtensionDef extensionDef : descriptor.getExtensions()) {
            if (DROIDIUM_NATIVE_EXTENSION_NAME.equals(extensionDef.getExtensionName())) {
                Map<String, String> properties = extensionDef.getExtensionProperties();
                configuration.setProperties(properties);
                configuration.validate();
            }
        }

        droidiumNativeConfiguration.set(configuration);
        droidiumNativeConfigured.fire(new DroidiumNativeConfigured());
    }
}
