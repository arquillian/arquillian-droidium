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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceOutputReciever;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.native_.utils.Command;
import org.arquillian.droidium.native_.utils.DroidiumNativeFileUtils;

/**
 * Starts and stops instrumentation of application under test, installs and uninstalls Selendroid server.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidHelper {

    private static final Logger logger = Logger.getLogger(SelendroidHelper.class.getName());

    private final AndroidDevice androidDevice;

    private File tmpDir;

    private static final String TOP_CMD = "top -n 1";

    private static final String PACKAGES_LIST_CMD = "pm list packages -f";

    private static final int SOCKET_TIME_OUT_SECONDS = 10;

    private static final int CONNECTION_TIME_OUT_SECONDS = 10;

    private static final int NUM_CONNECTION_RETIRES = 5;

    /**
     * Installs and uninstalls application under test and Selendroid server, starts instrumentation.
     *
     * @param androidDevice
     * @throws IllegalArgumentException if either of arguments is a null object
     */
    public SelendroidHelper(AndroidDevice androidDevice) throws IllegalArgumentException {
        Validate.notNull(androidDevice, "Android Device for SelendroidHelper can't be null object!");
        this.androidDevice = androidDevice;
    }

    public void setTmpDir(File tmpDir) {
        Validate.notNull(tmpDir, "Temporary directory for Selendroid helper can not be a null object!");
        this.tmpDir = tmpDir;
    }

    /**
     * Starts instrumentation of application under test.
     *
     * @param startApplicationInstrumentationCommand
     * @param applicationBasePackage
     */
    public void startInstrumentation(Command startApplicationInstrumentationCommand, String applicationBasePackage) {
        try {
            Monkey monkey = new Monkey(DroidiumNativeFileUtils.createRandomEmptyFile(tmpDir), applicationBasePackage, true);
            androidDevice.executeShellCommand(startApplicationInstrumentationCommand.getAsString(), monkey);
            waitForMonkey(androidDevice, monkey, TOP_CMD);
            waitUntilSelendroidServerCommunication();
        } catch (IOException ex) {
            throw new AndroidExecutionException(ex.getMessage());
        }
    }

    /**
     * Stops Selendroid server
     *
     * @param stopSelendroidServer which stops Selendroid server
     */
    public void stopSelendroidServer(Command stopSelendroidServer) {
        androidDevice.executeShellCommand(stopSelendroidServer.getAsString());
    }

    /**
     * Stops application under test
     *
     * @param command which stops an application
     */
    public void stopApplication(Command command) {
        try {
            Monkey monkey = new Monkey(DroidiumNativeFileUtils.createRandomEmptyFile(tmpDir),
                command.get(command.size() - 1), false);
            androidDevice.executeShellCommand(command.getAsString(), monkey);
            waitForMonkey(androidDevice, monkey, TOP_CMD);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Uninstalls application under test from Android device.
     *
     * @param command which uninstalls an application
     */
    public void uninstallApplication(Command command) {
        try {
            Monkey monkey = new Monkey(DroidiumNativeFileUtils.createRandomEmptyFile(tmpDir),
                command.get(command.size() - 1), false);
            androidDevice.executeShellCommand(command.getAsString(), monkey);
            waitForMonkey(androidDevice, monkey, PACKAGES_LIST_CMD);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Uninstalls modified Selendroid server from Android device.
     *
     * @param command which uninstalls Selendroid server
     */
    public void uninstallSelendroidServer(Command command) {
        androidDevice.executeShellCommand(command.getAsString());
    }

    /**
     * Waits for the start of Selendroid server.
     *
     * After installation and execution of instrumentation command, we repeatedly send HTTP request to status page to get
     * response code of 200 - server is up and running and we can proceed safely to testing process.
     */
    private void waitUntilSelendroidServerCommunication() {
        HttpClient httpClient = new DefaultHttpClient();

        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIME_OUT_SECONDS * 1000);
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIME_OUT_SECONDS * 1000);

        HttpGet httpGet = new HttpGet(getSelendroidStatusURI());

        boolean connectionSuccess = false;

        for (int i = NUM_CONNECTION_RETIRES; i > 0; i--) {
            try {
                HttpResponse response = httpClient.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    connectionSuccess = true;
                    break;
                }
                logger.log(Level.INFO, "Response was not 200, response was: " + statusCode + ". Repeating " + i + " times.");
            } catch (ClientProtocolException e) {
                logger.log(Level.WARNING, e.getMessage());
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }

        httpClient.getConnectionManager().shutdown();

        if (!connectionSuccess) {
            throw new AndroidExecutionException("Unable to get successful connection from Selendroid http server.");
        }
    }

    /**
     * Waits until {@link Monkey} is done.
     *
     * @param device
     * @param monkey
     */
    private void waitForMonkey(AndroidDevice device, Monkey monkey, String command) throws IOException {

        for (int i = 0; i < 5; i++) {
            device.executeShellCommand(command, monkey);
            if (monkey.isActive()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        throw new AndroidExecutionException("Waiting for monkey timeouted.");
    }

    /**
     * Checks if output lines from command on Android device contains package name of application.
     *
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
     *
     */
    private static class Monkey implements AndroidDeviceOutputReciever {

        private static final Logger logger = Logger.getLogger(Monkey.class.getName());

        private final Writer output;

        private boolean active = false;

        private boolean contains = false;

        private final String waitForString;

        /**
         *
         * @param output where to write output from receiver
         * @param waitForString for which string to wait
         * @param contains set to true if we are waiting for the presence of the {@code waitForString} in the {@code output},
         *        set to false when we are waiting until {@code waitForString} will be not present in the {@code output}
         * @throws IOException
         */
        public Monkey(File output, String waitForString, boolean contains) throws IOException {
            Validate.notNull(output, "File to write logs for Monkey can't be null!");
            Validate.notNullOrEmpty(waitForString, "String to wait for in Monkey can't be null nor empty!");
            this.output = new FileWriter(output);
            this.waitForString = waitForString;
            this.contains = contains;
        }

        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                logger.finest(line);
                try {
                    output.append(line).append("\n").flush();
                    if (contains) {
                        if (line.contains(waitForString)) {
                            this.active = true;
                            return;
                        }
                    } else {
                        if (!line.contains(waitForString)) {
                            this.active = true;
                            return;
                        }
                    }
                } catch (IOException e) {
                    // ignore output
                }
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        public boolean isActive() {
            return active;
        }
    }

    /**
     * @return Selendroid server URL where status code is got from.
     */
    private URI getSelendroidStatusURI() {
        try {
            return new URI("http://localhost:" + androidDevice.getDroneHostPort() + "/wd/hub/status");
        } catch (URISyntaxException e) {
            return null;
        }
    }

}
