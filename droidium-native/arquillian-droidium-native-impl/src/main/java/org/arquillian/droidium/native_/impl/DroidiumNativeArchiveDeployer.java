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
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidDeployArchive;
import org.arquillian.droidium.container.spi.event.AndroidUndeployArchive;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.exception.SelendroidRebuilderException;
import org.arquillian.droidium.native_.utils.Command;
import org.arquillian.droidium.native_.utils.DroidiumNativeFileUtils;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Deploys and undeploys modified and resigned Selendroid application and application under test.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeArchiveDeployer implements AndroidArchiveDeployer {

    private static final Logger logger = Logger.getLogger(DroidiumNativeArchiveDeployer.class.getName());

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

    private File modifiedSelendroid = null;

    private File modifiedApplicationUnderTest = null;

    private File applicationUnderTest = null;

    private File tmpDir;

    /**
     * Observes deploy event and fires deployment method
     *
     * @param event
     * @throws AndroidExecutionException
     */
    public void listenDeployEvent(@Observes AndroidDeployArchive event) throws AndroidExecutionException {
        deploy(event.getArchive());
    }

    /**
     * Observes undeployment event and fires undeployment method
     *
     * @param event
     * @throws AndroidExecutionException
     */
    public void listenUndeployEvent(@Observes AndroidUndeployArchive event) throws AndroidExecutionException {
        undeploy(event.getArchive());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.arquillian.droidium.container.api.AndroidArchiveDeployer#deploy(org.jboss.shrinkwrap.api.Archive)
     */
    @Override
    public void deploy(Archive<?> deploymentArchive) {

        ProcessExecutor processExecutor = this.processExecutor.get();
        AndroidSDK androidSDK = this.androidSDK.get();
        DroidiumNativeConfiguration droidiumNativeConfiguration = this.droidiumNativeConfiguration.get();
        AndroidDevice androidDevice = this.androidDevice.get();
        AndroidApplicationHelper applicationHelper = new AndroidApplicationHelper(processExecutor, androidSDK);

        // creates temporary directory where every modified application and resources are put
        tmpDir = DroidiumNativeFileUtils.createWorkingDir(droidiumNativeConfiguration.getTmpDir());

        SelendroidHelper selendroidHelper = new SelendroidHelper(androidDevice, tmpDir);

        SelendroidRebuilder selendroidRebuilder =
            new SelendroidRebuilder(processExecutor, androidSDK, droidiumNativeConfiguration, applicationHelper, tmpDir);

        // export archive with application to tmpDir/random.file.apk
        applicationUnderTest = applicationHelper.exportArchiveToFile(
            new File(tmpDir, AndroidApplicationHelper.getRandomAPKFileName()),
            deploymentArchive);

        // copy Selendroid server to working directory for further modifications
        DroidiumNativeFileUtils.copyFileToDirectory(droidiumNativeConfiguration.getServerApk(), tmpDir);

        File selendroidWorkingCopy = new File(tmpDir, droidiumNativeConfiguration.getServerApk().getName());

        selendroidRebuilder.setApplicationBasePackage(applicationHelper.getApplicationBasePackage(applicationUnderTest));

        APKSigner signer = new APKSigner(processExecutor, androidSDK, droidiumNativeConfiguration, applicationHelper);

        // signs rebuilt Selendroid server APK
        File rebuiltSelendroidServerToResign;
        try {
            rebuiltSelendroidServerToResign = selendroidRebuilder.rebuild(selendroidWorkingCopy);
        } catch (SelendroidRebuilderException ex) {
            throw new AndroidExecutionException("Unable to rebuild Selendroid server APK: " + ex.getMessage());
        }

        // resignes modified Selendroid server
        modifiedSelendroid = new File(tmpDir, AndroidApplicationHelper.getRandomAPKFileName());
        signer.resign(rebuiltSelendroidServerToResign, modifiedSelendroid);

        // resigns application under test
        modifiedApplicationUnderTest = new File(tmpDir, AndroidApplicationHelper.getRandomAPKFileName());
        signer.resign(applicationUnderTest, modifiedApplicationUnderTest);

        // command for installation of modified Selendroid server
        Command selendroidInstallCommand = new Command();
        selendroidInstallCommand.add(androidSDK.getAdbPath())
            .add("-s")
            .add(androidDevice.getSerialNumber())
            .add("install")
            .add(modifiedSelendroid.getAbsolutePath());

        logger.info("Selendroid server installation command: " + selendroidInstallCommand.toString());

        // command for installation of application under test
        Command applicationInstallCommand = new Command();
        applicationInstallCommand.add(androidSDK.getAdbPath())
            .add("-s")
            .add(androidDevice.getSerialNumber())
            .add("install")
            .add(modifiedApplicationUnderTest.getAbsolutePath());

        logger.info("Application under test installation command: " + applicationInstallCommand.toString());

        // install Selendroid server
        try {
            processExecutor.execute(selendroidInstallCommand.getAsList().toArray(new String[0]));
        } catch (InterruptedException ex) {
            throw new AndroidExecutionException("Selendroid installation was interrupted.");
        } catch (ExecutionException ex) {
            throw new AndroidExecutionException("Unable to execute Selendroid deployment process.");
        }

        // check that Selendroid server is installed
        if (!androidDevice.isPackageInstalled(applicationHelper.getApplicationBasePackage(modifiedSelendroid))) {
            throw new AndroidExecutionException("Modified Selendroid server was not installed on device!");
        }

        // install application under test
        try {
            processExecutor.execute(applicationInstallCommand.getAsList().toArray(new String[0]));
        } catch (InterruptedException e) {
            throw new AndroidExecutionException("Installation of application under test was interrupted.");
        } catch (ExecutionException e) {
            throw new AndroidExecutionException("Unable to execute installation command of application under test.");
        }

        // check that application under test is installed
        if (!androidDevice.isPackageInstalled(applicationHelper.getApplicationBasePackage(applicationUnderTest))) {
            throw new AndroidExecutionException("Application under test was not installed on device!");
        }

        // create port forwarding
        logger.log(Level.INFO, "Creating port forwarding from {0} to {1}",
            new Object[] { androidDevice.getDroneHostPort(), androidDevice.getDroneGuestPort() });

        androidDevice.createPortForwarding(androidDevice.getDroneHostPort(), androidDevice.getDroneGuestPort());

        // command for starting instrumentation of package under test by modified Selendroid server
        Command startApplicationInstrumentationCommand = new Command();
        startApplicationInstrumentationCommand.add("am")
            .add("instrument")
            .add("-e")
            .add("main_activity")
            .add("\'" + applicationHelper.getApplicationMainActivity(applicationUnderTest) + "\'")
            .add(SELENDROID_SERVER_ACTIVITY);

        logger.info(startApplicationInstrumentationCommand.toString());

        // execute instrumentation and waits until server is started and able to communicate with us
        try {
            selendroidHelper.startInstrumentation(
                startApplicationInstrumentationCommand,
                applicationHelper.getApplicationBasePackage(applicationUnderTest));
        } catch (AndroidExecutionException ex) {
            androidDevice.removePortForwarding(androidDevice.getDroneHostPort(), androidDevice.getDroneHostPort());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.arquillian.droidium.container.api.AndroidArchiveDeployer#undeploy(org.jboss.shrinkwrap.api.Archive)
     */
    @Override
    public void undeploy(Archive<?> archive) {

        ProcessExecutor processExecutor = this.processExecutor.get();
        AndroidDevice androidDevice = this.androidDevice.get();
        AndroidSDK androidSDK = this.androidSDK.get();
        DroidiumNativeConfiguration droidiumNativeConfiguration = this.droidiumNativeConfiguration.get();

        AndroidApplicationHelper applicationHelper = new AndroidApplicationHelper(processExecutor, androidSDK);
        SelendroidHelper selendroidHelper = new SelendroidHelper(androidDevice, tmpDir);

        String selendroidBasePackage = applicationHelper.getApplicationBasePackage(modifiedSelendroid);
        String applicationBasePackage = applicationHelper.getApplicationBasePackage(applicationUnderTest);

        // command for stopping application under test
        Command stopApplicationUnderTestCommand = new Command();
        stopApplicationUnderTestCommand.add("pm")
            .add("disable")
            .add(applicationBasePackage);

        // stop application under test
        selendroidHelper.stopApplicationUnderTest(stopApplicationUnderTestCommand);

        // command for uninstalling application under test
        Command applicationUninstallCommand = new Command();
        applicationUninstallCommand.add("pm")
            .add("uninstall")
            .add(applicationBasePackage);

        // uninstall application under test
        selendroidHelper.uninstallApplicationUnderTest(applicationUninstallCommand);

        // command for stopping Selendroid server
        Command stopSelendroidServer = new Command();
        stopSelendroidServer.add("pm")
            .add("disable")
            .add(selendroidBasePackage);

        // stop Selendroid server
        selendroidHelper.stopSelendroidServer(stopSelendroidServer);

        // command for uninstalling Selendroid server
        Command selendroidUninstallCommand = new Command();
        selendroidUninstallCommand.add("pm")
            .add("uninstall")
            .add(selendroidBasePackage);

        // uninstall Selendroid server
        selendroidHelper.uninstallSelendroidServer(selendroidUninstallCommand);

        // remove port forwarding
        androidDevice.removePortForwarding(androidDevice.getDroneHostPort(), androidDevice.getDroneGuestPort());

        // remove working directory if not specified otherwise
        if (droidiumNativeConfiguration.getRemoveTmpDir()) {
            DroidiumNativeFileUtils.removeWorkingDir(tmpDir);
        }
    }

}
