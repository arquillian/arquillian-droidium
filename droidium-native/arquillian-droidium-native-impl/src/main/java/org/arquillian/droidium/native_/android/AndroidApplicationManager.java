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
package org.arquillian.droidium.native_.android;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.native_.deployment.impl.AndroidDeployment;
import org.arquillian.droidium.native_.utils.Command;
import org.arquillian.droidium.native_.utils.DroidiumNativeFileUtils;
import org.arquillian.droidium.native_.utils.Monkey;

/**
 * Manages deployment and undeployment of Android applications.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidApplicationManager {

    private static final Logger logger = Logger.getLogger(AndroidApplicationManager.class.getName());

    private final AndroidDevice device;

    private final ProcessExecutor executor;

    private final AndroidSDK sdk;

    private static final String TOP_CMD = "top -n 1";

    private static final String PACKAGES_LIST_CMD = "pm list packages -f";

    /**
     *
     * @param device
     * @param executor
     * @param sdk
     * @throws IllegalArgumentException if any of parameters is a null object
     */
    public AndroidApplicationManager(AndroidDevice device, ProcessExecutor executor, AndroidSDK sdk) {
        Validate.notNull(device, "Android device you are trying to pass to Android application manager is a null object!");
        Validate.notNull(executor, "Process executor you are trying to pass to Android application manager is a null object!");
        Validate.notNull(sdk, "Android SDK you are trying to pass to Android application manager is a null object!");
        this.device = device;
        this.executor = executor;
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

        Command installCommand = new Command();
        installCommand.add(sdk.getAdbPath())
            .add("-s")
            .add(device.getSerialNumber())
            .add("install")
            .add(deployment.getResignedApk().getAbsolutePath());

        logger.info("AUT installation command: " + installCommand.toString());

        String applicationBasePackage = deployment.getApplicationBasePackage();

        try {
            executor.execute(installCommand.getAsArray());
        } catch (InterruptedException e) {
            throw new AndroidExecutionException("Installation of the application '" + applicationBasePackage
                + "' was interrupted.");
        } catch (ExecutionException e) {
            throw new AndroidExecutionException("Unable to execute installation command "
                + installCommand.getAsString()
                + " for the application "
                + applicationBasePackage);
        }

        if (!device.isPackageInstalled(applicationBasePackage)) {
            throw new AndroidExecutionException("Application " + applicationBasePackage
                + " was not installed on device " + device.getSerialNumber() + ".");
        }
    }

    /**
     * Uninstalls application from Android device.
     *
     * @param deployment
     * @throws IllegalArgumentException if {@code deployment} or {@code deployment.getApplicationBasePackage()} is a null
     *         object.
     */
    public void uninstall(AndroidDeployment deployment) {
        Validate.notNull(deployment, "Android deployment you are trying to pass is a null object!");
        Validate.notNull(deployment.getApplicationBasePackage(), "Application base package name is a null object!");

        Command command = new Command().addAsString("pm uninstall " + deployment.getApplicationBasePackage());

        try {
            Monkey monkey = new Monkey(DroidiumNativeFileUtils.createRandomEmptyFile(DroidiumNativeFileUtils.getTmpDir()),
                command.getLast(), false);
            device.executeShellCommand(command.getAsString(), monkey);
            Monkey.wait(device, monkey, PACKAGES_LIST_CMD);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Kills running Android application.
     *
     * @param deployment
     * @throws IllegalArgumentException if {@code deployment} or {@code deployment.getApplicationBasePackage()} is a null
     *         object.
     */
    public void disable(AndroidDeployment deployment) {
        Validate.notNull(deployment, "Android deployment you are trying to pass is a null object!");
        Validate.notNull(deployment.getApplicationBasePackage(), "Application base package name is a null object!");

        Command command = new Command().addAsString("pm disable " + deployment.getApplicationBasePackage());

        try {
            Monkey monkey = new Monkey(DroidiumNativeFileUtils.createRandomEmptyFile(DroidiumNativeFileUtils.getTmpDir()),
                command.getLast(), false);
            device.executeShellCommand(command.getAsString(), monkey);
            Monkey.wait(device, monkey, TOP_CMD);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
