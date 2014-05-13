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

import org.arquillian.droidium.container.enrichment.AndroidDeviceResourceProvider;
import org.arquillian.droidium.native_.configuration.DroidiumDronePointModifier;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfigurator;
import org.arquillian.droidium.native_.configuration.SelendroidDownloader;
import org.arquillian.droidium.native_.deployment.DeploymentActivitiesScanner;
import org.arquillian.droidium.native_.deployment.DeploymentInstrumentationResolver;
import org.arquillian.droidium.native_.deployment.DroidiumDronePointDestroyer;
import org.arquillian.droidium.native_.enrichment.DroneAndroidDeviceResourceProvider;
import org.arquillian.droidium.native_.instrumentation.InstrumentationPerformDecider;
import org.arquillian.droidium.native_.instrumentation.InstrumentationPerformer;
import org.arquillian.droidium.native_.instrumentation.InstrumentationRemoveDecider;
import org.arquillian.droidium.native_.selendroid.SelendroidDeploymentInstaller;
import org.arquillian.droidium.native_.selendroid.SelendroidDeploymentUnInstaller;
import org.arquillian.droidium.native_.webdriver.AndroidBrowserCapabilities;
import org.arquillian.droidium.native_.webdriver.AndroidDriverFactory;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * Arquillian Droidium Native extension.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {

        // configuration
        builder.observer(DroidiumNativeConfigurator.class);
        builder.observer(DroidiumNativeResourceManager.class);
        builder.observer(SelendroidDownloader.class);

        // resolvers & enrichers
        builder.observer(DeploymentInstrumentationResolver.class);
        builder.observer(DroidiumDronePointModifier.class);

        // instrumentation
        builder.observer(InstrumentationPerformDecider.class);
        builder.observer(InstrumentationRemoveDecider.class);
        builder.observer(InstrumentationPerformer.class);

        // installers & uninstallers & destoryers
        builder.observer(SelendroidDeploymentInstaller.class);
        builder.observer(SelendroidDeploymentUnInstaller.class);
        builder.observer(DroidiumDronePointDestroyer.class);

        // activity related
        builder.observer(DeploymentActivitiesScanner.class);

        // Selendroid driver
        builder.service(BrowserCapabilities.class, AndroidBrowserCapabilities.class);
        builder.service(Configurator.class, AndroidDriverFactory.class);
        builder.service(Instantiator.class, AndroidDriverFactory.class);
        builder.service(Destructor.class, AndroidDriverFactory.class);

        // Drone-aware AndroidDevice enricher
        builder.override(ResourceProvider.class, AndroidDeviceResourceProvider.class, DroneAndroidDeviceResourceProvider.class);
    }

}
