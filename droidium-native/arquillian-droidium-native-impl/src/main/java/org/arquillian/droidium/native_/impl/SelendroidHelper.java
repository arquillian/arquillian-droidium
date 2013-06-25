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
import org.arquillian.droidium.native_.configuration.Validate;
import org.arquillian.droidium.native_.utils.Command;

/**
 * Provides various helper methods for Selendroid server.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidHelper {

    private static final Logger logger = Logger.getLogger(SelendroidHelper.class.getName());

    private AndroidDevice androidDevice;

    private File serverLogFile;

    private static final String TOP_CMD = "top -n 1";

    private static final int SOCKET_TIME_OUT_SECONDS = 10;

    private static final int CONNECTION_TIME_OUT_SECONDS = 10;

    private static final int NUM_CONNECTION_RETIRES = 5;

    public SelendroidHelper(AndroidDevice androidDevice, File serverLogFile) {
        Validate.notNull(androidDevice, "Android Device for SelendroidHelper can't be null object!");
        Validate.notNull(serverLogFile, "Server log file for SelendroidHelper can't be null object!");

        this.androidDevice = androidDevice;
        this.serverLogFile = serverLogFile;
    }

    /**
     * Waits for Selendroid start. After installation and execution of instrumentation command, we repeatedly send http request
     * to status page to get response code of 200 - server is up and running and we can proceed safely.
     */
    public void waitForServerHTTPStart() {
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
     * Stop instrumentation.
     *
     * @param stopApplicationInstrumentationCommand
     */
    public void stopInstrumentation(Command stopApplicationInstrumentationCommand) {
        androidDevice.executeShellCommand(stopApplicationInstrumentationCommand.getAsString());
    }

    /**
     * Uninstalls application under test
     *
     * @param applicationUninstallCommand
     */
    public void uninstallApplicationUnderTest(Command applicationUninstallCommand) {
        androidDevice.executeShellCommand(applicationUninstallCommand.getAsString());
    }

    /**
     * Uninstalls modified Selendroid server
     *
     * @param uninstallSelendroidCommand
     */
    public void uninstallSelendroid(Command uninstallSelendroidCommand) {
        androidDevice.executeShellCommand(uninstallSelendroidCommand.getAsString());
    }

    /**
     * Starts instrumentation of underlying application under test.
     *
     * @param startApplicationInstrumentationCommand
     * @param applicationBasePackage
     */
    public void startInstrumentation(Command startApplicationInstrumentationCommand, String applicationBasePackage) {
        try {
            ServerMonkey monkey = new ServerMonkey(serverLogFile, applicationBasePackage);
            androidDevice.executeShellCommand(startApplicationInstrumentationCommand.getAsString(), monkey);
            waitUntilServerIsStarted(androidDevice, monkey);
        } catch (IOException ex) {
            throw new AndroidExecutionException(ex.getMessage());
        }
    }

    /**
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
     *
     */
    private static class ServerMonkey implements AndroidDeviceOutputReciever {
        private static final Logger logger = Logger.getLogger(ServerMonkey.class.getName());

        private final Writer output;

        private boolean active = false;

        private String waitForString;

        public ServerMonkey(File output, String waitForString) throws IOException {
            Validate.notNull(output, "File to write logs for ServerMonkey can't be null!");
            Validate.notNullOrEmpty(waitForString, "String to wait for in ServerMonkey can't be null nor empty!");
            this.output = new FileWriter(output);
            this.waitForString = waitForString;
        }

        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                logger.finest(line);
                try {
                    output.append(line).append("\n").flush();
                } catch (IOException e) {
                    // ignore output
                }
                if (line.contains(waitForString)) {
                    this.active = true;
                    return;
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
     * Waits until Selendroid is started by looking at the output of Android "top" command.
     *
     * @param device
     * @param monkey
     * @throws IOException
     */
    private void waitUntilServerIsStarted(AndroidDevice device, ServerMonkey monkey) throws IOException {

        logger.info("Starting Selendroid instrumentation on Android device.");
        for (int i = 0; i < 5; i++) {
            device.executeShellCommand(TOP_CMD, monkey);
            if (monkey.isActive()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        throw new AndroidExecutionException("Unable to start Selendroid instrumentation on Android device.");
    }

    /**
     *
     * @return Selendroid URL where status code is got from.
     */
    private URI getSelendroidStatusURI() {
        try {
            return new URI("http://localhost:" + androidDevice.getDroneHostPort() + "/wd/hub/status");
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
