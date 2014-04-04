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
package org.arquillian.droidium.native_.configuration;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.arquillian.droidium.native_.deployment.DeploymentActivitiesMapper;
import org.arquillian.droidium.native_.deployment.DeploymentInstrumentationMapper;
import org.arquillian.droidium.native_.metadata.DroidiumMetadataKey;
import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointContext;
import org.jboss.arquillian.drone.spi.event.AfterDroneConfigured;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.WebDriver;

/**
 * Enriches Android Drone injection point with metadata.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AfterDroneConfigured}</li>
 * <li>{@link AfterDroneInstantiated}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumDronePointModifier {

    @Inject
    private Instance<DeploymentInstrumentationMapper> deploymentInstrumentationMapper;

    @Inject
    private Instance<DeploymentActivitiesMapper> activityDeploymentMapper;

    @Inject
    private Instance<DroneContext> droneContext;

    /**
     * After some Drone is configured, we look if it stands for "android" browser and according to its remote port we eventually
     * parse deployment name which it logically operates on. This information is set to related {@link DronePointContext} as
     * DronePoints metadata.
     *
     * @param event
     */
    public void onAfterDroneConfigured(@Observes AfterDroneConfigured event) {

        if (!event.getDronePoint().conformsTo(WebDriver.class)) {
            return;
        }

        final DronePoint<WebDriver> dronePoint = (DronePoint<WebDriver>) event.getDronePoint();

        final DroneContext droneContext = this.droneContext.get();

        final DronePointContext<WebDriver> dronePointContext = droneContext.get(dronePoint);

        if (new DroidiumDronePointFilter().accepts(droneContext, dronePoint)) {
            final WebDriverConfiguration configuration = dronePointContext.getConfigurationAs(WebDriverConfiguration.class);

            final String port = parsePort(configuration.getRemoteAddress());

            dronePointContext.setMetadata(DroidiumMetadataKey.PORT.class, port);

            final String deploymentName = getDeploymentName(deploymentInstrumentationMapper.get(), port);

            if (deploymentName != null) {
                dronePointContext.setMetadata(DroidiumMetadataKey.DEPLOYMENT.class, deploymentName);
            }
        }
    }

    /**
     * Adds all activities which are controllable by just instantiated Drone instance into point metadata. Activies from APK
     * deployment are parsed upon deployment phase in BeforeSuite. Due to Drone's lazy instantiation, we are sure that activites
     * to be put to metadata are already parsed.
     *
     * @param event
     */
    public void onAfterDroneInstantiated(@Observes AfterDroneInstantiated event) {

        if (!event.getDronePoint().conformsTo(WebDriver.class)) {
            return;
        }

        final DroneContext droneContext = this.droneContext.get();

        final DronePointContext<WebDriver> dronePointContext = droneContext.get((DronePoint<WebDriver>) event.getDronePoint());

        if (dronePointContext.hasMetadata(DroidiumMetadataKey.DEPLOYMENT.class)) {
            final String deploymentName = dronePointContext.getMetadata(DroidiumMetadataKey.DEPLOYMENT.class);

            final List<String> activities = activityDeploymentMapper.get().getActivities(deploymentName);

            dronePointContext.setMetadata(DroidiumMetadataKey.ACTIVITIES.class, activities);
        }
    }

    private String getDeploymentName(final DeploymentInstrumentationMapper mapper, final String port) {
        for (Map.Entry<String, InstrumentationConfiguration> entry : mapper.get().entrySet()) {
            if (entry.getValue().getPort().equals(port)) {
                return entry.getKey();
            }
        }

        return null;
    }

    private String parsePort(final URL remoteUrl) {

        int port = remoteUrl.getPort();

        if (port == -1) {
            port = WebDriverConfiguration.DEFAULT_REMOTE_URL.getPort();
        }

        return Integer.toString(port);
    }

}