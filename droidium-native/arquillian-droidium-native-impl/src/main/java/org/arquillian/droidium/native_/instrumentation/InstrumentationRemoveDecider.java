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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.native_.configuration.DroneConfigurationHolder;
import org.arquillian.droidium.native_.deployment.AndroidDeploymentRegister;
import org.arquillian.droidium.native_.deployment.DeploymentWebDriverMapper;
import org.arquillian.droidium.native_.deployment.ExtensionDroneMapper;
import org.arquillian.droidium.native_.deployment.SelendroidDeploymentRegister;
import org.arquillian.droidium.native_.spi.SelendroidDeployment;
import org.arquillian.droidium.native_.spi.event.SelendroidUndeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.event.AfterDroneDestroyed;

/**
 * After some Drone instance is destroyed, it checks if there exist some Selendroid server for this instance and it is uninstalled
 * afterwards if it does.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AfterDroneDestroyed}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link SelendroidUndeploy}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class InstrumentationRemoveDecider {

    private static final Logger logger = Logger.getLogger(InstrumentationRemoveDecider.class.getName());

    @Inject
    private Instance<DeploymentWebDriverMapper> deploymentWebDriverMapper;

    @Inject
    private Instance<ExtensionDroneMapper> extensionDroneMapper;

    @Inject
    private Instance<AndroidDeploymentRegister> androidDeploymentRegister;

    @Inject
    private Instance<SelendroidDeploymentRegister> selendroidDeploymentRegister;

    @Inject
    private Event<SelendroidUndeploy> selendroidUndeploy;

    public void decideRemovingInstrumentation(@Observes AfterDroneDestroyed event) {

        String qualifier = event.getQualifier().getSimpleName().toLowerCase();

        logger.log(Level.FINE, "Uninstallation of Selendroid server for Drone qualifier: {0}", new Object[] { qualifier });

        SelendroidDeployment selendroidDeployment = null;

        for (Map.Entry<String, DroneConfigurationHolder> extensionDroneEntry : extensionDroneMapper.get().get().entrySet()) {
            if (extensionDroneEntry.getValue().getQualifier().equals(qualifier)) {
                String deploymentName = deploymentWebDriverMapper.get().getDeploymentName(extensionDroneEntry.getKey());

                logger.log(Level.FINE, "Uninstallation of Selendroid server for deployment {0} is going to be performed.",
                    new Object[] { deploymentName });

                selendroidDeployment = selendroidDeploymentRegister.get().get(deploymentName);
                break;
            }
        }

        if (selendroidDeployment != null) {
            selendroidUndeploy.fire(new SelendroidUndeploy(selendroidDeployment));
        } else {
            logger.log(Level.INFO, "There is not such instrumented deployment for Drone qualifier '{0}'",
                new Object[] { qualifier });
        }
    }
}
