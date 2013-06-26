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
package org.arquillian.droidium.native_.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.configuration.Validate;
import org.arquillian.droidium.native_.event.DroidiumNativeConfigured;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * Creator of Arquillian Drone configuration.
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

    private static final String ANDROID_DRONE_EXTENSION_NAME = "droidium-native";

    @Inject
    @SuiteScoped
    private InstanceProducer<DroidiumNativeConfiguration> droidiumNativeConfiguration;

    @Inject
    private Event<DroidiumNativeConfigured> droidiumNativeConfigured;

    @Inject
    private Instance<ProcessExecutor> processExecutor;

    @Inject
    private Instance<AndroidSDK> androidSDK;

    public void configureAndroidDrone(@Observes(precedence = 10) BeforeSuite event, ArquillianDescriptor descriptor) {

        logger.info("Configuring Android Drone Native");

        DroidiumNativeConfiguration configuration = new DroidiumNativeConfiguration();

        for (ExtensionDef extensionDef : descriptor.getExtensions()) {
            if (ANDROID_DRONE_EXTENSION_NAME.equals(extensionDef.getExtensionName())) {
                Map<String, String> properties = extensionDef.getExtensionProperties();
                if (properties.containsKey("androidServerApk")) {
                    configuration.setServerApk(new File(properties.get("androidServerApk")));
                }
            }
        }

        Validate.isReadable(configuration.getServerApk(), "You must provide a valid path to Android Server APK"
            + configuration.getServerApk());

        File serverLogFile = configuration.getServerLogFile();

        Validate.notNull(serverLogFile, "You must provide a valid path to Arquillian Android Server Monkey log file: "
            + configuration.getServerLogFile());

        try {
            serverLogFile.createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create Arquillian monkey log file at "
                + serverLogFile.getAbsolutePath(), e);
        }

        Validate.notNullOrEmpty(configuration.getAlias(),
            "You must provide valid alias for signing of APK files. You entered '" + configuration.getAlias() + "'.");
        Validate.notNullOrEmpty(configuration.getKeypass(),
            "You must provide valid keypass for signing of APK files. You entered '" + configuration.getKeypass() + "'.");
        Validate.notNullOrEmpty(configuration.getStorepass(),
            "You must provide valid storepass for signing of APK files. You entered '" + configuration.getStorepass() + "'.");

        droidiumNativeConfiguration.set(configuration);
        droidiumNativeConfigured.fire(new DroidiumNativeConfigured());
    }
}
