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
package org.arquillian.droidium.container.deployment;

import java.io.File;

import org.arquillian.droidium.container.impl.AndroidApplicationHelper;
import org.arquillian.droidium.container.impl.AndroidApplicationManager;
import org.arquillian.droidium.container.sign.APKSigner;
import org.arquillian.droidium.container.spi.AndroidDeployment;
import org.arquillian.droidium.container.spi.event.AfterAndroidDeploymentDeployed;
import org.arquillian.droidium.container.spi.event.AndroidDeploy;
import org.arquillian.droidium.container.spi.event.BeforeAndroidDeploymentDeployed;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Installs Android package to Android device.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AndroidDeploy}</li>
 * </ul>
 * Fires:<br>
 * <ul>
 * <li>{@link BeforeAndroidDeploymentDeployed}</li>
 * <li>{@link AfterAndroidDeploymentDeployed}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDeploymentInstaller {

    @Inject
    private Instance<APKSigner> signer;

    @Inject
    private Instance<AndroidApplicationHelper> androidApplicationHelper;

    @Inject
    private Instance<AndroidApplicationManager> androidApplicationManager;

    @Inject
    private Instance<AndroidDeploymentRegister> androidDeploymentRegister;

    @Inject
    private Event<BeforeAndroidDeploymentDeployed> beforeDeploy;

    @Inject
    private Event<AfterAndroidDeploymentDeployed> afterDeploy;

    public void onAndroidDeployArchive(@Observes AndroidDeploy event, DeploymentDescription description) {

        beforeDeploy.fire(new BeforeAndroidDeploymentDeployed(event.getArchive()));

        Archive<?> archive = event.getArchive();

        File deployApk = new File(DroidiumFileUtils.getTmpDir(), DroidiumFileUtils.getRandomAPKFileName());
        DroidiumFileUtils.export(archive, deployApk);
        File resignedApk = signer.get().resign(deployApk);

        final AndroidDeployment deployment = new AndroidDeployment();

        deployment.setDeployment(archive)
            .setDeployApk(deployApk)
            .setResignedApk(resignedApk)
            .setApplicationBasePackage(androidApplicationHelper.get().getApplicationBasePackage(resignedApk))
            .setDeploymentName(description.getName());

        androidDeploymentRegister.get().add(deployment);
        androidApplicationManager.get().install(deployment);

        afterDeploy.fire(new AfterAndroidDeploymentDeployed(deployment));
    }
}
