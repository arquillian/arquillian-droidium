/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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
 *
 * Copyright (C) 2009, 2010 Jayway AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.droidium.container.impl;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceRegister;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.container.spi.event.AndroidEmulatorShuttedDown;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceDelete;
import org.arquillian.droidium.container.task.EmulatorShutdownTask;
import org.arquillian.spacelift.execution.CountDownWatch;
import org.arquillian.spacelift.execution.Tasks;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Brings Android emulator down. <br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AndroidContainerStop}</li>
 * </ul>
 *
 * Fires:
 * <ul>
 * <li>{@link AndroidVirtualDeviceDelete}</li> if emulator is created dynamically
 * <li>{@link AndroidEmulatorShuttedDown}</li>
 * </ul>
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class AndroidEmulatorShutdown {

    private static final Logger logger = Logger.getLogger(AndroidEmulatorShutdown.class.getName());

    @Inject
    private Instance<AndroidContainerConfiguration> configuration;

    @Inject
    private Instance<AndroidDevice> androidDevice;

    @Inject
    private Instance<AndroidDeviceRegister> androidDeviceRegister;

    @Inject
    private Event<AndroidEmulatorShuttedDown> androidEmulatorShuttedDown;

    @Inject
    private Event<AndroidVirtualDeviceDelete> androidVirtualDeviceDelete;

    public void shutdownEmulator(@Observes AndroidContainerStop event) throws AndroidExecutionException {

        AndroidDevice device = androidDevice.get();
        AndroidContainerConfiguration configuration = this.configuration.get();

        androidDeviceRegister.get().remove(device);

        if (device != null && device.isEmulator()) {
            logger.log(Level.INFO, "Stopping Android emulator of AVD name {0}.", configuration.getAvdName());

            CountDownWatch countdown = new CountDownWatch(configuration.getEmulatorShutdownTimeoutInSeconds(), TimeUnit.SECONDS);

            logger.info("Waiting " + countdown.timeout() + " seconds for emulator " + device.getAvdName() + " to be disconnected and shutdown.");

            Tasks.chain(device, EmulatorShutdownTask.class).countdown(countdown).execute().await();

            logger.info("Device " + device.getAvdName() + " on port " + device.getConsolePort() + " was disconnected in " + countdown.timeElapsed() + " seconds.");

            if (configuration.isAVDGenerated()) {
                androidVirtualDeviceDelete.fire(new AndroidVirtualDeviceDelete());
            }

            androidEmulatorShuttedDown.fire(new AndroidEmulatorShuttedDown(device));
        }
    }

}