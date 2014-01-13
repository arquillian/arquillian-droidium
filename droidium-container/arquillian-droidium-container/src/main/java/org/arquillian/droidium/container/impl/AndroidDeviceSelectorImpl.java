/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceSelector;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.api.FileType;
import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.spi.event.AndroidBridgeInitialized;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceAvailable;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceCreate;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceDelete;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessExecution;
import org.arquillian.spacelift.process.ProcessExecutor;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Selects real physical Android device if serial id was specified in the configuration. If serial number was not specified one
 * of the following holds:
 *
 * <br>
 * <br>
 * 1. If console port was specified but AVD name was not, we try to connect to running emulator which listens to specified port.
 * If we fails to connect, {@link AndroidExecutionException} is thrown. <br>
 * 2. If AVD name was specified but console port was not, we try to connect to the first running emulator of such AVD name. <br>
 * 3. If both AVD name and console port were specified, we try to connect to this combination. <br>
 * 4. If we fail to get device in all above steps:
 * <ol>
 * <li>If AVD name was not specified, random AVD identifier is generated.</li>
 * <li>If AVD is among erroneous AVDs, it will be deleted, created from scratch, started and deleted after test.</li>
 * <li>If AVD is among non-erroneous AVDs, it will be started.</li>
 * <li>If AVD is not present, it will be created and started and deleted after test</li>
 * </ol>
 *
 * Observes:
 * <ul>
 * <li>{@link AndroidBridgeInitialized}</li>
 * </ul>
 *
 * Creates:
 * <ul>
 * <li>{@link AndroidDevice}</li>
 * </ul>
 *
 * Fires:
 * <ul>
 * <li>{@link AndroidVirtualDeviceCreate} - when we are going to create new Android virtual device</li>
 * <li>{@link AndroidVirtualDeviceAvailable} - when there is already avd of name we want in the system</li>
 * <li>{@link AndroidDeviceReady} - when we get intance of running Android device</li>
 * </ul>
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDeviceSelectorImpl implements AndroidDeviceSelector {

    private static Logger logger = Logger.getLogger(AndroidDeviceSelectorImpl.class.getSimpleName());

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
    private Instance<ProcessExecutor> executor;

    @Inject
    private Instance<IdentifierGenerator<FileType>> idGenerator;

    @Inject
    private Event<AndroidVirtualDeviceAvailable> androidVirtualDeviceAvailable;

    @Inject
    private Event<AndroidVirtualDeviceCreate> androidVirtualDeviceCreate;

    @Inject
    private Event<AndroidDeviceReady> androidDeviceReady;

    @Inject
    private Event<AndroidVirtualDeviceDelete> androidDeviceDelete;

    public void selectDevice(@Observes AndroidBridgeInitialized event) throws AndroidExecutionException {

        AndroidDevice device = null;

        AndroidContainerConfiguration configuration = this.configuration.get();

        if (isConnectingToPhysicalDevice()) {
            try {
                device = getPhysicalDevice();
                setDronePorts(device);
                androidDevice.set(device);
                androidDeviceReady.fire(new AndroidDeviceReady(device));
                return;
            } catch (AndroidExecutionException ex) {
                logger.log(Level.INFO, "Unable to connect to physical device with serial ID {0}. ",
                    new Object[] { configuration.getSerialId() });
            }
        }

        final List<String> androidListAVDOutput = getAndroidListAVDOutput(false);
        final List<String> compactAndroidListAVDOutput = getAndroidListAVDOutput(true);

        if (logger.isLoggable(Level.INFO)) {
            StringBuilder sb = new StringBuilder();
            for (String line : androidListAVDOutput) {
                sb.append(line).append("\n");
            }
            System.out.print(sb.toString());
        }

        if (isConnectingToVirtualDevice()) {
            device = getVirtualDevice();
            if (device != null) {
                setDronePorts(device);
                androidDevice.set(device);
                androidDeviceReady.fire(new AndroidDeviceReady(device));
                return;
            }
        }

        if (configuration.getAvdName() == null) {
            String generatedAvdName = idGenerator.get().getIdentifier(FileType.AVD);
            configuration.setAvdName(generatedAvdName);
            configuration.setAvdGenerated(true);
        }

        if (isInCompactAVDList(compactAndroidListAVDOutput, configuration.getAvdName())) {
            androidVirtualDeviceAvailable.fire(new AndroidVirtualDeviceAvailable(configuration.getAvdName()));
        } else if (isInRawAVDList(androidListAVDOutput, configuration.getAvdName())) {
            logger.log(Level.INFO, "You want to start an emulator backed by AVD of name {0} which seems to be broken. "
                + "This AVD will be deleted and AVD of the same name with configuration from arquillian.xml "
                + "will be created and started afterwards.", new Object[] { configuration.getAvdName() });

            androidDeviceDelete.fire(new AndroidVirtualDeviceDelete());
            androidVirtualDeviceCreate.fire(new AndroidVirtualDeviceCreate());
        } else {
            androidVirtualDeviceCreate.fire(new AndroidVirtualDeviceCreate());
        }

    }

    private boolean isInCompactAVDList(List<String> lines, String avdName) {
        for (String temp : lines) {
            if (temp.contains(avdName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInRawAVDList(List<String> lines, String avdName) {
        for (String temp : lines) {
            if (temp.contains("Name:")) {
                if (temp.substring(temp.indexOf(" ")).contains(avdName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setDronePorts(AndroidDevice device) {
        device.setDroneHostPort(configuration.get().getDroneHostPort());
        device.setDroneGuestPort(configuration.get().getDroneGuestPort());
    }

    private boolean isConnectingToVirtualDevice() {
        return isConsolePortDefined() || isAvdNameDefined();
    }

    private boolean isConnectingToPhysicalDevice() {
        String serialId = configuration.get().getSerialId();
        return serialId != null && !serialId.trim().isEmpty();
    }

    private boolean isConsolePortDefined() {
        String consolePort = configuration.get().getConsolePort();
        return consolePort != null && !consolePort.trim().equals("");
    }

    private boolean isAvdNameDefined() {
        String avdName = configuration.get().getAvdName();
        return avdName != null && !avdName.trim().equals("");
    }

    private boolean isOnlyConsolePortAvailable() {
        return isConsolePortDefined() && !isAvdNameDefined();
    }

    private boolean isOnlyAvdNameAvailable() {
        return isAvdNameDefined() && !isConsolePortDefined();
    }

    private AndroidDevice getVirtualDevice() {

        String consolePort = configuration.get().getConsolePort();
        String avdName = configuration.get().getAvdName();

        if (isOnlyConsolePortAvailable()) {
            try {
                return getVirtualDeviceByConsolePort(consolePort);
            } catch (AndroidExecutionException ex) {
                return null;
            }
        }

        if (isOnlyAvdNameAvailable()) {
            try {
                return getVirtualDeviceByAvdName(avdName);
            } catch (AndroidExecutionException ex) {
                return null;
            }
        }

        try {
            return getVirtualDevice(consolePort, avdName);
        } catch (AndroidExecutionException ex) {
            return null;
        }

    }

    private AndroidDevice getVirtualDevice(String consolePort, String avdName) throws AndroidExecutionException {
        Validate.notNullOrEmpty(consolePort, "Console port to get emulator of is a null object or an empty string.");
        Validate.notNullOrEmpty(avdName, "AVD name to get emulator of is a null object or an empty string.");

        List<AndroidDevice> devices = androidBridge.get().getEmulators();

        if (devices == null || devices.size() == 0) {
            throw new AndroidExecutionException("There are no emulators on the Android bridge.");
        }

        for (AndroidDevice device : devices) {
            try {
                if (device.getConsolePort().equals(consolePort) && device.getAvdName().equals(avdName)) {
                    logger.log(Level.INFO, "Connecting to virtual device running on console port {0} with AVD name {1}.",
                        new Object[] { consolePort, avdName });
                    return device;
                }
            } catch (NullPointerException ex) {
                logger.severe("Unable to connect to the emulator. Please be sure you are running adb server.");
            }
        }

        return null;
    }

    private AndroidDevice getVirtualDeviceByConsolePort(String consolePort) throws AndroidExecutionException {
        Validate.notNullOrEmpty(consolePort, "Console port to get emulator of is a null object or an empty string.");

        List<AndroidDevice> devices = androidBridge.get().getEmulators();

        if (devices == null || devices.size() == 0) {
            throw new AndroidExecutionException("There are no emulators on the Android bridge.");
        }

        for (AndroidDevice device : devices) {
            String deviceConsolePort = device.getConsolePort();
            if (deviceConsolePort != null && deviceConsolePort.equals(consolePort)) {
                logger.log(Level.INFO, "Connecting to virtual device running on console port {0}.", consolePort);
                return device;
            }
        }

        throw new AndroidExecutionException("Unable to get Android emulator running on the console port " + consolePort);
    }

    private AndroidDevice getVirtualDeviceByAvdName(String avdName) throws AndroidExecutionException {
        Validate.notNullOrEmpty(avdName, "AVD name to get emulator of is a null object or an empty string");

        List<AndroidDevice> devices = androidBridge.get().getEmulators();

        if (devices == null || devices.size() == 0) {
            throw new AndroidExecutionException("There are no emulators on the Android bridge.");
        }

        for (AndroidDevice device : devices) {
            String deviceAvdName = device.getAvdName();
            if (deviceAvdName != null && deviceAvdName.equals(avdName)) {
                logger.log(Level.INFO, "Connecting to virtual device running on AVD of name {0}.", avdName);
                return device;
            }
        }

        throw new AndroidExecutionException("No running emulator of AVD name " + avdName + ".");
    }

    private AndroidDevice getPhysicalDevice() throws AndroidExecutionException {

        String serialId = configuration.get().getSerialId();

        List<AndroidDevice> devices = androidBridge.get().getDevices();

        if (devices == null || devices.size() == 0) {
            throw new AndroidExecutionException("There are no physical devices on the Android bridge.");
        }

        for (AndroidDevice device : devices) {
            if (!device.isEmulator() && serialId.equals(device.getSerialNumber())) {
                logger.log(Level.INFO, "Connecting to physical device with serial ID {0}", serialId);
                return device;
            }
        }

        throw new AndroidExecutionException("Unable to get device with serial ID " + serialId + ".");
    }

    private List<String> getAndroidListAVDOutput(boolean compact) {
        try {
            CommandBuilder cb = new CommandBuilder().add(androidSDK.get().getAndroidPath(), "list", "avd");

            if (compact) {
                cb.add("-c");
            }

            Command command = cb.build();

            ProcessExecution execution = this.executor.get().execute(command);
            return execution.getOutput();
        } catch (AndroidExecutionException e) {
            throw new AndroidExecutionException(e, "Unable to get list of AVDs");
        }
    }

}