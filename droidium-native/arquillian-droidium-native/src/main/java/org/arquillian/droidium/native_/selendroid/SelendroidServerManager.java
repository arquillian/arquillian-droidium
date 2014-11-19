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
package org.arquillian.droidium.native_.selendroid;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.arquillian.droidium.container.utils.Monkey;
import org.arquillian.droidium.native_.exception.InvalidSelendroidPortException;
import org.arquillian.droidium.native_.spi.SelendroidDeployment;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessResult;
import org.arquillian.spacelift.process.impl.CommandTool;

/**
 * Manages deployment and undeployment of Selendroid servers which instrument Android packages. There is strict one-to-one
 * relationship between Selendroid server and instrumented Android application.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidServerManager {

    private static final Logger logger = Logger.getLogger(SelendroidServerManager.class.getName());

    private AndroidDevice device;

    private final AndroidSDK sdk;

    private static final String TOP_CMD = "top -n 1";

    private static final int SOCKET_TIME_OUT_SECONDS = 10;

    private static final int CONNECTION_TIME_OUT_SECONDS = 10;

    private static final int NUM_CONNECTION_RETIRES = 5;

    /**
     *
     * @param device
     * @param sdk
     * @throws IllegalArgumentException if {@code sdk} is a null object
     */
    public SelendroidServerManager(AndroidDevice device, AndroidSDK sdk) {
        Validate.notNull(sdk, "Android SDK to set can not be a null object!");
        this.device = device;
        this.sdk = sdk;
    }

    public SelendroidServerManager(AndroidSDK sdk) {
        this(null, sdk);
    }

    public SelendroidServerManager setDevice(AndroidDevice device) {
        if (device != null) {
            this.device = device;
        }
        return this;
    }

    /**
     * Installs resigned Selendroid server which reflects Android application meant to be instrumented to Android device.
     *
     * @param deployment deployment to install to Android device
     * @throws IllegalArgumentException if {@code deployment} or {@code SelendroidDeployment#getResigned()} is a null object
     * @throws AndroidExecutionException
     */
    public void install(SelendroidDeployment deployment) {
        Validate.notNull(device, "Android device can not be a null object!");
        Validate.notNull(deployment, "Selendroid deployment to deploy can not be a null object!");
        Validate.notNull(deployment.getResigned(), "Resigned Selendroid application to deploy can not be a null object!");

        Command selendroidInstallCommand = new CommandBuilder(sdk.getAdbPath())
            .parameter("-s")
            .parameter(device.getSerialNumber())
            .parameter("install")
            .parameter(deployment.getResigned().getAbsolutePath())
            .build();

        if (device.isPackageInstalled(deployment.getSelendroidPackageName())) {
            device.uninstallPackage(deployment.getSelendroidPackageName());
        }

        logger.fine("Selendroid server installation command: " + selendroidInstallCommand.toString());

        ProcessResult processResult = Tasks.prepare(CommandTool.class).command(selendroidInstallCommand).execute().await();

        if (processResult.exitValue() != 0) {
            throw new AndroidExecutionException("Unable to execute Selendroid installation process, exit value: " +
                processResult.exitValue());
        }

        if (!device.isPackageInstalled(deployment.getServerBasePackage())) {
            throw new AndroidExecutionException("Modified Selendroid server was not installed on device.");
        }
    }

    /**
     * Instruments Android application by Selendroid server in {@code deployment}.
     *
     * @param deployment
     * @throws IllegalArgumentException if {@code deployment} is a null object or if
     *         {@link SelendroidDeployment#getInstrumentedDeployment()} is a null object or if
     *         {@link SelendroidDeployment#getInstrumentationConfiguration()} is a null object.
     * @throws AndroidExecutionException
     */
    public void instrument(SelendroidDeployment deployment) {
        Validate.notNull(device, "Android device can not be a null object!");
        Validate.notNull(deployment, "Deployment to instument is a null object!");
        Validate.notNull(deployment.getInstrumentationConfiguration(),
            "Instrumentation configuration of the underlying deployment is a null object!");
        Validate.notNull(deployment.getInstrumentedDeployment(),
            "Android deployment for Selendroid deployment is a null object!");

        int port = Integer.parseInt(deployment.getInstrumentationConfiguration().getPort());
        createPortForwarding(port, port);

        Command startApplicationInstrumentationCommand = new CommandBuilder("am")
            .parameter("instrument")
            .parameter("-e").parameter("main_activity")
            .parameter("\'" + deployment.getInstrumentedDeployment().getApplicationMainActivity() + "\'")
            .parameter("-e")
            .parameter("server_port")
            .parameter(deployment.getInstrumentationConfiguration().getPort())
            .parameter(deployment.getServerBasePackage() + "/io.selendroid.ServerInstrumentation")
            .build();

        logger.fine(startApplicationInstrumentationCommand.toString());

        try {
            Monkey monkey = new Monkey(DroidiumFileUtils.createRandomEmptyFile(
                sdk.getPlatformConfiguration().getTmpDir()),
                deployment.getInstrumentedDeployment().getApplicationBasePackage(),
                true);
            device.executeShellCommand(startApplicationInstrumentationCommand.toString(), monkey);
            Monkey.wait(device, monkey, TOP_CMD);
            waitUntilSelendroidServerCommunication(port);
        } catch (Exception ex) {
            removePortForwarding(port, port);
            throw new AndroidExecutionException(ex.getMessage());
        }
    }

    /**
     * Disables Selendroid server running on Android device.
     *
     * @param deployment
     * @throws IllegalArgumentException if {@code deployment} is a null object
     */
    public void disable(SelendroidDeployment deployment) {
        Validate.notNull(device, "Android device can not be a null object!");
        Validate.notNull(deployment, "Selendroid deployment to disable can not be a null object!");

        try {
            device.executeShellCommand(new CommandBuilder("pm")
                .parameter("disable")
                .parameter(deployment.getServerBasePackage())
                .build()
                .toString());
        } catch (AndroidExecutionException ex) {
            throw new AndroidExecutionException("Unable to disable Selendroid deployment " + deployment.getServerBasePackage(), ex);
        }
    }

    /**
     * Uninstalls Selendroid server from Android device.
     *
     * @param deployment
     * @throws IllegalArgumentException if {@code deployment} is a null object
     */
    public void uninstall(SelendroidDeployment deployment) {
        Validate.notNull(device, "Android device can not be a null object!");
        Validate.notNull(deployment, "Selendroid deployment to uninstall can not be a null object!");
        try {
            device.executeShellCommand(new CommandBuilder("pm")
                .parameter("uninstall")
                .parameter(deployment.getServerBasePackage())
                .build()
                .toString());
        } catch (AndroidExecutionException ex) {
            throw new AndroidExecutionException("Unable to uninstall Selendroid server.", ex);
        } finally {
            int port = Integer.parseInt(deployment.getInstrumentationConfiguration().getPort());
            removePortForwarding(port, port);
        }
    }

    /**
     * Waits for the start of Selendroid server.
     *
     * After installation and execution of instrumentation command, we repeatedly send HTTP request to status page to get
     * response code of 200 - server is up and running and we can proceed safely to testing process.
     *
     * @param port port to wait on the communication from installed Selendroid server
     * @throws InvalidSelendroidPortException if {@code port} is invalid
     */
    private void waitUntilSelendroidServerCommunication(int port) {
        validatePort(port);

        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(CONNECTION_TIME_OUT_SECONDS * 1000)
            .setConnectionRequestTimeout(CONNECTION_TIME_OUT_SECONDS * 1000)
            .setSocketTimeout(SOCKET_TIME_OUT_SECONDS * 1000)
            .build();

        CloseableHttpClient client = HttpClients.custom()
            .setDefaultRequestConfig(config)
            .disableContentCompression()
            .build();

        HttpGet httpGet = new HttpGet(getSelendroidStatusURI(port));

        boolean connectionSuccess = false;

        for (int i = NUM_CONNECTION_RETIRES; i > 0; i--) {
            try {
                HttpResponse response = client.execute(httpGet);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    connectionSuccess = true;
                    break;
                }
                logger.log(Level.INFO, i + ": Response was not 200 from port " + port + ", response was: " + statusCode);
            } catch (ClientProtocolException e) {
                logger.log(Level.WARNING, e.getMessage());
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }

        try {
            client.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage());
        }

        if (!connectionSuccess) {
            throw new AndroidExecutionException("Unable to get successful connection from Selendroid http server.");
        }
    }

    private URI getSelendroidStatusURI(int port) {
        try {
            return new URI("http://localhost:" + port + "/wd/hub/status");
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private void createPortForwarding(int from, int to) {
        validatePort(from);
        validatePort(to);
        logger.log(Level.FINE, "Creating port forwarding from {0} to {1}", new Object[] { from, to });
        device.createPortForwarding(from, to);
    }

    private void removePortForwarding(int from, int to) {
        validatePort(from);
        validatePort(to);
        logger.log(Level.FINE, "Removing port forwarding from {0} to {1}", new Object[] { from, to });
        device.removePortForwarding(from, to);
    }

    private void validatePort(int port) {
        if (port < 1024 || port > 65535) {
            throw new InvalidSelendroidPortException("You have to specify port between 1024 and 65535, you entered: " + port);
        }
    }
}