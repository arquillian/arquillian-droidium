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
package org.arquillian.droidium.native_.deployment.impl;

import java.io.File;

import org.arquillian.droidium.container.AndroidDeployableContainer;
import org.arquillian.droidium.container.api.AndroidArchiveDeployer;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidDeployArchive;
import org.arquillian.droidium.container.spi.event.AndroidUndeployArchive;
import org.arquillian.droidium.native_.android.AndroidApplicationManager;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.deployment.DeploymentRegister;
import org.arquillian.droidium.native_.selendroid.SelendroidRebuilder;
import org.arquillian.droidium.native_.selendroid.SelendroidServerManager;
import org.arquillian.droidium.native_.sign.APKSigner;
import org.arquillian.droidium.native_.spi.event.InstrumentationPerformed;
import org.arquillian.droidium.native_.spi.event.InstrumentationRemoved;
import org.arquillian.droidium.native_.spi.event.PerformInstrumentation;
import org.arquillian.droidium.native_.spi.event.RemoveInstrumentation;
import org.arquillian.droidium.native_.utils.AndroidApplicationHelper;
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

    private AndroidApplicationHelper applicationHelper;

    private SelendroidRebuilder selendroidRebuilder;

    private APKSigner signer;

    // Deployment registers

    private DeploymentRegister<AndroidDeployment> androidRegister = new DeploymentRegister<AndroidDeployment>();

    private DeploymentRegister<SelendroidDeployment> selendroidRegister = new DeploymentRegister<SelendroidDeployment>();

    // Application Managers

    private AndroidApplicationManager androidApplicationManager;

    private SelendroidServerManager selendroidServerManager;

    // Holders of injected resources

    private AndroidDevice device;

    private AndroidSDK sdk;

    private DroidiumNativeConfiguration configuration;

    private ProcessExecutor executor;

    /**
     * Prepares resources for {@code DeploymentController}. It creates new temporary directory after every container startup
     * where all subsequent resources created and used for this container are stored.
     *
     * @param event
     */
    public void prepare(@Observes AfterStart event) {
        device = androidDevice.get();
        sdk = androidSDK.get();
        configuration = droidiumNativeConfiguration.get();
        executor = processExecutor.get();

        DroidiumNativeFileUtils.createWorkingDir(configuration.getTmpDir());

        applicationHelper = new AndroidApplicationHelper(executor, sdk);

        selendroidRebuilder = new SelendroidRebuilder(executor, sdk);
        signer = new APKSigner(executor, sdk, configuration);

        androidApplicationManager = new AndroidApplicationManager(device, executor, sdk);
        selendroidServerManager = new SelendroidServerManager(device, executor, sdk);
    }

    /**
     * Observes deployment event from {@link AndroidDeployableContainer#deploy(Archive)}. <br>
     * <br>
     * Observes:
     * <ul>
     * <li>{@link AndroidDeployArchive}</li>
     * </ul>
     *
     * @param event
     */
    public void deploy(@Observes AndroidDeployArchive event) {
        deploy(event.getArchive());
    }

    /**
     * Observes undeployment event from {@link AndroidDeployableContainer#undeploy(Archive)}. <br>
     * <br>
     * Observes:
     * <ul>
     * <li>{@link AndroidUndeployArchive}</li>
     * </ul>
     *
     * @param event
     */
    public void undeploy(@Observes AndroidUndeployArchive event) {
        undeploy(event.getArchive());
    }

    @Override
    public void deploy(Archive<?> archive) {
        File deployApk = DroidiumNativeFileUtils.export(archive,
            new File(DroidiumNativeFileUtils.getTmpDir(),
                AndroidApplicationHelper.getRandomAPKFileName()));

        File resignedApk = signer.resign(deployApk,
            new File(DroidiumNativeFileUtils.getTmpDir(),
                AndroidApplicationHelper.getRandomAPKFileName()));

        AndroidDeployment deployment = new AndroidDeployment();
        deployment.setDeployment(archive)
            .setDeployApk(deployApk)
            .setResignedApk(resignedApk)
            .setApplicationBasePackage(applicationHelper.getApplicationBasePackage(resignedApk))
            .setApplicationMainActivity(applicationHelper.getApplicationMainActivity(resignedApk));

        androidRegister.add(deployment);
        androidApplicationManager.install(deployment);
    }

    @Override
    public void undeploy(Archive<?> archive) {
        AndroidDeployment deployment = androidRegister.get(archive);
        androidApplicationManager.disable(deployment);
        androidApplicationManager.uninstall(deployment);
    }

    /**
     * Performs instrumentation on the just installed Android application. <br>
     * <br>
     * Observes:
     * <ul>
     * <li>{@link PerformInstrumentation}</li>
     * </ul>
     * Fires: <br>
     * <br>
     * <ul>
     * <li>{@link InstrumentationPerformed}</li>
     * </ul>
     *
     * @param event
     */
    public void performInstrumentation(@Observes PerformInstrumentation event) {

        File selendroidWorkingCopy = DroidiumNativeFileUtils.copyFileToDirectory(configuration.getServerApk(),
            DroidiumNativeFileUtils.getTmpDir());

        String applicationBasePackage = androidRegister.getLast().getApplicationBasePackage();
        String selendroidPackageName = applicationHelper.getApplicationBasePackage(selendroidWorkingCopy) + "_" + androidRegister.getSize();

        File selendroidRebuilt = selendroidRebuilder.rebuild(selendroidWorkingCopy, selendroidPackageName, applicationBasePackage);

        File selendroidResigned = signer.resign(selendroidRebuilt,
            new File(DroidiumNativeFileUtils.getTmpDir(), AndroidApplicationHelper.getRandomAPKFileName()));

        SelendroidDeployment deployment = new SelendroidDeployment();
        deployment.setWorkingCopy(selendroidWorkingCopy)
            .setRebuilt(selendroidRebuilt)
            .setResigned(selendroidResigned)
            .setServerBasePackage(applicationHelper.getApplicationBasePackage(selendroidResigned))
            .setInstrumentedDeployment(androidRegister.getLast())
            .setInstrumentationConfiguration(event.getConfiguration());

        selendroidRegister.add(deployment);
        selendroidServerManager.install(deployment);
        selendroidServerManager.instrument(deployment);

        instrumentationPerformedEvent.fire(new InstrumentationPerformed());
    }

    /**
     * Removes instrumentation meaning it stops the instance of Selendroid server for just removed application and uninstalls
     * the server afterwards. <br>
     * <br>
     * Observes:
     * <ul>
     * <li>{@link RemoveInstrumentation}</li>
     * </ul>
     * Fires: <br>
     * <br>
     * <ul>
     * <li>{@link InstrumentationRemoved}</li>
     * </ul>
     *
     * @param event
     */
    public void removeInstrumentation(@Observes RemoveInstrumentation event) {
        SelendroidDeployment deployment = selendroidRegister.get(event.getPackage());

        selendroidServerManager.disable(deployment);
        selendroidServerManager.uninstall(deployment);

        instrumentationRemovedEvent.fire(new InstrumentationRemoved());
    }

    /**
     * Removes the temporary directory which was created in {@link DeploymentController#prepare(AfterStart)}. Removing of the
     * directory is performed only in the case when {@link DroidiumNativeConfiguration#getRemoveTmpDir()} is true.
     *
     * @param event
     */
    public void removeResources(@Observes AfterStop event) {
        if (event.getDeployableContainer().getConfigurationClass().equals(AndroidContainerConfiguration.class)) {
            if (configuration.getRemoveTmpDir()) {
                DroidiumNativeFileUtils.removeWorkingDir(DroidiumNativeFileUtils.getTmpDir());
            }
        }
    }

}
