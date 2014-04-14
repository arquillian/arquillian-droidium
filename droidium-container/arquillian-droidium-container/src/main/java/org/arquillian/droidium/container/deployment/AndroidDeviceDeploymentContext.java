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
package org.arquillian.droidium.container.deployment;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.AndroidApplicationManager;
import org.arquillian.droidium.container.impl.AndroidDeviceMetadata;
import org.arquillian.droidium.container.impl.AndroidDeviceRegister;
import org.arquillian.spacelift.process.ProcessExecutor;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * This sets {@code AndroidApplicationManager} to have "right" {@link AndroidDevice} according to context.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDeviceDeploymentContext {

    @Inject
    @ContainerScoped
    public InstanceProducer<AndroidApplicationManager> androidApplicationManager;

    @Inject
    public Instance<AndroidDevice> androidDevice;

    @Inject
    public Instance<ProcessExecutor> processExecutor;

    @Inject
    public Instance<AndroidSDK> androidSDK;

    @Inject
    private Instance<AndroidDeviceRegister> androidDeviceRegister;

    public void onBeforeDeploy(@Observes BeforeDeploy event, AndroidDevice androidDevice) {

        AndroidDeviceRegister register = androidDeviceRegister.get();

        if (!register.contains(androidDevice)) {
            AndroidDeviceMetadata metadata = new AndroidDeviceMetadata();

            metadata.setContainerQualifier(event.getDeployment().getTarget().getName());
            metadata.addDeploymentName(event.getDeployment().getName());

            register.put(androidDevice, metadata);
        } else {
            register.addDeploymentForDevice(androidDevice, event.getDeployment().getName());
        }

        AndroidApplicationManager androidApplicationManager = new AndroidApplicationManager(
            androidDevice, processExecutor.get(), androidSDK.get());

        this.androidApplicationManager.set(androidApplicationManager);
    }

    public void onBeforeUnDeploy(@Observes BeforeUnDeploy event, AndroidDevice androidDevice) {

        AndroidApplicationManager androidApplicationManager = new AndroidApplicationManager(
            androidDevice, processExecutor.get(), androidSDK.get());

        this.androidApplicationManager.set(androidApplicationManager);
    }
}
