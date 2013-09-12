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
package org.arquillian.droidium.native_.deployment;

import org.arquillian.droidium.native_.android.AndroidApplicationManager;
import org.arquillian.droidium.native_.spi.AndroidDeployment;
import org.arquillian.droidium.native_.spi.event.AfterAllAndroidDeploymentsUndeployed;
import org.arquillian.droidium.native_.spi.event.AfterAndroidDeploymentUndeployed;
import org.arquillian.droidium.native_.spi.event.BeforeAllAndroidDeploymentsUndeployed;
import org.arquillian.droidium.native_.spi.event.BeforeAndroidDeploymentUndeployed;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;

/**
 * Uninstalls all packages previously deployed to Android device.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AfterClass}</li>
 * </ul>
 * Fires:<br>
 * <ul>
 * <li>{@link BeforeAllAndroidDeploymentsUndeployed}</li>
 * <li>{@link BeforeAndroidDeploymentUndeployed}</li>
 * <li>{@link AfterAndroidDeploymentUndeployed}</li>
 * <li>{@link AfterAllAndroidDeploymentsUndeployed}</li>
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
    private Event<BeforeAndroidDeploymentUndeployed> beforeUndeploy;

    @Inject
    private Event<AfterAndroidDeploymentUndeployed> afterUndeploy;

    @Inject
    private Event<BeforeAllAndroidDeploymentsUndeployed> beforeAllUndeployed;

    @Inject
    private Event<AfterAllAndroidDeploymentsUndeployed> afterAllUndeployed;

    /**
     * Precedence is set to negative value to be sure that this observer will be treated as the last one in AfterClass context.
     * We have to uninstall packages after all Drones are destroyed (so after all Selendroid servers are uninstalled since
     * undeployment of Selendroid server is triggered after Drone destruction). If we uninstalled it in the proper undeployment
     * lifecycle, it would automatically stop Selendroid servers so subsequent destroying of Drone instances would fail since
     * they can not communicate with Selendroid servers anymore.
     *
     * @param event
     */
    public void onAndroidDeploymentUninstall(@Observes(precedence = -100) AfterClass event) {

        beforeAllUndeployed.fire(new BeforeAllAndroidDeploymentsUndeployed());

        for (AndroidDeployment androidDeployment : androidDeploymentRegister.get().getAll()) {
            beforeUndeploy.fire(new BeforeAndroidDeploymentUndeployed(androidDeployment));

            androidApplicationManager.get().uninstall(androidDeployment);

            afterUndeploy.fire(new AfterAndroidDeploymentUndeployed(androidDeployment));
        }

        afterAllUndeployed.fire(new AfterAllAndroidDeploymentsUndeployed());
    }

}
