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
package org.arquillian.droidium.container.impl;

import java.io.IOException;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.spi.AndroidDeployment;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.arquillian.droidium.container.utils.Monkey;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessDetails;
import org.arquillian.spacelift.process.impl.CommandTool;

/**
 * Manages deployment and undeployment of Android applications. It installs, uninstalls and disables running applications on
 * Android device.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidApplicationManager {

    private static final Logger logger = Logger.getLogger(AndroidApplicationManager.class.getName());

    private AndroidDevice device;

    private final AndroidSDK sdk;

    private static final String TOP_CMD = "top -n 1";

    private static final String PACKAGES_LIST_CMD = "pm list packages -f";

    /**
     *
     * @param device
     * @param executor
     * @param sdk
     * @throws IllegalArgumentException if either {@code device} or {@code executor} or {@code sdk} is a null object
     */
    public AndroidApplicationManager(AndroidDevice device, AndroidSDK sdk) {
        Validate.notNull(device, "Android device you are trying to pass to Android application manager is a null object!");
        Validate.notNull(sdk, "Android SDK you are trying to pass to Android application manager is a null object!");
        this.device = device;
        this.sdk = sdk;
    }

    /**
     * Installs application into Android device.
     *
     * @param deployment deployment to install
     * @throws AndroidExecutionException if the installation fails
     * @throws IllegalArgumentException if {@code deployment} or {@code deployment.getResignedApk()} or
     *         {@code deployment.getApplicationBasePackage()} is a null object.
     */
    public void install(AndroidDeployment deployment) {
        Validate.notNull(deployment, "Android deployment you are trying to pass is a null object!");
        Validate.notNull(deployment.getResignedApk(), "Application to install is a null object!");
        Validate.notNull(deployment.getApplicationBasePackage(), "Application base package name is a null object!");

        Command installCommand = new CommandBuilder(sdk.getAdbPath())
            .parameters("-s")
            .parameter(device.getSerialNumber())
            .parameter("install")
            .parameter(deployment.getResignedApk().getAbsolutePath())
            .build();

        logger.fine("AUT installation command: " + installCommand.toString());

        String applicationBasePackage = deployment.getApplicationBasePackage();

        if (device.isPackageInstalled(applicationBasePackage)) {
            device.uninstallPackage(applicationBasePackage);
        }

        ProcessDetails processDetails = null;

        try {
            processDetails = Tasks.prepare(CommandTool.class)
                .command(installCommand)
                .execute()
                .await();
        } catch (AndroidExecutionException e) {
            // rewrap exception to have better stacktrace
            throw new AndroidExecutionException(e, "Unable to execute installation command '{0} {1}' for the application {2}. "
                + "Execution ended with exit code {3} with output\n{4}",
                sdk.getAdbPath(),
                installCommand,
                applicationBasePackage,
                processDetails.getExitValue(),
                processDetails.getOutput());
        }

        if (!device.isPackageInstalled(applicationBasePackage)) {
            throw new AndroidExecutionException("Application " + applicationBasePackage + " was not installed on device "
                + device.getSerialNumber() + ".");
        }
    }

    /**
     * Uninstalls application from Android device.
     *
     * @param deployment
     * @throws IllegalArgumentException if {@code deployment} or {@code deployment.getApplicationBasePackage()} is a null
     *         object.
     * @throws AndroidExecutionException when uninstallation fails
     */
    public void uninstall(AndroidDeployment deployment) {
        Validate.notNull(deployment, "Android deployment you are trying to uninstall can not be a null object!");
        Validate.notNull(deployment.getApplicationBasePackage(), "Application base package can not be a null object!");

        StringBuilder sb = new StringBuilder();
        String command = sb.append("pm ").append("uninstall ").append(deployment.getApplicationBasePackage()).toString();

        try {
            Monkey monkey = new Monkey(DroidiumFileUtils.createRandomEmptyFile(sdk.getPlatformConfiguration().getTmpDir()),
                deployment.getApplicationBasePackage(), false);
            device.executeShellCommand(command, monkey);

            Monkey.wait(device, monkey, PACKAGES_LIST_CMD);
        } catch (IOException ex) {
            throw new AndroidExecutionException("Unable to uninstall application " + deployment.getApplicationBasePackage()
                + " from Android device.");
        }
    }

    /**
     * Kills running Android application.
     *
     * @param deployment
     * @throws IllegalArgumentException if {@code deployment} or {@code deployment.getApplicationBasePackage()} is a null
     *         object.
     * @throws AndroidExecutionException when killing fails
     */
    public void disable(AndroidDeployment deployment) {
        Validate.notNull(deployment, "Android deployment you are trying to kill can not be a null object!");
        Validate.notNull(deployment.getApplicationBasePackage(), "Application base package name can not be a null object!");

        StringBuilder sb = new StringBuilder();
        String command = sb.append("pm ").append("disable ").append(deployment.getApplicationBasePackage()).toString();

        try {
            Monkey monkey = new Monkey(
                DroidiumFileUtils.createRandomEmptyFile(
                    sdk.getPlatformConfiguration().getTmpDir()),
                deployment.getApplicationBasePackage(),
                false);
            device.executeShellCommand(command.toString(), monkey);
            Monkey.wait(device, monkey, TOP_CMD);
        } catch (IOException e) {
            throw new AndroidExecutionException("Unable to disable running application "
                + deployment.getApplicationBasePackage());
        }
    }

    public AndroidApplicationManager setDevice(AndroidDevice device) {
        this.device = device;
        return this;
    }
}