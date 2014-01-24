/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.arquillian.droidium.container.impl;

import java.io.File;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.spi.event.AndroidBridgeInitialized;
import org.arquillian.droidium.container.spi.event.AndroidBridgeTerminated;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidEmulatorShuttedDown;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Connects to the Android Debug Bridge. <br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AndroidContainerStart}</li>
 * <li>{@link AndroidEmulatorShuttedDown}</li>
 * <li>{@link AndroidBridgeTerminated}</li>
 * </ul>
 *
 * Creates: <br>
 * <br>
 * <ul>
 * <li>{@link AndroidBridge}</li>
 * </ul>
 *
 * Fires: <br>
 * <br>
 * <ul>
 * <li>{@link AndroidBridgeInitialized}</li>
 * <li>{@link AndroidBridgeTerminated}</li>
 * </ul>
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidBridgeConnector {

    private static final Logger logger = Logger.getLogger(AndroidBridgeConnector.class.getName());

    @Inject
    @ContainerScoped
    private InstanceProducer<AndroidBridge> androidBridge;

    @Inject
    private Instance<AndroidSDK> androidSDK;

    @Inject
    private Instance<AndroidContainerConfiguration> configuration;

    @Inject
    private Event<AndroidBridgeInitialized> adbInitialized;

    @Inject
    private Event<AndroidBridgeTerminated> adbTerminated;

    /**
     * Initializes Android Debug Bridge and fires {@link AndroidBridgeInitialized} event.
     *
     * @param event
     * @throws AndroidExecutionException
     */
    public void initAndroidDebugBridge(@Observes AndroidContainerStart event) throws AndroidExecutionException {
        logger.info("Initializing Android Debug Bridge.");

        long start = System.currentTimeMillis();

        AndroidBridge bridge = new AndroidBridgeImpl(new File(androidSDK.get().getAdbPath()), configuration.get()
            .isForceNewBridge(), configuration.get().getDdmlibCommandTimeout());
        bridge.connect();
        long delta = System.currentTimeMillis() - start;

        logger.info("Android Debug Bridge was initialized in " + delta + "ms.");

        androidBridge.set(bridge);
        adbInitialized.fire(new AndroidBridgeInitialized());
    }

    /**
     * Destroys Android Debug Bridge and fires {@link AndroidBridgeTerminated} event.
     *
     * @param event
     * @throws AndroidExecutionException
     */
    public void terminateAndroidDebugBridge(@Observes AndroidEmulatorShuttedDown event) throws AndroidExecutionException {
        logger.info("Disconnecting Android Debug Bridge.");
        androidBridge.get().disconnect();
        adbTerminated.fire(new AndroidBridgeTerminated());
    }

    /**
     * Listens to {@link AndroidBridgeTerminated} event and reacts accordingly.
     *
     * @param event
     */
    public void afterTerminateAndroidDebugBridge(@Observes AndroidBridgeTerminated event) {
        logger.info("Executing operations after disconnecting from Android Debug Bridge");
    }
}
