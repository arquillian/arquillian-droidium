/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.arquillian.droidium.container;

import org.arquillian.droidium.container.api.AndroidDeviceSelector;
import org.arquillian.droidium.container.configuration.DroidiumExclusionDecider;
import org.arquillian.droidium.container.configuration.DroidiumExtensionsValidation;
import org.arquillian.droidium.container.deployment.AndroidDeploymentInstaller;
import org.arquillian.droidium.container.deployment.AndroidDeploymentUninstaller;
import org.arquillian.droidium.container.deployment.AndroidDeviceDeploymentContext;
import org.arquillian.droidium.container.enricher.AndroidDeviceResourceProvider;
import org.arquillian.droidium.container.impl.AndroidBridgeConnector;
import org.arquillian.droidium.container.impl.AndroidDeviceSelectorImpl;
import org.arquillian.droidium.container.impl.AndroidEmulatorShutdown;
import org.arquillian.droidium.container.impl.AndroidEmulatorStartup;
import org.arquillian.droidium.container.impl.AndroidSDCardManagerImpl;
import org.arquillian.droidium.container.impl.AndroidVirtualDeviceManager;
import org.arquillian.droidium.container.impl.DroidiumResourceManager;
import org.arquillian.droidium.container.log.AndroidLogInitializer;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * <p>
 * Arquillian Droidium container extension
 * </p>
 *
 * This is the place where all other observers and services are registered.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class DroidiumContainerExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, AndroidDeployableContainer.class);
        builder.service(ResourceProvider.class, AndroidDeviceResourceProvider.class);
        builder.service(AndroidDeviceSelector.class, AndroidDeviceSelectorImpl.class);
        builder.observer(AndroidDeployableContainer.class);
        builder.observer(AndroidBridgeConnector.class);
        builder.observer(AndroidLogInitializer.class);
        builder.observer(AndroidDeviceSelectorImpl.class);
        builder.observer(AndroidEmulatorStartup.class);
        builder.observer(AndroidEmulatorShutdown.class);
        builder.observer(AndroidSDCardManagerImpl.class);
        builder.observer(AndroidVirtualDeviceManager.class);
        builder.observer(DroidiumExtensionsValidation.class);
        builder.observer(AndroidDeploymentInstaller.class);
        builder.observer(AndroidDeploymentUninstaller.class);
        builder.observer(DroidiumExclusionDecider.class);
        builder.observer(DroidiumResourceManager.class);
        builder.observer(AndroidDeviceDeploymentContext.class);
    }

}
