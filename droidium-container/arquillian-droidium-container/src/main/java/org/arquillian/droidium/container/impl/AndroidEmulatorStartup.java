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
 */
package org.arquillian.droidium.container.impl;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceAvailable;
import org.arquillian.droidium.container.task.EmulatorIsOnlineTask;
import org.arquillian.droidium.container.task.EmulatorStatusCheckTask;
import org.arquillian.droidium.container.task.FreePortTask;
import org.arquillian.droidium.container.task.UnlockEmulatorTask;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.execution.CountDownWatch;
import org.arquillian.spacelift.execution.Execution;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessResult;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.task.os.CommandTool;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.threading.ExecutorService;

import com.android.ddmlib.AndroidDebugBridge;

/**
 * Starts an emulator and either connects to an existing device or creates one. <br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AndroidVirtualDeviceAvailable}</li>
 * </ul>
 *
 * Creates:
 * <ul>
 * <li>{@link AndroidDevice}</li>
 * </ul>
 *
 * Fires:
 * <ul>
 * <li>{@link AndroidDeviceReady}</li>
 * </ul>
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidEmulatorStartup {

    private static final Logger logger = Logger.getLogger(AndroidEmulatorStartup.class.getName());

    @Inject
    @ContainerScoped
    private InstanceProducer<AndroidDevice> androidDevice;

    @Inject
    private Instance<AndroidBridge> androidBridge;

    @Inject
    private Instance<AndroidContainerConfiguration> configuration;

    @Inject
    private Instance<AndroidSDK> androidSDK;

    @Inject
    private Event<AndroidDeviceReady> androidDeviceReady;

    @Inject
    private Instance<ExecutorService> executorService;

    public void startAndroidEmulator(@Observes AndroidVirtualDeviceAvailable event) throws AndroidExecutionException {
        if (!androidBridge.get().isConnected()) {
            throw new IllegalStateException("Android debug bridge must be connected in order to spawn an emulator");
        }

        logger.log(Level.INFO, "Starting Android emulator of AVD name {0}.", configuration.get().getAvdName());

        AndroidContainerConfiguration configuration = this.configuration.get();
        AndroidSDK sdk = this.androidSDK.get();

        DeviceDiscovery deviceDiscovery = new DeviceDiscovery();
        AndroidDebugBridge.addDeviceChangeListener(deviceDiscovery);

        Execution<ProcessResult> emulatorExecution = startEmulator();

        CountDownWatch watch = new CountDownWatch(configuration.getEmulatorBootupTimeoutInSeconds(), TimeUnit.SECONDS);

        Spacelift.task(deviceDiscovery, EmulatorIsOnlineTask.class)
            .execute().until(watch, EmulatorIsOnlineTask.isOnlineCondition);

        AndroidDevice androidDevice = deviceDiscovery.getDevice();

        Spacelift.task(EmulatorStatusCheckTask.class)
            .execution(emulatorExecution)
            .then(CommandTool.class)
            .command(new CommandBuilder(androidSDK.get().getAdbPath())
                .parameters("-s", androidDevice.getSerialNumber(), "shell", "getprop"))
            .execute().until(watch, EmulatorStatusCheckTask.isBootedCondition);

        Spacelift.task(UnlockEmulatorTask.class)
            .serialNumber(androidDevice.getSerialNumber())
            .sdk(sdk)
            .execute().await();

        androidDevice.setDroneHostPort(configuration.getDroneHostPort());
        androidDevice.setDroneGuestPort(configuration.getDroneGuestPort());

        AndroidDebugBridge.removeDeviceChangeListener(deviceDiscovery);

        this.androidDevice.set(androidDevice);

        androidDeviceReady.fire(new AndroidDeviceReady(androidDevice));
    }

    private Execution<ProcessResult> startEmulator() throws AndroidExecutionException {

        AndroidSDK sdk = this.androidSDK.get();
        AndroidContainerConfiguration configuration = this.configuration.get();

        CommandBuilder command = new CommandBuilder(sdk.getEmulatorPath());

        command.parameter("-avd").parameter(configuration.getAvdName());

        if (configuration.getSdCard() != null) {
            command.parameters("-sdcard", configuration.getSdCard());
        }

        if (configuration.getConsolePort() != null) {
            if (!Spacelift.task(FreePortTask.class).port(configuration.getConsolePort()).execute().await()) {
                throw new AndroidExecutionException("It seems there is already something which listens on specified "
                    + "console port " + configuration.getConsolePort() + " so Droidium can not start emulator there.");
            }
        }

        if (configuration.getAdbPort() != null) {
            if (!Spacelift.task(FreePortTask.class).port(configuration.getAdbPort()).execute().await()) {
                throw new AndroidExecutionException("It seems there is already something which listens on specified "
                    + "adb port " + configuration.getAdbPort() + " so Droidium can not start emulator there.");
            }
        }

        if (configuration.getConsolePort() != null && configuration.getAdbPort() != null) {
            command.parameter("-ports").parameter(configuration.getConsolePort() + "," + configuration.getAdbPort());
        } else if (configuration.getConsolePort() != null) {
            command.parameter("-port").parameter(configuration.getConsolePort());
        }

        command.splitToParameters(configuration.getEmulatorOptions());

        logger.log(Level.INFO, "Starting emulator \"{0}\", using {1}", new Object[] {
            configuration.getAvdName(), command });

        // define what patterns would be consider worthy to notify user
        ProcessInteractionBuilder interactions = new ProcessInteractionBuilder()
            .when("^SDL init failure.*$").printToErr()
            .when("^PANIC:.*$").printToErr()
            .when("^error.*$").printToErr()
            .when("^unknown option:.*$").printToErr();

        // start emulator but does not wait for its termination here
        try {
            return Spacelift.task(CommandTool.class)
                .command(command)
                .interaction(interactions.build())
                .execute();
        } catch (ExecutionException ex) {
            throw new AndroidExecutionException(ex, "Unable to start emulator \"{0}\", using {1}",
                configuration.getAvdName(),
                command);
        }
    }

}
