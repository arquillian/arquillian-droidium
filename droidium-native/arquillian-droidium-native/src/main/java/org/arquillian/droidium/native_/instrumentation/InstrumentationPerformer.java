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
package org.arquillian.droidium.native_.instrumentation;

import java.io.File;

import org.arquillian.droidium.container.deployment.AndroidDeploymentRegister;
import org.arquillian.droidium.container.impl.AndroidApplicationHelper;
import org.arquillian.droidium.container.sign.APKSigner;
import org.arquillian.droidium.container.spi.AndroidDeployment;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.deployment.SelendroidDeploymentRegister;
import org.arquillian.droidium.native_.metadata.DroidiumMetadataKey;
import org.arquillian.droidium.native_.selendroid.SelendroidRebuilder;
import org.arquillian.droidium.native_.selendroid.SelendroidServerManager;
import org.arquillian.droidium.native_.spi.SelendroidDeployment;
import org.arquillian.droidium.native_.spi.event.PerformInstrumentation;
import org.arquillian.droidium.native_.spi.event.SelendroidDeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.DroneContext;

/**
 * Initializes instrumentation process by observing {@link PerformInstrumentation} event and matching deployment name to
 * instrumentated package against which Drone instance is created.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link PerformInstrumentation}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link SelendroidDeploy}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class InstrumentationPerformer {

    private static int counter = 0;

    @Inject
    private Instance<DroidiumNativeConfiguration> configuration;

    @Inject
    private Instance<AndroidDeploymentRegister> androidDeploymentRegister;

    @Inject
    private Instance<SelendroidDeploymentRegister> selendroidDeploymentRegister;

    @Inject
    private Instance<SelendroidRebuilder> selendroidRebuilder;

    @Inject
    private Instance<AndroidApplicationHelper> applicationHelper;

    @Inject
    private Instance<APKSigner> signer;

    @Inject
    private Instance<SelendroidServerManager> selendroidServerManager;

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Event<SelendroidDeploy> selendroidDeploy;

    public void performInstrumentation(@Observes PerformInstrumentation event) {

        AndroidDeployment instrumentedDeployment = androidDeploymentRegister.get().get(event.getDeploymentName());

        File selendroidWorkingCopy = getSelendroidWorkingCopy();
        String selendroidPackageName = getSelendroidPackageName(selendroidWorkingCopy);
        String applicationBasePackage = instrumentedDeployment.getApplicationBasePackage();

        File selendroidRebuilt = selendroidRebuilder.get().rebuild(selendroidWorkingCopy, selendroidPackageName, applicationBasePackage);

        File selendroidResigned = getSelendroidResigned(selendroidRebuilt);

        final SelendroidDeployment selendroidDeployment = new SelendroidDeployment();
        selendroidDeployment.setWorkingCopy(selendroidWorkingCopy)
            .setRebuilt(selendroidRebuilt)
            .setResigned(selendroidResigned)
            .setServerBasePackage(applicationHelper.get().getApplicationBasePackage(selendroidResigned))
            .setSelendroidPackageName(selendroidPackageName)
            .setInstrumentedDeployment(instrumentedDeployment)
            .setDeploymentName(event.getDeploymentName())
            .setInstrumentationConfiguration(event.getConfiguration());

        selendroidDeploymentRegister.get().add(selendroidDeployment);

        // which exactly Selendroid deployment is this instrumented Android deployment backed by?
        droneContext.get().get(event.getDronePoint())
            .setMetadata(DroidiumMetadataKey.SELENDROID_PACKAGE_NAME.class, selendroidDeployment.getSelendroidPackageName());

        droneContext.get().get(event.getDronePoint())
            .setMetadata(DroidiumMetadataKey.ANDROID_PACKAGE_NAME.class, instrumentedDeployment.getApplicationBasePackage());

        selendroidDeploy.fire(new SelendroidDeploy(selendroidDeployment));

        selendroidServerManager.get().instrument(selendroidDeployment);
    }

    private File getSelendroidResigned(File selendroidRebuilt) {
        return signer.get().resign(selendroidRebuilt);
    }

    private File getSelendroidWorkingCopy() {
        return DroidiumFileUtils.copyFileToDirectory(configuration.get().getServerApk(), DroidiumFileUtils.getTmpDir());
    }

    private String getSelendroidPackageName(File selendroidWorkingCopy) {
        return applicationHelper.get().getApplicationBasePackage(selendroidWorkingCopy) + "_" + ++counter;
    }

}
