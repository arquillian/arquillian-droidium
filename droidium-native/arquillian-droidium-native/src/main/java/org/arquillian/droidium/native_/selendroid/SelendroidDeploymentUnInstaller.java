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
package org.arquillian.droidium.native_.selendroid;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.impl.AndroidDeviceRegister;
import org.arquillian.droidium.native_.spi.SelendroidDeployment;
import org.arquillian.droidium.native_.spi.event.AfterSelendroidDeploymentUnDeployed;
import org.arquillian.droidium.native_.spi.event.BeforeSelendroidDeploymentUnDeployed;
import org.arquillian.droidium.native_.spi.event.SelendroidUnDeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Uninstalls Selendroid deployment from Android device.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link SelendroidUnDeploy}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link BeforeSelendroidDeploymentUnDeployed}</li>
 * <li>{@link AfterSelendroidDeploymentUnDeployed}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidDeploymentUnInstaller {

    @Inject
    private Instance<SelendroidServerManager> selendroidServerManager;

    @Inject
    private Instance<AndroidDeviceRegister> androidDeviceRegister;

    @Inject
    private Event<BeforeSelendroidDeploymentUnDeployed> beforeUnDeployed;

    @Inject
    private Event<AfterSelendroidDeploymentUnDeployed> afterUnDeployed;

    public void onSelendroidUndeploy(@Observes SelendroidUnDeploy event) {

        final SelendroidDeployment selendroidUnDeployment = event.getUnDeployment();

        beforeUnDeployed.fire(new BeforeSelendroidDeploymentUnDeployed(selendroidUnDeployment));

        // to perform Selendroid uninstallation against "right" Android device, we are outside ContainerContext
        AndroidDevice device = androidDeviceRegister.get().getByDeploymentName(selendroidUnDeployment.getDeploymentName());

        selendroidServerManager.get().setDevice(device).uninstall(selendroidUnDeployment);

        afterUnDeployed.fire(new AfterSelendroidDeploymentUnDeployed(selendroidUnDeployment));
    }
}
