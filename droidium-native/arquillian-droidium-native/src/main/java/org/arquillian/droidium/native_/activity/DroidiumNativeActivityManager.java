/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.arquillian.droidium.native_.activity;

import java.util.List;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.native_.deployment.ActivityDeploymentMapper;
import org.arquillian.droidium.native_.deployment.DeploymentWebDriverMapper;
import org.arquillian.droidium.native_.deployment.ExtensionDroneMapper;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.event.AfterDroneInstantiated;
import org.jboss.arquillian.drone.spi.event.BeforeDroneDestroyed;
import org.openqa.selenium.WebDriver;

/**
 * Observes:
 * <ul>
 * <li>{@link AfterDroneInstantiated}</li>
 * <li>{@link BeforeDroneDestroyed}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeActivityManager {

    private static final Logger logger = Logger.getLogger(DroidiumNativeActivityManager.class.getName());

    @Inject
    private Instance<AndroidDevice> androidDevice;

    @Inject
    private Instance<DeploymentWebDriverMapper> deploymentWebDriverMapper;

    @Inject
    private Instance<ExtensionDroneMapper> extensionDroneMapper;

    @Inject
    private Instance<ActivityDeploymentMapper> activityDeploymentMapper;

    @Inject
    private Instance<ActivityWebDriverMapper> activityWebDriverMapper;

    /**
     * Adds all activities which are controllable by just instantiated Drone instance into context.
     *
     * @param event
     */
    public void addAllActitiviesForDrone(@Observes AfterDroneInstantiated event) {

        // get all activities which are supposed to be treatead by this, just instantiated, Drone instance
        String extensionQualifier = extensionDroneMapper.get()
            .getExtensionQualifierForDroneQualifer(event.getQualifier().getSimpleName().toLowerCase());

        if (extensionQualifier == null) {
            logger.info("No extension qualifier for this WebDriver instance which is supposed to control Android activities was found.");
            return;
        }

        String deploymentName = deploymentWebDriverMapper.get().getDeploymentName(extensionQualifier);

        // this WebDriver instance does not instrument anything
        if (deploymentName == null) {
            logger.info("No deployment for this WebDriver instance which is supposed to control Android activities was found.");
            return;
        }

        WebDriver instance = event.getInstance().asInstance(WebDriver.class);
        List<String> activities = activityDeploymentMapper.get().getActivities(deploymentName);

        activityWebDriverMapper.get().put(instance, activities);
    }

    /**
     * Removes mapping from WebDriver instance to be destroyed to activities it can control so after this instance is destroyed
     * you can not start these activities anymore by activity manager. To control them again, {@code AfterDroneInstantiated}
     * event has to be observed by this class in {@link #addAllActitiviesForDrone(AfterDroneInstantiated)} method which triggers
     * putting all activity classes to underlying register once again. This process occurs for method scoped Drones before and
     * after test method.
     *
     * @param event
     */
    public void removeAllActivitiesForDrone(@Observes BeforeDroneDestroyed event) {
        WebDriver instance = event.getInstance().asInstance(WebDriver.class);
        activityWebDriverMapper.get().removeActivities(instance);
    }

}
