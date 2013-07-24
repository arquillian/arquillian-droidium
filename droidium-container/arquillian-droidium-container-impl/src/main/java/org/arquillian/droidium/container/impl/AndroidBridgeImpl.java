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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.Validate;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

/**
 * Implementation of the {@link AndroidBridge} by which we can connect or disconnect to the bridge and query
 * {@link AndroidDebugBridge} for attached devices.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class AndroidBridgeImpl implements AndroidBridge {

    private static final Logger logger = Logger.getLogger(AndroidBridgeImpl.class.getName());

    private AndroidDebugBridge delegate;

    private static final long ADB_TIMEOUT_MS = 60L * 1000;

    private File adbLocation;

    private boolean forceNewBridge;

    AndroidBridgeImpl(File adbLocation, boolean forceNewBridge) throws IllegalArgumentException {
        Validate.isReadable(adbLocation, "ADB location does not represent a readable file: " + adbLocation);
        this.adbLocation = adbLocation;
        this.forceNewBridge = forceNewBridge;
    }

    @Override
    public void connect() throws AndroidExecutionException {
        logger.info("Connecting to the Android Debug Bridge at " + adbLocation.getAbsolutePath() + " forceNewBridge = "
            + forceNewBridge);

        AndroidDebugBridge.initIfNeeded(false);

        this.delegate = AndroidDebugBridge.createBridge(adbLocation.getAbsolutePath(), forceNewBridge);

        waitUntilConnected();
        waitForInitialDeviceList();
    }

    @Override
    public boolean isConnected() {
        Validate.notNull(delegate, "Android debug bridge must be set. Please call connect() method before execution");
        return delegate.isConnected();
    }

    @Override
    public void disconnect() throws AndroidExecutionException {
        Validate.notNull(delegate, "Android debug bridge must be set. Please call connect() method before execution");

        logger.info("Disconnecting Android Debug Bridge at " + adbLocation.getAbsolutePath());

        if (isConnected()) {
            logger.fine("\t Android Debug Bridge is connected.");
            if (!hasDevices()) {
                logger.fine("Android Debug Bridge does't have devices. Going to disconnect it.");
                AndroidDebugBridge.disconnectBridge();
                AndroidDebugBridge.terminate();
            } else {
                logger.fine("Android Debug Bridge has devices.");
                logger.info("There are still some devices on the Android Debug Bridge." +
                    " Bridge will not be disconnected until none are connected.");
            }
        } else {
            logger.info("Android Debug Bridge is already disconnected.");
        }
    }

    @Override
    public List<AndroidDevice> getDevices() {
        Validate.notNull(delegate, "Android debug bridge must be set. Please call connect() method before execution");

        IDevice[] idevices = delegate.getDevices();

        List<AndroidDevice> devices = new ArrayList<AndroidDevice>(idevices.length);
        for (IDevice d : idevices) {
            devices.add(new AndroidDeviceImpl(d));
        }

        return devices;
    }

    @Override
    public List<AndroidDevice> getEmulators() {
        Validate.notNull(delegate, "Android debug bridge must be set. Please call connect() method before execution");

        List<AndroidDevice> emulators = new ArrayList<AndroidDevice>();

        for (AndroidDevice device : getDevices()) {
            if (device.isEmulator()) {
                emulators.add(device);
            }
        }

        return emulators;
    }

    @Override
    public boolean hasDevices() {
        Validate.notNull(delegate, "Android debug bridge must be set. Please call connect() method before execution");
        return delegate.getDevices().length != 0;
    }

    /**
     * Run a wait loop until adb is connected or trials run out. This method seems to work more reliably then using a listener.
     *
     * @param adb
     */
    private void waitUntilConnected() {
        int trials = 10;
        while (trials > 0) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isConnected()) {
                break;
            }
            trials--;
        }
    }

    /**
     * Wait for the Android Debug Bridge to return an initial device list.
     *
     * @param adb
     */
    private void waitForInitialDeviceList() {
        if (!delegate.hasInitialDeviceList()) {
            logger.info("Waiting for initial device list from the Android Debug Bridge");
            long limitTime = System.currentTimeMillis() + ADB_TIMEOUT_MS;
            while (!delegate.hasInitialDeviceList() && (System.currentTimeMillis() < limitTime)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(
                        "Interrupted while waiting for initial device list from Android Debug Bridge");
                }
            }
            if (!delegate.hasInitialDeviceList()) {
                logger.severe("Did not receive initial device list from the Android Debug Bridge.");
            }
        }
    }
}
