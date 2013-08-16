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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidArchiveDeployer;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidDeployArchive;
import org.arquillian.droidium.container.spi.event.AndroidUndeployArchive;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.exception.SelendroidRebuilderException;
import org.arquillian.droidium.native_.spi.event.InstrumentationPerformed;
import org.arquillian.droidium.native_.spi.event.InstrumentationRemoved;
import org.arquillian.droidium.native_.spi.event.PerformInstrumentation;
import org.arquillian.droidium.native_.spi.event.RemoveInstrumentation;
import org.arquillian.droidium.native_.utils.Command;
import org.arquillian.droidium.native_.utils.DroidiumNativeFileUtils;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.AfterStop;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Deploys, undeploys and instruments Android packages. <br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AfterStart}</li>
 * <li>{@link AndroidDeployArchive}</li>
 * <li>{@link AndroidUndeployArchive}</li>
 * <li>{@link PerformInstrumentation}</li>
 * <li>{@link RemoveInstrumentation}</li>
 * </ul>
 *
 * Fires: <br>
 * <br>
 * <ul>
 * <li>{@link InstrumentationPerformed}</li>
 * <li>{@link InstrumentationRemoved}</li>
 * </ul>
 */
public class DeploymentController implements AndroidArchiveDeployer {

    private static final Logger logger = Logger.getLogger(DeploymentController.class.getName());

    /**
     * Activity to start after Selendroid server is installed in order to instrument application under test
     */
    private static final String SELENDROID_SERVER_ACTIVITY = "io.selendroid/.ServerInstrumentation";

    @Inject
    private Instance<AndroidDevice> androidDevice;

    @Inject
    private Instance<AndroidSDK> androidSDK;

    @Inject
    private Instance<DroidiumNativeConfiguration> droidiumNativeConfiguration;

    @Inject
    private Instance<ProcessExecutor> processExecutor;

    @Inject
    private Event<InstrumentationPerformed> instrumentationPerformedEvent;

    @Inject
    private Event<InstrumentationRemoved> instrumentationRemovedEvent;

    private File tmpDir;

    private AndroidApplicationHelper applicationHelper;

    private SelendroidHelper selendroidHelper;

    private SelendroidRebuilder selendroidRebuilder;

    private APKSigner signer;

    private DeploymentRegister register;

    private SelendroidDeployment selendroidDeployment;

    // holders of injected resources

    private AndroidDevice device;

    private AndroidSDK sdk;

    private DroidiumNativeConfiguration configuration;

    private ProcessExecutor executor;

    public void prepare(@Observes AfterStart event) {
        device = androidDevice.get();
        sdk = androidSDK.get();
        configuration = droidiumNativeConfiguration.get();
        executor = processExecutor.get();

        tmpDir = DroidiumNativeFileUtils.createWorkingDir(configuration.getTmpDir());

        applicationHelper = new AndroidApplicationHelper(executor, sdk);

        selendroidHelper = new SelendroidHelper(device, configuration);
        selendroidHelper.setTmpDir(tmpDir);

        selendroidRebuilder = new SelendroidRebuilder(executor, sdk, applicationHelper, tmpDir);
        signer = new APKSigner(executor, sdk, configuration, applicationHelper);
        register = new DeploymentRegister();
        selendroidDeployment = new SelendroidDeployment();
    }

    public void deploy(@Observes AndroidDeployArchive event) throws AndroidExecutionException {
        deploy(event.getArchive());
    }

    @Override
    public void deploy(Archive<?> archive) throws AndroidExecutionException {

        File deployApk = applicationHelper.exportArchiveToFile(
            new File(tmpDir, AndroidApplicationHelper.getRandomAPKFileName()), archive);

        File resignedApk = new File(tmpDir, AndroidApplicationHelper.getRandomAPKFileName());
        signer.resign(deployApk, resignedApk);

        AndroidDeployment deployment = new AndroidDeployment();
        deployment.setDeployArchive(archive);
        deployment.setDeployApk(deployApk);
        deployment.setResignedApk(resignedApk);
        deployment.setApplicationBasePackage(applicationHelper.getApplicationBasePackage(resignedApk));
        deployment.setApplicationMainActivity(applicationHelper.getApplicationMainActivity(resignedApk));

        register.add(deployment);

        Command applicationInstallCommand = new Command();
        applicationInstallCommand.add(sdk.getAdbPath())
            .add("-s")
            .add(device.getSerialNumber())
            .add("install")
            .add(deployment.getResignedApk().getAbsolutePath());

        try {
            executor.execute(applicationInstallCommand.getAsList().toArray(new String[0]));
        } catch (InterruptedException e) {
            throw new AndroidExecutionException("Installation of application '" + deployment.getApplicationBasePackage()
                + "' was interrupted.");
        } catch (ExecutionException e) {
            throw new AndroidExecutionException("Unable to execute installation command for the application "
                + deployment.getApplicationBasePackage());
        }

        if (!device.isPackageInstalled(deployment.getApplicationBasePackage())) {
            throw new AndroidExecutionException("Application " + deployment.getApplicationBasePackage()
                + " was not installed on device!");
        }

    }

    public void undeploy(@Observes AndroidUndeployArchive event) throws AndroidExecutionException {
        undeploy(event.getArchive());
    }

    @Override
    public void undeploy(Archive<?> archive) {
        String applicationBasePackage = register.get(archive).getApplicationBasePackage();

        Command stopApplicationCommand = new Command();
        stopApplicationCommand.add("pm")
            .add("disable")
            .add(applicationBasePackage);

        selendroidHelper.stopApplication(stopApplicationCommand);

        Command applicationUninstallCommand = new Command();
        applicationUninstallCommand.add("pm")
            .add("uninstall")
            .add(applicationBasePackage);

        selendroidHelper.uninstallApplication(applicationUninstallCommand);
    }

    /**
     * Performs the instrumentation of underlying package after it is installed. In our case, Selendroid server is rebuilt and
     * installed taking into account just installed package in {@code @Deployment}. The instrumentation command is executed
     * afterwards and port forwarding is created.<br>
     * <br>
     *
     * Fires: <br>
     * <ul>
     * <li>{@code InstrumentationPerformed}</li>
     * </ul>
     *
     * @param event
     */
    public void performInstrumentation(@Observes PerformInstrumentation event) {

        DroidiumNativeFileUtils.copyFileToDirectory(configuration.getServerApk(), tmpDir);
        File selendroidWorkingCopy = new File(tmpDir, configuration.getServerApk().getName());

        selendroidRebuilder.setApplicationBasePackage(register.getLast().getApplicationBasePackage());

        File rebuiltSelendroid;
        try {
            rebuiltSelendroid = selendroidRebuilder.rebuild(selendroidWorkingCopy);
        } catch (SelendroidRebuilderException ex) {
            throw new AndroidExecutionException("Unable to rebuild Selendroid server APK: " + ex.getMessage());
        }

        File resignedSelendroid = new File(tmpDir, AndroidApplicationHelper.getRandomAPKFileName());
        signer.resign(rebuiltSelendroid, resignedSelendroid);

        Command selendroidInstallCommand = new Command();
        selendroidInstallCommand.add(sdk.getAdbPath())
            .add("-s")
            .add(device.getSerialNumber())
            .add("install")
            .add(resignedSelendroid.getAbsolutePath());

        logger.info("Selendroid server installation command: " + selendroidInstallCommand.toString());

        selendroidDeployment.setWorkingCopy(selendroidWorkingCopy);
        selendroidDeployment.setRebuilt(rebuiltSelendroid);
        selendroidDeployment.setResigned(resignedSelendroid);
        selendroidDeployment.setBasePackage(applicationHelper.getApplicationBasePackage(resignedSelendroid));

        try {
            executor.execute(selendroidInstallCommand.getAsList().toArray(new String[0]));
        } catch (InterruptedException ex) {
            throw new AndroidExecutionException("Selendroid installation was interrupted.");
        } catch (ExecutionException ex) {
            throw new AndroidExecutionException("Unable to execute Selendroid installation process.");
        }

        if (!device.isPackageInstalled(selendroidDeployment.getBasePackage())) {
            throw new AndroidExecutionException("Modified Selendroid server was not installed on device.");
        }

        createPortForwarding();

        Command startApplicationInstrumentationCommand = new Command();
        startApplicationInstrumentationCommand.add("am")
            .add("instrument")
            .add("-e")
            .add("main_activity")
            .add("\'" + register.getLast().getApplicationMainActivity() + "\'")
            .add("-e")
            .add("server_port")
            .add(configuration.getServerPort())
            .add(SELENDROID_SERVER_ACTIVITY);

        logger.info(startApplicationInstrumentationCommand.toString());

        try {
            selendroidHelper.startInstrumentation(startApplicationInstrumentationCommand,
                register.getLast().getApplicationBasePackage());
        } catch (AndroidExecutionException ex) {
            removePortForwarding();
        }

        instrumentationPerformedEvent.fire(new InstrumentationPerformed());
    }

    /**
     * Selendroid server is removed from Android device and port forwarding is destroyed.<br>
     * <br>
     *
     * Fires: <br>
     * <ul>
     * <li>{@code InstrumentationRemoved}</li>
     * </ul>
     *
     * @param event
     */
    public void removeInstrumentation(@Observes RemoveInstrumentation event) {

        selendroidHelper.stopSelendroidServer(new Command()
            .add("pm")
            .add("disable")
            .add(selendroidDeployment.getBasePackage()));

        selendroidHelper.uninstallSelendroidServer(new Command()
            .add("pm")
            .add("uninstall")
            .add(selendroidDeployment.getBasePackage()));

        removePortForwarding();

        instrumentationRemovedEvent.fire(new InstrumentationRemoved());
    }

    /**
     * Removes temporary directory where Droidium placed all resources needed during the test.
     *
     * @param event
     */
    public void removeDroidiumResources(@Observes AfterStop event) {
        if (event.getDeployableContainer().getConfigurationClass().equals(AndroidContainerConfiguration.class)) {
            if (configuration.getRemoveTmpDir()) {
                DroidiumNativeFileUtils.removeWorkingDir(tmpDir);
            }
        }
    }

    private void createPortForwarding() {
        logger.log(Level.INFO, "Creating port forwarding from {0} to {1}",
            new Object[] { device.getDroneHostPort(), configuration.getServerPort() });
        device.createPortForwarding(device.getDroneHostPort(), Integer.parseInt(configuration.getServerPort()));
    }

    private void removePortForwarding() {
        logger.log(Level.INFO, "Removing port forwarding from {0} to {1}",
            new Object[] { device.getDroneHostPort(), configuration.getServerPort() });
        device.removePortForwarding(device.getDroneHostPort(), Integer.parseInt(configuration.getServerPort()));
    }

}
