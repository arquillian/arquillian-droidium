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
package org.arquillian.droidium.container.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;

import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;

/**
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DeviceDiscovery implements IDeviceChangeListener {

    private static final Logger logger = Logger.getLogger(DeviceDiscovery.class.getName());

    private IDevice delegate;

    private AndroidDevice device;

    private boolean online;

    private boolean offline;

    @Override
    public void deviceChanged(IDevice delegate, int changeMask) {
        if (this.delegate.equals(delegate) && (changeMask & IDevice.CHANGE_STATE) == 1) {
            if (device.isOnline()) {
                online = true;
            }
        }
    }

    @Override
    public void deviceConnected(IDevice delegate) {
        this.delegate = delegate;
        device = new AndroidDeviceImpl(delegate);
        logger.log(Level.INFO, "Discovered an emulator device id={0} connected to ADB bus", device.getSerialNumber());
    }

    @Override
    public void deviceDisconnected(IDevice delegate) {
        if (delegate.isEmulator()) {
            if (delegate.getAvdName().equals(this.device.getAvdName())) {
                offline = true;
                logger.fine("Device id=" + delegate.getAvdName() + " disconnected from ADB bus.");
            }
        } else {
            if (delegate.getSerialNumber().equals(this.device.getSerialNumber())) {
                offline = true;
                logger.fine("Device id=" + delegate.getSerialNumber() + " disconnected from ADB bus.");
            }
        }
    }

    public DeviceDiscovery setDevice(AndroidDevice device) {
        this.device = device;
        return this;
    }

    public AndroidDevice getDevice() {
        return device;
    }

    public DeviceDiscovery setDelegate(IDevice delegate) {
        this.delegate = delegate;
        return this;
    }

    public IDevice getDelegate() {
        return delegate;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isOffline() {
        return offline;
    }
}
