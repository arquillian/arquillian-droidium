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
package org.arquillian.droidium.native_.configuration;

import java.io.File;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.native_.activity.ActivityWebDriverMapper;
import org.arquillian.droidium.native_.activity.NativeActivityManager;
import org.arquillian.droidium.native_.android.AndroidApplicationHelper;
import org.arquillian.droidium.native_.android.AndroidApplicationManager;
import org.arquillian.droidium.native_.deployment.ActivityDeploymentMapper;
import org.arquillian.droidium.native_.deployment.AndroidDeploymentRegister;
import org.arquillian.droidium.native_.deployment.DeploymentWebDriverMapper;
import org.arquillian.droidium.native_.deployment.ExtensionDroneMapper;
import org.arquillian.droidium.native_.deployment.SelendroidDeploymentRegister;
import org.arquillian.droidium.native_.instrumentation.DeploymentInstrumentationMapper;
import org.arquillian.droidium.native_.selendroid.SelendroidRebuilder;
import org.arquillian.droidium.native_.selendroid.SelendroidServerManager;
import org.arquillian.droidium.native_.sign.APKSigner;
import org.arquillian.droidium.native_.utils.DroidiumNativeFileUtils;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;

/**
 * Observes:
 * <ul>
 * <li>{@link AfterStart}</li>
 * <li>{@link BeforeStop}</li>
 * </ul>
 *
 * Produces suite scoped:<br>
 * <ul>
 * <li>{@link AndroidApplicationHelper}</li>
 * <li>{@link ActivityDeploymentMapper}</li>
 * <li>{@link ActivityWebDriverMapper}</li>
 * <li>{@link ExtensionDroneMapper}</li>
 * <li>{@link DeploymentWebDriverMapper}</li>
 * <li>{@link DeploymentInstrumentationMapper}</li>
 * <li>{@link AndroidApplicationManager}</li>
 * <li>{@link SelendroidServerManager}</li>
 * <li>{@link AndroidDeploymentRegister}</li>
 * <li>{@link SelendroidDeploymentRegister}</li>
 * </ul>
 *
 * Produces container scoped:
 * <ul>
 * <li>{@link APKSigner}</li>
 * <li>{@link SelendroidRebuilder}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeResourceManager {

    // these injections are created before or while Android container starts

    @Inject
    private Instance<AndroidDevice> androidDevice;

    @Inject
    private Instance<AndroidSDK> androidSDK;

    @Inject
    private Instance<ProcessExecutor> processExecutor;

    @Inject
    private Instance<DroidiumNativeConfiguration> droidiumNativeConfiguration;

    // producers

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidApplicationHelper> androidApplicationHelper;

    @Inject
    @SuiteScoped
    private InstanceProducer<ActivityDeploymentMapper> activityDeploymentMapper;

    @Inject
    @SuiteScoped
    private InstanceProducer<ActivityWebDriverMapper> activityWebDriverMapper;

    @Inject
    @SuiteScoped
    private InstanceProducer<ExtensionDroneMapper> extensionDroneMapper;

    @Inject
    @SuiteScoped
    private InstanceProducer<DeploymentInstrumentationMapper> deploymentInstrumentationMapper;

    @Inject
    @SuiteScoped
    private InstanceProducer<DeploymentWebDriverMapper> deploymentWebDriverMapper;

    @Inject
    @ContainerScoped
    private InstanceProducer<APKSigner> apkSigner;

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidApplicationManager> androidApplicationManager;

    @Inject
    @SuiteScoped
    private InstanceProducer<SelendroidServerManager> selendroidServerManager;

    @Inject
    @ContainerScoped
    private InstanceProducer<SelendroidRebuilder> selendroidRebuilder;

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidDeploymentRegister> androidDeploymentRegister;

    @Inject
    @SuiteScoped
    private InstanceProducer<SelendroidDeploymentRegister> selendroidDeploymentRegister;

    /**
     * Creates all resources related to this container after its start so we can further use them by injection points.
     *
     * @param event
     */
    public void onAfterContainerStart(@Observes AfterStart event) {
        if (event.getDeployableContainer().getConfigurationClass() == AndroidContainerConfiguration.class) {
            // all resources for this container will be placed there, meaning all packages and servers
            DroidiumNativeFileUtils.createWorkingDir(droidiumNativeConfiguration.get().getTmpDir());

            androidApplicationHelper.set(getAndroidApplicationHelper());
            activityDeploymentMapper.set(getActivityDeploymentMapper());
            activityWebDriverMapper.set(getActivityWebDriverMapper());
            extensionDroneMapper.set(getWebDriverPortMapper());
            deploymentWebDriverMapper.set(getDeploymentWebDriverMapper());
            deploymentInstrumentationMapper.set(getDeploymentInstrumentationMapper());
            apkSigner.set(getAPKSigner());

            androidApplicationManager.set(getAndroidApplicationManager());
            selendroidServerManager.set(getSelendroidServerManager());
            selendroidRebuilder.set(getSelendroidRebuilder());

            androidDeploymentRegister.set(new AndroidDeploymentRegister());
            selendroidDeploymentRegister.set(new SelendroidDeploymentRegister());

            NativeActivityManager activityManager = new NativeActivityManager(activityWebDriverMapper.get());
            androidDevice.get().getActivityManagerProvider().setActivityManager(activityManager);
        }
    }

    private DeploymentInstrumentationMapper getDeploymentInstrumentationMapper() {
        DeploymentInstrumentationMapper deploymentInstrumentationMapper = new DeploymentInstrumentationMapper();
        return deploymentInstrumentationMapper;
    }

    /**
     * Deletes temporary directory where all Selendroid and Android resources (files, resigned apks, logs ... ) are stored.
     *
     * This resource directory will not be deleted when you suppress it by {@code removeTmpDir="false"} in extension
     * configuration in arquillian.xml.
     *
     * @param event
     */
    public void onDroidiumContainerStop(@Observes BeforeStop event) {
        if (event.getDeployableContainer().getConfigurationClass() == AndroidContainerConfiguration.class) {
            if (droidiumNativeConfiguration.get().getRemoveTmpDir()) {
                File dir = DroidiumNativeFileUtils.getTmpDir();
                DroidiumNativeFileUtils.removeWorkingDir(dir);
            }
        }
    }

    // helpers

    private DeploymentWebDriverMapper getDeploymentWebDriverMapper() {
        DeploymentWebDriverMapper deploymentWebDriverMapper = new DeploymentWebDriverMapper();
        return deploymentWebDriverMapper;
    }

    private ActivityWebDriverMapper getActivityWebDriverMapper() {
        ActivityWebDriverMapper activityWebDriverMapper = new ActivityWebDriverMapper();
        return activityWebDriverMapper;
    }

    private ActivityDeploymentMapper getActivityDeploymentMapper() {
        ActivityDeploymentMapper activityDeploymentMapper = new ActivityDeploymentMapper();
        return activityDeploymentMapper;

    }

    private ExtensionDroneMapper getWebDriverPortMapper() {
        ExtensionDroneMapper webDriverPortMapper = new ExtensionDroneMapper();
        return webDriverPortMapper;
    }

    private SelendroidServerManager getSelendroidServerManager() {
        SelendroidServerManager selendroidServerManager = new SelendroidServerManager(androidDevice.get(),
            processExecutor.get(), androidSDK.get());
        return selendroidServerManager;
    }

    private AndroidApplicationManager getAndroidApplicationManager() {
        AndroidApplicationManager androidApplicationManager = new AndroidApplicationManager(androidDevice.get(),
            processExecutor.get(), androidSDK.get());
        return androidApplicationManager;
    }

    private APKSigner getAPKSigner() {
        APKSigner signer = new APKSigner(processExecutor.get(), androidSDK.get(), droidiumNativeConfiguration.get());
        return signer;
    }

    private SelendroidRebuilder getSelendroidRebuilder() {
        SelendroidRebuilder selendroidRebuilder = new SelendroidRebuilder(processExecutor.get(), androidSDK.get());
        return selendroidRebuilder;
    }

    private AndroidApplicationHelper getAndroidApplicationHelper() {
        AndroidApplicationHelper applicationHelper = new AndroidApplicationHelper(processExecutor.get(), androidSDK.get());
        return applicationHelper;
    }

}
