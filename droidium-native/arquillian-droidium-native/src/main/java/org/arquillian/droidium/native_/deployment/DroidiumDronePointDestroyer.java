/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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

import java.util.Iterator;

import org.arquillian.droidium.container.spi.event.BeforeAndroidDeploymentUnDeployed;
import org.arquillian.droidium.native_.metadata.DroidiumMetadataKey;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointFilter;
import org.jboss.arquillian.drone.spi.command.DestroyDrone;
import org.openqa.selenium.WebDriver;

/**
 * If some Selendroid-based WebDriver controls activities belonging to an APK which is about to be uninstalled from device, we
 * need to destroy this WebDriver before uninstallation takes place.
 *
 * If we uninstalled such APK before Drone destruction, subsequent Drone destruction ({@link WebDriver#quit()}) woud fail since
 * it would talk to just uninstalled APK hence to nobody. Because of this, we need to be sure that if there is some Drone which
 * acts on that deployment, it is indeed destructed before we proceed.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumDronePointDestroyer {

    @Inject
    private Instance<DroneContext> droneContext;

    @Inject
    private Event<DestroyDrone> destroyDrone;

    public void destroyDrone(@Observes final BeforeAndroidDeploymentUnDeployed event) {

        final Iterator<DronePoint<WebDriver>> dronePointsIterator = droneContext.get()
            .find(WebDriver.class)
            .filter(new DroidiumDestroyDronePointFilter(event.getUnDeployment().getDeploymentName())).iterator();

        while (dronePointsIterator.hasNext()) {
            destroyDrone.fire(new DestroyDrone(dronePointsIterator.next()));
        }
    }

    private class DroidiumDestroyDronePointFilter implements DronePointFilter<WebDriver> {

        private String deploymentName;

        public DroidiumDestroyDronePointFilter(String deploymentName) {
            this.deploymentName = deploymentName;
        }

        @Override
        public boolean accepts(DroneContext context, DronePoint<? extends WebDriver> dronePoint) {

            if (!context.get(dronePoint).hasMetadata(DroidiumMetadataKey.DEPLOYMENT.class)) {
                return false;
            }

            final String deploymentName = context.get(dronePoint).getMetadata(DroidiumMetadataKey.DEPLOYMENT.class);

            return deploymentName != null && this.deploymentName.equals(deploymentName);
        }
    }

}
