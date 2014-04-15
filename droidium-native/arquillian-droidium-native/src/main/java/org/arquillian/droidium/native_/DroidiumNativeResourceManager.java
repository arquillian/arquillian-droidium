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
package org.arquillian.droidium.native_;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.native_.deployment.DeploymentActivitiesMapper;
import org.arquillian.droidium.native_.deployment.DeploymentInstrumentationMapper;
import org.arquillian.droidium.native_.deployment.SelendroidDeploymentRegister;
import org.arquillian.droidium.native_.enrichment.NativeActivityManager;
import org.arquillian.droidium.native_.selendroid.SelendroidRebuilder;
import org.arquillian.droidium.native_.selendroid.SelendroidServerManager;
import org.arquillian.droidium.native_.spi.event.DroidiumNativeConfigured;
import org.arquillian.spacelift.process.ProcessExecutor;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Observes:
 * <ul>
 * <li>{@link DroidiumNativeConfigured}</li>
 * <li>{@link AfterStart}</li>
 * </ul>
 *
 * Produces application scoped:<br>
 * <ul>
 * <li>{@link DeploymentActivitiesMapper}</li>
 * <li>{@link DeploymentInstrumentationMapper}</li>
 * <li>{@link SelendroidRebuilder}</li>
 * <li>{@link SelendroidDeploymentRegister}</li>
 * <li>{@link SelendroidServerManager}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeResourceManager {

    @Inject
    private Instance<AndroidDevice> androidDevice;

    // already created from Droidium Platform extension

    @Inject
    private Instance<AndroidSDK> androidSDK;

    @Inject
    private Instance<ProcessExecutor> processExecutor;

    // producers

    @Inject
    @ApplicationScoped
    private InstanceProducer<DeploymentActivitiesMapper> deploymentActivitiesMapper;

    @Inject
    @ApplicationScoped
    private InstanceProducer<DeploymentInstrumentationMapper> deploymentInstrumentationMapper;

    @Inject
    @ApplicationScoped
    private InstanceProducer<SelendroidRebuilder> selendroidRebuilder;

    @Inject
    @ApplicationScoped
    private InstanceProducer<SelendroidDeploymentRegister> selendroidDeploymentRegister;

    @Inject
    @ApplicationScoped
    private InstanceProducer<SelendroidServerManager> selendroidServerManager;

    public void onAfterStart(@Observes AfterStart event) {
        if (event.getDeployableContainer().getConfigurationClass() == AndroidContainerConfiguration.class) {
            AndroidDevice androidDevice = this.androidDevice.get();

            if (androidDevice != null) {
                androidDevice.setActivityManager(new NativeActivityManager());
            }
        }
    }

    public void onDroidiumNativeConfigured(@Observes DroidiumNativeConfigured event) {
        deploymentActivitiesMapper.set(new DeploymentActivitiesMapper());

        deploymentInstrumentationMapper.set(new DeploymentInstrumentationMapper());

        selendroidDeploymentRegister.set(new SelendroidDeploymentRegister());

        selendroidRebuilder.set(new SelendroidRebuilder(processExecutor.get(), androidSDK.get()));

        selendroidServerManager.set(new SelendroidServerManager(processExecutor.get(), androidSDK.get()));
    }
}