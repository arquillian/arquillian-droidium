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

import org.arquillian.droidium.native_.spi.SelendroidDeployment;
import org.arquillian.droidium.native_.spi.event.AfterSelendroidDeploymentDeployed;
import org.arquillian.droidium.native_.spi.event.BeforeSelendroidDeploymentDeployed;
import org.arquillian.droidium.native_.spi.event.SelendroidDeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Installs modified Selendroid server to Android device.<br>
 * </br> Observes:
 * <ul>
 * <li>{@link SelendroidDeploy}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link BeforeSelendroidDeploymentDeployed}</li>
 * <li>{@link AfterSelendroidDeploymentDeployed}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidDeploymentInstaller {

    @Inject
    private Instance<SelendroidServerManager> selendroidServerManager;

    @Inject
    private Event<BeforeSelendroidDeploymentDeployed> beforeDeployed;

    @Inject
    private Event<AfterSelendroidDeploymentDeployed> afterDeployed;

    public void onSelendroidDeploy(@Observes SelendroidDeploy event) {

        final SelendroidDeployment selendroidDeployment = event.getDeployment();

        beforeDeployed.fire(new BeforeSelendroidDeploymentDeployed(selendroidDeployment));

        selendroidServerManager.get().install(selendroidDeployment);

        afterDeployed.fire(new AfterSelendroidDeploymentDeployed(selendroidDeployment));
    }
}
