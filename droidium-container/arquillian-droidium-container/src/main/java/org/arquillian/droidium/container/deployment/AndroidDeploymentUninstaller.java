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

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.impl.AndroidApplicationManager;
import org.arquillian.droidium.container.spi.AndroidDeployment;
import org.arquillian.droidium.container.spi.event.AfterAndroidDeploymentUnDeployed;
import org.arquillian.droidium.container.spi.event.AndroidUnDeploy;
import org.arquillian.droidium.container.spi.event.BeforeAndroidDeploymentUnDeployed;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Observes:
 * <ul>
 * <li>{@link AndroidUnDeploy}</li>
 * </ul>
 * Fires:<br>
 * <ul>
 * <li>{@link BeforeAndroidDeploymentUnDeployed}</li>
 * <li>{@link AfterAndroidDeploymentUnDeployed}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDeploymentUninstaller {

    @Inject
    private Instance<AndroidApplicationManager> androidApplicationManager;

    @Inject
    private Instance<AndroidDeploymentRegister> androidDeploymentRegister;

    @Inject
    private Event<BeforeAndroidDeploymentUnDeployed> beforeUnDeploy;

    @Inject
    private Event<AfterAndroidDeploymentUnDeployed> afterUnDeploy;

    /**
     *
     * @param event
     */
    public void onAndroidUnDeploy(@Observes AndroidUnDeploy event, AndroidDevice device) {

        AndroidDeployment androidDeployment = androidDeploymentRegister.get().get(event.getArchive());

        beforeUnDeploy.fire(new BeforeAndroidDeploymentUnDeployed(androidDeployment));

        androidApplicationManager.get().setDevice(device).uninstall(androidDeployment);

        afterUnDeploy.fire(new AfterAndroidDeploymentUnDeployed(androidDeployment));

    }

}
