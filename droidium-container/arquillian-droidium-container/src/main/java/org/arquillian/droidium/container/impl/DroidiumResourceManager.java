/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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

import org.arquillian.droidium.container.api.FileType;
import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.deployment.AndroidDeploymentRegister;
import org.arquillian.droidium.container.sign.APKSigner;
import org.arquillian.droidium.container.spi.event.DroidiumExtensionConfigured;
import org.arquillian.droidium.container.utils.AndroidIdentifierGenerator;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.arquillian.droidium.platform.event.DroidiumPlatformConfigured;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.arquillian.spacelift.process.ProcessExecutor;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStopping;

/**
 * TODO dopisat javadoc
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumResourceManager {

    @Inject
    @ApplicationScoped
    private InstanceProducer<AndroidSDK> androidSDK;

    @Inject
    @ApplicationScoped
    private InstanceProducer<IdentifierGenerator<FileType>> idGenerator;

    @Inject
    @ApplicationScoped
    private InstanceProducer<APKSigner> signer;

    @Inject
    @ApplicationScoped
    private InstanceProducer<AndroidDeploymentRegister> androidDeploymentRegister;

    @Inject
    @ApplicationScoped
    private InstanceProducer<AndroidApplicationHelper> androidApplicationHelper;

    @Inject
    @ApplicationScoped
    private InstanceProducer<AndroidDeviceRegister> androidDeviceRegister;

    @Inject
    private Instance<DroidiumPlatformConfiguration> platformConfiguration;

    @Inject
    private Instance<ProcessExecutor> processExecutor;

    @Inject
    private Event<DroidiumExtensionConfigured> droidiumExtensionConfigured;

    public void onDroidiumPlatformConfigured(@Observes DroidiumPlatformConfigured event) {

        // AndroidSDK

        androidSDK.set(new AndroidSDK(platformConfiguration.get(), processExecutor.get()));

        // Other producers

        idGenerator.set(new AndroidIdentifierGenerator());

        signer.set(new APKSigner(processExecutor.get(), this.androidSDK.get()));

        androidDeploymentRegister.set(new AndroidDeploymentRegister());

        androidApplicationHelper.set(new AndroidApplicationHelper(processExecutor.get(), this.androidSDK.get()));

        androidDeviceRegister.set(new AndroidDeviceRegister());

        droidiumExtensionConfigured.fire(new DroidiumExtensionConfigured());
    }

    /**
     * Deletes temporary directory where all resources (files, resigned apks, logs ... ) are stored.
     *
     * This resource directory will not be deleted when you suppress it by {@code removeTmpDir="false"} in the configuration.
     *
     * @param event
     */
    public void onManagerStopping(@Observes ManagerStopping event) {
        if (platformConfiguration.get().getRemoveTmpDir()) {
            DroidiumFileUtils.removeDir(androidSDK.get().getPlatformConfiguration().getTmpDir());
        }
    }
}
