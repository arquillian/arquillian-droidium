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

import org.arquillian.droidium.native_.deployment.SelendroidDeploymentRegister;
import org.arquillian.droidium.native_.metadata.DroidiumMetadataKey;
import org.arquillian.droidium.native_.spi.SelendroidDeployment;
import org.arquillian.droidium.native_.spi.event.SelendroidUnDeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePointContext;
import org.jboss.arquillian.drone.spi.event.AfterDroneDestroyed;

/**
 * After some Drone instance is destroyed, it checks if there exist some Selendroid server for this instance and it is
 * uninstalled afterwards if it does.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AfterDroneDestroyed}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link SelendroidUnDeploy}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class InstrumentationRemoveDecider {

    @Inject
    private Instance<SelendroidDeploymentRegister> selendroidDeploymentRegister;

    @Inject
    private Event<SelendroidUnDeploy> selendroidUnDeploy;

    @Inject
    private Instance<DroneContext> droneContext;

    public void decideRemovingInstrumentation(@Observes AfterDroneDestroyed event) {

        final DroneContext droneContext = this.droneContext.get();

        final DronePointContext<?> dronePointContext = droneContext.get(event.getDronePoint());

        if (dronePointContext.hasMetadata(DroidiumMetadataKey.DEPLOYMENT.class)
            && dronePointContext.hasMetadata(DroidiumMetadataKey.INSTRUMENTATION_TEST_PACKAGE_NAME.class)
            && dronePointContext.hasMetadata(DroidiumMetadataKey.TESTED_APP_PACKAGE_NAME.class)) {

            final String selendroidDeploymentName = dronePointContext.getMetadata(DroidiumMetadataKey.INSTRUMENTATION_TEST_PACKAGE_NAME.class);

            final SelendroidDeployment selendroidDeployment = selendroidDeploymentRegister.get().get(selendroidDeploymentName);

            if (selendroidDeployment != null) {
                selendroidUnDeploy.fire(new SelendroidUnDeploy(selendroidDeployment));

                selendroidDeploymentRegister.get().remove(selendroidDeployment);
            }
        }
    }
}
