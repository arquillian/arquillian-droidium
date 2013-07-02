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
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.arquillian.droidium.container.api.AndroidArchiveDeployer;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidDeployArchive;
import org.arquillian.droidium.container.spi.event.AndroidUndeployArchive;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.utils.Command;
import org.arquillian.droidium.native_.utils.FileIdentifierGenerator;
import org.arquillian.droidium.native_.utils.IdentifierType;
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

    private static final String SELENDROID_SERVER_ACTIVITY = "org.openqa.selendroid/.ServerInstrumentation";

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
     * Observer deploy event and fires deployment method
     *
     * @param event
     * @throws AndroidExecutionException
     */
    public void listenDeployEvent(@Observes AndroidDeployArchive event) throws AndroidExecutionException {
        deploy(event.getArchive());
    }

    /**
     * Observe undeploymente event and fires undeployment method
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
        SelendroidHelper selendroidHelper = new SelendroidHelper(androidDevice, droidiumNativeConfiguration.getLogFile());

        // creates temporary directory where every modified application and resources are put
        tmpDir = createWorkingDir(droidiumNativeConfiguration.getTmpDir());

        SelendroidRebuilder selendroidRebuilder =
            new SelendroidRebuilder(processExecutor, androidSDK, droidiumNativeConfiguration, applicationHelper, tmpDir);

        // export archive with application to tmpDir/random.file.apk
        applicationUnderTest = applicationHelper.exportArchiveToFile(
            new File(tmpDir, AndroidApplicationHelper.getRandomAPKFileName()),
            deploymentArchive);

        copyFileToDirectory(droidiumNativeConfiguration.getServerApk(), tmpDir);

        File selendroidWorkingCopy = new File(tmpDir, droidiumNativeConfiguration.getServerApk().getName());

        selendroidRebuilder.setApplicationBasePackage(applicationHelper.getApplicationBasePackage(applicationUnderTest));

        APKSigner signer = new APKSigner(processExecutor, androidSDK, droidiumNativeConfiguration, applicationHelper);

        // signs rebuilt Selendroid into modifiedSelendroid
        modifiedSelendroid = new File(tmpDir, AndroidApplicationHelper.getRandomAPKFileName());
        signer.sign(selendroidRebuilder.rebuild(selendroidWorkingCopy), modifiedSelendroid);

        // signs application under test
        modifiedApplicationUnderTest = new File(tmpDir, AndroidApplicationHelper.getRandomAPKFileName());
        signer.reSign(applicationUnderTest, modifiedApplicationUnderTest);

        // command for installation of modified Selendroid
        Command selendroidInstallCommand = new Command();
        selendroidInstallCommand.add(androidSDK.getAdbPath())
            .add("-s")
            .add(androidDevice.getSerialNumber())
            .add("install")
            .add(modifiedSelendroid.getAbsolutePath());

        logger.info("Selendroid server install command: " + selendroidInstallCommand.toString());

        // command for installation of application under test
        Command applicationInstallCommand = new Command();
        applicationInstallCommand.add(androidSDK.getAdbPath())
            .add("-s")
            .add(androidDevice.getSerialNumber())
            .add("install")
            .add(modifiedApplicationUnderTest.getAbsolutePath());

        logger.info("Application under test install command: " + applicationInstallCommand.toString());

        // install Selendroid
        try {
            processExecutor.execute(selendroidInstallCommand.getAsList().toArray(new String[0]));
        } catch (InterruptedException ex) {
            throw new AndroidExecutionException("Selendroid installation was interrupted.");
        } catch (ExecutionException ex) {
            throw new AndroidExecutionException("Unable to execute Selendroid deployment process.");
        }

        // check Selendroid is installed
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

        // check application under test is installed
        if (!androidDevice.isPackageInstalled(applicationHelper.getApplicationBasePackage(applicationUnderTest))) {
            throw new AndroidExecutionException("Application under test was not installed on device!");
        }

        // create port forwarding
        logger.log(Level.INFO, "Creating port forwarding from {0} to {1}",
            new Object[] { androidDevice.getDroneHostPort(), androidDevice.getDroneGuestPort() });

        androidDevice.createPortForwarding(androidDevice.getDroneHostPort(), androidDevice.getDroneGuestPort());

        // command for starting instrumentation of package under test by Selendroid
        Command startApplicationInstrumentationCommand = new Command();
        startApplicationInstrumentationCommand.add("am")
            .add("instrument")
            .add("-e")
            .add("main_activity")
            .add("\'" + applicationHelper.getApplicationMainActivity(applicationUnderTest) + "\'")
            .add(SELENDROID_SERVER_ACTIVITY);

        logger.info(startApplicationInstrumentationCommand.toString());

        selendroidHelper.startInstrumentation(
            startApplicationInstrumentationCommand,
            applicationHelper.getApplicationBasePackage(applicationUnderTest));

        selendroidHelper.waitForServerHTTPStart();
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
        SelendroidHelper selendroidHelper = new SelendroidHelper(androidDevice, droidiumNativeConfiguration.getLogFile());

        String selendroidBasePackage = applicationHelper.getApplicationBasePackage(modifiedSelendroid);
        String applicationBasePackage = applicationHelper.getApplicationBasePackage(applicationUnderTest);

        // command for stopping instrumentation
        Command stopApplicationInstrumentationCommand = new Command();
        stopApplicationInstrumentationCommand.add("am")
            .add("force-stop")
            .add(selendroidBasePackage);

        // stop Selendroid instrumentation
        selendroidHelper.stopInstrumentation(stopApplicationInstrumentationCommand);

        // command for uninstalling Selendroid server
        Command selendroidUninstallCommand = new Command();
        selendroidUninstallCommand.add("pm")
            .add("uninstall")
            .add(selendroidBasePackage);

        // uninstall Selendroid server
        selendroidHelper.uninstallSelendroid(selendroidUninstallCommand);

        // command for uninstalling application under test
        Command applicationUninstallCommand = new Command();
        applicationUninstallCommand.add("pm")
            .add("uninstall")
            .add(applicationBasePackage);

        // uninstall application under test
        selendroidHelper.uninstallApplicationUnderTest(applicationUninstallCommand);

        // remove port forwarding
        androidDevice.removePortForwarding(androidDevice.getDroneHostPort(), androidDevice.getDroneGuestPort());

        if (droidiumNativeConfiguration.getRemoveTmpDir()) {
            removeWorkingDir(tmpDir);
        }
    }

    private void removeWorkingDir(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            logger.log(Level.INFO, "Unable to delete temporary working dir {0}. Reason: {1}",
                new Object[] { dir.getAbsolutePath(), e.getMessage() });
        }
    }

    /**
     * Creates directory with random name in {@code System.getProperty("java.io.tmpdir")}
     *
     * @return directory in system temporary directory
     */
    private File createWorkingDir(File parent) {
        FileIdentifierGenerator fig = new FileIdentifierGenerator();
        File temp;

        try {
            do {
                temp = new File(parent, fig.getIdentifier(IdentifierType.FILE.getClass()));
            } while (!temp.mkdir());
        } catch (SecurityException ex) {
            logger.severe("Security manager denies to create the working dir in " + parent.getAbsolutePath());
            throw new RuntimeException("Unable to create working directory in " + parent.getAbsolutePath());
        }

        return temp;
    }

    /**
     * Copies file to directory
     *
     * @param src source file
     * @param dest destination directory
     */
    private void copyFileToDirectory(File src, File dest) {
        try {
            FileUtils.copyFileToDirectory(src, dest);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to copy " + src.getAbsolutePath() + " to " + dest.getAbsolutePath());
        }
    }
}
