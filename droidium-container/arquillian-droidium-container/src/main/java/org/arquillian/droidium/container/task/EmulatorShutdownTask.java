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
package org.arquillian.droidium.container.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.impl.DeviceDiscovery;
import org.arquillian.spacelift.execution.CountDownWatch;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.Task;

import com.android.ddmlib.AndroidDebugBridge;

/**
 * Brings an emulator down.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class EmulatorShutdownTask extends Task<AndroidDevice, Void> {

    private CountDownWatch countdown;

    public Task<AndroidDevice, Void> countdown(CountDownWatch countdown) {
        this.countdown = countdown;
        return this;
    }

    @Override
    protected Void process(AndroidDevice device) throws Exception {

        final DeviceDiscovery deviceDiscovery = new DeviceDiscovery();
        deviceDiscovery.setDevice(device);

        AndroidDebugBridge.addDeviceChangeListener(deviceDiscovery);

        try {
            getExecutionService().execute(sendEmulatorCommand(extractPortFromDevice(device), "kill")).awaitAtMost(10, TimeUnit.SECONDS);
        } catch (ExecutionException ex) {
            throw new AndroidExecutionException(String.format("Sending of kill command to emulator {0} was not completed "
                + "successfully in 10 seconds.", device.getSerialNumber()), ex);
        }

        try {
            getExecutionService().execute(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    return deviceDiscovery.isOffline();
                }
            }).awaitAtMost(countdown);
        } catch (ExecutionException ex) {
            throw new AndroidExecutionException(String.format("Unable to get the emulator {0} down", device.getSerialNumber()), ex);
        }

        AndroidDebugBridge.removeDeviceChangeListener(deviceDiscovery);

        return null;

    }

    /**
     * This method extracts a port number from the serial number of a device. It assumes that the device name is of format
     * [xxxx-nnnn] where nnnn is the port number.
     *
     * @param device The device to extract the port number from.
     * @return Returns the port number of the device
     */
    private int extractPortFromDevice(AndroidDevice device) {
        String portStr = device.getSerialNumber().substring(device.getSerialNumber().lastIndexOf("-") + 1);
        if (portStr != null && portStr.length() > 0) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        // If the port is not available then return -1
        return -1;
    }

    private Callable<Boolean> sendEmulatorCommand(final int port, final String command) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws IOException {
                Socket socket = null;
                BufferedReader in = null;
                PrintWriter out = null;

                try {
                    socket = new Socket("127.0.0.1", port);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String telnetOutputString = null;

                    while ((telnetOutputString = in.readLine()) != null) {
                        if (telnetOutputString.equals("OK")) {
                            break;
                        }
                    }

                    out.write(command);
                    out.write("\n");
                    out.flush();
                } finally {
                    try {
                        out.close();
                        in.close();
                        socket.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }

                return true;
            }

        };
    }
}
