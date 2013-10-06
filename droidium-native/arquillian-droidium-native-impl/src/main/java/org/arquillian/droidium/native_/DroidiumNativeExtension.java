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

import org.arquillian.droidium.container.api.ActivityManagerProvider;
import org.arquillian.droidium.native_.activity.DroidiumNativeActivityManager;
import org.arquillian.droidium.native_.activity.DroidiumNativeActivityManagerProvider;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfigurator;
import org.arquillian.droidium.native_.configuration.DroidiumNativeResourceManager;
import org.arquillian.droidium.native_.configuration.ExtensionDroneResolver;
import org.arquillian.droidium.native_.deployment.ActivityDeploymentScanner;
import org.arquillian.droidium.native_.deployment.DeploymentWebDriverResolver;
import org.arquillian.droidium.native_.instrumentation.DeploymentInstrumentationResolver;
import org.arquillian.droidium.native_.instrumentation.InstrumentationPerformDecider;
import org.arquillian.droidium.native_.instrumentation.InstrumentationPerformer;
import org.arquillian.droidium.native_.instrumentation.InstrumentationRemoveDecider;
import org.arquillian.droidium.native_.selendroid.SelendroidDeploymentInstaller;
import org.arquillian.droidium.native_.selendroid.SelendroidDeploymentUninstaller;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Arquillian Droidium Native extension
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

        // resolvers
        builder.observer(DeploymentInstrumentationResolver.class);
        builder.observer(DeploymentWebDriverResolver.class);
        builder.observer(ExtensionDroneResolver.class);

        // instrumentation
        builder.observer(InstrumentationPerformDecider.class);
        builder.observer(InstrumentationRemoveDecider.class);
        builder.observer(InstrumentationPerformer.class);

        // installers & uninstallers
        builder.observer(SelendroidDeploymentInstaller.class);
        builder.observer(SelendroidDeploymentUninstaller.class);

        // activity related
        builder.observer(DroidiumNativeActivityManagerProvider.class);
        builder.observer(DroidiumNativeActivityManager.class);
        builder.observer(ActivityDeploymentScanner.class);

        // services
        builder.service(ActivityManagerProvider.class, DroidiumNativeActivityManagerProvider.class);
    }

}
